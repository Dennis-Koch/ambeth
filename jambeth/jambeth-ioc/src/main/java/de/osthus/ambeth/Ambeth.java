package de.osthus.ambeth;

import java.io.IOException;

import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.config.IProperties;
import de.osthus.ambeth.config.Properties;
import de.osthus.ambeth.ioc.IServiceContext;
import de.osthus.ambeth.ioc.factory.BeanContextFactory;

public class Ambeth implements IAmbethConfiguration, IAmbethApplication
{
	public static IAmbethConfiguration create()
	{
		Ambeth ambeth = new Ambeth();
		return ambeth;
	}

	protected Properties properties = new Properties();

	protected ArrayList<Class<?>> ambethModules = new ArrayList<Class<?>>();

	protected ArrayList<Class<?>> applicationModules = new ArrayList<Class<?>>();

	private IServiceContext bootstrapContext;

	private IServiceContext serviceContext;

	private Ambeth()
	{
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IAmbethConfiguration withProperties(IProperties properties)
	{
		this.properties.load(properties);

		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IAmbethConfiguration withProperties(java.util.Properties properties)
	{
		this.properties.load(properties);

		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IAmbethConfiguration withProperty(String name, String value)
	{
		properties.putString(name, value);

		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IAmbethConfiguration withArgs(String... args)
	{
		properties.fillWithCommandLineArgs(args);

		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IAmbethConfiguration withAmbethModules(Class<?>... modules)
	{
		ambethModules.addAll(modules);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IAmbethConfiguration withApplicationModules(Class<?>... modules)
	{
		applicationModules.addAll(modules);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IAmbethApplication start()
	{
		Properties properties = Properties.getApplication();
		properties.load(this.properties);
		Properties.loadBootstrapPropertyFile();

		bootstrapContext = BeanContextFactory.createBootstrap(properties);
		IServiceContext frameworkContext = bootstrapContext.createService(ambethModules.toArray(Class.class));
		serviceContext = frameworkContext.createService(applicationModules.toArray(Class.class));

		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IServiceContext getApplicationContext()
	{
		return serviceContext;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void close() throws IOException
	{
		bootstrapContext.dispose();
	}
}
