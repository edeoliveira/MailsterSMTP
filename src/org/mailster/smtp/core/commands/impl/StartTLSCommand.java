package org.mailster.smtp.core.commands.impl;

import java.io.IOException;

import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.ssl.SslFilter;
import org.mailster.smtp.core.SMTPContext;
import org.mailster.smtp.core.commands.AbstractCommand;
import org.mailster.smtp.util.DummySSLSocketFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The STARTLS command implementation.
 * 
 * @author De Oliveira Edouard &lt;doe_wanted@yahoo.fr&gt;
 */
public class StartTLSCommand extends AbstractCommand
{
	private static final Logger LOG = LoggerFactory.getLogger(StartTLSCommand.class);

	private static SslFilter sslFilter;

	static
	{
		try
		{
			DummySSLSocketFactory socketFactory = new DummySSLSocketFactory();
			sslFilter = new SslFilter(socketFactory.getSSLContext());
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public StartTLSCommand()
	{
		super("STARTTLS", "The STARTTLS command starts a secured connection for the current\nSMTP session");
	}

	/**
	 * Ability to override the SSLFilter
	 * @param filter
	 */
	public static void setSSLFilter(SslFilter filter)
	{
		if (filter == null)
			throw new IllegalArgumentException("filter argument can't be null");

		sslFilter = filter;
	}

	public void execute(String commandString, IoSession ioSession, SMTPContext ctx) 
		throws IOException
	{
		if (commandString.trim().indexOf(" ") > 0)
		{
			sendResponse(ioSession, "501 Syntax error (no parameters allowed)");
			return;
		}

		try
		{
			if (sslFilter.isSslStarted(ioSession))
			{
				sendResponse(ioSession, "454 TLS not available due to temporary reason: TLS already active");
				return;
			}

			// Insert SSLFilter to get ready for handshaking
			ioSession.getFilterChain().addFirst("SSLfilter", sslFilter);

			// Disable encryption temporarily.
			// This attribute will be removed by SSLFilter
			// inside the Session.write() call below.
			ioSession.setAttribute(SslFilter.DISABLE_ENCRYPTION_ONCE, Boolean.TRUE);

			// Write StartTLSResponse which won't be encrypted.
			sendResponse(ioSession, "220 Ready to start TLS");

			// Now DISABLE_ENCRYPTION_ONCE attribute is cleared.
			assert ioSession.getAttribute(SslFilter.DISABLE_ENCRYPTION_ONCE) == null;

			ctx.getSMTPState().resetAll(); // clean state
			ctx.getDeliveryHandler().resetMessageState();
		}
		catch (Exception e)
		{
			LOG.debug("startTLS() failed: {}", e.getMessage());
			LOG.trace("startTLS() failure stack trace", e);			
		}
	}
}