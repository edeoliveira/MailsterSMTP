package org.mailster.smtp.command.impl;

import java.io.IOException;

import org.apache.mina.core.session.IoSession;
import org.mailster.smtp.api.RejectException;
import org.mailster.smtp.api.TooMuchDataException;
import org.mailster.smtp.command.AbstractBaseCommand;
import org.mailster.smtp.core.mina.SMTPContext;

/**
 * Data command splitted to adapt to MINA framework.
 * Called when <CR><LF>.<CR><LF> is received after entering 
 * DATA mode.
 *
 * @author De Oliveira Edouard &lt;doe_wanted@yahoo.fr&gt;
 */
public class DataEndCommand extends AbstractBaseCommand
{
	public DataEndCommand()
	{
		super("DATA_END", null);
	}

	public void execute(String commandString, IoSession ioSession, SMTPContext ctx) 
		throws TooMuchDataException, IOException
	{
		try
		{
			ctx.getDeliveryHandler().data(ctx.getInputStream());
			ResetCommand.resetContext(ctx);
			sendResponse(ioSession, "250 Ok");
		}
		catch (RejectException ex)
		{
			ResetCommand.resetContext(ctx);
			sendResponse(ioSession, ex.getMessage());
		}
	}
}