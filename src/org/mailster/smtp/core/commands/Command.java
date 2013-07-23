package org.mailster.smtp.core.commands;

import java.io.IOException;

import org.apache.mina.core.session.IoSession;
import org.mailster.smtp.core.SMTPContext;

/**
 * The interface each SMTP command has to implement.
 * 
 * @author Edouard De Oliveira &lt;doe_wanted@yahoo.fr&gt;
 */
public interface Command
{
	public void execute(String commandString, IoSession ioSession, SMTPContext ctx) 
		throws IOException;	
	
	public String getName();
	
	public boolean isAuthRequired();
}
