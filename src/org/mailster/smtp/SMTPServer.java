package org.mailster.smtp;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.filterchain.DefaultIoFilterChainBuilder;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.executor.ExecutorFilter;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.transport.socket.SocketAcceptor;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;
import org.mailster.smtp.api.handler.AbstractDeliveryHandler;
import org.mailster.smtp.api.listener.MessageListener;
import org.mailster.smtp.auth.AuthenticationHandlerFactory;
import org.mailster.smtp.command.CommandHandler;
import org.mailster.smtp.core.DeliveryHandlerFactory;
import org.mailster.smtp.core.mina.SMTPCodecDecoder;
import org.mailster.smtp.core.mina.SMTPCodecFactory;
import org.mailster.smtp.core.mina.SMTPConnectionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main SMTPServer class.  Construct this object, set the
 * hostName, port, and bind address if you wish to override the 
 * defaults, and call start(). 
 * 
 * This class starts opens a <a href="http://mina.apache.org/">Mina</a> 
 * based listener and creates a new
 * instance of the SMTPConnectionHandler class when a new connection
 * comes in.  The SMTPConnectionHandler then parses the incoming SMTP
 * stream and hands off the processing to the CommandHandler which
 * will execute the appropriate SMTP command class.
 *  
 * Using this server is easy just use the constructor passing in a list
 * of {@link MessageListener}. This is a higher, and sometimes more 
 * convenient level of abstraction. You can further manipulate the list
 * of listeners using the methods provided by the {@link DeliveryHandlerFactory}.
 * 
 * You can also customize the way that messages are delivered to the 
 * listeners by writing your own delivery handler implementation by extending 
 * {@link AbstractDeliveryHandler} and registering it with the server using 
 * the following code line :
 * 
 * server.getDeliveryHandlerFactory().
 *  setDeliveryHandlerImplClass(Class<? extends AbstractDeliveryHandler> impl);
 * 
 * You can always add, remove listeners by using the provided
 * methods of the {@link DeliveryHandlerFactory}.
 * 
 * In neither case is the SMTP server (this library) responsible
 * for deciding what recipients to accept or what to do with the
 * incoming data.  That is left to you.
 * 
 * @author De Oliveira Edouard &lt;doe_wanted@yahoo.fr&gt;
 * 
 * NB: some code comes from a fork from the SubethaSMTP project. 
 */
public class SMTPServer
{
	private static Logger log = LoggerFactory.getLogger(SMTPServer.class);

	/**
	 * IoService JMX name.
	 */
	//private static final String IO_SERVICE_MBEAN_NAME = "subethasmtp.mina.server:type=IoServiceMBean";

	/**
	 * default to all interfaces
	 */
	private InetAddress bindAddress = null;

	/**
	 * default to 25
	 */
	private int port = 25;
	
	private DeliveryHandlerFactory deliveryHandlerFactory;
	private CommandHandler commandHandler;
	private SMTPConnectionHandler handler;
	
	private SocketAcceptor acceptor;
	private ExecutorService executor;
	private SMTPCodecFactory codecFactory;
	
	private boolean running = false;
	private boolean shutdowned = false;

	/**
	 * The server configuration.
	 */
	private SMTPServerConfig config = new SMTPServerConfig();

	public SMTPServer()
	{
		List<MessageListener> listeners = Collections.emptyList();
		initInstance(listeners);
	}
	
	public SMTPServer(MessageListener listener)
	{
		List<MessageListener> listeners = new ArrayList<MessageListener>(1);
		listeners.add(listener);
		initInstance(listeners);
	}
	
	public SMTPServer(Collection<MessageListener> listeners)
	{
		initInstance(listeners);
	}
	
	private void initInstance(Collection<MessageListener> listeners)
	{
		this.deliveryHandlerFactory = new DeliveryHandlerFactory(listeners);
		this.commandHandler = new CommandHandler();
		initService();		
	}
	
	/**
	 * Start the JMX service.
	 * @throws NullPointerException 
	 * @throws MalformedObjectNameException 
	 * @throws NotCompliantMBeanException 
	 * @throws MBeanRegistrationException 
	 * @throws InstanceAlreadyExistsException 
	 */
	/*public void startJMXService(IoService svc) 
		throws InstanceAlreadyExistsException, MBeanRegistrationException, 
				NotCompliantMBeanException, MalformedObjectNameException, NullPointerException
	{
		MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
		mbs.registerMBean(new IoServiceMBean(svc), new ObjectName(IO_SERVICE_MBEAN_NAME));
	}*/

	/**
	 * Stop the JMX service.
	 * @throws NullPointerException 
	 * @throws MalformedObjectNameException 
	 * @throws MBeanRegistrationException 
	 * @throws InstanceNotFoundException 
	 * 
	 * @throws InstanceNotFoundException
	 * @throws MBeanRegistrationException
	 */
	/*public void stopJMXService() 
		throws InstanceNotFoundException, MBeanRegistrationException, 
				MalformedObjectNameException, NullPointerException
	{
		MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
		mbs.unregisterMBean(new ObjectName(IO_SERVICE_MBEAN_NAME));
	}*/

