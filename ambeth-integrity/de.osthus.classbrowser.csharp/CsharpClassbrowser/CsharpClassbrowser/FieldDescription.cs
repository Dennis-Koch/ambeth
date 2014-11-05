using System;
using System.Collections.Generic;

namespace CsharpClassbrowser
{
    /// <summary>
    /// Holds the description for fields.
    /// </summary>
    public class FieldDescription : INamed
    {

        // ============================================================================================
        #region Constructors
        // ============================================================================================

        /// <summary>
        /// Create a new instance.
        /// </summary>
        /// <param name="fieldName">Field name; mandatory</param>
        /// <param name="fieldType">Field type; mandatory</param>
        /// <param name="modifiers">Modifiers; may be null</param>
        public FieldDescription(string fieldName, string fieldType, IList<string> modifiers)
        {
            if (string.IsNullOrWhiteSpace(fieldName) || string.IsNullOrWhiteSpace(fieldType))
            {
                throw new ArgumentNullException("Mandatory method description value missing!");
            }
            this.Name = fieldName;
            this.FieldType = fieldType;
            this.Annotations = new List<AnnotationInfo>();
            this.Modifiers = modifiers == null ? new List<string>() : modifiers;
        }

        #endregion

        // ============================================================================================
        #region Properties
        // ============================================================================================

        public string Name { get; internal set; }
        public string FieldType { get; internal set; }
        public IList<AnnotationInfo> Annotations { get; internal set; }
        public IList<String> Modifiers { get; internal set; }

        #endregion

    }
}
