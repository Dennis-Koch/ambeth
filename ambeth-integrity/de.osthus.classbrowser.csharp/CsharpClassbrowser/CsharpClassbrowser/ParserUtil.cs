﻿using System;
using System.CodeDom;
using System.Collections.Generic;
using System.Reflection;
using Microsoft.CSharp;
using System.Text.RegularExpressions;

namespace CsharpClassbrowser
{
    /// <summary>
    /// Helper class which supports the parsing of the assemblies and offers some convenience methods.
    /// </summary>
    public static class ParserUtil
    {

        // ============================================================================================
        #region Constants
        // ============================================================================================

        public const string TYPE_CLASS = "class";
        public const string TYPE_DELEGATE = "delegate";
        public const string TYPE_ENUM = "enum";
        public const string TYPE_INTERFACE = "interface";
        public const string TYPE_ATTRIBUTE = "annotation";

        public const string MODIFIER_PUBLIC = "public";
        public const string MODIFIER_PROTECTED = "protected";
        public const string MODIFIER_PRIVATE = "private";
        public const string MODIFIER_STATIC = "static";
        public const string MODIFIER_VIRTUAL = "virtual";
        public const string MODIFIER_FINAL = "final";
        public const string MODIFIER_ABSTRACT = "abstract";
        public const string MODIFIER_INTERNAL = "internal";

        public const string GENERIC_TYPE_PREFIX = "generic:";

        public static readonly Regex backingFieldPattern = new Regex("<(.+)>k__BackingField");

        #endregion

        // ============================================================================================
        #region Methods
        // ============================================================================================

        /// <summary>
        /// Analyze the given assemblies.
        /// </summary>
        /// <param name="assemblies">List of assemblies; optional</param>
        /// <param name="moduleMap">Dictionary with the module name of each class file or null (then the assembly is used to get the module 
        /// information)); key is the full qualified class name in LOWER CASE and value the module name</param>
        /// <returns>A list of found types as TypeDescription entities; never null (but may be empty)</returns>
        public static IList<TypeDescription> AnalyzeAssemblies(IList<Assembly> assemblies, IDictionary<string, string> moduleMap)
        {
            IList<TypeDescription> foundTypes = new List<TypeDescription>();
            if (assemblies == null)
            {
                return foundTypes;
            }

            foreach (var assembly in assemblies)
            {
                try
                {
                    string source = GetSource(assembly);
                    foreach (Type type in assembly.GetTypes())
                    {
                        var typeType = ParserUtil.GetTypeType(type);
                        if (typeType != null && !IsTypeSkipped(type))
                        {
                            int genericTypeParams = CountGenericTypeParams(type);
                            string simpleName = RemoveGenericTypeParamsFromName(type.Name);
                            string fullTypeName = RemoveGenericTypeParamsFromName(type.FullName);
                            string moduleName;
                            if (moduleMap != null)
                            {
                                moduleName = GetModuleFromSources(type, moduleMap);
                            }
                            else
                            {
                                moduleName = GetModuleFromAssembly(source);
                            }
                            TypeDescription typeDescription = new TypeDescription(source, moduleName, type.Namespace, simpleName, fullTypeName, typeType, genericTypeParams);
                            AddAnnotations(type, typeDescription);
                            AddMethodDescriptions(type, typeDescription);
                            AddFieldDescriptions(type, typeDescription);
                            log(typeDescription);
                            foundTypes.Add(typeDescription);
                        }
                    }
                }
                catch (Exception ex)
                {
                    Console.WriteLine("Assembly '" + assembly.FullName + "' can't be parsed due to: " + ex.Message);
                }
            }

            return foundTypes;
        }

        /// <summary>
        /// Check if the given type has to be skipped.
        /// </summary>
        /// <param name="typeToBeAnalyzed">Type to check; mandatory</param>
        /// <returns>True if class should be skipped</returns>
        private static bool IsTypeSkipped(Type typeToBeAnalyzed)
        {
            bool skip = false;
            if (typeToBeAnalyzed.IsNested || typeToBeAnalyzed.Name.ToLowerInvariant().Contains(".repackaged.") || typeToBeAnalyzed.Name.StartsWith("clrTest.reflection.", StringComparison.InvariantCultureIgnoreCase))
            {
                skip = true;
            }
            return skip;
        }

