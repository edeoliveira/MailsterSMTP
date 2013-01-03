package junit.command;

import junit.util.ServerTestCase;

/**
 * @author Jon Stevens
 * @author De Oliveira Edouard &lt;doe_wanted@yahoo.fr&gt;
 */
public class StartTLSTest extends ServerTestCase
{
	public StartTLSTest(String name)
	{
		super(name);
	}

	public void testQuit() throws Exception
	{
		expect("220");

		send("HELO foo.com");
		expect("250");

		send("STARTTLS foo");
		expect("501 Syntax error (no parameters allowed)");

		send("QUIT");
		expect("221 Bye");
	}
}
