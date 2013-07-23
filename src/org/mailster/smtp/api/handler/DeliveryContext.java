package org.mailster.smtp.api.handler;

import java.io.InputStream;
import java.net.SocketAddress;

import org.mailster.smtp.SMTPServerConfig;
import org.mailster.smtp.core.auth.Credential;

/**
 * Interface which provides context to the message handlers.
 * 
 * @author De Oliveira Edouard &lt;doe_wanted@yahoo.fr&gt;
 */
public interface DeliveryContext
{
	/**
	 * @return the server configuration.
	 */
	public SMTPServerConfig getSMTPServerConfig();
	
	/**
	 * @return the IP address of the remote server.
	 */
	public SocketAddress getRemoteAddress();
	
	/**
	 * @return the original data stream.
	 */
	public InputStream getInputStream();
	
	/**
	 * @return the logged identity. Can be null if connection is still in
	 * authorization state or if authentication isn't required. 
	 */
	public Credential getCredential();	
}