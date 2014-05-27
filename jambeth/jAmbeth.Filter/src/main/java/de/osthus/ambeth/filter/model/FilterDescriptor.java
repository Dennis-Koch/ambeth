package de.osthus.ambeth.filter.model;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * The FilterDescriptor is used for querying filtered results
 * 
 * <p>
 * Java class for FilterDescriptorType complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 */
@XmlType
@XmlAccessorType(XmlAccessType.FIELD)
public class FilterDescriptor<T> implements IFilterDescriptor<T>
{

	@XmlElement
	protected Class<T> entityType;

	@XmlElement
	protected String member;

	@XmlElement
	protected List<String> value;

	@XmlElement
	protected Boolean caseSensitive;

	@XmlElement
	protected FilterOperator operator;

	public FilterDescriptor()
	{
		// Intended blank. For XML serialization usage only
	}

	public FilterDescriptor(Class<T> entityType)
	{
		setEntityType(entityType);
	}

	@Override
	public String getMember()
	{
		return member;
	}

	public void setMember(String member)
	{
		this.member = member;
	}

	public FilterDescriptor<T> withMember(String member)
	{
		setMember(member);
		return this;
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
	public List<String> getValue()
	{
		if (value == null)
		{
			value = new ArrayList<String>();
		}
		return value;
	}

	public void setValue(List<String> value)
	{
		this.value = value;
	}

	public FilterDescriptor<T> withValue(String value)
	{
		if (this.value == null)
		{
			this.value = new ArrayList<String>();
		}
		this.value.add(value);
		return this;
	}

	public FilterDescriptor<T> withValues(List<String> value)
	{
		setValue(value);
		return this;
	}

	@Override
	public Boolean isCaseSensitive()
	{
		return caseSensitive;
	}

	public void setCaseSensitive(Boolean caseSensitive)
	{
		this.caseSensitive = caseSensitive;
	}

	public FilterDescriptor<T> withCaseSensitive(Boolean caseSensitive)
	{
		setCaseSensitive(caseSensitive);
		return this;
	}

	@Override
	public FilterOperator getOperator()
	{
		return operator;
	}

	public void setOperator(FilterOperator operator)
	{
		this.operator = operator;
	}

	public FilterDescriptor<T> withOperator(FilterOperator operator)
	{
		setOperator(operator);
		return this;
	}

	@Override
	public LogicalOperator getLogicalOperator()
	{
		return null;
	}

	@Override
	public List<IFilterDescriptor<T>> getChildFilterDescriptors()
	{
		return null;
	}
}
