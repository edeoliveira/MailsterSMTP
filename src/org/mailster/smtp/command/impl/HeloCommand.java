package org.mailster.smtp.command.impl;

import java.io.IOException;

import org.apache.mina.core.session.IoSession;
import org.mailster.smtp.command.AbstractBaseCommand;
import org.mailster.smtp.core.mina.SMTPContext;

/**
 * The HELO command implementation.
 * 
 * @author De Oliveira Edouard &lt;doe_wanted@yahoo.fr&gt;
 * @author Ian McFarland &lt;ian@neo.com&gt;
 * @author Jon Stevens
 */
public class HeloCommand extends AbstractBaseCommand
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

		ctx.getSession().setHasSeenHelo(true);
		sendResponse(ioSession, "250 " + ctx.getSMTPServerConfig().getHostName());
	}
}
