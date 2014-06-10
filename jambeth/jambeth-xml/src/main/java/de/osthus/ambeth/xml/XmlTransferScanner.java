package de.osthus.ambeth.xml;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import de.osthus.ambeth.ioc.IDisposableBean;
import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.ioc.IStartingBean;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.merge.IEntityMetaDataProvider;
import de.osthus.ambeth.typeinfo.ITypeInfoProvider;
import de.osthus.ambeth.util.IClasspathScanner;
import de.osthus.ambeth.util.ImmutableTypeSet;

public class XmlTransferScanner implements IInitializingBean, IStartingBean, IDisposableBean
{
	@LogInstance
	private ILogger log;

	public static final String DefaultNamespace = "http://schemas.osthus.de/Ambeth";

	@Autowired(optional = true)
	protected IClasspathScanner classpathScanner;

	@Autowired
	protected IEntityMetaDataProvider entityMetaDataProvider;

	@Autowired
	protected ITypeInfoProvider typeInfoProvider;

	@Autowired
	protected IXmlTypeExtendable xmlTypeExtendable;

	protected List<Class<?>> rootElementClasses;

	protected List<Runnable> unregisterRunnables = new ArrayList<Runnable>();

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		if (classpathScanner == null)
		{
			if (log.isInfoEnabled())
			{
				log.info("Skipped scanning for XML transfer types. Reason: No instance of " + IClasspathScanner.class.getName() + " resolved");
			}
			return;
		}
		List<Class<?>> rootElementClasses = classpathScanner.scanClassesAnnotatedWith(XmlRootElement.class, XmlType.class,
				de.osthus.ambeth.annotation.XmlType.class);
		if (log.isInfoEnabled())
		{
			log.info("Found " + rootElementClasses.size() + " classes annotated as XML transfer types");
		}
		if (log.isDebugEnabled())
		{
			Collections.sort(rootElementClasses, new Comparator<Class<?>>()
			{
				@Override
				public int compare(Class<?> o1, Class<?> o2)
				{
					return o1.getName().compareTo(o2.getName());
				}
			});
			StringBuilder sb = new StringBuilder();
			sb.append("Xml entities found: ");
			for (int a = 0, size = rootElementClasses.size(); a < size; a++)
			{
				sb.append("\n\t").append(rootElementClasses.get(a).getName());
			}
			log.debug(sb.toString());
		}
		for (int a = rootElementClasses.size(); a-- > 0;)
		{
			final Class<?> rootElementClass = rootElementClasses.get(a);
			String name, namespace;

			de.osthus.ambeth.annotation.XmlType genericXmlType = rootElementClass.getAnnotation(de.osthus.ambeth.annotation.XmlType.class);
			if (genericXmlType != null)
			{
				name = genericXmlType.name();
				namespace = genericXmlType.namespace();
			}
			else
			{
				XmlRootElement xmlRootElement = rootElementClass.getAnnotation(XmlRootElement.class);
				if (xmlRootElement != null)
				{
					name = xmlRootElement.name();
					namespace = xmlRootElement.namespace();
				}
				else
				{
					XmlType xmlType = rootElementClass.getAnnotation(XmlType.class);
					name = xmlType.name();
					namespace = xmlType.namespace();
				}
			}
			if (DefaultNamespace.equals(namespace) || "##default".equals(namespace) || namespace != null && namespace.length() == 0)
			{
				namespace = null;
			}
			if (name == null || name.length() == 0 || "##default".equals(name))
			{
				name = typeInfoProvider.getTypeInfo(rootElementClass).getSimpleName();
			}
			xmlTypeExtendable.registerXmlType(rootElementClass, name, namespace);
			final String fName = name;
			final String fNamespace = namespace;
			unregisterRunnables.add(new Runnable()
			{
				@Override
				public void run()
				{
					xmlTypeExtendable.unregisterXmlType(rootElementClass, fName, fNamespace);
				}
			});
		}
		this.rootElementClasses = rootElementClasses;
	}

	@Override
	public void afterStarted() throws Throwable
	{
		if (rootElementClasses == null)
		{
			return;
		}
		// Eager fetch all meta data. Even if some of the classes are NOT an entity this is not a problem
		List<Class<?>> types = new ArrayList<Class<?>>();
		for (Class<?> type : rootElementClasses)
		{
			if (type.isInterface() || ImmutableTypeSet.isImmutableType(type))
			{
				continue;
			}
			types.add(type);
		}
		entityMetaDataProvider.getMetaData(types);
	}

	@Override
	public void destroy() throws Throwable
	{
		for (int a = unregisterRunnables.size(); a-- > 0;)
		{
			unregisterRunnables.get(a).run();
		}
	}
}
