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

            IList<String> annotations = typeDescription.Annotations;
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

            XmlAttribute nameAttribute = doc.CreateAttribute("MethodName");
            nameAttribute.InnerText = methodDescription.Name;
            methodNode.Attributes.Append(nameAttribute);

            XmlAttribute returnTypeAttribute = doc.CreateAttribute("ReturnType");
            returnTypeAttribute.InnerText = methodDescription.ReturnType;
            methodNode.Attributes.Append(returnTypeAttribute);

            IList<String> annotations = methodDescription.Annotations;
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

            foreach (var parameterType in methodDescription.ParameterTypes)
            {
                XmlNode parameterTypeNode = doc.CreateElement("MethodParameterType");
                parameterTypeNode.InnerText = parameterType;
                parameterTypesRootNode.AppendChild(parameterTypeNode);
            }

            return methodNode;
        }


        /// <summary>
        /// Create the XMLNode for the field.
        /// </summary>
        /// <param name="doc">XmlDocument used to create the XML entities; mandatory</param>
        /// <param name="fieldDescription">FieldDescription to get the information from; mandatory</param>
        /// <returns>XmlNode with the field infos</returns>
        public static XmlNode CreateFieldNode(XmlDocument doc, FieldDescription fieldDescription)
        {
            if (doc == null || fieldDescription == null)
            {
                throw new ArgumentNullException("Mandatory values for creating the field node are missing!");
            }

            XmlNode fieldNode = doc.CreateElement("FieldDescription");

            XmlAttribute nameAttribute = doc.CreateAttribute("FieldName");
            nameAttribute.InnerText = fieldDescription.Name;
            fieldNode.Attributes.Append(nameAttribute);

            XmlAttribute fieldTypeAttribute = doc.CreateAttribute("FieldType");
            fieldTypeAttribute.InnerText = fieldDescription.FieldType;
            fieldNode.Attributes.Append(fieldTypeAttribute);

            IList<String> annotations = fieldDescription.Annotations;
            CreateAnnotationNodes(fieldNode, annotations, doc);

            XmlNode modifiersRootNode = doc.CreateElement("FieldModifiers");
            fieldNode.AppendChild(modifiersRootNode);

            foreach (var modifier in fieldDescription.Modifiers)
            {
                XmlNode modifierNode = doc.CreateElement("FieldModifier");
                modifierNode.InnerText = modifier;
                modifiersRootNode.AppendChild(modifierNode);
            }

            return fieldNode;
        }

        /// <summary>
        /// Create the XML nodes for the annotations.
        /// </summary>
        /// <param name="parentNode">Node to append the Annotations node to; mandatory</param>
        /// <param name="annotations">List of the annotation names; mandatory</param>
        /// <param name="doc">XmlDocument used to create the XML entities; mandatory</param>
        private static void CreateAnnotationNodes(XmlNode parentNode, IList<String> annotations, XmlDocument doc)
        {
            if (annotations.Count == 0)
            {
                return;
            }

            XmlNode annotationRootNode = doc.CreateElement(TAG_NAME_ANNOTATIONS);
            parentNode.AppendChild(annotationRootNode);

            foreach (String annotation in annotations)
            {
                XmlNode annotationNode = doc.CreateElement(TAG_NAME_ANNOTATION);
                annotationNode.InnerText = annotation;
                annotationRootNode.AppendChild(annotationNode);
            }
        }

        #endregion

    }
}
