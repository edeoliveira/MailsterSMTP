package junit;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.columba.ristretto.message.Address;
import org.columba.ristretto.smtp.SMTPException;
import org.columba.ristretto.smtp.SMTPProtocol;
import wiser.Wiser;

import javax.activation.DataHandler;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.util.ByteArrayDataSource;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.util.Properties;
import java.util.Random;

import static org.junit.Assert.assertArrayEquals;

/**
 * This class serves as a test case for both Wiser (since it is used
 * internally here) as well as harder to reach code within the SMTP
 * server that tests a roundtrip message through the DATA portion
 * of the SMTP spec.
 * 
 * @author Jon Stevens
 * @author Jeff Schnitzer
 * @author De Oliveira Edouard &lt;doe_wanted@yahoo.fr&gt;
 * @author Ville Skyttä (contributed some encoding tests)
 */
public class SMTPClientTest extends TestCase
{
	public static final int PORT = 2566;

	protected Wiser wiser;
	protected Session session;
	private Random rnd;
	
	public SMTPClientTest(String name) { super(name); }
	
	protected void setUp() throws Exception
	{
		super.setUp();
		
		Properties props = new Properties();
		props.setProperty("mail.smtp.host", "localhost");
		props.setProperty("mail.smtp.port", Integer.toString(PORT));
		this.session = Session.getInstance(props);
		
		this.wiser = new Wiser();
		this.wiser.setPort(PORT);
		
		this.wiser.start();
		rnd = new Random();
	}
	
	protected void tearDown() throws Exception
	{
		this.wiser.stop();
		this.wiser = null;

		this.session = null;
		
		super.tearDown();
	}
	
	public void testMultipleRecipients() throws Exception
	{
		MimeMessage message = new MimeMessage(this.session);
		message.addRecipient(Message.RecipientType.TO, new InternetAddress("anyone@anywhere.com"));
		message.addRecipient(Message.RecipientType.TO, new InternetAddress("anyone2@anywhere.com"));
		message.setFrom(new InternetAddress("someone@somewhereelse.com"));
		message.setSubject("barf");
		message.setText("body");

		Transport.send(message);

		assertEquals(2, this.wiser.getMessages().size());
	}

	public void testLargeMessage() throws Exception
	{
		MimeMessage message = new MimeMessage(this.session);
		message.addRecipient(Message.RecipientType.TO, new InternetAddress("anyone@anywhere.com"));
		message.addRecipient(Message.RecipientType.TO, new InternetAddress("anyone2@anywhere.com"));
		message.setFrom(new InternetAddress("someone@somewhereelse.com"));
		message.setSubject("barf");
		message.setText("bodyalksdjflkasldfkasjldfkjalskdfjlaskjdflaksdjflkjasdlfkjl");

		Transport.send(message);
		
		assertEquals(2, this.wiser.getMessages().size());
		
		assertEquals("barf", this.wiser.getMessages().get(0).getMimeMessage().getSubject());
		assertEquals("barf", this.wiser.getMessages().get(1).getMimeMessage().getSubject());
	}

	public void testUtf8EightBitMessage() throws Exception
	{
		// Beware editor/compiler character encoding issues; safest to put unicode escapes here
		
		String body = "\u00a4uro ma\u00f1ana";
		testEightBitMessage(body, "UTF-8");

		assertEquals(body, this.wiser.getMessages().get(0).getMimeMessage().getContent());
	}

	public void testUtf16EightBitMessage() throws Exception
	{
		String body = "\u3042\u3044\u3046\u3048\u304a";
		testEightBitMessage(body, "UTF-16");

		assertEquals(body, this.wiser.getMessages().get(0).getMimeMessage().getContent());
	}
	
	public void testIso88591EightBitMessage() throws Exception
	{
		// Beware editor/compiler character encoding issues; safest to put unicode escapes here

		String body = "ma\u00f1ana";	// spanish ene (ie, n with diacritical tilde)
		testEightBitMessage(body, "ISO-8859-1");

		assertEquals(body, this.wiser.getMessages().get(0).getMimeMessage().getContent());
	}

