package de.osthus.ambeth.metadata;

public interface IEmbeddedMember
{
	Member[] getMemberPath();

	String getMemberPathString();

	String[] getMemberPathToken();

	Member getChildMember();
}
