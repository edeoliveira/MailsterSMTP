package junit.command;

import junit.util.ServerTestCase;

/**
 * @author Jon Stevens
 * @author De Oliveira Edouard &lt;doe_wanted@yahoo.fr&gt;
 */
public class DataTest extends ServerTestCase
{
	public DataTest(String name)
	{
		super(name);
	}

	public void testNeedMail() throws Exception
	{
		expect("220");

		send("HELO foo.com");
		expect("250");

		send("DATA");
		expect("503 Error: need MAIL command");
	}

	public void testNeedRcpt() throws Exception
	{
		expect("220");

		send("HELO foo.com");
		expect("250");

		send("MAIL FROM: success@example.org");
		expect("250");

		send("DATA");
		expect("503 Error: need RCPT command");
	}

	public void testData() throws Exception
	{
		expect("220");

		send("HELO foo.com");
		expect("250");

		send("MAIL FROM: success@example.org");
		expect("250");

		send("RCPT TO: success@example.org");
		expect("250");

		send("DATA");
		expect("354 End data with <CR><LF>.<CR><LF>");
	}

	public void testRsetAfterData() throws Exception
	{
		expect("220");

		send("HELO foo.com");
		expect("250");

		send("MAIL FROM: success@example.org");
		expect("250");

		send("RCPT TO: success@example.org");
		expect("250");

		send("DATA");
		expect("354 End data with <CR><LF>.<CR><LF>");

		send("alsdkfj \r\n.");
		expect("250");

		send("RSET");
		expect("250 Ok");
		
		send("HELO foo.com");
		expect("250");
		
		send("MAIL FROM: ed@foo.com");
		expect("250");
	}
}
