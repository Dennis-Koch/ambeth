package de.osthus.ambeth.ci;

import java.io.File;
import java.io.FileWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import de.osthus.ambeth.annotation.ConfigurationConstantDescription;
import de.osthus.ambeth.annotation.ConfigurationConstants;
import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.config.Properties;
import de.osthus.ambeth.config.XmlConfigurationConstants;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.ioc.IServiceContext;
import de.osthus.ambeth.ioc.factory.BeanContextFactory;
import de.osthus.ambeth.util.ClasspathScanner;
import de.osthus.ambeth.util.IClasspathScanner;
import de.osthus.ambeth.xml.DefaultXmlWriter;

public final class ConfigurationPropertiesScanner
{
	public static void main(String[] args) throws Exception
	{
		Properties props = Properties.getApplication();

		props.put(XmlConfigurationConstants.PackageScanPatterns, ".+");
		props.put("ambeth.log.level", "INFO");

		IServiceContext bootstrapContext = BeanContextFactory.createBootstrap(props);
		IClasspathScanner classpathScanner = bootstrapContext.registerBean(ClasspathScanner.class).finish();
		List<Class<?>> types = classpathScanner.scanClassesAnnotatedWith(ConfigurationConstants.class);
		Collections.sort(types, new Comparator<Class<?>>()
		{
			@Override
			public int compare(Class<?> o1, Class<?> o2)
			{
				return o1.getName().compareTo(o2.getName());
			}
		});

		List<Field> fields = new ArrayList<Field>();
		for (Class<?> type : types)
		{
			Field[] fieldsOfType = type.getFields();
			for (Field fieldOfType : fieldsOfType)
			{
				int modifiers = fieldOfType.getModifiers();
				if ((modifiers & Modifier.STATIC) == 0)
				{
					continue;
				}
				if ((modifiers & Modifier.PUBLIC) == 0)
				{
					continue;
				}
				if ((modifiers & Modifier.FINAL) == 0)
				{
					continue;
				}
				if (!String.class.equals(fieldOfType.getType()))
				{
					continue;
				}
				fields.add(fieldOfType);
			}
		}
		Collections.sort(fields, new Comparator<Field>()
		{
			@Override
			public int compare(Field o1, Field o2)
			{
				try
				{
					return ((String) o1.get(null)).compareTo((String) o2.get(null));
				}
				catch (Throwable e)
				{
					throw RuntimeExceptionUtil.mask(e);
				}
			}
		});

		File configOverviewFile = new File(args[0]);
		FileWriter fileWriter = new FileWriter(configOverviewFile);
		try
		{
			DefaultXmlWriter xmlWriter = new DefaultXmlWriter(fileWriter, null);

			String allPropsElementName = "properties";
			String propElementName = "property";
			String propName = "name";
			String propDescriptionName = "description";
			xmlWriter.writeOpenElement(allPropsElementName);
			for (Field field : fields)
			{
				String propertyConstant = (String) field.get(null);

				System.out.println("Found property '" + propertyConstant + "'");
				xmlWriter.writeStartElement(propElementName);
				xmlWriter.writeAttribute(propName, propertyConstant);
				xmlWriter.writeStartElementEnd();
				xmlWriter.writeOpenElement(propDescriptionName);

				ConfigurationConstantDescription description = field.getAnnotation(ConfigurationConstantDescription.class);

				if (description != null)
				{
					xmlWriter.writeEscapedXml(description.value());
				}
				xmlWriter.writeCloseElement(propDescriptionName);

				xmlWriter.writeCloseElement(propElementName);
			}
			xmlWriter.writeCloseElement(allPropsElementName);
		}
		finally
		{
			fileWriter.close();
		}
		System.out.println("Finished successfully. Found " + fields.size() + " properties");
		System.out.println("Wrote properties to file '" + configOverviewFile.getAbsolutePath() + "'");
	}

	private ConfigurationPropertiesScanner()
	{
		// Intended blank
	}
}
