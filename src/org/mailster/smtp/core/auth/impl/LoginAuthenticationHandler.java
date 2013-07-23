package org.mailster.smtp.core.auth.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;

import org.mailster.smtp.core.SMTPContext;
import org.mailster.smtp.core.auth.AuthenticationHandler;
import org.mailster.smtp.core.auth.Credential;
import org.mailster.smtp.core.auth.LoginFailedException;
import org.mailster.smtp.core.auth.LoginValidator;
import org.mailster.smtp.util.Base64;

/**
 * Implements the SMTP AUTH LOGIN mechanism.<br />
 * You are only required to plug your LoginValidator implementation
 * for username and password validation to take effect.
 * 
 * @author Marco Trevisan <mrctrevisan@yahoo.it>
 * @author De Oliveira Edouard &lt;doe_wanted@yahoo.fr&gt;
 */
public class LoginAuthenticationHandler implements AuthenticationHandler
{
	private String username;

	private String password;

	private LoginValidator helper;

	private List<String> authentificationMechanisms;
	
	/** Creates a new instance of LoginAuthenticationHandler */
	public LoginAuthenticationHandler(LoginValidator helper)
	{
		this.helper = helper;

		authentificationMechanisms = new ArrayList<String>(1);
		authentificationMechanisms.add("LOGIN");
	}

	/**
	 * 
	 */
	public List<String> getAuthenticationMechanisms()
	{
		return Collections.unmodifiableList(authentificationMechanisms);
	}

	/**
	 * 
	 */
	public boolean auth(String clientInput, StringBuilder response, SMTPContext ctx) 
		throws LoginFailedException
	{
		StringTokenizer stk = new StringTokenizer(clientInput);
		String token = stk.nextToken();
		if (token.trim().equalsIgnoreCase("AUTH"))
		{
			// The RFC2554 "initial-response" parameter must not be present
			// The line must be in the form of "AUTH LOGIN"
			if (!stk.nextToken().trim().equalsIgnoreCase("LOGIN"))
			{
				// Mechanism mismatch
				response.append("504 AUTH mechanism mismatch");
				return true;
			}

			if (stk.hasMoreTokens())
			{
				// the client submitted an initial response
				response.append("535 Initial response not allowed in AUTH LOGIN");
				return true;
			}

			response.append("334 ").append(Base64.encodeToString("Username:".getBytes(), false));
			return false;
		}

		if (username == null)
		{
			byte[] decoded = Base64.decode(clientInput);
			if (decoded == null)
			{
				throw new LoginFailedException();
			}
			this.username = new String(decoded);
			response.append("334 ").append(Base64.encodeToString("Password:".getBytes(), false));
			return false;
		}

		byte[] decoded = Base64.decode(clientInput);
		if (decoded == null)
		{
			throw new LoginFailedException();
		}

		this.password = new String(decoded);
		
		try
		{
			helper.login(username, password);
			resetState();
		}
		catch (LoginFailedException lfe)
		{
			resetState();
			throw lfe;
		}
		
		ctx.setCredential(new Credential(username));
		return true;
	}

	/**
	 * 
	 */
	public void resetState()
	{
		this.username = null;
		this.password = null;
	}
}
