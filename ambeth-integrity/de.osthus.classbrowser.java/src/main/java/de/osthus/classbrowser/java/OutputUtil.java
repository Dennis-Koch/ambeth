package de.osthus.classbrowser.java;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

/**
 * Helper class which supports the output of the results.
 * 
 * @author juergen.panser
 */
public class OutputUtil
{

	// ---- CONSTANTS ----------------------------------------------------------

	private static final String TAG_NAME_ANNOTATION = "Annotation";

	private static final String TAG_NAME_ANNOTATIONS = "Annotations";

	private static final String TAG_NAME_PARAMETER = "Parameter";

	private static final String TAG_NAME_DEFAULT_VALUE = "DefaultValue";

	private static final String TAG_NAME_CURRENT_VALUE = "CurrentValue";

	private static final boolean IGNORE_DEPRECATED_METHODS = true;

	private static final boolean IGNORE_DEPRECATED_TYPES = true;

	public static final String EXPORT_FILE_NAME = "export_java.xml";

	// ---- CONSTRUCTORS -------------------------------------------------------

	private OutputUtil()
	{
		// No instances allowed
	}

	// ---- METHODS ------------------------------------------------------------

	/**
	 * Import the given file and read the type descriptions.
	 * 
	 * @param fileName
	 *            File to import (with full path)
	 * @return Map with TypeDescription as value and the full type name (lower case) as key; never null
	 */
	public static SortedMap<String, TypeDescription> importFromFile(String fileName)
	{
		SortedMap<String, TypeDescription> results = new TreeMap<String, TypeDescription>();

		SAXBuilder builder = new SAXBuilder();
		File xmlFile = new File(fileName);
		try
		{
			Document document = builder.build(xmlFile);
			Element rootNode = document.getRootElement(); // TypeDescriptions

			List<Element> typeDescriptionNodes = rootNode.getChildren("TypeDescription");
			for (Element typeNode : typeDescriptionNodes)
			{
				// Read mandatory attributes
				String typeAttributeValue = getMandatoryAttributeValue(typeNode, "Type");
				String nameAttributeValue = getMandatoryAttributeValue(typeNode, "TypeName");
				String fullNameAttributeValue = getMandatoryAttributeValue(typeNode, "FullTypeName");
				String sourceAttributeValue = getMandatoryAttributeValue(typeNode, "Source");
				// Read optional attributes
				String moduleAttributeValue = getAttributeValue(typeNode, "ModuleName");
				String namespaceAttributeValue = getAttributeValue(typeNode, "NamespaceName");
				int genericTypeParams = 0;
				Attribute genericTypeParamsAttribute = typeNode.getAttribute("GenericTypeParams");
				if (genericTypeParamsAttribute != null)
				{
					genericTypeParams = genericTypeParamsAttribute.getIntValue();
				}

				TypeDescription typeDescription = new TypeDescription(sourceAttributeValue, moduleAttributeValue, namespaceAttributeValue, nameAttributeValue,
						fullNameAttributeValue, typeAttributeValue, genericTypeParams);

				List<AnnotationInfo> annotations = readAnnotationInfo(typeNode);
				typeDescription.getAnnotations().addAll(annotations);
				if (typeDescription.isDeprecated() && IGNORE_DEPRECATED_TYPES)
				{
					continue;
				}

				Element methodDescriptionsNode = typeNode.getChild("MethodDescriptions");
				List<Element> methodDescriptionNodes = methodDescriptionsNode.getChildren("MethodDescription");
				for (Element methodNode : methodDescriptionNodes)
				{
					Attribute methodNameAttribute = methodNode.getAttribute("MethodName");
					Attribute methodReturnTypeAttribute = methodNode.getAttribute("ReturnType");

					annotations = readAnnotationInfo(methodNode);
					if (IDeprecation.INSTANCE.isDeprecated(annotations) && IGNORE_DEPRECATED_METHODS)
					{
						continue;
					}

					List<String> modifiers = new ArrayList<String>();
					Element methodModifiersNode = methodNode.getChild("MethodModifiers");
					List<Element> methodModifierNodes = methodModifiersNode.getChildren("MethodModifier");
					for (Element methodModifierNode : methodModifierNodes)
					{
						modifiers.add(methodModifierNode.getValue());
					}

					List<String> parameterTypes = new ArrayList<String>();
					Element methodParameterTypesNode = methodNode.getChild("MethodParameterTypes");
					List<Element> methodParameterTypeNodes = methodParameterTypesNode.getChildren("MethodParameterType");
					for (Element methodParameterTypeNode : methodParameterTypeNodes)
					{
						parameterTypes.add(methodParameterTypeNode.getValue());
					}

					MethodDescription methodDescription = new MethodDescription(methodNameAttribute.getValue(), methodReturnTypeAttribute.getValue(),
							modifiers, parameterTypes);
					methodDescription.getAnnotations().addAll(annotations);

					typeDescription.getMethodDescriptions().add(methodDescription);
				}

				Element fieldDescriptionsNode = typeNode.getChild("FieldDescriptions");
				List<Element> fieldDescriptionNodes = fieldDescriptionsNode.getChildren("FieldDescription");
				for (Element fieldNode : fieldDescriptionNodes)
				{
					Attribute fieldNameAttribute = fieldNode.getAttribute("FieldName");
					Attribute fieldTypeAttribute = fieldNode.getAttribute("FieldType");

					annotations = readAnnotationInfo(fieldNode);

					List<String> modifiers = new ArrayList<String>();
					Element fieldModifiersNode = fieldNode.getChild("FieldModifiers");
					List<Element> fieldModifierNodes = fieldModifiersNode.getChildren("FieldModifier");
					for (Element fieldModifierNode : fieldModifierNodes)
					{
						modifiers.add(fieldModifierNode.getValue());
					}

					FieldDescription fieldDescription = new FieldDescription(fieldNameAttribute.getValue(), fieldTypeAttribute.getValue(), modifiers);
					typeDescription.getFieldDescriptions().add(fieldDescription);
					fieldDescription.getAnnotations().addAll(annotations);

					Attribute isEnumConstantAttribute = fieldNode.getAttribute("isEnumConstant");
					if (isEnumConstantAttribute != null && "true".equals(isEnumConstantAttribute.getValue()))
					{
						fieldDescription.setEnumConstant(true);
					}

					Element initialValueNode = fieldNode.getChild("InitialValue");
					if (initialValueNode != null)
					{
						String initialValue = initialValueNode.getText();
						fieldDescription.setInitialValue(initialValue);
					}
				}

				String key = typeDescription.getFullTypeName().toLowerCase();
				results.put(key, typeDescription);
			}
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}

		return results;
	}

