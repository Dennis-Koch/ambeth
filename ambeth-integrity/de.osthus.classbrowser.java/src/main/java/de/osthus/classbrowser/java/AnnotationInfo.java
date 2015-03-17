package de.osthus.classbrowser.java;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * Holds the information for annotations.
 * 
 * @author jochen.hormes
 */
public class AnnotationInfo
{
	// ---- VARIABLES ----------------------------------------------------------

	private String annotationType;

	private List<AnnotationParamInfo> parameters;

	// ---- CONSTRUCTORS -------------------------------------------------------

	/**
	 * Create a new instance.
	 * 
	 * @param annotationType
	 *            Annotation type name; mandatory
	 * @param parameters
	 *            Parameter values; mandatory
	 */
	public AnnotationInfo(String annotationType, List<AnnotationParamInfo> parameters)
	{
		if (StringUtils.isBlank(annotationType))
		{
			throw new IllegalArgumentException("Mandatory annotation info value missing!");
		}

		this.annotationType = annotationType;
		this.parameters = parameters;
	}

	// ---- GETTER/SETTER METHODS ----------------------------------------------

	public String getAnnotationType()
	{
		return annotationType;
	}

	public List<AnnotationParamInfo> getParameters()
	{
		return parameters;
	}

	// ---- METHODS ------------------------------------------------------------

	@Override
	public String toString()
	{
		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).append("annotation type", annotationType).append("params", parameters).toString();
	}
}
