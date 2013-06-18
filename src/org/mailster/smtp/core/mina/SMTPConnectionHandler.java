package org.mailster.smtp.core.mina;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.mina.core.buffer.BufferDataException;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.ssl.SslFilter;
import org.apache.mina.filter.ssl.SslFilter.SslFilterMessage;
import org.apache.mina.transport.socket.SocketSessionConfig;
import org.mailster.smtp.SMTPServerConfig;
import org.mailster.smtp.api.TooMuchDataException;
import org.mailster.smtp.command.Command;
import org.mailster.smtp.command.CommandHandler;
import org.mailster.smtp.command.exceptions.CommandException;
import org.mailster.smtp.core.DeliveryHandlerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The IoHandler that handles a connection. This class
 * passes most of it's responsibilities off to the
 * CommandHandler.
 * 
 * @author De Oliveira Edouard &lt;doe_wanted@yahoo.fr&gt;
 */
public class SMTPConnectionHandler extends IoHandlerAdapter
{
	// Session objects
	public final static String CONTEXT_ATTRIBUTE = SMTPConnectionHandler.class.getName() + ".ctx";

	private static Logger log = LoggerFactory.getLogger(SMTPConnectionHandler.class);

	private SMTPServerConfig config;
	
	private CommandHandler commandHandler;
	
	private DeliveryHandlerFactory factory;

	/**
	 * A thread safe variable that represents the number
	 * of active connections.
	 */
	private AtomicInteger numberOfConnections = new AtomicInteger(0);
	
	public SMTPConnectionHandler(SMTPServerConfig cfg, CommandHandler handler,
			DeliveryHandlerFactory factory)
	{
		this.config = cfg;
		this.commandHandler = handler;
		this.factory = factory;
	}

	/**
	 * Are we over the maximum amount of connections ?
	 */
	private boolean hasTooManyConnections()
	{
		return (config.getMaxConnections() > -1 && 
				getNumberOfConnections() >= config.getMaxConnections());
	}
	
	/** 
	 * Update the number of active connections.
	 */
	private void updateNumberOfConnections(int delta)
	{
		int count = numberOfConnections.addAndGet(delta);		
		log.debug("Active connections count = {}", count);
	}

	/**
	 * @return The number of open connections
	 */
	public int getNumberOfConnections()
	{
		return numberOfConnections.get();
	}
	
	/** */
	public void sessionCreated(IoSession session)
	{
		updateNumberOfConnections(+1);

		if (session.getTransportMetadata().getSessionConfigType() == SocketSessionConfig.class)
		{
			((SocketSessionConfig)session.getConfig()).setReceiveBufferSize(config.getReceiveBufferSize());
			((SocketSessionConfig)session.getConfig()).setSendBufferSize(64);
		}

		session.getConfig().setIdleTime(IdleStatus.READER_IDLE, config.getConnectionTimeout() / 1000);

		// We're going to use SSL negotiation notification.
		session.setAttribute(SslFilter.USE_NOTIFICATION);

		// Init protocol internals
		log.debug("SMTP connection count: {}", getNumberOfConnections());

		SMTPContext minaCtx = new SMTPContext(config, factory, session);
		session.setAttribute(CONTEXT_ATTRIBUTE, minaCtx);

		try
		{
			if (hasTooManyConnections())
			{
				log.debug("Too many connections to the SMTP server !");
				sendResponse(session, "554 Transaction failed. Too many connections.");
			}
			else
				sendResponse(session, "220 " + config.getHostName() + " ESMTP " + SMTPServerConfig.NAME);
		}
		catch (IOException e1)
		{
			try
			{
				sendResponse(session, "450 Problem when connecting. Please try again later.");
			}
			catch (IOException e) {}

			if (log.isDebugEnabled())
				log.debug("Error on session creation", e1);

			session.close(false);
		}
	}

	/**
	 * Session closed.
	 */
	public void sessionClosed(IoSession session) throws Exception
	{
		updateNumberOfConnections(-1);
	}

	/**
	 * Sends a response telling that the session is idle and closes it.
	 */
	public void sessionIdle(IoSession session, IdleStatus status)
	{
		try
		{
			sendResponse(session, "421 Timeout waiting for data from client.");
		}
		catch (IOException ioex)
		{
		}
		finally
		{
			session.close(false);
		}
	}

	/** */
	public void exceptionCaught(IoSession session, Throwable cause)
	{
		if (log.isDebugEnabled())
			log.debug("Exception occured :", cause);
		
		boolean fatal = true;

		try
		{
			if (cause instanceof BufferDataException)
			{
				sendResponse(session, "501 " + cause.getMessage());
			}
			else if (cause instanceof CommandException)
			{
				fatal = false;
				sendResponse(session, "500 Syntax error");	
			}
			else
			{
				// primarily if things fail during the MessageListener.deliver(), then try
				// to send a temporary failure back so that the server will try to resend 
				// the message later.
				sendResponse(session, "450 Problem attempting to execute commands. Please try again later.");
			}
		}
		catch (IOException e)
		{
		}
		finally
		{
			if (fatal)
				session.close(false);
		}
	}

	/** */
	public void messageReceived(IoSession session, Object message) throws Exception
	{
		if (message == null)
		{
			if (log.isDebugEnabled())
				log.debug("no more lines from client");
			return;
		}
		
		if (message instanceof SslFilterMessage)
		{
			if (log.isDebugEnabled())
				log.debug("SSL FILTER message -> " + message);
			return;
		}

		SMTPContext minaCtx = (SMTPContext) session.getAttribute(CONTEXT_ATTRIBUTE);

		if (message instanceof InputStream)
		{
			minaCtx.setInputStream((InputStream) message);
			try
			{
				this.commandHandler.handleCommand("DATA_END", session, minaCtx);
			}
			catch (TooMuchDataException tmdEx)
			{
				sendResponse(session, "552 Too much mail data");
			}
		}
		else
		{
			String line = (String) message;
			
			if (log.isDebugEnabled())
				log.debug("C: " + line);
			
            if (minaCtx.getSession().isAuthenticating())
            	this.commandHandler.handleAuthChallenge(line, session, minaCtx);
            else
            if (!minaCtx.getSession().isAuthenticated() 
            		&& !minaCtx.getAuthenticationHandler().getAuthenticationMechanisms().isEmpty())
            {
            	// Per RFC 2554
            	Command cmd = this.commandHandler.getCommandFromString(line);
            	
            	if (cmd.isAuthRequired())
            		sendResponse(session, "530 Authentication required");
            	else
            		this.commandHandler.handleCommand(line, session, minaCtx);
            }
            else
            	this.commandHandler.handleCommand(line, session, minaCtx);
		}
	}

	/** */
	public static void sendResponse(IoSession session, String response) throws IOException
	{
		if (log.isDebugEnabled())
			log.debug("S: " + response);

		if (response != null)
			session.write(response);
		
		SMTPContext minaCtx = (SMTPContext) session.getAttribute(CONTEXT_ATTRIBUTE);
		if (!minaCtx.getSession().isActive())
			session.close(false);
	}
}