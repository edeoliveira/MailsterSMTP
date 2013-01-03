package junit.command;

import junit.util.ServerTestCase;

/**
 * @author Jon Stevens
 * @author De Oliveira Edouard &lt;doe_wanted@yahoo.fr&gt;
 */
public class QuitTest extends ServerTestCase
{
	public QuitTest(String name)
	{
		super(name);
	}

	public void testQuit() throws Exception
	{
		expect("220");

		send("HELO foo.com");
		expect("250");

		send("MAIL FROM: test@example.com");
		expect("250 Ok");

		send("QUIT");
		expect("221 Bye");
	}
}