        /// <summary>
        /// Get the module where the type is coming from.
        /// </summary>
        /// <param name="typeToBeAnalyzed">Type to find</param>
        /// <param name="moduleMap">Dictionary with the module name of each class file; key is the full qualified class name in LOWER CASE and value the module name
        /// <returns>Module name or empty string (if no module can be found); never null</returns>
        public static String GetModuleFromSources(Type typeToBeAnalyzed, IDictionary<string, string> moduleMap)
        {
            if (typeToBeAnalyzed == null || moduleMap == null)
            {
                throw new ArgumentException("Mandatory value missing!");
            }

            string typeName = typeToBeAnalyzed.FullName.ToLower();
            // TODO: Remove the HACK
            string fileName = typeName.Replace("de.osthus.", string.Empty).ToLower() + ".cs";

            string moduleName;
            if (moduleMap.TryGetValue(fileName, out moduleName))
            {
                return moduleName;
            }

            return string.Empty;
        }

        /// <summary>
        /// Get the module where the type is coming from.
        /// </summary>
        /// <param name="assemblySourceFilePath">Full assembly source file name including the path</param>
        /// <returns>Module name or empty string (if no module can be found); never null</returns>
        public static String GetModuleFromAssembly(string assemblySourceFilePath)
        {
            if (string.IsNullOrWhiteSpace(assemblySourceFilePath))
            {
                throw new ArgumentException("Mandatory value missing!");
            }
            return System.IO.Path.GetFileNameWithoutExtension(assemblySourceFilePath);
        }

        /// <summary>
        /// Get the number of generic type parameters for a given type.
        /// </summary>
        /// <param name="type">Type to analyze</param>
        /// <returns>Number of generic type parameters</returns>
        private static int CountGenericTypeParams(Type type)
        {
            if (type != null)
            {
                var parts = type.Name.Split('`');
                if (parts.Length > 1)
                {
                    return Convert.ToInt32(parts[parts.Length - 1]);
                }
            }
            return 0;
        }

        /// <summary>
        /// Remove the generic type parameters from a given type name.
        /// </summary>
        /// <param name="typeName">Type name</param>
        /// <returns>Adapted type name</returns>
        private static string RemoveGenericTypeParamsFromName(string typeName)
        {
            if (typeName == null)
            {
                return null;
            }
            var parts = typeName.Split('`');
            return parts[0];
        }

        /// <summary>
        /// Get the source where the types are coming from.
        /// </summary>
        /// <param name="assembly">Assembly; mandatory</param>
        /// <returns>Source</returns>
        public static string GetSource(Assembly assembly)
        {
            if (assembly == null)
            {
                throw new ArgumentNullException("Assembly");
            }

            string codeBase = assembly.CodeBase;
            UriBuilder uri = new UriBuilder(codeBase);
            string path = Uri.UnescapeDataString(uri.Path);
            //return System.IO.Path.GetDirectoryName(path);
            return path;
        }

        /// <summary>
        /// Get the type (text constant) for the given type. Returns null if the type isn't supported and hasn't to be handled.
        /// </summary>
        /// <param name="givenType">Type to identify; mandatory</param>
        /// <returns>One of the TYPE constants (e.g. TYPE_CLASS) or null</returns>
        public static string GetTypeType(Type givenType)
        {
            if (givenType == null)
            {
                throw new ArgumentNullException("Type");
            }

            if (givenType.IsInterface)
            {
                return TYPE_INTERFACE;
            }
            else if (givenType.IsClass)
            {
                if (typeof(Attribute).IsAssignableFrom(givenType))
                {
                    return TYPE_ATTRIBUTE;
                }
                else if (typeof(Delegate).IsAssignableFrom(givenType))
                {
                    return TYPE_DELEGATE;
                }
                return TYPE_CLASS;
            }
            else if (givenType.IsEnum)
            {
                return TYPE_ENUM;
            }
            else
            {
                return null;
            }
        }

        /// <summary>
        /// Add all runtime visible attributes from the given type to the given description.
        /// </summary>
        /// <param name="givenType">Type to get the method infos from; mandatory</param>
        /// <param name="typeDescription">Description to write the attribute infos to; mandatory</param>
        private static void AddAnnotations(Type givenType, TypeDescription typeDescription)
        {
            GetAnnotationNames(givenType, typeDescription.Annotations);
        }

