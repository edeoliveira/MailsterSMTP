package junit.command;

import junit.util.ServerTestCase;

/**
 * @author Jon Stevens
 * @author De Oliveira Edouard &lt;doe_wanted@yahoo.fr&gt;
 */
public class ReceiptTest extends ServerTestCase
{
	public ReceiptTest(String name)
	{
		super(name);
	}

	public void testReceiptBeforeMail() throws Exception
	{
		expect("220");

		send("HELO foo.com");
		expect("250");

		send("RCPT TO: bar@foo.com");
		expect("503 Error: need MAIL command");
	}

	public void testReceiptErrorInParams() throws Exception
	{
		expect("220");

		send("HELO foo.com");
		expect("250");

		send("MAIL FROM: success@example.org");
		expect("250 Ok");

		send("RCPT");
		expect("501");
	}

	public void testReceiptAccept() throws Exception
	{
		expect("220");

		send("HELO foo.com");
		expect("250");

		send("MAIL FROM: success@example.org");
		expect("250 Ok");

		send("RCPT TO: failure@example.org");
		expect("553 <failure@example.org> address unknown.");

		send("RCPT TO: success@example.org");
		expect("250 Ok");
	}

	public void testReceiptNoWhiteSpace() throws Exception
	{
		expect("220");

		send("HELO foo.com");
		expect("250");

		send("MAIL FROM: success@example.org");
		expect("250 Ok");

		send("RCPT TO:success@example.org");
		expect("250 Ok");
	}

	public void testReceiptEmptyAddress() throws Exception
	{
		expect("220");

		send("HELO foo.com");
		expect("250");

		send("MAIL FROM: success@example.org");
		expect("250 Ok");

		send("RCPT TO:");
		expect("553 <> Invalid email address");
	}
}
