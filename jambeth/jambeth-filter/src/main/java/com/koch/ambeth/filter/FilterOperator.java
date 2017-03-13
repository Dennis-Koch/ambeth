package com.koch.ambeth.filter;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

@XmlType
@XmlEnum
public enum FilterOperator
{

	/**
	 * Left operand must end with the right one
	 * 
	 */
	@XmlEnumValue("EndsWith")
	ENDS_WITH("EndsWith"),

	/**
	 * Left operand must not be contained in the right one
	 * 
	 */
	@XmlEnumValue("IsNotContainedIn")
	IS_NOT_CONTAINED_IN("IsNotContainedIn"),

	/**
	 * Left operand must be different from the right one
	 * 
	 */
	@XmlEnumValue("IsNotEqualTo")
	IS_NOT_EQUAL_TO("IsNotEqualTo"),

	/**
	 * Left operand must start with the right one
	 * 
	 */
	@XmlEnumValue("StartsWith")
	STARTS_WITH("StartsWith"),

	/**
	 * Left operand must be equal to the right one
	 * 
	 */
	@XmlEnumValue("IsEqualTo")
	IS_EQUAL_TO("IsEqualTo"),

	/**
	 * Left operand must contain the right one
	 * 
	 */
	@XmlEnumValue("Contains")
	CONTAINS("Contains"),

	/**
	 * Left operand must be larger than or equal to the right one
	 * 
	 */
	@XmlEnumValue("IsGreaterThan")
	IS_GREATER_THAN("IsGreaterThan"),

	/**
	 * Left operand must not contain the right one
	 * 
	 */
	@XmlEnumValue("DoesNotContain")
	DOES_NOT_CONTAIN("DoesNotContain"),

	/**
	 * Left operand must be contained in the right one
	 * 
	 */
	@XmlEnumValue("IsContainedIn")
	IS_CONTAINED_IN("IsContainedIn"),

	/**
	 * Left operand is checked against right incl. wildcards
	 * 
	 */
	@XmlEnumValue("Like")
	LIKE("Like"),

	/**
	 * Left operand must be smaller than or equal to the right one
	 * 
	 */
	@XmlEnumValue("IsLessThanOrEqualTo")
	IS_LESS_THAN_OR_EQUAL_TO("IsLessThanOrEqualTo"),

	/**
	 * Left operand must be larger than the right one
	 * 
	 */
	@XmlEnumValue("IsGreaterThanOrEqualTo")
	IS_GREATER_THAN_OR_EQUAL_TO("IsGreaterThanOrEqualTo"),

	/**
	 * Left operand must be contained in the enumerated right one
	 * 
	 */
	@XmlEnumValue("IsIn")
	IS_IN("IsIn"),

	/**
	 * Left operand must not be contained in the enumerated right one
	 * 
	 */
	@XmlEnumValue("IsNotIn")
	IS_NOT_IN("IsNotIn"),

	/**
	 * Left operand must be smaller than the right one
	 * 
	 */
	@XmlEnumValue("IsLessThan")
	IS_LESS_THAN("IsLessThan"),

	@XmlEnumValue("IsNull")
	IS_NULL("IsNull"),

	@XmlEnumValue("IsNotNull")
	IS_NOT_NULL("IsNotNull"),

	@XmlEnumValue("FullText")
	FULL_TEXT("FullText");

	private final String value;

	FilterOperator(String v)
	{
		value = v;
	}

	public String value()
	{
		return value;
	}
}
