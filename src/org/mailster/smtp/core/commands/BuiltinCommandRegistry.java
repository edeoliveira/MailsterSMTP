package org.mailster.smtp.core.commands;

import org.mailster.smtp.core.commands.impl.AuthCommand;
import org.mailster.smtp.core.commands.impl.DataCommand;
import org.mailster.smtp.core.commands.impl.EhloCommand;
import org.mailster.smtp.core.commands.impl.HeloCommand;
import org.mailster.smtp.core.commands.impl.HelpCommand;
import org.mailster.smtp.core.commands.impl.MailCommand;
import org.mailster.smtp.core.commands.impl.NoopCommand;
import org.mailster.smtp.core.commands.impl.QuitCommand;
import org.mailster.smtp.core.commands.impl.ReceiptCommand;
import org.mailster.smtp.core.commands.impl.ResetCommand;
import org.mailster.smtp.core.commands.impl.StartTLSCommand;


/**
 * Enumerates all the internal {@link Command} available.
 * 
 * @author De Oliveira Edouard &lt;doe_wanted@yahoo.fr&gt;
 */
public enum BuiltinCommandRegistry
{
	AUTH(AuthCommand.class), 
	DATA(DataCommand.class),
	EHLO(EhloCommand.class), 
	HELO(HeloCommand.class), 
	HELP(HelpCommand.class), 
	MAIL(MailCommand.class), 
	NOOP(NoopCommand.class), 
	QUIT(QuitCommand.class), 
	RCPT(ReceiptCommand.class), 
	RSET(ResetCommand.class), 
	STARTTLS(StartTLSCommand.class); 
	
	private Command instance;

	private BuiltinCommandRegistry(Class<? extends Command> c)
	{
		try
		{
			this.instance = c.newInstance();
		} catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}
	
	public Command getCommand() 
	{
		return instance;
	}
}
