using System;
using System.Collections.Generic;
using System.Xml;

namespace CsharpClassbrowser
{
    /// <summary>
    /// Helper class which supports the output of the results.
    /// </summary>
    public static class OutputUtil
    {

        // ============================================================================================
        #region Constants
        // ============================================================================================

        private const string TAG_NAME_ANNOTATION = "Annotation";

        private const string TAG_NAME_ANNOTATIONS = "Annotations";

        private const string TAG_NAME_PARAMETER = "Parameter";

        private const string TAG_NAME_DEFAULT_VALUE = "DefaultValue";

        private const string TAG_NAME_CURRENT_VALUE = "CurrentValue";

        public const string EXPORT_FILE_NAME = "export_csharp.xml";

        #endregion

        // ============================================================================================
        #region Methods
        // ============================================================================================

        /// <summary>
        /// Export the given type descriptions to an XML file in the given directory.
        /// </summary>
        /// <param name="foundTypes">Type descriptions; mandatory (but may be empty)</param>
        /// <param name="targetPath">Path to save the file 'export.xml' to; mandatory</param>
        public static void Export(IList<TypeDescription> foundTypes, string targetPath)
        {
            if (foundTypes == null || string.IsNullOrWhiteSpace(targetPath))
            {
                throw new ArgumentNullException("Mandatory values for the export are missing!");
            }
            XmlDocument doc = new XmlDocument();
            XmlNode rootNode = doc.CreateElement("TypeDescriptions");
            doc.AppendChild(rootNode);
            foreach (var typeDescription in foundTypes)
            {
                XmlNode typeNode = CreateTypeNode(doc, typeDescription);
                rootNode.AppendChild(typeNode);
            }
            string targetFile = System.IO.Path.Combine(targetPath, EXPORT_FILE_NAME);
            doc.Save(targetFile);
        }

        /// <summary>
        /// Create the XMLNode for the type.
        /// </summary>
        /// <param name="doc">XmlDocument used to create the XML entities; mandatory</param>
        /// <param name="typeDescription">TypeDescription to get the information from; mandatory</param>
        /// <returns>XmlNode with the type infos</returns>
        public static XmlNode CreateTypeNode(XmlDocument doc, TypeDescription typeDescription)
        {
            if (doc == null || typeDescription == null)
            {
                throw new ArgumentNullException("Mandatory values for creating the type node are missing!");
            }

            XmlNode typeNode = doc.CreateElement("TypeDescription");

            XmlAttribute typeAttribute = doc.CreateAttribute("Type");
            typeAttribute.InnerText = typeDescription.TypeType;
            typeNode.Attributes.Append(typeAttribute);

            XmlAttribute namespaceAttribute = doc.CreateAttribute("NamespaceName");
            namespaceAttribute.InnerText = typeDescription.NamespaceName;
            typeNode.Attributes.Append(namespaceAttribute);

            XmlAttribute nameAttribute = doc.CreateAttribute("TypeName");
            nameAttribute.InnerText = typeDescription.Name;
            typeNode.Attributes.Append(nameAttribute);

            XmlAttribute fullNameAttribute = doc.CreateAttribute("FullTypeName");
            fullNameAttribute.InnerText = typeDescription.FullTypeName;
            typeNode.Attributes.Append(fullNameAttribute);

            XmlAttribute moduleAttribute = doc.CreateAttribute("ModuleName");
            moduleAttribute.InnerText = typeDescription.ModuleName;
            typeNode.Attributes.Append(moduleAttribute);

            XmlAttribute sourceAttribute = doc.CreateAttribute("Source");
            sourceAttribute.InnerText = typeDescription.Source;
            typeNode.Attributes.Append(sourceAttribute);

            if (typeDescription.GenericTypeParams > 0)
            {
                XmlAttribute genericTypeParamsAttribute = doc.CreateAttribute("GenericTypeParams");
                genericTypeParamsAttribute.InnerText = typeDescription.GenericTypeParams.ToString();
                typeNode.Attributes.Append(genericTypeParamsAttribute);
            }

            IList<AnnotationInfo> annotations = typeDescription.Annotations;
            CreateAnnotationNodes(typeNode, annotations, doc);

            XmlNode methodRootNode = doc.CreateElement("MethodDescriptions");
            typeNode.AppendChild(methodRootNode);

            foreach (var methodDescription in typeDescription.MethodDescriptions)
            {
                var methodNode = CreateMethodNode(doc, methodDescription);
                methodRootNode.AppendChild(methodNode);
            }

            XmlNode fieldRootNode = doc.CreateElement("FieldDescriptions");
            typeNode.AppendChild(fieldRootNode);

            foreach (var fieldDescription in typeDescription.FieldDescriptions)
            {
                var fieldNode = CreateFieldNode(doc, fieldDescription);
                fieldRootNode.AppendChild(fieldNode);
            }

            return typeNode;
        }

