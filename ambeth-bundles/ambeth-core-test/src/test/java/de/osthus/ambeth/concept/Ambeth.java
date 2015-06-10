package de.osthus.ambeth.concept;

import java.io.IOException;
import java.util.Properties;

import de.osthus.ambeth.bundle.IBundleModule;
import de.osthus.ambeth.config.IProperties;

public class Ambeth implements IAmbethConfiguration, IAmbethApplication
{
	public static IAmbethConfiguration createDefault()
	{
		return null;
	}

	public static IAmbethConfiguration createBundle(Class<? extends IBundleModule> bundleModule)
	{
		return null;
	}

	public static IAmbethConfiguration createEmpty()
	{
		return null;
	}

	private Ambeth()
	{
	}

	@Override
	public void close() throws IOException
	{
	}

	@Override
	public IServiceContext getApplicationContext()
	{
		return null;
	}

	@Override
	public IAmbethConfiguration withProperties(IProperties properties)
	{
		return null;
	}

	@Override
	public IAmbethConfiguration withProperties(Properties properties)
	{
		return null;
	}

	@Override
	public IAmbethConfiguration withProperty(String name, String value)
	{
		return null;
	}

	@Override
	public IAmbethConfiguration withArgs(String... args)
	{
		return null;
	}

	@Override
	public IAmbethConfiguration withAmbethModules(Class<?>... modules)
	{
		return null;
	}

	@Override
	public IAmbethConfiguration withApplicationModules(Class<?>... modules)
	{
		return null;
	}

	@Override
	public IAmbethApplication start()
	{
		return null;
	}

	@Override
	public IServiceContext start1()
	{
		return null;
	}

	@Override
	public void startAndClose()
	{
	}
}
