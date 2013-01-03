package junit;

import junit.framework.TestCase;

import org.mailster.smtp.SMTPServer;

/**
 * This class attempts to quickly start/stop the server 10 times. 
 * It makes sure that the socket bind address is correctly
 * shut down.
 * 
 * @author De Oliveira Edouard &lt;doe_wanted@yahoo.fr&gt;
 */
public class StartStopTest extends TestCase 
{
    /** */
    public static final int PORT = 6666;

    /** */
    protected int counter = 0;

    /** */
    public StartStopTest(String name) 
    {
        super(name);
    }

    public void testMultipleStartStop() 
    {
        for (int i = 0; i < 10; i++) 
        {
        	SMTPServer server = new SMTPServer();
            server.setPort(PORT);

            server.start();
            server.stop();

            counter++;
        }
        assertEquals(counter, 10);
    }

    public void testMultipleStartStopWithSameInstance() 
    {
    	SMTPServer server = new SMTPServer();
        for (int i = 0; i < 10; i++) 
        {
            server.start();
            server.stop();
        }
    }

    public void testShutdown() 
    {
        boolean failed = false;
        SMTPServer server = new SMTPServer();
        server.start();
        server.stop();
        server.shutdown();

        try 
        {
            server.start();
        } 
        catch (RuntimeException ex) {
            failed = true;
            ex.printStackTrace();
        }

        assertTrue(failed);
    }
}