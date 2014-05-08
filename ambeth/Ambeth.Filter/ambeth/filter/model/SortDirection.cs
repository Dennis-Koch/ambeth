using System;
using De.Osthus.Ambeth.Annotation;

namespace De.Osthus.Ambeth.Filter.Model
{
    [XmlType(Name = "SortDirection", Namespace = "http://schemas.osthus.de/Ambeth")]
    public enum SortDirection
    {
        /**
         * result is sorted descending
         * 
         */
        [System.Xml.Serialization.XmlEnum("Descending")]
        DESCENDING,

        /**
         * result is sorted ascending
         * 
         */
        [System.Xml.Serialization.XmlEnum("Ascending")]
        ASCENDING
    }
}