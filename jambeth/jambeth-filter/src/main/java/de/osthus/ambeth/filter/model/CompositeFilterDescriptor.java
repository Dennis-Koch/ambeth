package de.osthus.ambeth.filter.model;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import de.osthus.ambeth.collections.EmptyList;

@XmlType
@XmlAccessorType(XmlAccessType.FIELD)
public class CompositeFilterDescriptor<T> implements IFilterDescriptor<T>
{
	@XmlElement
	protected Class<T> entityType;

	@XmlElement
	protected LogicalOperator logicalOperator;

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@XmlElement
	protected List<IFilterDescriptor<T>> childFilterDescriptors = (List) EmptyList.createTypedEmptyList(IFilterDescriptor.class);

	public CompositeFilterDescriptor()
	{
		// Intended blank. For XML serialization usage only
	}

	public CompositeFilterDescriptor(Class<T> entityType)
	{
		setEntityType(entityType);
	}

	@Override
	public Class<T> getEntityType()
	{
		return entityType;
	}

	public void setEntityType(Class<T> entityType)
	{
		this.entityType = entityType;
	}

	@Override
	public LogicalOperator getLogicalOperator()
	{
		return logicalOperator;
	}

	public void setLogicalOperator(LogicalOperator logicalOperator)
	{
		this.logicalOperator = logicalOperator;
	}

	public CompositeFilterDescriptor<T> withLogicalOperator(LogicalOperator logicalOperator)
	{
		setLogicalOperator(logicalOperator);
		return this;
	}

	@Override
	public List<IFilterDescriptor<T>> getChildFilterDescriptors()
	{
		return childFilterDescriptors;
	}

	public void setChildFilterDescriptors(List<IFilterDescriptor<T>> childFilterDescriptors)
	{
		this.childFilterDescriptors = childFilterDescriptors;
	}

	public CompositeFilterDescriptor<T> withChildFilterDescriptors(List<IFilterDescriptor<T>> childFilterDescriptors)
	{
		setChildFilterDescriptors(childFilterDescriptors);
		return this;
	}

	@Override
	public String getMember()
	{
		return null;
	}

	@Override
	public FilterOperator getOperator()
	{
		return null;
	}

	@Override
	public List<String> getValue()
	{
		return null;
	}

	@Override
	public Boolean isCaseSensitive()
	{
		return null;
	}
}
