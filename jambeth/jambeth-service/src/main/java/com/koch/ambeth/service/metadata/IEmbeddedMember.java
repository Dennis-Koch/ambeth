package com.koch.ambeth.service.metadata;

public interface IEmbeddedMember
{
	Member[] getMemberPath();

	String getMemberPathString();

	String[] getMemberPathToken();

	Member getChildMember();
}
