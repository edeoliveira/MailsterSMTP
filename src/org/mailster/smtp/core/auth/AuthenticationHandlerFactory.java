package org.mailster.smtp.core.auth;

/**
 * Factory that creates {@link AuthenticationHandler}.
 * 
 * @author Marco Trevisan <mrctrevisan@yahoo.it>
 * @author De Oliveira Edouard &lt;doe_wanted@yahoo.fr&gt;
 */
public interface AuthenticationHandlerFactory
{
	public AuthenticationHandler create();
}
