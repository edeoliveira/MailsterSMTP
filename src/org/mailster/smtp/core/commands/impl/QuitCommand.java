package org.mailster.smtp.core.commands.impl;

import java.io.IOException;

import org.apache.mina.core.session.IoSession;
import org.mailster.smtp.core.SMTPContext;
import org.mailster.smtp.core.commands.AbstractCommand;

/**
 * The QUIT command implementation.
 * 
 * @author De Oliveira Edouard &lt;doe_wanted@yahoo.fr&gt;
 * @author Ian McFarland &lt;ian@neo.com&gt;
 * @author Jon Stevens
 */
public class QuitCommand extends AbstractCommand
{
	public QuitCommand()
	{
		super("QUIT", "The QUIT command closes the SMTP session");
	}

	@Override
	public boolean isAuthRequired()
	{
		return false;
	}

	public void execute(String commandString, IoSession ioSession, SMTPContext ctx) 
		throws IOException
	{
		ctx.getSMTPState().quit();
		sendResponse(ioSession, "221 Bye");		
	}
}