        /// <summary>
        /// Add all methods from the given type to the given description.
        /// </summary>
        /// <param name="givenType">Type to get the method infos from; mandatory</param>
        /// <param name="typeDescription">Description to write the method infos to; mandatory</param>
        public static void AddMethodDescriptions(Type givenType, TypeDescription typeDescription)
        {
            if (givenType == null || typeDescription == null)
            {
                throw new ArgumentNullException("Mandatory values for adding the method descriptions are missing!");
            }

            var methodInfos = givenType.GetMethods(BindingFlags.Instance | BindingFlags.Static | BindingFlags.Public | BindingFlags.NonPublic | BindingFlags.DeclaredOnly);
            if (methodInfos != null)
            {
                if (ParserUtil.TYPE_DELEGATE == typeDescription.TypeType)
                {
                    var methodDescription = CreateDelegateMethodDescriptionFrom(methodInfos);
                    typeDescription.MethodDescriptions.Add(methodDescription);
                }
                else
                {
                    foreach (var methodInfo in methodInfos)
                    {
                        var methodDescription = CreateMethodDescriptionFrom(methodInfo);
                        typeDescription.MethodDescriptions.Add(methodDescription);
                    }
                }
            }
        }

        /// <summary>
        /// Add all fields from the given type to the given description.
        /// </summary>
        /// <param name="givenType">Type to get the field infos from; mandatory</param>
        /// <param name="typeDescription">Description to write the field infos to; mandatory</param>
        public static void AddFieldDescriptions(Type givenType, TypeDescription typeDescription)
        {
            if (givenType == null || typeDescription == null)
            {
                throw new ArgumentNullException("Mandatory values for adding the field descriptions are missing!");
            }

            var fieldInfos = givenType.GetFields(BindingFlags.Instance | BindingFlags.Static | BindingFlags.Public | BindingFlags.NonPublic | BindingFlags.DeclaredOnly);
            if (fieldInfos != null)
            {
                bool isEnum = ParserUtil.TYPE_ENUM.Equals(typeDescription.TypeType);

                foreach (var fieldInfo in fieldInfos)
                {
                    IList<String> attributes = GetAnnotationNames(fieldInfo);

                    Match match = backingFieldPattern.Match(fieldInfo.Name);
                    if (match.Success)
                    {
                        // check for a .NET property of the field to map the annotations from there to the current field

                        String propertyName = match.Groups[1].Value;
                        PropertyInfo pi = givenType.GetProperty(propertyName);
                        if (pi == null)
                        {
                            throw new Exception("Property not found: " + givenType.FullName + "." + propertyName);
                        }
                        IList<String> propertyAttributes = GetAnnotationNames(pi);
                        foreach (String propertyAttribute in propertyAttributes)
                        {
                            attributes.Add(propertyAttribute);
                        }
                    }

                    bool isLoggerField = attributes.Contains("De.Osthus.Ambeth.Log.LogInstanceAttribute");
                    bool isAutowired = attributes.Contains("De.Osthus.Ambeth.Ioc.Annotation.AutowiredAttribute");

                    bool isProperty = attributes.Contains("De.Osthus.Ambeth.Config.PropertyAttribute");

                    // on enums only "recognize" the enum values and not the internal field to save the integer value
                    bool isEnumAndEnumConstant = isEnum && fieldInfo.IsStatic;

                    if (isAutowired || isLoggerField || isEnumAndEnumConstant || isProperty)
                    {
                        var fieldDescription = CreateFieldDescriptionFrom(fieldInfo);
                        IList<String> annotations = fieldDescription.Annotations;
                        foreach (String attribute in attributes)
                        {
                            annotations.Add(attribute);
                        }
                        typeDescription.FieldDescriptions.Add(fieldDescription);
                    }
                }
            }
        }

        /// <summary>
        /// Create the "dummy" method description for delegates.
        /// </summary>
        /// <param name="methodInfos">All method infos of the delegate class; mandatory</param>
        /// <returns>Single MethodDescription for the delegate</returns>
        public static MethodDescription CreateDelegateMethodDescriptionFrom(MethodInfo[] methodInfos)
        {
            if (methodInfos == null || methodInfos.Length != 3)
            {
                throw new ArgumentNullException("Mandatory values for creating a delegate method description are missing!");
            }

            string returnType = null;
            IList<string> attributes = null;
            IList<string> modifiers = null;
            IList<string> parameterTypes = null;
            foreach (var methodInfo in methodInfos)
            {
                if ("Invoke" == methodInfo.Name)
                {
                    returnType = GetReturnTypeFrom(methodInfo);
                    parameterTypes = GetParameterTypesFrom(methodInfo);
                    attributes = GetAnnotationNames(methodInfo);
                }
            }

            MethodDescription methodDescription = new MethodDescription("Invoke", returnType, modifiers, parameterTypes);
            if (attributes != null)
            {
                IList<String> annotations = methodDescription.Annotations;
                foreach (String attribute in attributes)
                {
                    annotations.Add(attribute);
                }
            }
            return methodDescription;
        }

