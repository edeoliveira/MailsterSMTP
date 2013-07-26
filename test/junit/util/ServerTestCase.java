package junit.util;

import junit.framework.TestCase;

import org.mailster.smtp.api.handler.SessionContext;

import wiser.Wiser;

/**
 * A base class for testing the SMTP server at the raw protocol level.
 * Handles setting up and tearing down of the server.
 * 
 * @author Jon Stevens
 * @author Jeff Schnitzer
 */
public abstract class ServerTestCase extends TestCase
{
	/** */
	public static final int PORT = 2566;

	/**
	 * Override the accept method in Wiser so we can test
	 * the accept method().
	 */
	public class TestWiser extends Wiser
	{
		public boolean accept(SessionContext ctx, String from, String recipient)
		{
            return !recipient.equals("failure@example.org");
        }
	}
	
	/** */
	protected TestWiser wiser;
	
	/** */
	protected Client c;

	/** */
	public ServerTestCase(String name)
	{
		super(name);
	}

	/** */
	protected void setUp() throws Exception
	{
		super.setUp();
		
		this.wiser = new TestWiser();
		this.wiser.setHostname("localhost");
		this.wiser.setPort(PORT);
		this.wiser.start();
		
		this.c = new Client("localhost", PORT);
	}
	
	/** */
	protected void tearDown() throws Exception
	{
		this.wiser.shutdown();
		this.wiser = null;

		this.c.close();

		super.tearDown();
	}
	
	public void send(String msg) throws Exception
	{
		this.c.send(msg);
	}

	public void expect(String msg) throws Exception
	{
		this.c.expect(msg);
	}
}