package com.koch.ambeth.extscanner;

import com.koch.ambeth.ioc.config.Property;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.util.EqualsUtil;
import com.koch.ambeth.util.collections.ArrayList;

public class ConfigurationEntry implements IMultiPlatformFeature, Comparable<ConfigurationEntry> {
	@LogInstance
	private ILogger log;

	public boolean inJavascript;

	@Override
	public boolean inJavascript() {
		return false;
	}

	public boolean inJava;

	@Override
	public boolean inJava() {
		return inJava;
	}

	public boolean inCSharp;

	@Override
	public boolean inCSharp() {
		return inCSharp;
	}

	public Boolean isMandatory;

	public final String propertyName;

	public final String moduleName;

	protected boolean defaultValueSpecified = false;

	private String defaultValue;

	public final ArrayList<String> contantDefinitions = new ArrayList<>();

	public final ArrayList<TypeEntry> usedInTypes = new ArrayList<>();

	@Override
	public int compareTo(ConfigurationEntry o) {
		return propertyName.compareTo(o.propertyName);
	}

	public boolean isDefaultValueSpecified() {
		return defaultValueSpecified;
	}

	public String getDefaultValue() {
		return defaultValue;
	}

	public void setDefaultValue(String defaultValue) {
		if (defaultValueSpecified && !EqualsUtil.equals(this.defaultValue, defaultValue)) {
			log.warn("Default value for property '" + propertyName + "' is not unique: '"
					+ this.defaultValue + "' vs. '" + defaultValue + "'");
			return;
		}
		if (Property.DEFAULT_VALUE.equals(defaultValue)) {
			// nothing to do
			return;
		}
		defaultValueSpecified = true;
		this.defaultValue = defaultValue;
	}

	public String[] possibleValues;

	public String labelName;

	public ConfigurationEntry(ILogger log, String propertyName, String propertyLabelName,
			String moduleName) {
		this.log = log;
		this.propertyName = propertyName;
		labelName = propertyLabelName;
		this.moduleName = moduleName;
	}
}
