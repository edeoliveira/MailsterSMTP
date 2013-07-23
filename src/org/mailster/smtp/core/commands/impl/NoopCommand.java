package org.mailster.smtp.core.commands.impl;

import java.io.IOException;

import org.apache.mina.core.session.IoSession;
import org.mailster.smtp.core.SMTPContext;
import org.mailster.smtp.core.commands.AbstractCommand;

/**
 * The NOOP command implementation.
 * 
 * @author De Oliveira Edouard &lt;doe_wanted@yahoo.fr&gt;
 */
public class NoopCommand extends AbstractCommand
{
	public NoopCommand()
	{
		super("NOOP", "The NOOP command does nothing. It can be used to keep the\n"+
				"current session alive pinging it to prevent a timeout");
	}

	@Override
	public boolean isAuthRequired()
	{
		return false;
	}

	public void execute(String commandString, IoSession ioSession, SMTPContext ctx) 
		throws IOException
	{
		sendResponse(ioSession, "250 Ok");
	}
}
