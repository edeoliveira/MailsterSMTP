package junit.util;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.concurrent.Executors;


import org.apache.mina.core.RuntimeIoException;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.buffer.SimpleBufferAllocator;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.textline.TextLineCodecFactory;
import org.apache.mina.filter.executor.ExecutorFilter;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.transport.socket.SocketConnector;
import org.apache.mina.transport.socket.nio.NioSocketConnector;

/**
 * NIO Test client
 * 
 * @author De Oliveira Edouard &lt;doe_wanted@yahoo.fr&gt;
 */
public class SMTPClient
{
	private static final String HOSTNAME = "127.0.0.1";
	private static final int PORT = 25;
	private static final int CONNECT_TIMEOUT = 30000; // 30 secs in ms
	private static final int MAX_THREADS = 3000;

	public static void main(String[] args) throws Throwable
	{
		IoBuffer.setUseDirectBuffer(false);
		IoBuffer.setAllocator(new SimpleBufferAllocator());

		SocketConnector connector =
			new NioSocketConnector(Runtime.getRuntime().availableProcessors() + 1);

		// Configure the service.
		connector.setConnectTimeoutMillis(CONNECT_TIMEOUT);

		connector.getFilterChain().addLast("threadPool", new ExecutorFilter(Executors.newFixedThreadPool(MAX_THREADS)));
		connector.getFilterChain().addLast("codec",
											new ProtocolCodecFilter(new TextLineCodecFactory(Charset.forName("UTF-8"))));
		connector.getFilterChain().addLast("logger", new LoggingFilter());

		SMTPSessionHandler handler = new SMTPSessionHandler("localhost");
		connector.setHandler(handler);
		while (true)
		{
			try
			{
				for (int i = 0; i < 10; i++)
					connector.connect(new InetSocketAddress(HOSTNAME, PORT));
				Thread.sleep(100);
			}
			catch (RuntimeIoException e)
			{
				System.err.println("Failed to connect.");
				e.printStackTrace();
				Thread.sleep(1000);
			}
		}
	}
}
