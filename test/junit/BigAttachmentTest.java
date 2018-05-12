package junit;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.Address;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.URLName;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import junit.framework.TestCase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import wiser.Wiser;
import wiser.WiserMessage;

import com.sun.mail.smtp.SMTPTransport;

/**
 * This class tests the transfer speed of emails that carry 
 * attached files.
 * 
 * @author De Oliveira Edouard &lt;doe_wanted@yahoo.fr&gt;
 */
public class BigAttachmentTest extends TestCase
{
	private static final Logger LOG = LoggerFactory.getLogger(BigAttachmentTest.class);
	private static final int SMTP_PORT = 1085;
	private static final int BUFFER_SIZE = 32768;
	private static final String BIGFILE_PATH = System.getProperty("java.home").replace("\\","/")+"/lib/rt.jar";
	
	private Wiser server;
	
	public BigAttachmentTest(String name) 
	{
		super(name);
	}

	protected void setUp() throws Exception
	{
		super.setUp();
		server = new Wiser();
		server.setPort(SMTP_PORT);
		server.setReceiveBufferSize(BUFFER_SIZE);
		server.start();		
	}

	protected void tearDown() throws Exception
	{
		super.tearDown();
		try 
		{ 
			server.stop(); 
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void testAttachments() throws Exception
	{	
		Properties props = System.getProperties();
		props.setProperty("mail.smtp.host", "localhost");
		props.setProperty("mail.smtp.port", SMTP_PORT+"");
		Session session = Session.getInstance(props);

		MimeMessage baseMsg = new MimeMessage(session);
		MimeBodyPart bp1 = new MimeBodyPart();
		bp1.setHeader("Content-Type", "text/plain");
		bp1.setContent("Hello World!!!", "text/plain; charset=\"ISO-8859-1\"");
		
		// Attach the file
		MimeBodyPart bp2 = new MimeBodyPart();

		// Can't test if file not found
		if (!(new File(BIGFILE_PATH)).exists()) {
			LOG.error("Couldn't find the test big file :"+BIGFILE_PATH);
			return;
		}

		FileDataSource fileAttachment = new FileDataSource(BIGFILE_PATH);
		DataHandler dh = new DataHandler(fileAttachment);
		bp2.setDataHandler(dh);
		bp2.setFileName(fileAttachment.getName());

		Multipart multipart = new MimeMultipart();
		multipart.addBodyPart(bp1);
		multipart.addBodyPart(bp2);

		baseMsg.setFrom(new InternetAddress("Ted <ted@home.com>"));
		baseMsg.setRecipient(Message.RecipientType.TO, new InternetAddress(
				"success@example.org"));
		baseMsg.setSubject("Test Big attached file message");
		baseMsg.setContent(multipart);
        baseMsg.saveChanges();
        
        LOG.debug("Send started");        
        Transport t = new SMTPTransport(session, new URLName("smtp://localhost:"+SMTP_PORT));
		long started = System.currentTimeMillis();
        t.connect();
        t.sendMessage(baseMsg, new Address[] {new InternetAddress(
				"success@example.org")});
        t.close();
        started = System.currentTimeMillis() - started;
        LOG.info("Elapsed ms = "+started);
        
        WiserMessage msg = server.getMessages().get(0);
        
        assertEquals(1, server.getMessages().size());		
		assertEquals("success@example.org", msg.getEnvelopeReceiver());
		
		File compareFile = File.createTempFile("attached", ".tmp");
		LOG.debug("Writing received attachment ...");

		FileOutputStream fos = new FileOutputStream(compareFile);
		((MimeMultipart) msg.getMimeMessage().getContent()).getBodyPart(1).getDataHandler().writeTo(fos);
		fos.close();
		try 
		{
			LOG.debug("Checking integrity ...");
			assertTrue(checkIntegrity(new File(BIGFILE_PATH), compareFile));
			LOG.debug("Checking integrity DONE");
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			compareFile.deleteOnExit();
			msg.dispose();
		}
	}
	
	private boolean checkIntegrity(File src, File dest) throws IOException, NoSuchAlgorithmException
	{
		BufferedInputStream ins = new BufferedInputStream(new FileInputStream(src));
		BufferedInputStream ind = new BufferedInputStream(new FileInputStream(dest));
		MessageDigest md1 = MessageDigest.getInstance("MD5");
		MessageDigest md2 = MessageDigest.getInstance("MD5");
		
		int r = 0;
		byte[] buf1 = new byte[BUFFER_SIZE];
		byte[] buf2 = new byte[BUFFER_SIZE];
		
		while (r !=-1)
		{
			r = ins.read(buf1);
			ind.read(buf2);
			
			md1.update(buf1);
			md2.update(buf2);
		}
		
		ins.close();
		ind.close();
		return MessageDigest.isEqual(md1.digest(), md2.digest());
	}
}