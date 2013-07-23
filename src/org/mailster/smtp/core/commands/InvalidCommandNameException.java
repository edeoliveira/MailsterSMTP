package org.mailster.smtp.core.commands;

/**
 * @author De Oliveira Edouard &lt;doe_wanted@yahoo.fr&gt;
 * @author Ian McFarland &lt;ian@neo.com&gt;
 */
public class InvalidCommandNameException extends CommandException
{
	private static final long serialVersionUID = 8650069874808249416L;

	/**
	 * {@inheritDoc}
	 */
	public InvalidCommandNameException()
	{
		super();
	}

	/**
	 * {@inheritDoc}
	 */
	public InvalidCommandNameException(String string)
	{
		super(string);
	}

	/**
	 * {@inheritDoc}
	 */
	public InvalidCommandNameException(String string, Throwable throwable)
	{
		super(string, throwable);
	}

	/**
	 * {@inheritDoc}
	 */
	public InvalidCommandNameException(Throwable throwable)
	{
		super(throwable);
	}
}
