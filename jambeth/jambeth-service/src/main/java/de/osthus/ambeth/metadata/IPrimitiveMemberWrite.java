package de.osthus.ambeth.metadata;

public interface IPrimitiveMemberWrite
{
	void setTechnicalMember(boolean technicalMember);

	void setTransient(boolean isTransient);

	void setDefinedBy(PrimitiveMember definedBy);
}
