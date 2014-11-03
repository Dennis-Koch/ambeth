package de.osthus.classbrowser.java;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
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

				List<String> annotations = readAnnotationNames(typeNode);
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

					annotations = readAnnotationNames(methodNode);
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

					annotations = readAnnotationNames(fieldNode);

					List<String> modifiers = new ArrayList<String>();
					Element fieldModifiersNode = fieldNode.getChild("FieldModifiers");
					List<Element> fieldModifierNodes = fieldModifiersNode.getChildren("FieldModifier");
					for (Element fieldModifierNode : fieldModifierNodes)
					{
						modifiers.add(fieldModifierNode.getValue());
					}

					FieldDescription fieldDescription = new FieldDescription(fieldNameAttribute.getValue(), fieldTypeAttribute.getValue(), modifiers);
					fieldDescription.getAnnotations().addAll(annotations);
					typeDescription.getFieldDescriptions().add(fieldDescription);
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

	private static List<String> readAnnotationNames(Element typeNode)
	{
		List<String> annotations = new ArrayList<String>();
		Element typeAnnotationsNode = typeNode.getChild(TAG_NAME_ANNOTATIONS);
		if (typeAnnotationsNode != null)
		{
			List<Element> typeAnnotationNodes = typeAnnotationsNode.getChildren(TAG_NAME_ANNOTATION);
			for (Element typeAnnotationNode : typeAnnotationNodes)
			{
				annotations.add(typeAnnotationNode.getValue());
			}
		}
		return annotations;
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

		List<String> annotations = typeDescription.getAnnotations();
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

		List<String> annotations = methodDescription.getAnnotations();
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
	 * @return Element with the field infos
	 */
	protected static Element createFieldNode(FieldDescription fieldDescription)
	{
		if (fieldDescription == null)
		{
			throw new IllegalArgumentException("Mandatory values for creating the field node are missing!");
		}

		Element fieldNode = new Element("FieldDescription");

		fieldNode.setAttribute(new Attribute("FieldName", fieldDescription.getName()));
		fieldNode.setAttribute(new Attribute("FieldType", fieldDescription.getFieldType()));

		List<String> annotations = fieldDescription.getAnnotations();
		createAnnotationNodes(fieldNode, annotations);

		Element modifiersRootNode = new Element("FieldModifiers");
		fieldNode.addContent(modifiersRootNode);

		for (String modifier : fieldDescription.getModifiers())
		{
			Element modifierNode = new Element("FieldModifier");
			modifierNode.setText(modifier);
			modifiersRootNode.addContent(modifierNode);
		}

		return fieldNode;
	}

	/**
	 * Create the XML nodes for the annotations.
	 * 
	 * @param parentNode
	 *            Node to append the Annotations node to
	 * @param annotations
	 *            List of the annotation names
	 */
	protected static void createAnnotationNodes(Element parentNode, List<String> annotations)
	{
		if (annotations.isEmpty())
		{
			return;
		}

		Element annotationRootNode = new Element(TAG_NAME_ANNOTATIONS);
		parentNode.addContent(annotationRootNode);

		for (String annotation : annotations)
		{
			Element annotationNode = new Element(TAG_NAME_ANNOTATION);
			annotationNode.setText(annotation);
			annotationRootNode.addContent(annotationNode);
		}
	}

}
