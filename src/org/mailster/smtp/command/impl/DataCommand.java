package org.mailster.smtp.command.impl;

import java.io.IOException;

import org.apache.mina.core.session.IoSession;
import org.mailster.smtp.command.AbstractBaseCommand;
import org.mailster.smtp.core.Session;
import org.mailster.smtp.core.mina.SMTPContext;

/**
 * The DATA command implementation.
 * 
 * @author De Oliveira Edouard &lt;doe_wanted@yahoo.fr&gt;
 * @author Ian McFarland &lt;ian@neo.com&gt;
 * @author Jon Stevens
 * @author Jeff Schnitzer
 */
public class DataCommand extends AbstractBaseCommand
{
	public DataCommand()
	{
		super("DATA", "The DATA command initiates the message transmission.\n" +
						"Message ends with <CR><LF>.<CR><LF>");
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
		else if (session.getRecipientCount() == 0)
		{
			sendResponse(ioSession, "503 Error: need RCPT command");
			return;
		}

		session.setDataMode(true);
		sendResponse(ioSession, "354 End data with <CR><LF>.<CR><LF>");		
	}
}