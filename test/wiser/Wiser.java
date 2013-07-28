package wiser;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.mailster.smtp.SMTPServer;
import org.mailster.smtp.api.MessageListener;
import org.mailster.smtp.api.handler.SessionContext;
import org.mailster.smtp.core.TooMuchDataException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Wiser is a smart mail testing application.
 *
 * @author Jon Stevens
 * @author De Oliveira Edouard &lt;doe_wanted@yahoo.fr&gt;
 */
public class Wiser implements MessageListener
{
	/** */
	private static final Logger LOG = LoggerFactory.getLogger(Wiser.class);

	/** */
	SMTPServer server;

	/** */
	List<WiserMessage> messages = Collections.synchronizedList(new ArrayList<WiserMessage>());

	/**
	 * Create a new SMTP server with this class as the listener.
	 * The default port is set to 25. Call setPort()/setHostname() before
	 * calling start().
	 */
	public Wiser()
	{
		Collection<MessageListener> listeners = new ArrayList<MessageListener>(1);
		listeners.add(this);

		this.server = new SMTPServer(listeners);
		this.server.setPort(25);

		// Set max connections much higher since we use NIO now.
        this.server.getConfig().setMaxConnections(30000);
	}

	/**
	 * The port that the server should listen on.
	 * @param port
	 */
	public void setPort(int port)
	{
		this.server.setPort(port);
	}

	/**
	 * Set the size at which the mail will be temporary
	 * stored on disk.
	 * @param dataDeferredSize
	 */
	public void setDataDeferredSize(int dataDeferredSize)
	{
		this.server.getConfig().setDataDeferredSize(dataDeferredSize);
	}

	/**
	 * Set the receive buffer size.
	 * @param size
	 */
	public void setReceiveBufferSize(int size)
	{
		this.server.getConfig().setReceiveBufferSize(size);
	}

	/**
	 * The hostname that the server should listen on.
	 * @param hostname
	 */
	public void setHostname(String hostname)
	{
		this.server.getConfig().setHostName(hostname);
	}

	/**
	 * Starts the SMTP Server
	 */
	public void start()
	{
		this.server.start();
	}

	/**
	 * Stops the SMTP Server
	 */
	public void stop()
	{
		this.server.stop();
	}

	/**
	 * Shutdowns the SMTP Server
	 */
	public void shutdown()
	{
		this.server.shutdown();
	}
	
	/**
	 * A main() for this class. Starts up the server.
	 */
	public static void main(String[] args) throws Exception
	{
		Wiser wiser = new Wiser();
		wiser.start();
	}

	/**
	 * Always accept everything
	 */
	public boolean accept(SessionContext ctx, String from, String recipient)
	{
		return true;
	}

	/**
	 * Cache the messages in memory. Now avoids unnecessary memory copying.
	 */
	public void deliver(SessionContext ctx, String from, 
						String recipient, InputStream data) 
		throws TooMuchDataException, IOException
	{
		LOG.debug("Delivering new message ...");
		WiserMessage msg = new WiserMessage(from, recipient, data);
		this.queueMessage(msg);
	}

	/**
	 * deliver() calls queueMessage to store the message in an internal 
	 * List&lt;WiserMessage&gt. You can extend Wiser and override this method if 
	 * you want to store it in a different location instead.
	 *
	 * @param message
	 */
	protected void queueMessage(WiserMessage message)
	{
		this.messages.add(message);
	}

	/**
	 * @return the list of WiserMessages
	 */
	public List<WiserMessage> getMessages()
	{
		return this.messages;
	}

	/**
	 * @return an instance of the SMTPServer object
	 */
	public SMTPServer getServer()
	{
		return this.server;
	}
}