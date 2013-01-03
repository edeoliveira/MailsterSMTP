package junit.command;

import junit.util.ServerTestCase;

/**
 * @author Jon Stevens
 * @author De Oliveira Edouard &lt;doe_wanted@yahoo.fr&gt;
 */
public class HelpTest extends ServerTestCase
{
	public HelpTest(String name)
	{
		super(name);
	}

	public void testCommandHandling() throws Exception
	{
		expect("220");

		send("blah blah blah");
		expect("500");
		
		send("HELP");
		expect("214");
		
		send("HELP DATA");
		expect("214");
	}
}
