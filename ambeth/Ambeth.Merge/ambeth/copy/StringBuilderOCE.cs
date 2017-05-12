using System.Text;
using System;

namespace De.Osthus.Ambeth.Copy
{
    /// <summary>
    /// Allows to copy a StringBuilder instance efficiently
    /// </summary>
    public class StringBuilderOCE : IObjectCopierExtension
    {
        public Object DeepClone(Object original, IObjectCopierState objectCopierState)
        {
            StringBuilder sb = (StringBuilder)original;
            return new StringBuilder(sb.ToString());
        }
    }
}