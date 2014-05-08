using System;
using System.Collections.Generic;

namespace CsharpClassbrowser
{
    /// <summary>
    /// Holds the description for methods.
    /// </summary>
    public class MethodDescription : INamed
    {

        // ============================================================================================
        #region Constructors
        // ============================================================================================

        /// <summary>
        /// Create a new instance.
        /// </summary>
        /// <param name="methodName">Method name; mandatory</param>
        /// <param name="returnType">Return type; mandatory</param>
        /// <param name="modifiers">Modifiers; may be null</param>
        /// <param name="parameterTypes">Parameters types; may be null</param>
        public MethodDescription(string methodName, string returnType, IList<string> modifiers, IList<string> parameterTypes)
        {
            if (string.IsNullOrWhiteSpace(methodName) || string.IsNullOrWhiteSpace(returnType))
            {
                throw new ArgumentNullException("Mandatory method description value missing!");
            }
            this.Name = methodName;
            this.ReturnType = returnType;
            this.Annotations = new List<String>();
            this.Modifiers = modifiers == null ? new List<string>() : modifiers;
            this.ParameterTypes = parameterTypes == null ? new List<string>() : parameterTypes;
        }

        #endregion

        // ============================================================================================
        #region Properties
        // ============================================================================================

        public string Name { get; internal set; }
        public string ReturnType { get; internal set; }
        public IList<String> Annotations { get; internal set; }
        public IList<String> Modifiers { get; internal set; }
        public IList<String> ParameterTypes { get; internal set; }

        #endregion

    }
}
