package org.mailster.smtp.core.command.impl;

import java.io.IOException;

import org.apache.mina.core.session.IoSession;
import org.mailster.smtp.api.RejectException;
import org.mailster.smtp.core.SMTPContext;
import org.mailster.smtp.core.Session;
import org.mailster.smtp.core.command.AbstractCommand;

/**
 * The MAIL command implementation.
 * 
 * @author De Oliveira Edouard &lt;doe_wanted@yahoo.fr&gt;
 * @author Ian McFarland &lt;ian@neo.com&gt;
 * @author Jon Stevens
 */
public class MailCommand extends AbstractCommand
{
	public MailCommand()
	{
		super("MAIL", "The MAIL FROM command specifies the sender", 
				"FROM: <address>\n address = the email address of the sender");
	}

	public void execute(String commandString, IoSession ioSession, SMTPContext ctx) 
		throws IOException
	{
		Session session = ctx.getSession();
		if (!session.getHasSeenHelo())
		{
			sendResponse(ioSession, "503 Error: send HELO/EHLO first");
		}
		else if (session.getHasSender())
		{
			sendResponse(ioSession, "503 Sender already specified");
		}
		else
		{
			if (commandString.trim().equals("MAIL FROM:"))
			{
				sendResponse(ioSession, "501 Syntax: MAIL FROM: <address>");
				return;
			}

			String args = getArgPredicate(commandString);
			if (!args.toUpperCase().startsWith("FROM:"))
			{
				sendResponse(ioSession, "501 Syntax: MAIL FROM: <address>  Error in parameters: \""
								+ getArgPredicate(commandString) + "\"");
				return;
			}

			String emailAddress = extractEmailAddress(args, 5);
			if (isValidEmailAddress(emailAddress))
			{
				try
				{
					ctx.getDeliveryHandler().from(emailAddress);
					session.setHasSender(true);
					sendResponse(ioSession, "250 Ok");
				}
				catch (RejectException ex)
				{
					sendResponse(ioSession, ex.getMessage());
				}
			}
			else
			{
				sendResponse(ioSession, "553 <" + emailAddress + "> Invalid email address");
			}
		}
	}
}
