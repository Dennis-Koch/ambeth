package de.osthus.classbrowser.java;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * Holds the description for methods.
 * 
 * @author juergen.panser
 */
public class MethodDescription implements INamed, IDeprecation
{

	// ---- VARIABLES ----------------------------------------------------------

	private String methodName;

	private String returnType;

	private List<String> modifiers;

	private List<String> parameterTypes;

	private List<String> annotations = new ArrayList<String>();

	// ---- CONSTRUCTORS -------------------------------------------------------

	/**
	 * Create a new instance.
	 * 
	 * @param methodName
	 *            Method name; mandatory
	 * @param returnType
	 *            Return type; mandatory
	 * @param modifiers
	 *            Modifiers; may be null
	 * @param parameterTypes
	 *            Parameters types; may be null
	 */
	public MethodDescription(String methodName, String returnType, List<String> modifiers, List<String> parameterTypes)
	{
		if (StringUtils.isBlank(methodName) || StringUtils.isBlank(returnType))
		{
			throw new IllegalArgumentException("Mandatory method description value missing!");
		}
		this.methodName = methodName;
		this.returnType = returnType;
		this.modifiers = modifiers == null ? new ArrayList<String>() : modifiers;
		this.parameterTypes = parameterTypes == null ? new ArrayList<String>() : parameterTypes;
	}

	// ---- GETTER/SETTER METHODS ----------------------------------------------

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.osthus.classbrowser.java.INamed#getName()
	 */
	@Override
	public String getName()
	{
		return methodName;
	}

	public String getReturnType()
	{
		return returnType;
	}

	public List<String> getModifiers()
	{
		return modifiers;
	}

	public List<String> getParameterTypes()
	{
		return parameterTypes;
	}

	public List<String> getAnnotations()
	{
		return annotations;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.osthus.classbrowser.java.IDeprecation#isDeprecated()
	 */
	@Override
	public boolean isDeprecated()
	{
		return IDeprecation.INSTANCE.isDeprecated(annotations);
	}

	// ---- METHODS ------------------------------------------------------------

	@Override
	public String toString()
	{
		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).append("method modifiers", this.modifiers).append("method name", this.methodName)
				.append("param types", this.parameterTypes).append("return type", this.returnType).toString();
	}

}
