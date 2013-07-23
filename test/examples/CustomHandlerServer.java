package examples;

import java.io.IOException;
import java.io.InputStream;

import org.mailster.smtp.SMTPServer;
import org.mailster.smtp.api.MessageListenerAdapter;
import org.mailster.smtp.api.SessionContext;
import org.mailster.smtp.api.TooMuchDataException;

/**
 * @author De Oliveira Edouard &lt;doe_wanted@yahoo.fr&gt;
 */
public class CustomHandlerServer 
{
	public static void main(String[] args) 
	{
		SMTPServer server = new SMTPServer(new MessageListenerAdapter() {
			public void deliver(SessionContext ctx, String from, String recipient,
					InputStream data) throws TooMuchDataException, IOException {
				System.out.println("New message received");
			}
		});
		
		server.getDeliveryHandlerFactory().setDeliveryHandlerImplClass(
				CustomDeliveryHandlerImpl.class);
		
		// TODO Optionally you can set an auth factory
		// server.setAuthenticationHandlerFactory(...);
		server.start();
	}
}