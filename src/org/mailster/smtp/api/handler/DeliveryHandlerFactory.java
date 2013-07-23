package org.mailster.smtp.api.handler;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;

import org.mailster.smtp.DefaultDeliveryHandler;
import org.mailster.smtp.api.MessageListener;
import org.mailster.smtp.core.auth.AuthenticationHandler;
import org.mailster.smtp.core.auth.AuthenticationHandlerFactory;
import org.mailster.smtp.core.auth.impl.DummyAuthenticationHandler;

/**
 * This factory creates a delivery handler for each new SMTP session and 
 * uses the configured {@link AuthenticationHandlerFactory} to create an
 * {@link AuthenticationHandler} used for all sessions until replaced
 * by another factory using the following method  
 * {@link #setAuthenticationHandlerFactory(AuthenticationHandlerFactory)}. 
 *
 * @author De Oliveira Edouard &lt;doe_wanted@yahoo.fr&gt; 
 */
public class DeliveryHandlerFactory
{	
	private Collection<MessageListener> listeners;
	private AuthenticationHandlerFactory authenticationHandlerFactory;
	private AuthenticationHandler authHandler;
	private Class<? extends AbstractDeliveryHandler> deliveryHandlerImplClass = 
		DefaultDeliveryHandler.class;
	
	/**
	 * Initializes this factory with the listeners.
	 */
	public DeliveryHandlerFactory(Collection<MessageListener> listeners)
	{
		this.listeners = listeners == null ? 
			new ArrayList<MessageListener>() : listeners;
	}
	
	public synchronized void addAllListeners(Collection<MessageListener> listeners)
	{
		this.listeners.addAll(listeners);
	}
	
	public synchronized void addListener(MessageListener listener)
	{
		this.listeners.add(listener);
	}
	
	public synchronized void removeListener(MessageListener listener)
	{
		this.listeners.remove(listener);
	}
	
	private synchronized ArrayList<MessageListener> copyListeners() 
	{
	    return new ArrayList<MessageListener>(listeners);
	}
	
	public AbstractDeliveryHandler create(DeliveryContext ctx)
	{
		return create(ctx, deliveryHandlerImplClass);
	}
	
	/**
	 * Sets the {@link AbstractDeliveryHandler} implementation to use.
	 */
	public void setDeliveryHandlerImplClass(Class<? extends AbstractDeliveryHandler> c) 
	{
		this.deliveryHandlerImplClass = c;
	}
	
	private AbstractDeliveryHandler create(DeliveryContext ctx, 
			Class<? extends AbstractDeliveryHandler> c)
	{
		try 
		{
			Constructor<? extends AbstractDeliveryHandler> cstr = 
				c.getConstructor(DeliveryContext.class, AuthenticationHandler.class);
			AbstractDeliveryHandler handler = cstr.newInstance(ctx, getAuthenticationHandler());
			handler.setListeners(copyListeners());
			
			return handler;
		} 
		catch (Exception e) 
		{
			throw new IllegalArgumentException(
				"Failed instantiating DeliveryHandler - "+c.getName(), e);
		}	
	}
	
	/**
	 * Returns the auth handler factory
	 */
	public synchronized AuthenticationHandlerFactory getAuthenticationHandlerFactory()
	{
		return authenticationHandlerFactory;
	}
	
	/**
	 * Sets the auth handler factory.
	 */
	public synchronized void setAuthenticationHandlerFactory(AuthenticationHandlerFactory authenticationHandlerFactory)
	{
		this.authHandler = null;
		this.authenticationHandlerFactory = authenticationHandlerFactory;
	}
	
	/**
	 * Holds the AuthenticationHandler instantiation logic.
	 * Either try to use a user defined AuthHandlerFactory
	 * or default to the internal class DummyAuthenticationHandler
	 * which always returns true.
	 *
	 * @return a new AuthenticationHandler
	 */
	public synchronized AuthenticationHandler getAuthenticationHandler()
	{
		if (this.authHandler != null)
		{
			return this.authHandler;
		}

		if (getAuthenticationHandlerFactory() != null)
		{
			// The user has plugged in a factory. let's use it.
			this.authHandler = getAuthenticationHandlerFactory().create();
			if (this.authHandler == null)
				throw new NullPointerException("AuthenticationHandlerFactory returned a null handler");
		}
		else
		{
			// A placeholder.
			this.authHandler = new DummyAuthenticationHandler();
		}
		
		// Return the variable, which can be null
		return this.authHandler;
	}
}