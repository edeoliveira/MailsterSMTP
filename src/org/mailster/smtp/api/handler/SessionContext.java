package org.mailster.smtp.api.handler;

import java.net.SocketAddress;

import org.mailster.smtp.api.MessageListener;
import org.mailster.smtp.core.auth.Credential;

/**
 * Interface which provides the session context to the {@link MessageListener}.
 * This is a subset of the original {@link DeliveryContext} to decomplexify
 * the interface.
 * 
 * It also provides the session attributes handling methods to provide the
 * implementors with some extended customization possibilities without having
 * to implement the {@link AbstractDeliveryHandler}.
 * 
 * @author De Oliveira Edouard &lt;doe_wanted@yahoo.fr&gt;
 */
public interface SessionContext
{
	/**
	 * @return the IP address of the remote server.
	 */
	public SocketAddress getRemoteAddress();
	
	/**
	 * @return the logged identity. Can be null if connection is still in
	 * authorization state or if authentication isn't required. 
	 */
	public Credential getCredential();	

	/**
	 * Adds an attribute to the current session object. The lifetime of an 
	 * attribute is the same as the one of the SMTP session. 
	 */
	public void addAttribute(String key, Object attr);
	
	/**
	 * Sets the value of attribute stored under the given key.
	 */
	public void setAttribute(String key, Object attr);
	
	/**
	 * Removes an attribute stored under the given key.
	 */
	public void removeAttribute(String key);
	
	/**
	 * Get the attribute value stored under the given key.
	 */
	public Object getAttribute(String key);
}