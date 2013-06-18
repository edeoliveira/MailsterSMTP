package org.mailster.smtp.command.impl;

import java.io.IOException;

import org.apache.mina.core.session.IoSession;
import org.mailster.smtp.command.AbstractBaseCommand;
import org.mailster.smtp.core.mina.SMTPContext;

/**
 * The RSET command implementation.
 * 
 * @author De Oliveira Edouard &lt;doe_wanted@yahoo.fr&gt;
 * @author Ian McFarland &lt;ian@neo.com&gt;
 * @author Jon Stevens
 */
public class ResetCommand extends AbstractBaseCommand
{
	public ResetCommand()
	{
		super("RSET", "The RSET command resets the state of the mail session");
	}

	@Override
	public boolean isAuthRequired()
	{
		return false;
	}

	public void execute(String commandString, IoSession ioSession, SMTPContext ctx) 
		throws IOException
	{
		resetContext(ctx);
		sendResponse(ioSession, "250 Ok");
	}
	
	public static void resetContext(SMTPContext ctx)
	{
		ctx.getSession().reset();
		ctx.getDeliveryHandler().resetMessageState();
	}
}
