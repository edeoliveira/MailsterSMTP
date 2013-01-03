package org.mailster.smtp.auth.impl;

import java.util.ArrayList;
import java.util.List;

import org.mailster.smtp.auth.AuthenticationHandler;
import org.mailster.smtp.auth.LoginFailedException;
import org.mailster.smtp.core.mina.SMTPContext;

/**
 * Implements a dummy AUTH mechanism.<br />
 * Will always allow to login without asking for any parameter.
 * This is a test purpose handler only and should not be used
 * in production environemnts.
 * 
 * @author De Oliveira Edouard &lt;doe_wanted@yahoo.fr&gt;
 * @author Jeff Schnitzer 
 */
public class DummyAuthenticationHandler implements AuthenticationHandler
{
	public List<String> getAuthenticationMechanisms()
	{
		return new ArrayList<String>();
	}
	
	public boolean auth(String clientInput, StringBuilder response, SMTPContext ctx) 
		throws LoginFailedException
	{
		return true;
	}
	
	public void resetState()
	{
	}
}