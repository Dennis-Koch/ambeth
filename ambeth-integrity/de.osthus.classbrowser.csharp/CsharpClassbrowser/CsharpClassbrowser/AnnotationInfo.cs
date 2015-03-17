using System;
using System.Collections.Generic;

namespace CsharpClassbrowser
{
    /// <summary>
    /// Holds the information for annotations.
    /// </summary>
    public class AnnotationInfo
    {

        // ============================================================================================
        #region Constructors
        // ============================================================================================

        /// <summary>
        /// Create a new instance.
        /// </summary>
        /// <param name="returnType">Annotation type; mandatory</param>
        /// <param name="parameterTypes">Parameters; mandatory</param>
        public AnnotationInfo(string annotationType, IList<AnnotationParamInfo> parameters)
        {
            if (string.IsNullOrWhiteSpace(annotationType))
            {
                throw new ArgumentNullException("Mandatory annotation info value missing!");
            }

            this.AnnotationType = annotationType;
            this.Parameters = parameters;
        }

        #endregion

        // ============================================================================================
        #region Properties
        // ============================================================================================

        public string AnnotationType { get; internal set; }
        public IList<AnnotationParamInfo> Parameters { get; internal set; }

        #endregion

    }
}
