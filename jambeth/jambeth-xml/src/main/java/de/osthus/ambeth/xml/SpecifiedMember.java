package de.osthus.ambeth.xml;

import de.osthus.ambeth.typeinfo.ITypeInfoItem;

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
