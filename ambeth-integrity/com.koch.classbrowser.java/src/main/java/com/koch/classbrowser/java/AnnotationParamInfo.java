package com.koch.classbrowser.java;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * Holds the information for annotation params.
 * 
 * @author jochen.hormes
 */
public class AnnotationParamInfo implements INamed
{
	// ---- VARIABLES ----------------------------------------------------------

	private String name;

	private String type;

	private Object defaultValue;

	private Object currentValue;

	// ---- CONSTRUCTORS -------------------------------------------------------

	/**
	 * Create a new instance.
	 * 
	 * @param name
	 *            Annotation parameter name; mandatory
	 * @param type
	 *            Annotation parameter type; mandatory
	 * @param defaultValue
	 *            Annotation parameter default value; may be null
	 * @param currentValue
	 *            Annotation parameter current value; may be null
	 */
	public AnnotationParamInfo(String name, String type, Object defaultValue, Object currentValue)
	{
		if (StringUtils.isBlank(name) || StringUtils.isBlank(type))
		{
			throw new IllegalArgumentException("Mandatory annotation param info value missing!");
		}

		this.name = name;
		this.type = type;
		this.defaultValue = defaultValue;
		this.currentValue = currentValue;
	}

	// ---- GETTER/SETTER METHODS ----------------------------------------------

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getName()
	{
		return name;
	}

	public String getType()
	{
		return type;
	}

	public Object getDefaultValue()
	{
		return defaultValue;
	}

	public Object getCurrentValue()
	{
		return currentValue;
	}

	// ---- METHODS ------------------------------------------------------------

	@Override
	public String toString()
	{
		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).append("param name", type).append("param type", type)
				.append("param default value", defaultValue).append("param current value", currentValue).toString();
	}
}
