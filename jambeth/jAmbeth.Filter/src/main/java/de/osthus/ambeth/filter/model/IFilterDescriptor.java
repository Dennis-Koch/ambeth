package de.osthus.ambeth.filter.model;

import java.util.List;

import de.osthus.ambeth.annotation.XmlType;

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
