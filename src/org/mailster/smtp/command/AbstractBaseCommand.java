package org.mailster.smtp.command;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.apache.mina.core.session.IoSession;
import org.mailster.smtp.command.exceptions.CommandException;
import org.mailster.smtp.core.mina.SMTPConnectionHandler;

/**
 * An abstract class which provides a minimal function set used
 * by SMTP commands implementations.
 * 
 * @author De Oliveira Edouard &lt;doe_wanted@yahoo.fr&gt;
 * @author Ian McFarland &lt;ian@neo.com&gt;
 * @author Jon Stevens
 */
abstract public class AbstractBaseCommand implements Command
{
	private String name;
	private CommandHandler handler;
	private static Map<String, HelpMessage> helpMessageMap = new HashMap<String, HelpMessage>();
	
	public AbstractBaseCommand(String name, String help)
	{
		this.name = name;
		if (help != null)
			setHelp(new HelpMessage(name, help));
	}
	
	public AbstractBaseCommand(String name, String help, String argumentDescription)
	{
		this.name = name;
		if (help != null)
			setHelp(new HelpMessage(name, help, argumentDescription));
	}
	
	private void setHelp(HelpMessage helpMessage)
	{
		helpMessageMap.put(helpMessage.getName().toUpperCase(), helpMessage);
	}
	
	public CommandHandler getCommandHandler()
	{
		return handler;
	}
	
	public void setCommandHandler(CommandHandler handler)
	{
		this.handler = handler;
	}
	
	public HelpMessage getHelp(String commandName)
		throws CommandException
	{
		HelpMessage msg = helpMessageMap.get(commandName.toUpperCase());
		if (msg == null)
			throw new CommandException();
		return msg;
	}
	
	protected Map<String, HelpMessage> getHelp()
	{
		return helpMessageMap;
	}
	
	protected String getArgPredicate(String commandString)
	{
		if (commandString == null || commandString.length() < 4)
			return "";
		
		return commandString.substring(4).trim();
	}
	
	public String getName()
	{
		return name;
	}
	
	protected void sendResponse(IoSession session, String response) 
		throws IOException
	{
		SMTPConnectionHandler.sendResponse(session, response);
	}
	
	protected boolean isValidEmailAddress(String address)
	{
		// MAIL FROM: <>
		if (address.length() == 0)
			return true;

		try
		{
			InternetAddress[] ia = InternetAddress.parse(address, true);
			return (ia.length != 0);
		}
		catch (AddressException ae)
		{
			return false;
		}
	}
	
	protected static void getTokenizedString(StringBuilder sb, Collection<String> items, String delim)
	{
		for( Iterator<String> it=items.iterator(); it.hasNext(); )
		{
			sb.append(it.next());
			if( it.hasNext() )
				sb.append(delim);
		}
	}
	
	protected String[] getArgs(String commandString)
	{
		List<String> strings = new ArrayList<String>();
		StringTokenizer st = new StringTokenizer(commandString);
		
		while (st.hasMoreTokens())
			strings.add(st.nextToken());
		
		return strings.toArray(new String[strings.size()]);
	}
	
	protected String extractEmailAddress(String args, int subcommandOffset)
	{
		String address = args.substring(subcommandOffset).trim();
		if (address.indexOf('<') == 0)
			address = address.substring(1, address.indexOf('>'));
		
		return address;
	}

	@Override
	public boolean isAuthRequired()
	{
		return true;
	}
}