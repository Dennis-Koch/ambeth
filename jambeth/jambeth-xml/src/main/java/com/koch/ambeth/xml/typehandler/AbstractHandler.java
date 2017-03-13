package com.koch.ambeth.xml.typehandler;

import com.koch.ambeth.ioc.IInitializingBean;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.util.IConversionHelper;
import com.koch.ambeth.util.objectcollector.IThreadLocalObjectCollector;
import com.koch.ambeth.xml.ICyclicXmlDictionary;
import com.koch.ambeth.xml.namehandler.ClassNameHandler;

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