        /// <summary>
        /// Create the XMLNode for the method.
        /// </summary>
        /// <param name="doc">XmlDocument used to create the XML entities; mandatory</param>
        /// <param name="fieldDescription">MethodDescription to get the information from; mandatory</param>
        /// <returns>XmlNode with the method infos</returns>
        public static XmlNode CreateMethodNode(XmlDocument doc, MethodDescription methodDescription)
        {
            if (doc == null || methodDescription == null)
            {
                throw new ArgumentNullException("Mandatory values for creating the method node are missing!");
            }

            XmlNode methodNode = doc.CreateElement("MethodDescription");
            AppendAttribute(methodNode, "MethodName", methodDescription.Name, doc);
            AppendAttribute(methodNode, "ReturnType", methodDescription.ReturnType, doc);

            IList<AnnotationInfo> annotations = methodDescription.Annotations;
            CreateAnnotationNodes(methodNode, annotations, doc);

            XmlNode modifiersRootNode = doc.CreateElement("MethodModifiers");
            methodNode.AppendChild(modifiersRootNode);

            foreach (var modifier in methodDescription.Modifiers)
            {
                XmlNode modifierNode = doc.CreateElement("MethodModifier");
                modifierNode.InnerText = modifier;
                modifiersRootNode.AppendChild(modifierNode);
            }

            XmlNode parameterTypesRootNode = doc.CreateElement("MethodParameterTypes");
            methodNode.AppendChild(parameterTypesRootNode);

            IList<String> paramTypes = methodDescription.ParameterTypes;
            IList<String> paramNames = methodDescription.ParameterNames;
            for (int i = 0; i < paramTypes.Count; i++)
            {
                String parameterType = paramTypes[i];
                String parameterName = paramNames[i];
                XmlNode parameterTypeNode = doc.CreateElement("MethodParameterType");
                parameterTypesRootNode.AppendChild(parameterTypeNode);
                parameterTypeNode.InnerText = parameterType;
                AppendAttribute(parameterTypeNode, "Name", parameterName, doc);
            }

            return methodNode;
        }


        /// <summary>
        /// Create the XMLNode for the field.
        /// </summary>
        /// <param name="doc">XmlDocument used to create the XML entities; mandatory</param>
        /// <param name="fieldDescription">FieldDescription to get the information from; mandatory</param>
        /// <returns>XmlNode with the field info</returns>
        public static XmlNode CreateFieldNode(XmlDocument doc, FieldDescription fieldDescription)
        {
            if (doc == null || fieldDescription == null)
            {
                throw new ArgumentNullException("Mandatory values for creating the field node are missing!");
            }

            XmlNode fieldNode = doc.CreateElement("FieldDescription");
            AppendAttribute(fieldNode, "FieldName", fieldDescription.Name, doc);
            AppendAttribute(fieldNode, "FieldType", fieldDescription.FieldType, doc);
            if (fieldDescription.EnumConstant)
            {
                AppendAttribute(fieldNode, "isEnumConstant", "true", doc);
            }

            IList<AnnotationInfo> annotations = fieldDescription.Annotations;
            CreateAnnotationNodes(fieldNode, annotations, doc);

            XmlNode modifiersRootNode = doc.CreateElement("FieldModifiers");
            fieldNode.AppendChild(modifiersRootNode);
            foreach (var modifier in fieldDescription.Modifiers)
            {
                XmlNode modifierNode = doc.CreateElement("FieldModifier");
                modifiersRootNode.AppendChild(modifierNode);
                modifierNode.InnerText = modifier;
            }

            String initialValue = fieldDescription.InitialValue;
            if (initialValue != null)
            {
                XmlNode initialValueNode = doc.CreateElement("InitialValue");
                fieldNode.AppendChild(initialValueNode);
                initialValueNode.InnerText = initialValue;
            }

            return fieldNode;
        }

        /// <summary>
        /// Create the XML nodes for the annotations.
        /// </summary>
        /// <param name="parentNode">Node to append the Annotations node to; mandatory</param>
        /// <param name="annotations">List of the annotation info; mandatory</param>
        /// <param name="doc">XmlDocument used to create the XML entities; mandatory</param>
        private static void CreateAnnotationNodes(XmlNode parentNode, IList<AnnotationInfo> annotations, XmlDocument doc)
        {
            if (annotations.Count == 0)
            {
                return;
            }

            XmlNode annotationRootNode = doc.CreateElement(TAG_NAME_ANNOTATIONS);
            parentNode.AppendChild(annotationRootNode);

            foreach (AnnotationInfo annotation in annotations)
            {
                XmlNode annotationNode = doc.CreateElement(TAG_NAME_ANNOTATION);
                annotationRootNode.AppendChild(annotationNode);
                AppendAttribute(annotationNode, "type", annotation.AnnotationType, doc);

                IList<AnnotationParamInfo> parameters = annotation.Parameters;
                foreach (AnnotationParamInfo parameter in parameters)
                {
                    XmlNode paramNode = doc.CreateElement(TAG_NAME_PARAMETER);
                    annotationNode.AppendChild(paramNode);
                    AppendAttribute(paramNode, "name", parameter.Name, doc);
                    AppendAttribute(paramNode, "type", parameter.Type, doc);

                    Object defaultValue = parameter.DefaultValue;
                    if (defaultValue != null)
                    {
                        XmlNode defaultValueNode = doc.CreateElement(TAG_NAME_DEFAULT_VALUE);
                        paramNode.AppendChild(defaultValueNode);
                        String defaultValueString = defaultValue.ToString();
                        if ("\u0000".Equals(defaultValueString))
                        {
                            // Workaround for a character that is illegal in xml
                            defaultValueString = "\\u0000";
                        }
                        defaultValueNode.InnerText = defaultValueString;
                    }

                    Object currentValue = parameter.CurrentValue;
                    if (currentValue != defaultValue && (currentValue == null || !currentValue.Equals(defaultValue)))
                    {
                        XmlNode currentValueNode = doc.CreateElement(TAG_NAME_CURRENT_VALUE);
                        paramNode.AppendChild(currentValueNode);
                        if (currentValue != null)
                        {
                            currentValueNode.InnerText = currentValue.ToString();
                        }
                        else
                        {
                            AppendAttribute(currentValueNode, "isNull", "true", doc);
                        }
                    }
                }
            }
        }

        private static void AppendAttribute(XmlNode parent, string attributeName, string attributeValue, XmlDocument doc)
        {
            XmlAttribute attribute = doc.CreateAttribute(attributeName);
            attribute.InnerText = attributeValue;
            parent.Attributes.Append(attribute);
        }

        #endregion

    }
}
