package org.mailster.smtp.core.commands.impl;

import java.io.IOException;

import org.apache.mina.core.session.IoSession;
import org.mailster.smtp.core.SMTPContext;
import org.mailster.smtp.core.commands.AbstractCommand;

/**
 * The HELO command implementation.
 * 
 * @author De Oliveira Edouard &lt;doe_wanted@yahoo.fr&gt;
 * @author Ian McFarland &lt;ian@neo.com&gt;
 * @author Jon Stevens
 */
public class HeloCommand extends AbstractCommand
{
	public HeloCommand()
	{
		super("HELO", 
			  "The HELO command posts the client hostname info to the server.", 
			  "<hostname>\n hostname = your hostname");
	}

	@Override
	public boolean isAuthRequired()
	{
		return false;
	}

	public void execute(String commandString, IoSession ioSession, SMTPContext ctx) 
		throws IOException
	{
		String[] args = getArgs(commandString);
		if (args.length < 2)
		{
			sendResponse(ioSession, "501 Syntax: HELO <hostname>");
			return;
		}

		ctx.getSMTPState().setHasSeenHelo(true);
		sendResponse(ioSession, "250 " + ctx.getSMTPServerConfig().getHostName());
	}
}
