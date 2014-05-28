package de.osthus.ambeth.xml.typehandler;

import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.objectcollector.IThreadLocalObjectCollector;
import de.osthus.ambeth.util.IConversionHelper;
import de.osthus.ambeth.xml.ICyclicXmlDictionary;
import de.osthus.ambeth.xml.namehandler.ClassNameHandler;

public abstract class AbstractHandler implements IInitializingBean
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected IConversionHelper conversionHelper;

	@Autowired
	protected IThreadLocalObjectCollector objectCollector;

	@Autowired
	protected ICyclicXmlDictionary xmlDictionary;

	@Autowired
	protected ClassNameHandler classElementHandler;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		// Intended blank
	}
}
