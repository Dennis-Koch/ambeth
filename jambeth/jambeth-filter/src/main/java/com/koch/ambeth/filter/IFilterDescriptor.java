package com.koch.ambeth.filter;

import java.util.List;

import com.koch.ambeth.util.annotation.XmlType;

@XmlType
public interface IFilterDescriptor<T>
{
	Class<T> getEntityType();

	String getMember();

	List<String> getValue();

	Boolean isCaseSensitive();

	FilterOperator getOperator();

	LogicalOperator getLogicalOperator();

	List<IFilterDescriptor<T>> getChildFilterDescriptors();
}
