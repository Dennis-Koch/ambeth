using System;
using De.Osthus.Ambeth.Annotation;

namespace De.Osthus.Ambeth.Filter.Model
{
    [XmlType(Name = "LogicalOperator", Namespace = "http://schemas.osthus.de/Ambeth")]
    public enum LogicalOperator
    {
        /**
         * OR logic for the combination of filters
         * 
         */
        [System.Xml.Serialization.XmlEnum("Or")]
        OR,

        /**
         * AND logic for the combination of filters
         * 
         */
        [System.Xml.Serialization.XmlEnum("And")]
        AND
    }
}