package de.osthus.ambeth.extscanner;

import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.config.Property;
import de.osthus.ambeth.util.EqualsUtil;

public class ConfigurationEntry
{
	public boolean inJava;

	public boolean inJava()
	{
		return inJava;
	}

	public boolean inCSharp;

	public boolean inCSharp()
	{
		return inCSharp;
	}

	public Boolean isMandatory;

	public final String propertyName;

	protected boolean defaultValueSpecified = false;

	private String defaultValue;

	public final ArrayList<String> contantDefinitions = new ArrayList<String>();

	public final ArrayList<String> usedInTypes = new ArrayList<String>();

	public boolean isDefaultValueSpecified()
	{
		return defaultValueSpecified;
	}

	public String getDefaultValue()
	{
		return defaultValue;
	}

	public void setDefaultValue(String defaultValue)
	{
		if (defaultValueSpecified && !EqualsUtil.equals(this.defaultValue, defaultValue))
		{
			throw new IllegalStateException("Default value for property '" + propertyName + "' is not unique");
		}
		if (Property.DEFAULT_VALUE.equals(defaultValue))
		{
			// nothing to do
			return;
		}
		defaultValueSpecified = true;
		this.defaultValue = defaultValue;
	}

	public String[] possibleValues;

	public ConfigurationEntry(String propertyName)
	{
		this.propertyName = propertyName;
	}
}
