package org.mailster.smtp.command.impl;

import java.io.IOException;

import org.apache.mina.core.session.IoSession;
import org.mailster.smtp.command.AbstractBaseCommand;
import org.mailster.smtp.core.mina.SMTPContext;

/**
 * The NOOP command implementation.
 * 
 * @author De Oliveira Edouard &lt;doe_wanted@yahoo.fr&gt;
 */
public class NoopCommand extends AbstractBaseCommand
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
