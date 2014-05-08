using System;
using System.Collections.Generic;

namespace CsharpClassbrowser
{
    /// <summary>
    /// Holds the description for types.
    /// </summary>
    public class TypeDescription : CsharpClassbrowser.INamed
    {

        // ============================================================================================
        #region Constructors
        // ============================================================================================

        /// <summary>
        /// Create a new instance.
        /// </summary>
        /// <param name="source">Source; mandatory</param>
        /// <param name="moduleName">Module name; mandatory (but may be empty if the module can't be found)</param>
        /// <param name="namespaceName">Namespace name; optional</param>
        /// <param name="typeName">Short name of the type; mandatory</param>
        /// <param name="fullTypeName">Full name of the type including the namespace; mandatory</param>
        /// <param name="typeType">Type constant; mandatory</param>
        /// <param name="genericTypeParams">Number of generic type parameters</param>
        public TypeDescription(string source, string moduleName, string namespaceName, string typeName, string fullTypeName, string typeType, int genericTypeParams)
        {
            if (string.IsNullOrWhiteSpace(source) || string.IsNullOrWhiteSpace(typeName) || string.IsNullOrWhiteSpace(fullTypeName) ||
                string.IsNullOrWhiteSpace(typeType) || moduleName == null)
            {
                throw new ArgumentNullException("Mandatory type description value missing!");
            }
            this.Source = source;
            this.ModuleName = moduleName;
            this.TypeType = typeType;
            this.NamespaceName = namespaceName;
            this.Name = typeName;
            this.FullTypeName = fullTypeName;
            this.GenericTypeParams = genericTypeParams;
            this.Annotations = new List<String>();
            this.MethodDescriptions = new List<MethodDescription>();
            this.FieldDescriptions = new List<FieldDescription>();
        }

        #endregion

        // ============================================================================================
        #region Properties
        // ============================================================================================

        public string Source { get; internal set; }
        public string ModuleName { get; internal set; }
        public string TypeType { get; internal set; }
        public string NamespaceName { get; internal set; }
        public string Name { get; internal set; }
        public string FullTypeName { get; internal set; }
        public int GenericTypeParams { get; internal set; }
        public IList<String> Annotations { get; internal set; }
        public IList<MethodDescription> MethodDescriptions { get; internal set; }
        public IList<FieldDescription> FieldDescriptions { get; internal set; }

        #endregion

    }
}
