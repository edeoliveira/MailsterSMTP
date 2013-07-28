package org.mailster.smtp.core;

/**
 * Describes the state of an SMTP session. 
 * 
 * @author Edouard De Oliveira &lt;doe_wanted@yahoo.fr&gt;
 * @author Ian McFarland &lt;ian@neo.com&gt;
 * @author Jon Stevens
 * @author Jeff Schnitzer
 */
public class SMTPState
{
	private boolean authenticating	= false;
	private boolean authenticated	= false;
	private boolean dataMode		= false;
	private boolean hasSeenHelo		= false;
	private boolean active			= true;
	private boolean hasSender		= false;
	private int recipientCount		= 0;
	
	public SMTPState()
	{
	}

	public boolean isActive()
	{
		return this.active;
	}

	public void quit()
	{
		this.active = false;
	}

	public boolean getHasSender()
	{
		return this.hasSender;
	}

	public void setHasSender(boolean value)
	{
		this.hasSender = value;
	}

	public boolean getHasSeenHelo()
	{
		return this.hasSeenHelo;
	}

	public void setHasSeenHelo(boolean hasSeenHelo)
	{
		this.hasSeenHelo = hasSeenHelo;
	}

	public boolean isDataMode()
	{
		return this.dataMode;
	}

	public void setDataMode(boolean dataMode)
	{
		this.dataMode = dataMode;
	}
	
	public void addRecipient()
	{
		this.recipientCount++;
	}
	
	public int getRecipientCount()
	{
		return this.recipientCount;
	}

	public boolean isAuthenticated()
	{
		return authenticated;
	}

	public void setAuthenticated(boolean authenticated)
	{
		this.authenticated = authenticated;
	}
	
	public boolean isAuthenticating() 
	{
		return authenticating;
	}

	public void setAuthenticating(boolean authenticating) 
	{
		this.authenticating = authenticating;
	}

	/**
	 * Executes a full reset() of the session
	 * which requires a new HELO command to be sent
	 */
	public void resetAll()
	{
		reset(false);
		setAuthenticated(false);
	}

	/**
	 * Reset session, but don't require new HELO/EHLO
	 */
	public void reset()
	{
		reset(true);
	}
	
	private void reset(boolean hasSeenHelo)
	{
		this.hasSender = false;
		this.dataMode = false;
		this.active = true;
		this.hasSeenHelo = hasSeenHelo;
		this.recipientCount = 0;
	}
}
