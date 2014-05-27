package de.osthus.ambeth.filter.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType
@XmlAccessorType(XmlAccessType.FIELD)
public class SortDescriptor implements ISortDescriptor
{
	@XmlElement(name = "Member", required = true)
	protected String member;

	@XmlElement(name = "SortDirection", required = true)
	protected SortDirection sortDirection = SortDirection.ASCENDING;

	@Override
	public String getMember()
	{
		return member;
	}

	public void setMember(String member)
	{
		this.member = member;
	}

	public SortDescriptor withMember(String member)
	{
		setMember(member);
		return this;
	}

	@Override
	public SortDirection getSortDirection()
	{
		return sortDirection;
	}

	public void setSortDirection(SortDirection sortDirection)
	{
		this.sortDirection = sortDirection;
	}

	public SortDescriptor withSortDirection(SortDirection sortDirection)
	{
		setSortDirection(sortDirection);
		return this;
	}
}
