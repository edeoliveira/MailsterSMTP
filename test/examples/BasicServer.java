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
public class BasicServer 
{
	public static void main(String[] args) 
	{
		SMTPServer server = new SMTPServer(new MessageListenerAdapter() {
			public void deliver(SessionContext ctx, String from, String recipient,
					InputStream data) throws TooMuchDataException, IOException {
				System.out.println("New message received");
			}
		});
		/*
		server.setAuthenticationHandlerFactory(new AllSchemesAuthenticationHandler(new LoginValidator() {
			public void login(String username, String password)
					throws LoginFailedException {
				System.out.println("username="+username);
				System.out.println("password="+password);
			}
		}));*/
		server.start();
	}
}