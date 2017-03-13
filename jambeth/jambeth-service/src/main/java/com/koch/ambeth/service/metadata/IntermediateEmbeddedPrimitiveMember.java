package com.koch.ambeth.service.metadata;

import java.lang.annotation.Annotation;

public class IntermediateEmbeddedPrimitiveMember extends IntermediatePrimitiveMember implements IEmbeddedMember
{
	protected final Member[] memberPath;

	protected final PrimitiveMember childMember;

	protected final String[] memberPathToken;

	protected final String memberPathString;

	public IntermediateEmbeddedPrimitiveMember(Class<?> entityType, Class<?> realType, Class<?> elementType, String propertyName, Member[] memberPath,
			PrimitiveMember childMember)
	{
		super(entityType, entityType, realType, elementType, propertyName, null);
		this.memberPath = memberPath;
		this.childMember = childMember;
		memberPathToken = EmbeddedMember.buildMemberPathToken(memberPath);
		memberPathString = EmbeddedMember.buildMemberPathString(memberPath);
	}

	@Override
	public boolean isToMany()
	{
		return childMember.isToMany();
	}

	@Override
	public <V extends Annotation> V getAnnotation(Class<V> annotationType)
	{
		return childMember.getAnnotation(annotationType);
	}

	@Override
	public boolean isTechnicalMember()
	{
		return childMember.isTechnicalMember();
	}

	@Override
	public boolean isTransient()
	{
		return childMember.isTransient();
	}

	@Override
	public void setTechnicalMember(boolean technicalMember)
	{
		((IPrimitiveMemberWrite) childMember).setTechnicalMember(technicalMember);
	}

	@Override
	public void setTransient(boolean isTransient)
	{
		((IPrimitiveMemberWrite) childMember).setTransient(isTransient);
	}

	@Override
	public void setDefinedBy(PrimitiveMember definedBy)
	{
		((IPrimitiveMemberWrite) childMember).setDefinedBy(definedBy);
	}

	@Override
	public Member[] getMemberPath()
	{
		return memberPath;
	}

	@Override
	public String getMemberPathString()
	{
		return memberPathString;
	}

	@Override
	public String[] getMemberPathToken()
	{
		return memberPathToken;
	}

	@Override
	public Member getChildMember()
	{
		return childMember;
	}
}