        /// <summary>
        /// Create a method description from the given information.
        /// </summary>
        /// <param name="methodInfo">MethodInfo to get the infos from; mandatory</param>
        /// <returns>MethodDescription</returns>
        public static MethodDescription CreateMethodDescriptionFrom(MethodInfo methodInfo)
        {
            if (methodInfo == null)
            {
                throw new ArgumentNullException("Mandatory values for creating a method description are missing!");
            }

            var returnType = GetReturnTypeFrom(methodInfo);
            var modifiers = GetModifiersFrom(methodInfo);
            var parameterTypes = GetParameterTypesFrom(methodInfo);

            MethodDescription methodDescription = new MethodDescription(methodInfo.Name, returnType, modifiers, parameterTypes);
            GetAnnotationNames(methodInfo, methodDescription.Annotations);

            return methodDescription;
        }

        /// <summary>
        /// Create a field description from the given information.
        /// </summary>
        /// <param name="fieldInfo">FieldInfo to get the infos from; mandatory</param>
        /// <returns>FieldDescription</returns>
        public static FieldDescription CreateFieldDescriptionFrom(FieldInfo fieldInfo)
        {
            if (fieldInfo == null)
            {
                throw new ArgumentNullException("Mandatory values for creating a field description are missing!");
            }

            var fieldType = GetTypeFrom(fieldInfo.FieldType);
            var modifiers = GetModifiersFrom(fieldInfo);

            return new FieldDescription(fieldInfo.Name, fieldType, modifiers);
        }

        /// <summary>
        /// Get the text representation of the methods return type.
        /// </summary>
        /// <param name="methodInfo">MethodInfo to get the infos from; mandatory</param>
        /// <returns>Return type as text</returns>
        public static string GetReturnTypeFrom(MethodInfo methodInfo)
        {
            if (methodInfo == null)
            {
                throw new ArgumentNullException("Mandatory values to get the return type are missing!");
            }

            return GetTypeFrom(methodInfo.ReturnType);
        }

        /// <summary>
        /// Get the text representation of the given type.
        /// </summary>
        /// <param name="givenType">Type to get the infos from; mandatory</param>
        /// <returns>Type as text</returns>
        public static string GetTypeFrom(Type givenType)
        {
            if (givenType == null)
            {
                throw new ArgumentNullException("Mandatory values to get the type are missing!");
            }

            //string result = givenType.Namespace + "." + givenType.Name;
            //if (givenType.IsGenericParameter)
            //{
            //    result = GENERIC_TYPE_PREFIX + result;
            //}
            //else if (givenType.IsGenericType)
            //{
            //    Type[] typeParameters = givenType.GetGenericArguments();
            //    IList<string> paramResults = new List<string>(typeParameters.Length);
            //    foreach (Type tParam in typeParameters)
            //    {
            //        paramResults.Add(GetTypeFrom(tParam));
            //    }
            //    result += "[" + string.Join(",", paramResults) + "]";
            //}
            //return result;

            using (var codeProvider = new CSharpCodeProvider())
            {
                var codeTypeReference = new CodeTypeReference(givenType);
                var result = codeProvider.GetTypeOutput(codeTypeReference);
                //if (result.StartsWith("System.Nullable"))
                //{
                //    result = result.Substring("System.Nullable".Length + 1);
                //    result = result.Replace(">", "?");
                //}

                return result;
            }
        }

        /// <summary>
        /// Get the text representation of all method modifiers.
        /// </summary>
        /// <param name="methodInfo">MethodInfo to get the infos from; mandatory</param>
        /// <returns>List of modifiers; never null (but may be empty)</returns>
        public static IList<string> GetModifiersFrom(MethodInfo methodInfo)
        {
            if (methodInfo == null)
            {
                throw new ArgumentNullException("Mandatory values to get the method modifiers are missing!");
            }

            IList<string> modifiers = new List<string>();
            if (methodInfo.IsPublic)
            {
                modifiers.Add(MODIFIER_PUBLIC);
            }
            if (methodInfo.IsFamily)
            {
                modifiers.Add(MODIFIER_PROTECTED);
            }
            if (methodInfo.IsFamilyOrAssembly)
            {
                // TODO: verify because this is protected OR internal -> maybe skip the restrictive one (internal)
                modifiers.Add(MODIFIER_PROTECTED);
                modifiers.Add(MODIFIER_INTERNAL);
            }
            if (methodInfo.IsAssembly)
            {
                modifiers.Add(MODIFIER_INTERNAL);
            }
            if (methodInfo.IsPrivate)
            {
                modifiers.Add(MODIFIER_PRIVATE);
            }
            if (methodInfo.IsStatic)
            {
                modifiers.Add(MODIFIER_STATIC);
            }
            if (methodInfo.IsVirtual)
            {
                modifiers.Add(MODIFIER_VIRTUAL);
            }
            if (methodInfo.IsAbstract)
            {
                modifiers.Add(MODIFIER_ABSTRACT);
            }

            return modifiers;
        }

