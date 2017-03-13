package com.koch.ambeth.service.metadata;

public interface IPrimitiveMemberWrite
{
	void setTechnicalMember(boolean technicalMember);

	void setTransient(boolean isTransient);

	void setDefinedBy(PrimitiveMember definedBy);
}
