package wiser;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class wraps a received message and provides
 * a way to generate a JavaMail MimeMessage from the data.
 *
 * @author Jon Stevens
 * @author De Oliveira Edouard &lt;doe_wanted@yahoo.fr&gt;
 */
public class WiserMessage
{
	private final static Logger LOG = LoggerFactory.getLogger(WiserMessage.class);

	private String envelopeSender;
	private String envelopeReceiver;
	private InputStream stream;
	private byte[] array;
	private MimeMessage message = null;

	public WiserMessage(String envelopeSender, String envelopeReceiver, InputStream stream)
	{
		this.envelopeSender = envelopeSender;
		this.envelopeReceiver = envelopeReceiver;
		this.stream = stream;
	}

	/**
	 * Generate a JavaMail MimeMessage.
	 * @throws MessagingException
	 */
	public synchronized MimeMessage getMimeMessage() throws MessagingException
	{
		if (this.message == null)
		{
			 this.message = new MimeMessage(
					 Session.getDefaultInstance(new Properties()), 
					 this.stream);
		}
		return this.message;
	}

	/**
	 * Get's the raw message DATA.
	 * Note : this could result in loading many data into memory in case of big
	 * attached files. This is why the array is only generated on the first call.
	 *
	 * @return the byte array of the raw message or an empty byte array
	 * if an exception occured.
	 * 
	 * @deprecated Should be moved to a util class this is a bad design idea and may
	 * cause {@link OutOfMemoryError} exceptions
	 */
	public synchronized byte[] getData()
	{
		if (this.array == null)
		{
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			BufferedInputStream in;

			if (this.stream instanceof BufferedInputStream)
				in = (BufferedInputStream) this.stream;
			else
				in = new BufferedInputStream(this.stream);

			// read the data from the stream
			try
			{
				int b;
				byte[] buf = new byte[8192];
				while ((b = in.read(buf)) >= 0)
				{
					out.write(buf, 0, b);
				}

				this.array = out.toByteArray();
			}
			catch (IOException ioex)
			{
				this.array = new byte[0];
			}
			finally
			{
				try
				{
					in.close();
				}
				catch (IOException e) {}
			}
		}

		return this.array;
	}

	/**
	 * Get's the RCPT TO:
	 */
	public String getEnvelopeReceiver()
	{
		return this.envelopeReceiver;
	}

	/**
	 * Get's the MAIL FROM:
	 */
	public String getEnvelopeSender()
	{
		return this.envelopeSender;
	}

	public void dispose()
	{
		try
		{
		    if (this.stream != null)
	            {
	                this.stream.close();
	                this.stream = null;
	            }			
		}
		catch (Throwable t)
		{
		    LOG.error("On WiserMessage dispose", t);
		}
	}

	@Override
	protected void finalize() throws Throwable
	{
		super.finalize();
		dispose();
	}
}