	private static ArrayList<AnnotationInfo> readAnnotationInfo(Element typeNode)
	{
		ArrayList<AnnotationInfo> annotations = new ArrayList<>();
		Element typeAnnotationsNode = typeNode.getChild(TAG_NAME_ANNOTATIONS);
		if (typeAnnotationsNode != null)
		{
			List<Element> annotationNodes = typeAnnotationsNode.getChildren(TAG_NAME_ANNOTATION);
			for (Element annotationNode : annotationNodes)
			{
				String annotationType = annotationNode.getAttribute("type").getValue();
				List<AnnotationParamInfo> parameters = readAnnotationParamInfo(annotationNode);
				AnnotationInfo annotationInfo = new AnnotationInfo(annotationType, parameters);
				annotations.add(annotationInfo);
			}
		}
		return annotations;
	}

	private static List<AnnotationParamInfo> readAnnotationParamInfo(Element annotationNode)
	{
		List<Element> parameterNodes = annotationNode.getChildren(TAG_NAME_PARAMETER);
		if (parameterNodes.isEmpty())
		{
			return Collections.emptyList();
		}

		List<AnnotationParamInfo> paramInfo = new ArrayList<>(parameterNodes.size());
		for (Element parameterNode : parameterNodes)
		{
			String paramName = parameterNode.getAttribute("name").getValue();
			String paramType = parameterNode.getAttribute("type").getValue();

			Element defaultValueNode = parameterNode.getChild(TAG_NAME_DEFAULT_VALUE);
			Object defaultValue = defaultValueNode == null ? null : defaultValueNode.getText();

			Element currentValueNode = parameterNode.getChild(TAG_NAME_CURRENT_VALUE);
			Object currentValue;
			if (currentValueNode == null)
			{
				currentValue = defaultValue;
			}
			else
			{
				currentValue = "true".equals(currentValueNode.getAttribute("isNull")) ? null : currentValueNode.getText();
			}

			AnnotationParamInfo param = new AnnotationParamInfo(paramName, paramType, defaultValue, currentValue);
			paramInfo.add(param);
		}

		return paramInfo;
	}

	/**
	 * Get an attribute value from the given node. Throws an exception if the attribute isn't found or the value is illegal.
	 * 
	 * @param node
	 *            Node to get the attribute from
	 * @param key
	 *            Attribute key/name
	 * @return Attribute value; never null nor empty
	 */
	private static String getMandatoryAttributeValue(Element node, String key)
	{
		String result = getAttributeValue(node, key);
		if (StringUtils.isBlank(result))
		{
			throw new IllegalStateException("Mandatory attribute '" + key + "' has no value!");
		}
		return result;
	}

