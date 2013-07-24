package org.mailster.smtp.api.handler;

import java.io.IOException;
import java.io.InputStream;
import java.net.SocketAddress;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.mailster.smtp.api.MessageListener;
import org.mailster.smtp.api.SessionContext;
import org.mailster.smtp.api.TooMuchDataException;
import org.mailster.smtp.core.SMTPContext;
import org.mailster.smtp.core.auth.AuthenticationHandler;
import org.mailster.smtp.core.auth.Credential;
import org.mailster.smtp.core.auth.LoginFailedException;

/**
 * A simple base class to make implementation of delivery handlers easier.
 * 
 * @author De Oliveira Edouard &lt;doe_wanted@yahoo.fr&gt;
 */
abstract public class AbstractDeliveryHandler 	
	implements AuthenticationHandler	
{
	private class SessionContextImpl implements SessionContext
	{
		private HashMap<String, Object> attrs = new HashMap<String, Object>();
		
		public SessionContextImpl() {
		}

		public void addAttribute(String key, Object attr) {
			attrs.put(key, attr);
		}

		public Object getAttribute(String key) {
			return attrs.get(key);
		}

		public Credential getCredential() {
			return ctx.getCredential();
		}

		public SocketAddress getRemoteAddress() {
			return ctx.getRemoteAddress();
		}

		public void removeAttribute(String key) {
			attrs.remove(key);
		}

		public void setAttribute(String key, Object attr) {
			attrs.put(key, attr);
		}
	}
	
	private AuthenticationHandler authHandler;
	private Collection<MessageListener> listeners;
	private DeliveryContext ctx;
	private SessionContextImpl sessionCtx;
	
	public DeliveryContext getDeliveryContext() 
	{
		return ctx;
	}

	public SessionContext getSessionContext()
	{
		return sessionCtx;
	}
	
	protected AbstractDeliveryHandler(DeliveryContext ctx, 
			AuthenticationHandler authHandler)
	{
		this.authHandler = authHandler;
		this.ctx = ctx;
		this.sessionCtx = new SessionContextImpl();
	}

	public void setListeners(Collection<MessageListener> listeners) 
	{
		this.listeners = listeners;
	}

	public Collection<MessageListener> getListeners() 
	{
		return listeners;
	}	
	
	/** */
	public boolean auth(String clientInput, StringBuilder response, SMTPContext ctx) 
		throws LoginFailedException
	{
		return authHandler.auth(clientInput, response, ctx);
	}

	/** */
	public void resetState()
	{
		authHandler.resetState();
	}
	
	/** */
	public List<String> getAuthenticationMechanisms()
	{
		return authHandler.getAuthenticationMechanisms();
	}
	
	/** 
	 * Returns the @link {@link AuthenticationHandler}.
	 */
	public AuthenticationHandler getAuthenticationHandler()
	{
		return authHandler;
	}
	
	/**
	 * Called first, after the MAIL FROM during a SMTP exchange.
	 *
	 * @param from is the sender as specified by the client.  It will
	 *  be a rfc822-compliant email address, already validated by
	 *  the server.
	 * @throws RejectException if the sender should be denied.
	 */
	public abstract void from(String from) throws RejectException;
	
	/**
	 * Called once for every RCPT TO during a SMTP exchange.
	 * This will occur after a from() call.
	 *
	 * @param recipient is a rfc822-compliant email address,
	 *  validated by the server.
	 * @throws RejectException if the recipient should be denied.
	 */
	public abstract void recipient(String recipient) throws RejectException;
	
	/**
	 * Called when the DATA part of the SMTP exchange begins.  Will
	 * only be called if at least one recipient was accepted.
	 *
	 * @param data will be the smtp data stream, stripped of any extra '.' chars
	 *
	 * @throws TooMuchDataException if the listener can't handle that much data.
	 *         An error will be reported to the client.
	 * @throws IOException if there is an IO error reading the input data.
	 */
	public abstract void data(InputStream data) throws TooMuchDataException, IOException;
	
	/**
	 * This method is called whenever a RSET command is sent or after the end of 
	 * the DATA command. It can be used to clean up any pending deliveries.
	 */
	public abstract void resetMessageState();	
}