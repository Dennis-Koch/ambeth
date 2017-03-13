package com.koch.ambeth.testutil;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import com.koch.ambeth.ioc.IDisposableBean;
import com.koch.ambeth.ioc.IInitializingBean;
import com.koch.ambeth.ioc.config.Property;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.log.config.Properties;
import com.koch.ambeth.util.ParamChecker;
import com.koch.ambeth.util.appendable.AppendableStringBuilder;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;
import com.koch.ambeth.xml.DefaultXmlWriter;

public class Measurement implements IMeasurement, IInitializingBean, IDisposableBean
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	protected final StringBuilder measurementXML = new StringBuilder();

	protected final DefaultXmlWriter xmlWriter = new DefaultXmlWriter(new AppendableStringBuilder(measurementXML), null);

	protected String measurementFile;

	protected String testClassName;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		ParamChecker.assertNotNull(measurementFile, "MeasurementFile");
		ParamChecker.assertNotNull(testClassName, "TestClassName");
	}

	@Override
	public void destroy() throws Throwable
	{
		writeToFile();
	}

	@Property(name = "measurement.file", defaultValue = "measurements.xml")
	public void setMeasurementFile(String measurementFile)
	{
		this.measurementFile = measurementFile;
	}

	public void setTestClassName(String testClassName)
	{
		this.testClassName = testClassName;
	}

	public void writeToFile()
	{
		if (measurementXML.length() > 0)
		{
			String measurements = measurementXML.toString();
			measurementXML.setLength(0);

			// Do not use dots in xml element names
			String name = testClassName.replaceAll("\\.", "_");
			xmlWriter.writeOpenElement(name);
			xmlWriter.write(measurements);
			xmlWriter.writeCloseElement(name);

			try
			{
				OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(measurementFile, true), Properties.CHARSET_UTF_8);
				try
				{
					osw.append(measurementXML);
				}
				finally
				{
					osw.close();
				}
			}
			catch (IOException e)
			{
				throw RuntimeExceptionUtil.mask(e);
			}
		}
	}

	@Override
	public void log(String name, Object value)
	{
		String elementName = name.replaceAll(" ", "_").replaceAll("\\.", "_").replaceAll("\\(", "_").replaceAll("\\)", "_").replaceAll("%", "perc");
		while (elementName.contains("__"))
		{
			elementName = elementName.replaceAll("__", "_");
		}
		xmlWriter.writeOpenElement(elementName);
		xmlWriter.writeEscapedXml(value.toString());
		xmlWriter.writeCloseElement(elementName);
	}
}