	/**
	 * Get an attribute value from the given node. Throws an exception if the attribute isn't found.
	 * 
	 * @param node
	 *            Node to get the attribute from
	 * @param key
	 *            Attribute key/name
	 * @return Attribute value; never null but may be empty
	 */
	private static String getAttributeValue(Element node, String key)
	{
		if (node == null || StringUtils.isBlank(key))
		{
			throw new IllegalArgumentException("Input parameter missing!");
		}
		Attribute attribute = node.getAttribute(key);
		if (attribute == null)
		{
			throw new IllegalStateException("Attribute '" + key + "' not found!");
		}
		return attribute.getValue();
	}

	/**
	 * Export the given type descriptions to an XML file in the given directory.
	 * 
	 * @param foundTypes
	 *            Type descriptions; mandatory (but may be empty)
	 * @param targetPath
	 *            Path to save the file to; mandatory
	 */
	public static void export(List<TypeDescription> foundTypes, String targetPath)
	{
		if (foundTypes == null || StringUtils.isBlank(targetPath))
		{
			throw new IllegalArgumentException("Mandatory values for the export are missing!");
		}

		Element rootNode = new Element("TypeDescriptions");
		Document doc = new Document(rootNode);

		for (TypeDescription typeDescription : foundTypes)
		{
			Element typeNode = createTypeNode(typeDescription);
			rootNode.addContent(typeNode);
		}

		String targetFile = FilenameUtils.concat(targetPath, EXPORT_FILE_NAME);
		writeExportToFile(targetFile, doc);
	}

