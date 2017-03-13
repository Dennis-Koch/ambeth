package com.koch.ambeth.service.metadata;

import java.lang.annotation.Annotation;

public class IntermediateEmbeddedRelationMember extends IntermediateRelationMember implements IEmbeddedMember
{
	protected final Member[] memberPath;

	protected final Member childMember;

	protected final String[] memberPathToken;

	protected final String memberPathString;

	public IntermediateEmbeddedRelationMember(Class<?> entityType, Class<?> realType, Class<?> elementType, String propertyName, Member[] memberPath,
			Member childMember)
	{
		super(entityType, entityType, realType, elementType, propertyName, null);
		this.memberPath = memberPath;
		this.childMember = childMember;
		this.memberPathToken = EmbeddedMember.buildMemberPathToken(memberPath);
		this.memberPathString = EmbeddedMember.buildMemberPathString(memberPath);
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
