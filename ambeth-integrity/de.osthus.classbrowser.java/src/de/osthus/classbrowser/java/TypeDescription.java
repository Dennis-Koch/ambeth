package de.osthus.classbrowser.java;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

/**
 * Holds the description for types.
 * 
 * @author juergen.panser
 */
public class TypeDescription implements INamed, IDeprecation {

	// ---- VARIABLES ----------------------------------------------------------

	private String source;

	private String moduleName;

	private String namespaceName;

	private String typeName;

	private String fullTypeName;

	private String typeType;

	private int genericTypeParams;

	private final List<String> annotations = new ArrayList<String>();

	private final List<MethodDescription> methodDescriptions = new ArrayList<MethodDescription>();

	private final List<FieldDescription> fieldDescriptions = new ArrayList<FieldDescription>();

	// ---- CONSTRUCTORS -------------------------------------------------------

	/**
	 * Create a new instance.
	 * 
	 * @param source
	 *            Source; mandatory
	 * @param moduleName
	 *            Module name; mandatory
	 * @param namespaceName
	 *            Namespace name; optional
	 * @param typeName
	 *            Short name of the type; mandatory (because no anonymous classes are allowed where the canonical name
	 *            may be null or the simple name be empty)
	 * @param fullTypeName
	 *            Full name of the type including the namespace; mandatory
	 * @param typeType
	 *            Type constant; mandatory
	 * @param genericTypeParams
	 *            Number of generic type parameters
	 */
	public TypeDescription(String source, String moduleName, String namespaceName, String typeName, String fullTypeName, String typeType, int genericTypeParams) {
		if (StringUtils.isBlank(source) || StringUtils.isBlank(typeName) || StringUtils.isBlank(fullTypeName) || StringUtils.isBlank(typeType)) {
			throw new IllegalArgumentException("Mandatory type description value missing!");
		}
		this.source = source;
		this.moduleName = moduleName;
		this.namespaceName = namespaceName;
		this.typeName = typeName;
		this.fullTypeName = fullTypeName;
		this.typeType = typeType;
		this.genericTypeParams = genericTypeParams;
	}

	// ---- GETTER/SETTER METHODS ----------------------------------------------

	public String getSource() {
		return source;
	}

	public String getModuleName() {
		return moduleName;
	}

	public String getNamespaceName() {
		return namespaceName;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.osthus.classbrowser.java.INamed#getName()
	 */
	@Override
	public String getName() {
		return typeName;
	}

	public String getFullTypeName() {
		return fullTypeName;
	}

	public String getTypeType() {
		return typeType;
	}

	public int getGenericTypeParams() {
		return genericTypeParams;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.osthus.classbrowser.java.IDeprecation#isDeprecated()
	 */
	@Override
	public boolean isDeprecated() {
		return IDeprecation.INSTANCE.isDeprecated(annotations);
	}

	public List<String> getAnnotations() {
		return annotations;
	}

	public List<MethodDescription> getMethodDescriptions() {
		return methodDescriptions;
	}

	public List<FieldDescription> getFieldDescriptions() {
		return fieldDescriptions;
	}

}