	/**
	 * Write the given document to the given file.
	 * 
	 * @param fileName
	 *            File name; mandatory
	 * @param doc
	 *            XML document; mandatory
	 */
	public static void writeExportToFile(String fileName, Document doc)
	{
		if (StringUtils.isBlank(fileName) || doc == null)
		{
			throw new IllegalArgumentException("Mandatory export parameter missing!");
		}
		XMLOutputter xmlOutPutter = new XMLOutputter(Format.getPrettyFormat());
		try
		{
			xmlOutPutter.output(doc, new FileWriter(fileName));
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}

	/**
	 * Create the XML node for the type.
	 * 
	 * @param typeDescription
	 *            TypeDescription to get the information from; mandatory
	 * @return Element with the type infos
	 */
	protected static Element createTypeNode(TypeDescription typeDescription)
	{
		if (typeDescription == null)
		{
			throw new IllegalArgumentException("Mandatory values for creating the type node are missing!");
		}

		Element typeNode = new Element("TypeDescription");

		typeNode.setAttribute(new Attribute("Type", typeDescription.getTypeType()));
		typeNode.setAttribute(new Attribute("NamespaceName", typeDescription.getNamespaceName()));
		typeNode.setAttribute(new Attribute("TypeName", typeDescription.getName()));
		typeNode.setAttribute(new Attribute("FullTypeName", typeDescription.getFullTypeName()));
		typeNode.setAttribute(new Attribute("Source", typeDescription.getSource()));
		typeNode.setAttribute(new Attribute("ModuleName", typeDescription.getModuleName()));
		if (typeDescription.getGenericTypeParams() > 0)
		{
			typeNode.setAttribute(new Attribute("GenericTypeParams", String.valueOf(typeDescription.getGenericTypeParams())));
		}

		List<AnnotationInfo> annotations = typeDescription.getAnnotations();
		createAnnotationNodes(typeNode, annotations);

		Element methodRootNode = new Element("MethodDescriptions");
		typeNode.addContent(methodRootNode);

		for (MethodDescription methodDescription : typeDescription.getMethodDescriptions())
		{
			Element methodNode = createMethodNode(methodDescription);
			methodRootNode.addContent(methodNode);
		}

		Element fieldRootNode = new Element("FieldDescriptions");
		typeNode.addContent(fieldRootNode);

		for (FieldDescription fieldDescription : typeDescription.getFieldDescriptions())
		{
			Element fieldNode = createFieldNode(fieldDescription);
			fieldRootNode.addContent(fieldNode);
		}

		return typeNode;
	}

	/**
	 * Create the XML node for the method.
	 * 
	 * @param methodDescription
	 *            MethodDescription to get the information from; mandatory
	 * @return Element with the method infos
	 */
	protected static Element createMethodNode(MethodDescription methodDescription)
	{
		if (methodDescription == null)
		{
			throw new IllegalArgumentException("Mandatory values for creating the method node are missing!");
		}

		Element methodNode = new Element("MethodDescription");

		methodNode.setAttribute(new Attribute("MethodName", methodDescription.getName()));
		methodNode.setAttribute(new Attribute("ReturnType", methodDescription.getReturnType()));

		List<AnnotationInfo> annotations = methodDescription.getAnnotations();
		createAnnotationNodes(methodNode, annotations);

		Element modifiersRootNode = new Element("MethodModifiers");
		methodNode.addContent(modifiersRootNode);

		for (String modifier : methodDescription.getModifiers())
		{
			Element modifierNode = new Element("MethodModifier");
			modifierNode.setText(modifier);
			modifiersRootNode.addContent(modifierNode);
		}

		Element parameterTypesRootNode = new Element("MethodParameterTypes");
		methodNode.addContent(parameterTypesRootNode);

		for (String parameterType : methodDescription.getParameterTypes())
		{
			Element parameterTypeNode = new Element("MethodParameterType");
			parameterTypeNode.setText(parameterType);
			parameterTypesRootNode.addContent(parameterTypeNode);
		}

		return methodNode;
	}

	/**
	 * Create the XML node for the field.
	 * 
	 * @param fieldDescription
	 *            FieldDescription to get the information from; mandatory
	 * @return Element with the field info
	 */
	protected static Element createFieldNode(FieldDescription fieldDescription)
	{
		if (fieldDescription == null)
		{
			throw new IllegalArgumentException("Mandatory values for creating the field node are missing!");
		}

		Element fieldNode = new Element("FieldDescription");

		fieldNode.setAttribute("FieldName", fieldDescription.getName());
		fieldNode.setAttribute("FieldType", fieldDescription.getFieldType());
		if (fieldDescription.isEnumConstant())
		{
			fieldNode.setAttribute("isEnumConstant", "true");
		}

		List<AnnotationInfo> annotations = fieldDescription.getAnnotations();
		createAnnotationNodes(fieldNode, annotations);

		Element modifiersRootNode = new Element("FieldModifiers");
		fieldNode.addContent(modifiersRootNode);

		for (String modifier : fieldDescription.getModifiers())
		{
			Element modifierNode = new Element("FieldModifier");
			modifierNode.setText(modifier);
			modifiersRootNode.addContent(modifierNode);
		}

		String initialValue = fieldDescription.getInitialValue();
		if (initialValue != null)
		{
			Element modifierNode = new Element("InitialValue");
			modifiersRootNode.addContent(modifierNode);
			modifierNode.setText(initialValue);
		}

		return fieldNode;
	}

	/**
	 * Create the XML nodes for the annotations.
	 * 
	 * @param parentNode
	 *            Node to append the Annotations node to
	 * @param annotations
	 *            List of the annotation Info
	 */
	protected static void createAnnotationNodes(Element parentNode, List<AnnotationInfo> annotations)
	{
		if (annotations.isEmpty())
		{
			return;
		}

		Element annotationRootNode = new Element(TAG_NAME_ANNOTATIONS);
		parentNode.addContent(annotationRootNode);

		for (AnnotationInfo annotation : annotations)
		{
			Element annotationNode = new Element(TAG_NAME_ANNOTATION);
			annotationRootNode.addContent(annotationNode);
			annotationNode.setAttribute("type", annotation.getAnnotationType());

			List<AnnotationParamInfo> parameters = annotation.getParameters();
			for (AnnotationParamInfo parameter : parameters)
			{
				Element paramNode = new Element(TAG_NAME_PARAMETER);
				annotationNode.addContent(paramNode);
				paramNode.setAttribute("name", parameter.getName());
				paramNode.setAttribute("type", parameter.getType());

				Object defaultValue = parameter.getDefaultValue();
				if (defaultValue != null)
				{
					Element defaultValueNode = new Element(TAG_NAME_DEFAULT_VALUE);
					paramNode.addContent(defaultValueNode);
					String defaultValueString = defaultValue.toString();
					if ("\u0000".equals(defaultValueString))
					{
						// Workaround for a character that is illegal in xml
						defaultValueString = "\\u0000";
					}
					defaultValueNode.setText(defaultValueString);
				}

				Object currentValue = parameter.getCurrentValue();
				if (currentValue != defaultValue && (currentValue == null || !currentValue.equals(defaultValue)))
				{
					Element currentValueNode = new Element(TAG_NAME_CURRENT_VALUE);
					paramNode.addContent(currentValueNode);
					if (currentValue != null)
					{
						currentValueNode.setText(currentValue.toString());
					}
					else
					{
						currentValueNode.setAttribute("isNull", "true");
					}
				}
			}
		}
	}
}
