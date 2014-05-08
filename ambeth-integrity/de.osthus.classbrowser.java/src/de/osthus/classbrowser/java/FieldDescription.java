package de.osthus.classbrowser.java;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * Holds the description for fields.
 * 
 * @author juergen.panser
 */
public class FieldDescription implements INamed {

	// ---- VARIABLES ----------------------------------------------------------

	private String fieldName;

	private String fieldType;

	private List<String> modifiers;

	private List<String> annotations = new ArrayList<String>();

	// ---- CONSTRUCTORS -------------------------------------------------------

	/**
	 * Create a new instance.
	 * 
	 * @param fieldName
	 *            Field name; mandatory
	 * @param fieldType
	 *            Field type; mandatory
	 * @param modifiers
	 *            Modifiers; may be null
	 */
	public FieldDescription(String fieldName, String fieldType, List<String> modifiers) {
		if (StringUtils.isBlank(fieldName) || StringUtils.isBlank(fieldType)) {
			throw new IllegalArgumentException("Mandatory method description value missing!");
		}
		this.fieldName = fieldName;
		this.fieldType = fieldType;
		this.modifiers = modifiers == null ? new ArrayList<String>() : modifiers;
	}

	// ---- GETTER/SETTER METHODS ----------------------------------------------

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.osthus.classbrowser.java.INamed#getName()
	 */
	@Override
	public String getName() {
		return fieldName;
	}

	public String getFieldType() {
		return fieldType;
	}

	public List<String> getModifiers() {
		return modifiers;
	}

	public List<String> getAnnotations() {
		return annotations;
	}

	// ---- METHODS ------------------------------------------------------------

	@Override
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).append("field modifiers", this.modifiers).append("field type", this.fieldType)
				.append("field name", this.fieldName).toString();
	}

}
