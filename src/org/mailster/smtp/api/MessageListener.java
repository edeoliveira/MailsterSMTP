package org.mailster.smtp.api;

import java.io.IOException;
import java.io.InputStream;

import org.mailster.smtp.api.handler.SessionContext;
import org.mailster.smtp.core.TooMuchDataException;



/**
 * This is an interface for processing the end-result messages that is
 * higher-level than the MessageHandler and related factory.
 * 
 * While the SMTP message is being received, all listeners are asked if they
 * want to accept each recipient. After the message has arrived, the message is
 * handed off to all accepting listeners.
 * 
 * @author De Oliveira Edouard &lt;doe_wanted@yahoo.fr&gt;
 * @author Jeff Schnitzer
 */
public interface MessageListener
{
	/**
	 * Called once for every RCPT TO during a SMTP exchange.
	 * 
	 * @param ctx is the context of the message that provides some basic info like 
	 *        the credential, the remote ip address or the ability to store some 
	 *        private attributes
	 * @param from is a rfc822-compliant email address.
	 * @param recipient is a rfc822-compliant email address.
	 * 
	 * @return true if the listener wants delivery of the message, false if the
	 *         message is not for this listener.
	 */
	public boolean accept(SessionContext ctx, String from, String recipient);

	/**
	 * When message data arrives, this method will be called for every recipient
	 * this listener accepted.
	 * 
	 * @param ctx is the context of the message that provides some basic info like 
	 *        the credential, the remote ip address or the ability to store some 
	 *        private attributes
	 * @param from is the envelope sender in rfc822 form
	 * @param recipient will be an accepted recipient in rfc822 form
	 * @param data will be the smtp data stream, stripped of any extra '.' chars
	 * 
	 * @throws TooMuchDataException if the listener can't handle that much data.
	 *         An error will be reported to the client.
	 * @throws IOException if there is an IO error reading the input data.
	 */
	public void deliver(SessionContext ctx, String from, 
						String recipient, InputStream data)
		throws TooMuchDataException, IOException;
}
