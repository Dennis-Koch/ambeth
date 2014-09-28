package de.osthus.ambeth.metadata;

public class IntermediateEmbeddedPrimitiveMember extends IntermediatePrimitiveMember implements IEmbeddedMember
{
	protected final Member[] memberPath;

	protected final Member childMember;

	protected final String[] memberPathToken;

	protected final String memberPathString;

	public IntermediateEmbeddedPrimitiveMember(Class<?> type, Class<?> realType, Class<?> elementType, String propertyName, Member[] memberPath,
			Member childMember)
	{
		super(type, realType, elementType, propertyName);
		this.memberPath = memberPath;
		this.childMember = childMember;
		this.memberPathToken = EmbeddedMember.buildMemberPathToken(memberPath);
		this.memberPathString = EmbeddedMember.buildMemberPathString(memberPath);
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