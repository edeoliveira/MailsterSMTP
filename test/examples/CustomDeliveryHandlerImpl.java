package examples;

import java.io.IOException;
import java.io.InputStream;

import org.mailster.smtp.api.RejectException;
import org.mailster.smtp.api.TooMuchDataException;
import org.mailster.smtp.api.handler.AbstractDeliveryHandler;
import org.mailster.smtp.api.handler.DeliveryContext;
import org.mailster.smtp.core.auth.AuthenticationHandler;

/**
 * @author De Oliveira Edouard &lt;doe_wanted@yahoo.fr&gt;
 */
public class CustomDeliveryHandlerImpl 
	extends AbstractDeliveryHandler
{
	/**
	 * An instance of this implementation will be created for each 
	 * SMTP session.
	 * 
	 * @param ctx the delivery context
	 * @param authHandler the authentication handler
	 */
	protected CustomDeliveryHandlerImpl(DeliveryContext ctx,
			AuthenticationHandler authHandler) 
	{
		super(ctx, authHandler);
	}

	@Override
	public void data(InputStream data) throws RejectException,
			TooMuchDataException, IOException 
	{
		// TODO This is where mail data is really delivered 
		// to the listeners		
	}

	@Override
	public void from(String from) throws RejectException {
		// Called once (unless session is reset). It gives the
		// email address of the sender
	}

	@Override
	public void recipient(String recipient) throws RejectException {
		// Called once for each recipient 		
	}

	@Override
	public void resetMessageState() {
		// TODO Called if SMTP session is reset thus allowing to clean
		// this class state.
	}
}
