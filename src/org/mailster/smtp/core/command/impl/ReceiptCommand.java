package org.mailster.smtp.core.command.impl;

import java.io.IOException;

import org.apache.mina.core.session.IoSession;
import org.mailster.smtp.api.RejectException;
import org.mailster.smtp.core.SMTPContext;
import org.mailster.smtp.core.Session;
import org.mailster.smtp.core.command.AbstractCommand;

/**
 * The RCPT command implementation.
 * 
 * @author De Oliveira Edouard &lt;doe_wanted@yahoo.fr&gt;
 * @author Ian McFarland &lt;ian@neo.com&gt;
 * @author Jon Stevens
 * @author Jeff Schnitzer
 */
public class ReceiptCommand extends AbstractCommand
{
	public ReceiptCommand()
	{
		super("RCPT", "The RCPT command specifies the recipient. This command can be used\n" +
				"any number of times to specify multiple recipients.", 
				"TO: <recipient>\n recipient = the email address of the recipient of the message");
	}

	public void execute(String commandString, IoSession ioSession, SMTPContext ctx) 
		throws IOException
	{
		Session session = ctx.getSession();
		if (!session.getHasSender())
		{
			sendResponse(ioSession, "503 Error: need MAIL command");
			return;
		}
		
		int max = ctx.getSMTPServerConfig().getMaxRecipients();
		if (max > -1 && session.getRecipientCount() >= max)
		{
			sendResponse(ioSession, "452 Too many recipients");
			return;
		}

		String args = getArgPredicate(commandString);
		if (args.toUpperCase().startsWith("TO:"))
		{
			String recipientAddress = extractEmailAddress(args, 3);
			try
			{
				ctx.getDeliveryHandler().recipient(recipientAddress);
				session.addRecipient();
				sendResponse(ioSession, "250 Ok");
			}
			catch (RejectException ex)
			{
				sendResponse(ioSession, ex.getMessage());
			}
		}
        else
        {
            sendResponse(ioSession, "501 Syntax: RCPT TO: <address> Error in parameters: \"" + args + "\"");
        }
	}
}
