package com.koch.ambeth.service.metadata;

public abstract class PrimitiveMember extends Member
{
	public abstract boolean isTechnicalMember();

	public abstract boolean isTransient();

	public abstract PrimitiveMember getDefinedBy();
}
