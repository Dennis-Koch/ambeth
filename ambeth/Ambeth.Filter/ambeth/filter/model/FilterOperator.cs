using System;
using De.Osthus.Ambeth.Annotation;

namespace De.Osthus.Ambeth.Filter.Model
{
    [XmlType(Name = "FilterOperator", Namespace = "http://schemas.osthus.de/Ambeth")]
    public enum FilterOperator
    {
        /**
         * Left operand must end with the right one
         * 
         */
        [System.Xml.Serialization.XmlEnum("EndsWith")]
        ENDS_WITH,

        /**
         * Left operand must not be contained in the right one
         * 
         */
        [System.Xml.Serialization.XmlEnum("IsNotContainedIn")]
        IS_NOT_CONTAINED_IN,

        /**
         * Left operand must be different from the right one
         * 
         */
        [System.Xml.Serialization.XmlEnum("IsNotEqualTo")]
        IS_NOT_EQUAL_TO,

        /**
         * Left operand must start with the right one
         * 
         */
        [System.Xml.Serialization.XmlEnum("STARTS_WITH")]
        STARTS_WITH,

        /**
         * Left operand must be equal to the right one
         * 
         */
        [System.Xml.Serialization.XmlEnum("IsEqualTo")]
        IS_EQUAL_TO,

        /**
         * Left operand must contain the right one
         * 
         */
        [System.Xml.Serialization.XmlEnum("Contains")]
        CONTAINS,

        /**
         * Left operand must be larger than or equal to the right one
         * 
         */
        [System.Xml.Serialization.XmlEnum("IsGreaterThan")]
        IS_GREATER_THAN,

        /**
         * Left operand must not contain the right one
         * 
         */
        [System.Xml.Serialization.XmlEnum("DoesNotContain")]
        DOES_NOT_CONTAIN,

        /**
         * Left operand must be contained in the right one
         * 
         */
        [System.Xml.Serialization.XmlEnum("IsContainedIn")]
        IS_CONTAINED_IN,

        /**
         * Left operand is checked against right incl. wildcards
         * 
         */
        [System.Xml.Serialization.XmlEnum("Like")]
        LIKE,

        /**
         * Left operand must be smaller than or equal to the right one
         * 
         */
        [System.Xml.Serialization.XmlEnum("IsLessThanOrEqualTo")]
        IS_LESS_THAN_OR_EQUAL_TO,

        /**
         * Left operand must be larger than the right one
         * 
         */
        [System.Xml.Serialization.XmlEnum("IsGreaterThanOrEqualTo")]
        IS_GREATER_THAN_OR_EQUAL_TO,

       	/**
	     * Left operand must be contained in the enumerated right one
	     * 
	     */
	    [System.Xml.Serialization.XmlEnum("IsIn")]
	    IS_IN,

	    /**
	     * Left operand must not be contained in the enumerated right one
	     * 
	     */
	    [System.Xml.Serialization.XmlEnum("IsNotIn")]
	    IS_NOT_IN,

        /**
         * Left operand must be smaller than the right one
         * 
         */
        [System.Xml.Serialization.XmlEnum("IsLessThan")]
        IS_LESS_THAN,

        [System.Xml.Serialization.XmlEnum("IsNull")]
        IS_NULL,

        [System.Xml.Serialization.XmlEnum("IsNotNull")]
        IS_NOT_NULL,

        [System.Xml.Serialization.XmlEnum("FullText")]
        FULL_TEXT
    }
}
