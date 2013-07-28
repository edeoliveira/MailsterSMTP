package org.mailster.smtp.core;

import java.io.InputStream;
import java.net.SocketAddress;

import org.apache.mina.core.session.IoSession;
import org.mailster.smtp.SMTPServerConfig;
import org.mailster.smtp.api.handler.AbstractDeliveryHandler;
import org.mailster.smtp.api.handler.DeliveryContext;
import org.mailster.smtp.api.handler.DeliveryHandlerFactory;
import org.mailster.smtp.core.auth.AuthenticationHandler;
import org.mailster.smtp.core.auth.Credential;

/**
 * The context of a SMTP session.
 * 
 * @author De Oliveira Edouard &lt;doe_wanted@yahoo.fr&gt;
 */
public class SMTPContext implements DeliveryContext
{
	private SMTPServerConfig cfg;
	
	private SMTPState smtpState;	
	private SocketAddress remoteAddress;
	private Credential credential;
	
	private InputStream inputStream;

	private AbstractDeliveryHandler deliveryHandler;
	private AuthenticationHandler authenticationHandler;

	public SMTPContext(SMTPServerConfig cfg, DeliveryHandlerFactory factory, 
			IoSession ioSession)
	{
		this.cfg = cfg;
		this.remoteAddress = ioSession.getRemoteAddress();
		this.smtpState = new SMTPState();
		
		this.deliveryHandler = factory.create(this);
		this.authenticationHandler = deliveryHandler.getAuthenticationHandler();
	}

	public AbstractDeliveryHandler getDeliveryHandler() 
	{
		return deliveryHandler;
	}

	public AuthenticationHandler getAuthenticationHandler() 
	{
		return authenticationHandler;
	}

	public InputStream getInputStream() 
	{
		return inputStream;
	}

	public void setInputStream(InputStream inputStream) 
	{
		this.inputStream = inputStream;
	}

	public SMTPState getSMTPState()
	{
		return smtpState;
	}

	public SocketAddress getRemoteAddress()
	{
		return remoteAddress;
	}

	public SMTPServerConfig getSMTPServerConfig()
	{
		return cfg;
	}

	public Credential getCredential() 
	{
		return credential;
	}

	public void setCredential(Credential credential) 
	{
		this.credential = credential;
	}
	
	public void reset()
	{
		smtpState.reset();
		deliveryHandler.resetMessageState();
	}	
}
