package org.mailster.smtp.core.auth;

/**
 * Use this when your authentication scheme uses a username and a password.
 *
 * @author De Oliveira Edouard &lt;doe_wanted@yahoo.fr&gt;
 * @author Marco Trevisan <mrctrevisan@yahoo.it>
 */
public interface LoginValidator
{
	public void login(final String username, final String password)
		throws LoginFailedException;
}
