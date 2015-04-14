using System;

namespace CsharpClassbrowser
{
    /// <summary>
    /// Holds the information for annotation params.
    /// </summary>
    public class AnnotationParamInfo : INamed
    {

        // ============================================================================================
        #region Constructors
        // ============================================================================================

        /// <summary>
        /// Create a new instance.
        /// </summary>
        /// <param name="returnType">Annotation type; mandatory</param>
        /// <param name="parameterTypes">Parameters; mandatory</param>
        public AnnotationParamInfo(string name, string type, object defaultValue, object currentValue)
        {
            if (string.IsNullOrWhiteSpace(name) || string.IsNullOrWhiteSpace(type))
            {
                throw new ArgumentNullException("Mandatory annotation param info value missing!");
            }

            this.Name = name;
            this.Type = type;
            this.DefaultValue = defaultValue;
            this.CurrentValue = currentValue;
        }

        #endregion

        // ============================================================================================
        #region Properties
        // ============================================================================================

        public string Name { get; internal set; }
        public string Type { get; internal set; }
        public object DefaultValue { get; internal set; }
        public object CurrentValue { get; internal set; }

        #endregion

    }
}
