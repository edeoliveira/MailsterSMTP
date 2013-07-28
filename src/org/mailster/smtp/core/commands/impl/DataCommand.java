package org.mailster.smtp.core.commands.impl;

import java.io.IOException;

import org.apache.mina.core.session.IoSession;
import org.mailster.smtp.core.SMTPContext;
import org.mailster.smtp.core.SMTPState;
import org.mailster.smtp.core.commands.AbstractCommand;

/**
 * The DATA command implementation.
 * 
 * @author De Oliveira Edouard &lt;doe_wanted@yahoo.fr&gt;
 * @author Ian McFarland &lt;ian@neo.com&gt;
 * @author Jon Stevens
 * @author Jeff Schnitzer
 */
public class DataCommand extends AbstractCommand
{
	public DataCommand()
	{
		super("DATA", "The DATA command initiates the message transmission.\n" +
						"Message ends with <CR><LF>.<CR><LF>");
	}

	public void execute(String commandString, IoSession ioSession, SMTPContext ctx) 
		throws IOException
	{
		SMTPState smtpState = ctx.getSMTPState();

		if (!smtpState.getHasSender())
		{
			sendResponse(ioSession, "503 Error: need MAIL command");
			return;
		}
		else if (smtpState.getRecipientCount() == 0)
		{
			sendResponse(ioSession, "503 Error: need RCPT command");
			return;
		}

		smtpState.setDataMode(true);
		sendResponse(ioSession, "354 End data with <CR><LF>.<CR><LF>");		
	}
}