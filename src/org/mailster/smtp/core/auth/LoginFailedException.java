package org.mailster.smtp.core.auth;

/**
 * Exception expected to be thrown by a validator (i.e LoginValidator)
 *
 * @author Marco Trevisan <mrctrevisan@yahoo.it>
 * @author De Oliveira Edouard &lt;doe_wanted@yahoo.fr&gt;
 */
public class LoginFailedException extends Exception
{
	private static final long serialVersionUID = -2568432389605367270L;

	/**
	 * {@inheritDoc}
	 */
	public LoginFailedException()
	{
		super("Authentication failed");
	}
	
	/** 
	 * {@inheritDoc}
	 */
	public LoginFailedException(String msg)
	{
		super(msg);
	}	
}
