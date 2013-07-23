package org.mailster.smtp;

import org.mailster.smtp.core.auth.AuthenticationHandler;
import org.mailster.smtp.core.auth.AuthenticationHandlerFactory;
import org.mailster.smtp.core.auth.LoginValidator;
import org.mailster.smtp.core.auth.PluginAuthenticationHandler;
import org.mailster.smtp.core.auth.impl.DummyAuthenticationHandler;
import org.mailster.smtp.core.auth.impl.LoginAuthenticationHandler;
import org.mailster.smtp.core.auth.impl.PlainAuthenticationHandler;

/**
 * Implements a {@link PluginAuthenticationHandler} handler which
 * loads all implementations available except 
 * {@link DummyAuthenticationHandler}. 
 * 
 * @see DummyAuthenticationHandler for exclusion reasons.
 * 
 * @author De Oliveira Edouard &lt;doe_wanted@yahoo.fr&gt;
 */
public class AllSchemesAuthenticationHandler implements
		AuthenticationHandlerFactory 
{
	private LoginValidator validator;
	
	public AllSchemesAuthenticationHandler(LoginValidator validator)
	{
		this.validator = validator;
	}
	
	public AuthenticationHandler create() 
	{
		PluginAuthenticationHandler ret = new PluginAuthenticationHandler();
		ret.addPlugin(new PlainAuthenticationHandler(validator));
		ret.addPlugin(new LoginAuthenticationHandler(validator));
		return ret;
	}
}
