package org.mailster.smtp;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.ArrayList;
import java.util.List;

import org.mailster.smtp.api.MessageListener;
import org.mailster.smtp.api.handler.AbstractDeliveryHandler;
import org.mailster.smtp.api.handler.Delivery;
import org.mailster.smtp.api.handler.DeliveryContext;
import org.mailster.smtp.api.handler.RejectException;
import org.mailster.smtp.core.TooMuchDataException;
import org.mailster.smtp.core.auth.AuthenticationHandler;
import org.mailster.smtp.util.SharedStreamUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default class that extends the {@link AbstractDeliveryHandler} class.
 * Provides a default implementation for mail delivery.
 * 
 * @author De Oliveira Edouard &lt;doe_wanted@yahoo.fr&gt;
 */
public class DefaultDeliveryHandler
	extends AbstractDeliveryHandler
{
	private static final Logger LOG = 
		LoggerFactory.getLogger(DefaultDeliveryHandler.class);
	
	private List<Delivery> deliveries = new ArrayList<Delivery>();
	private String from;
	
	public DefaultDeliveryHandler(DeliveryContext ctx, AuthenticationHandler authHandler)
	{
		super(ctx, authHandler);
	}
	
	/** 
	 * {@inheritDoc}
	 */	
	public void from(String from) 
		throws RejectException
	{
		this.from = from;
	}
	
	/** 
	 * {@inheritDoc}
	 */	
	public void recipient(String recipient) 
		throws RejectException
	{
		boolean addedListener = false;
		
		for (MessageListener listener: getListeners())
		{
			if (listener.accept(getSessionContext(), this.from, recipient))
			{
				this.deliveries.add(new Delivery(listener, recipient));
				addedListener = true;
			}
		}
		
		if (!addedListener)
			throw new RejectException(553, "<" + recipient + "> address unknown.");
	}
	
	/** 
	 * {@inheritDoc}
	 */
	public void resetMessageState()
	{
		this.deliveries.clear();
	}
	
	/**
	 * Implementation of the data receiving portion of things. By default
	 * deliver a copy of the stream to each recipient of the message(the first 
	 * recipient is provided the original stream to save memory space). If
	 * you want to change this behavior, then you should implement the
	 * MessageHandler interface yourself.
	 */
	public void data(InputStream data) 
		throws TooMuchDataException, IOException
	{
		boolean useCopy = false;
		
		if (LOG.isTraceEnabled())
		{
			Charset charset = getDeliveryContext().getSMTPServerConfig().getCharset();
			InputStream in = SharedStreamUtils.getPrivateInputStream(useCopy, data);
			byte[] buf = new byte[16384];
			
			try
			{
				CharsetDecoder decoder = charset.newDecoder();
				int len;
				while ((len=in.read(buf)) >= 0)
				{
					LOG.trace(decoder.decode(
						ByteBuffer.wrap(buf, 0, len)).toString());
				}			
			}
			catch (IOException ioex)
			{
				LOG.trace("Mail data logging failed", ioex);
			}
			useCopy = true;
		}
		
		// Prevent concurrent modifications
		List<Delivery> list = new ArrayList<Delivery>(this.deliveries);
		
		for (Delivery delivery : list)
		{				
		    delivery.getListener().deliver(getSessionContext(), this.from, 
		    		delivery.getRecipient(), SharedStreamUtils.getPrivateInputStream(useCopy, data));
		    
		    // Use a stream copy on second iteration if not the case yet
		    useCopy = true;
		}
	}
}