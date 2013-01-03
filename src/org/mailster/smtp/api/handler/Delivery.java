package org.mailster.smtp.api.handler;

import org.mailster.smtp.api.listener.MessageListener;

/**
 * Tracks which listeners need delivery.
 * 
 * @author De Oliveira Edouard &lt;doe_wanted@yahoo.fr&gt;
 */
public class Delivery 
{
	private MessageListener listener;		
	private String recipient;
	
	public MessageListener getListener() 
	{ 
		return this.listener; 
	}
	
	public String getRecipient() 
	{ 
		return this.recipient; 
	}
	
	public Delivery(MessageListener listener, String recipient)
	{
		this.listener = listener;
		this.recipient = recipient;
	}
}
