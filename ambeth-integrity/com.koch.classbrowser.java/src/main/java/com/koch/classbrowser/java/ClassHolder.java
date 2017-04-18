/**
 * 
 */
package com.koch.classbrowser.java;

import org.apache.commons.lang3.StringUtils;

/**
 * @author juergen.panser
 * 
 */
public class ClassHolder
{

	// ---- INNER CLASSES ------------------------------------------------------

	// ---- CONSTANTS ----------------------------------------------------------

	// ---- VARIABLES ----------------------------------------------------------

	private String source;

	private Class<?> clazz;

	// ---- CONSTRUCTORS -------------------------------------------------------

	/**
	 * @param source
	 *            Source; mandatory
	 * @param clazz
	 *            Class; mandatory
	 */
	public ClassHolder(String source, Class<?> clazz)
	{
		if (StringUtils.isBlank(source) || clazz == null)
		{
			throw new IllegalArgumentException("Mandatory class holder values missing!");
		}
		this.source = source;
		this.clazz = clazz;
	}

	// ---- GETTER/SETTER METHODS ----------------------------------------------

	public String getSource()
	{
		return source;
	}

	public Class<?> getClazz()
	{
		return clazz;
	}

}