	/**
	 * Initializes the runtime service.
	 */
	private void initService()
	{
		try
		{
			IoBuffer.setUseDirectBuffer(false);
			acceptor = new NioSocketAcceptor(Runtime.getRuntime().availableProcessors() + 1);
			
			acceptor.getSessionConfig().setReuseAddress(true);
			DefaultIoFilterChainBuilder chain = acceptor.getFilterChain();

			if (log.isTraceEnabled())
				chain.addLast("logger", new LoggingFilter());

			codecFactory = new SMTPCodecFactory(config);
			chain.addLast("codec", new ProtocolCodecFilter(codecFactory));
			
			executor = Executors.newCachedThreadPool(new ThreadFactory() {
				int sequence;
				
				public Thread newThread(Runnable r) 
				{					
					sequence += 1;
					return new Thread(r, "SubEthaSMTP Thread "+sequence);
				}			
			});
			
			chain.addLast("threadPool", new ExecutorFilter(executor));
			
			handler = new SMTPConnectionHandler(getConfig(), getCommandHandler(),
					getDeliveryHandlerFactory());
		}
		catch (Exception ex)
		{
			throw new RuntimeException(ex);
		}
	}

	/**
	 * Call this method to get things rolling after instantiating the
	 * SMTPServer.
	 */
	public synchronized void start()
	{
		if (running)
		{
			log.info("SMTP server is already started.");
			return;
		}
		
		if (shutdowned)
			throw new RuntimeException("Error: server has been shutdown previously");
		
		// Read smtp decoder configuration options
		((SMTPCodecDecoder) codecFactory.getDecoder(null)).
			setup(getConfig().getCharset(), getConfig().getDataDeferredSize());
		
		InetSocketAddress isa;

		if (this.bindAddress == null)
		{
			isa = new InetSocketAddress(this.port);
		}
		else
		{
			isa = new InetSocketAddress(this.bindAddress, this.port);
		}

		acceptor.setBacklog(config.getBacklog());
		acceptor.setHandler(handler);
		
		try
		{
			acceptor.bind(isa);
			running = true;
			log.info("SMTP server started ...");
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}

	/**
	 * Stops the server by unbinding server socket. To really clean
	 * things out, one must call {@link #shutdown()}.
	 */
	public synchronized void stop()
	{
		try
		{
			try { 
				acceptor.unbind(); 
			} catch (Exception e) { e.printStackTrace(); }
			
			log.info("SMTP server stopped.");
		}
		finally
		{
			running = false;
		}
	}
	
	/**
	 * Shut things down gracefully. Please pay attention to the fact 
	 * that a shutdown implies that the server would fail to restart 
	 * because som internal resources have been freed.
	 * 
	 * You can directly call shutdown() if you do not intend to restart 
	 * it later. Calling start() after shutdown() will throw a 
	 * {@link RuntimeException}.
	 */
	public synchronized void shutdown()
	{
		try
		{
			log.info("SMTP server shutting down...");
			if (isRunning())
				stop();			
			
			try { 
				executor.shutdown(); 
			} catch (Exception e) { e.printStackTrace(); }
			
			shutdowned = true;
			log.info("SMTP server shutdown complete.");
		}
		finally
		{
			running = false;
		}
	}

	/**
	 * Is the server running after start() has been called?
	 */
	public synchronized boolean isRunning()
	{
		return this.running;
	}
	
	/** 
	 * Returns the bind address. Null means all interfaces. 
	 */
	public InetAddress getBindAddress()
	{
		return this.bindAddress;
	}

	/**
	 * Sets the bind address. Null means all interfaces.
	 */
	public void setBindAddress(InetAddress bindAddress)
	{
		this.bindAddress = bindAddress;
	}

	/**
	 * Returns the port the server is running on.
	 */
	public int getPort()
	{
		return this.port;
	}

	/**
	 * Sets the port the server will run on.
	 */
	public void setPort(int port)
	{
		this.port = port;
	}

	public void setAuthenticationHandlerFactory(AuthenticationHandlerFactory factory)
	{
		this.deliveryHandlerFactory.setAuthenticationHandlerFactory(factory);
	}
	
	/**
	 * All smtp data is routed through the handler.
	 */
	public DeliveryHandlerFactory getDeliveryHandlerFactory()
	{
		return this.deliveryHandlerFactory;
	}

	/**
	 * The CommandHandler manages handling the SMTP commands
	 * such as QUIT, MAIL, RCPT, DATA, etc.
	 * 
	 * @return An instance of CommandHandler
	 */
	public CommandHandler getCommandHandler()
	{
		return this.commandHandler;
	}
	
	/**
	 * Returns the server configuration.
	 */
	public SMTPServerConfig getConfig() 
	{
		return config;
	}
}