	public void testIso885915EightBitMessage() throws Exception
	{
		// Beware editor/compiler character encoding issues; safest to put unicode escapes here

		String body = "\0xa4uro";	// should be the euro symbol
		testEightBitMessage(body, "ISO-8859-15");

		assertEquals(body, this.wiser.getMessages().get(0).getMimeMessage().getContent());
	}

	private void testEightBitMessage(String body, String charset) throws Exception
	{
		MimeMessage message = new MimeMessage(this.session);
		message.addRecipient(Message.RecipientType.TO, new InternetAddress("anyone@anywhere.com"));
		message.setFrom(new InternetAddress("someone@somewhereelse.com"));
		message.setSubject("hello");
		message.setText(body, charset);
		message.setHeader("Content-Transfer-Encoding", "8bit");

		Transport.send(message);
	}

	private void testCRLFEncodingMessage(String body, String charset) throws Exception
	{
		Address from = new Address("someone@somewhereelse.com");
		Address to = new Address("anyone@anywhere.com");
        
		//Construct the protocol that is bound to the SMTP server
        SMTPProtocol protocol = new SMTPProtocol("localhost", PORT);
        
        try {
            // Open the port
            protocol.openPort();
            protocol.helo(InetAddress.getLocalHost());
                        
            // Setup from and recipient
            protocol.mail(from);
            protocol.rcpt(to);
            
            // Finally send the data
            protocol.data(new ByteArrayInputStream(
            		("Subject: hello\n\n"+body).getBytes(charset)));
            
            // And close the session
            protocol.quit();
            
        } catch (IOException e1) {
            System.err.println(e1.getLocalizedMessage());
        } catch (SMTPException e1) {
            System.err.println(e1.getMessage());
        }
	}
	
	public void testIso2022JPEightBitMessage() throws Exception 
  	{
		String body = "\u3042\u3044\u3046\u3048\u304a"; // some Japanese letters
		testEightBitMessage(body, "iso-2022-jp");
		
		assertEquals(body, this.wiser.getMessages().get(0).getMimeMessage().getContent());
	}

	public void testPreservingCRLF() throws Exception 
  	{	
		String body = "\n\nKeep these pesky carriage returns\n\n";
		testCRLFEncodingMessage(body, "ISO-8859-1");
	
		Thread.sleep(500);
		String received = this.wiser.getMessages().
			get(0).getMimeMessage().getContent().toString();
		assertEquals(body, received);
	}

	public void testPreservingCRLFHeavily() throws Exception 
  	{	
		String body = "\r\n\r\nKeep these\r\npesky\r\n\r\ncarriage returns\r\n";
		testCRLFEncodingMessage(body, "ISO-8859-1");
	
		Thread.sleep(500);
		String received = this.wiser.getMessages().
			get(0).getMimeMessage().getContent().toString();
		assertEquals(body, received);
	}
	
	/** */
	public void testBinaryEightBitMessage() throws Exception
	{
		byte[] body = new byte[64];
		rnd.nextBytes(body);
		
		MimeMessage message = new MimeMessage(this.session);
		message.addRecipient(Message.RecipientType.TO, new InternetAddress("anyone@anywhere.com"));
		message.setFrom(new InternetAddress("someone@somewhereelse.com"));
		message.setSubject("hello");
		message.setHeader("Content-Transfer-Encoding", "8bit");
		message.setDataHandler(new DataHandler(new ByteArrayDataSource(body, "application/octet-stream")));

		Transport.send(message);

		InputStream in = this.wiser.getMessages().get(0).getMimeMessage().getInputStream();
		ByteArrayOutputStream tmp = new ByteArrayOutputStream();
		byte[] buf = new byte[64];
		int n;
		while ((n = in.read(buf)) != -1)
		{
			tmp.write(buf, 0, n);
		}
		in.close();

		assertArrayEquals(body, tmp.toByteArray());
	}
	
	/** */
	public static Test suite()
	{
		return new TestSuite(SMTPClientTest.class);
	}
}
