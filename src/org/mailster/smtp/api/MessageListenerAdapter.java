package org.mailster.smtp.api;

import java.io.IOException;
import java.io.InputStream;


/**
 * An adapter to the {@link MessageListener} interface.
 * 
 * @author De Oliveira Edouard &lt;doe_wanted@yahoo.fr&gt;
 */
public class MessageListenerAdapter implements MessageListener {

	public boolean accept(SessionContext ctx, String from, String recipient) {
		return true;
	}

	public void deliver(SessionContext ctx, String from, String recipient,
			InputStream data) throws TooMuchDataException, IOException {
	}
}
