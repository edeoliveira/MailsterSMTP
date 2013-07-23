package org.mailster.smtp.core.command;

import org.mailster.smtp.core.command.impl.AuthCommand;
import org.mailster.smtp.core.command.impl.DataCommand;
import org.mailster.smtp.core.command.impl.DataEndCommand;
import org.mailster.smtp.core.command.impl.EhloCommand;
import org.mailster.smtp.core.command.impl.HeloCommand;
import org.mailster.smtp.core.command.impl.HelpCommand;
import org.mailster.smtp.core.command.impl.MailCommand;
import org.mailster.smtp.core.command.impl.NoopCommand;
import org.mailster.smtp.core.command.impl.QuitCommand;
import org.mailster.smtp.core.command.impl.ReceiptCommand;
import org.mailster.smtp.core.command.impl.ResetCommand;
import org.mailster.smtp.core.command.impl.StartTLSCommand;


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
	STARTTLS(StartTLSCommand.class), 
	
	// Add a fake command to handle the asynchronous end of DATA 
    DATA_END(DataEndCommand.class);

	private Class<? extends Command> commandClass;

	private BuiltinCommandRegistry(Class<? extends Command> c)
	{
		this.commandClass = c;
	}

	public String getClassName()
	{
		return commandClass.getSimpleName();
	}
	
	public Command getNewInstance() 
		throws InstantiationException, IllegalAccessException
	{
		return this.commandClass.newInstance();
	}
}
