package com.koch.ambeth.ci;

/*-
 * #%L
 * jambeth-test
 * %%
 * Copyright (C) 2017 Koch Softwaredevelopment
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 * #L%
 */

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.koch.ambeth.ioc.IServiceContext;
import com.koch.ambeth.ioc.factory.BeanContextFactory;
import com.koch.ambeth.log.config.Properties;
import com.koch.ambeth.util.IClasspathScanner;
import com.koch.ambeth.util.annotation.ConfigurationConstantDescription;
import com.koch.ambeth.util.annotation.ConfigurationConstants;
import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;
import com.koch.ambeth.xml.DefaultXmlWriter;
import com.koch.ambeth.xml.config.XmlConfigurationConstants;
import com.koch.ambeth.xml.util.ClasspathScanner;

public final class ConfigurationPropertiesScanner {
	public static void main(String[] args) throws Exception {
		Properties props = Properties.getApplication();

		props.put(XmlConfigurationConstants.PackageScanPatterns, ".+");
		props.put("ambeth.log.level", "INFO");

		IServiceContext bootstrapContext = BeanContextFactory.createBootstrap(props);
		IClasspathScanner classpathScanner =
				bootstrapContext.registerBean(ClasspathScanner.class).finish();
		List<Class<?>> types = classpathScanner.scanClassesAnnotatedWith(ConfigurationConstants.class);
		Collections.sort(types, new Comparator<Class<?>>() {
			@Override
			public int compare(Class<?> o1, Class<?> o2) {
				return o1.getName().compareTo(o2.getName());
			}
		});

		List<Field> fields = new ArrayList<Field>();
		for (Class<?> type : types) {
			Field[] fieldsOfType = type.getFields();
			for (Field fieldOfType : fieldsOfType) {
				int modifiers = fieldOfType.getModifiers();
				if ((modifiers & Modifier.STATIC) == 0) {
					continue;
				}
				if ((modifiers & Modifier.PUBLIC) == 0) {
					continue;
				}
				if ((modifiers & Modifier.FINAL) == 0) {
					continue;
				}
				if (!String.class.equals(fieldOfType.getType())) {
					continue;
				}
				fields.add(fieldOfType);
			}
		}
		Collections.sort(fields, new Comparator<Field>() {
			@Override
			public int compare(Field o1, Field o2) {
				try {
					return ((String) o1.get(null)).compareTo((String) o2.get(null));
				}
				catch (Throwable e) {
					throw RuntimeExceptionUtil.mask(e);
				}
			}
		});

		File configOverviewFile = new File(args[0]);
		OutputStreamWriter fileWriter =
				new OutputStreamWriter(new FileOutputStream(configOverviewFile), Charset.forName("UTF-8"));
		try {
			DefaultXmlWriter xmlWriter = new DefaultXmlWriter(fileWriter, null);

			String allPropsElementName = "properties";
			String propElementName = "property";
			String propName = "name";
			String propDescriptionName = "description";
			xmlWriter.writeOpenElement(allPropsElementName);
			for (Field field : fields) {
				String propertyConstant = (String) field.get(null);

				System.out.println("Found property '" + propertyConstant + "'");
				xmlWriter.writeStartElement(propElementName);
				xmlWriter.writeAttribute(propName, propertyConstant);
				xmlWriter.writeStartElementEnd();
				xmlWriter.writeOpenElement(propDescriptionName);

				ConfigurationConstantDescription description =
						field.getAnnotation(ConfigurationConstantDescription.class);

				if (description != null) {
					xmlWriter.writeEscapedXml(description.value());
				}
				xmlWriter.writeCloseElement(propDescriptionName);

				xmlWriter.writeCloseElement(propElementName);
			}
			xmlWriter.writeCloseElement(allPropsElementName);
		}
		finally {
			fileWriter.close();
		}
		System.out.println("Finished successfully. Found " + fields.size() + " properties");
		System.out.println("Wrote properties to file '" + configOverviewFile.getAbsolutePath() + "'");
	}

	private ConfigurationPropertiesScanner() {
		// Intended blank
	}
}
