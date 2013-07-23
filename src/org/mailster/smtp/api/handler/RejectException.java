package org.mailster.smtp.api.handler;

/**
 * Thrown to reject an SMTP command with a specific code.
 * 
 * @author De Oliveira Edouard &lt;doe_wanted@yahoo.fr&gt;
 * @author Jeff Schnitzer
 */
public class RejectException extends Exception
{
	private static final long serialVersionUID = -2518325451992929294L;
	
	/**
	 * The smtp error code
	 */
	int code;
	
	/**
	 * {@inheritDoc}
	 */
	public RejectException()
	{
		this(554, "Transaction failed");
	}

	/**
	 * {@inheritDoc}
	 */
	public RejectException(int code, String message)
	{
		super(code + " " + message);
		
		this.code = code;
	}

	/**
	 * Returns the smtp error code.
	 */
	public int getCode()
	{
		return this.code;
	}
}
