package com.koch.ambeth.xml;

import com.koch.ambeth.util.typeinfo.ITypeInfoItem;

public class SpecifiedMember
{
	private ITypeInfoItem member;

	private ITypeInfoItem specifiedMember;

	public SpecifiedMember(ITypeInfoItem member, ITypeInfoItem specifiedMember)
	{
		super();
		this.member = member;
		this.specifiedMember = specifiedMember;
	}

	public ITypeInfoItem getMember()
	{
		return member;
	}

	public ITypeInfoItem getSpecifiedMember()
	{
		return specifiedMember;
	}

	@Override
	public String toString()
	{
		return getMember().toString();
	}
}
