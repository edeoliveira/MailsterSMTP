package org.mailster.smtp.core.commands.impl;

import java.io.IOException;

import org.apache.mina.core.session.IoSession;
import org.mailster.smtp.core.SMTPContext;
import org.mailster.smtp.core.SMTPState;
import org.mailster.smtp.core.auth.LoginFailedException;
import org.mailster.smtp.core.commands.AbstractCommand;

/**
 * The AUTH command implementation.
 * 
 * @author De Oliveira Edouard &lt;doe_wanted@yahoo.fr&gt;
 * @author Marco Trevisan <mrctrevisan@yahoo.it>
 */
public class AuthCommand extends AbstractCommand
{
	public static final String VERB = "AUTH";
	
	public static final String AUTH_CANCEL_COMMAND = "*";

	/** Creates a new instance of AuthCommand */
	public AuthCommand()
	{
		super(VERB, "The AUTH command allow to select the authentication mechanism to use\n"+
					"when authenticating with the server", 
				"<mechanism> [<initial-response>]\n"
				+ " mechanism = a string identifying a SASL authentication mechanism,\n"
				+ " initial-response = an optional base64-encoded response");
	}

	@Override
	public boolean isAuthRequired()
	{
		return false;
	}

	public void execute(String commandString, IoSession ioSession, SMTPContext ctx) 
		throws IOException
	{
		SMTPState smtpState = ctx.getSMTPState();
		
		if (smtpState.isAuthenticated())
		{
			sendResponse(ioSession, "503 Refusing any other AUTH command");
			return;
		}

		boolean authenticating = smtpState.isAuthenticating();
		
		if (!authenticating)
		{
			String[] args = getArgs(commandString);
			
			// Let's check the command syntax
			if (args.length < 2)
			{
				sendResponse(ioSession, "501 Syntax: " + VERB
						+ " mechanism [initial-response]");
				return;
			}
			
			// Let's check if we support the required authentication mechanism
			String mechanism = args[1];
			if (!ctx.getAuthenticationHandler().getAuthenticationMechanisms().contains(
					mechanism.toUpperCase()))
			{
				sendResponse(ioSession, "504 Unrecognized authentication type");
				return;
			}
		}
		
		// OK, let's go trough the authentication process.
		// The authentication process may require a series of
		// challenge-responses
		try
		{						
			if (authenticating && commandString.trim().equals(AUTH_CANCEL_COMMAND))
			{
				// RFC 2554 explicitly states this:
				sendResponse(ioSession, "501 Authentication canceled by client");
				return;
			}
			
			StringBuilder response = new StringBuilder();
			boolean finished = ctx.getAuthenticationHandler().auth(commandString, response, ctx);
			
			smtpState.setAuthenticating(!finished);
			
			if (!finished)
			{
				// challenge-response iteration
				sendResponse(ioSession, response.toString());				
				return;
			}

			smtpState.setAuthenticated(true);
			sendResponse(ioSession, "235 Authentication successful");
		}
		catch (LoginFailedException ex)
		{
			sendResponse(ioSession, "535 Authentication failure");
			smtpState.setAuthenticated(false);
			smtpState.setAuthenticating(false);
		}
	}
}