        /// <summary>
        /// Get the text representation of all field modifiers.
        /// </summary>
        /// <param name="fieldInfo">FieldInfo to get the infos from; mandatory</param>
        /// <returns>List of modifiers; never null (but may be empty)</returns>
        public static IList<string> GetModifiersFrom(FieldInfo fieldInfo)
        {
            if (fieldInfo == null)
            {
                throw new ArgumentNullException("Mandatory values to get the field modifiers are missing!");
            }

            IList<string> modifiers = new List<string>();
            if (fieldInfo.IsPublic)
            {
                modifiers.Add(MODIFIER_PUBLIC);
            }
            if (fieldInfo.IsFamily)
            {
                modifiers.Add(MODIFIER_PROTECTED);
            }
            if (fieldInfo.IsFamilyOrAssembly)
            {
                // TODO: verify because this is protected OR internal -> maybe skip the restrictive one (internal)
                modifiers.Add(MODIFIER_PROTECTED);
                modifiers.Add(MODIFIER_INTERNAL);
            }
            if (fieldInfo.IsAssembly)
            {
                modifiers.Add(MODIFIER_INTERNAL);
            }
            if (fieldInfo.IsPrivate)
            {
                modifiers.Add(MODIFIER_PRIVATE);
            }
            if (fieldInfo.IsStatic)
            {
                modifiers.Add(MODIFIER_STATIC);
            }

            return modifiers;
        }

        /// <summary>
        /// Get the text representation of all method parameter types.
        /// </summary>
        /// <param name="methodInfo">MethodInfo to get the infos from; mandatory</param>
        /// <returns>List of parameter types; never null (but may be empty)</returns>
        public static IList<string> GetParameterTypesFrom(MethodInfo methodInfo)
        {
            if (methodInfo == null)
            {
                throw new ArgumentNullException("Mandatory values to get the parameter types are missing!");
            }

            IList<string> parameters = new List<string>();
            foreach (var parameter in methodInfo.GetParameters())
            {
                var paramType = GetTypeFrom(parameter.ParameterType);
                parameters.Add(paramType);
            }

            return parameters;
        }

        private static string Capitalize(string text)
        {
            System.Globalization.CultureInfo cultureInfo = System.Threading.Thread.CurrentThread.CurrentCulture;
            return cultureInfo.TextInfo.ToTitleCase(text);
        }

        /// <summary>
        /// Extracts the full names of all annotations present on the annotated element.
        /// </summary>
        /// <param name="attributeProvider">Object to be analysed; mandatory</param>
        /// <returns>List of the full annotation names; never null (but may be empty)</returns>
        private static List<String> GetAnnotationNames(MemberInfo attributeProvider)
        {
            List<String> annotationNames = new List<String>();
            GetAnnotationNames(attributeProvider, annotationNames);
            return annotationNames;
        }

        // FIXME Attribute name is always 'System.Runtime.CompilerServices.CompilerGeneratedAttribute'
        private static void GetAnnotationNames(MemberInfo attributeProvider, IList<String> target)
        {
            Object[] attributes = attributeProvider.GetCustomAttributes(true);
            foreach (Object attr in attributes)
            {
                Attribute attribute = (Attribute)attr;
                String name = attribute.GetType().FullName;
                if (!"System.Runtime.CompilerServices.CompilerGeneratedAttribute".Equals(name))
                {
                    target.Add(name);
                }
            }
        }

        private static void log(TypeDescription typeDescription)
        {
            log("============================================================");
            log(Capitalize(typeDescription.TypeType) + " found in '" + typeDescription.Source + "'. Full name is '" + typeDescription.FullTypeName + "'.");
            foreach (var methodDescription in typeDescription.MethodDescriptions)
            {
                string modfifierText = string.Join(" ", methodDescription.Modifiers);
                if (!string.IsNullOrWhiteSpace(modfifierText))
                {
                    modfifierText = "[" + modfifierText + "] ";
                }
                string paramText = string.Join(" ", methodDescription.ParameterTypes);
                if (!string.IsNullOrWhiteSpace(paramText))
                {
                    paramText = " (" + paramText + ")";
                }
                log("Method " + modfifierText + methodDescription.Name + paramText + " returns " + methodDescription.ReturnType);
            }
        }

        private static void log(String text)
        {
            System.Diagnostics.Trace.TraceInformation(text);
        }

        #endregion

    }
}
