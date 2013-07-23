package org.mailster.smtp.core.commands;

import org.mailster.smtp.core.commands.impl.AuthCommand;
import org.mailster.smtp.core.commands.impl.DataCommand;
import org.mailster.smtp.core.commands.impl.DataEndCommand;
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
