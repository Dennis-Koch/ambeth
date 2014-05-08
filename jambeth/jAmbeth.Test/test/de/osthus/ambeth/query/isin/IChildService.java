package de.osthus.ambeth.query.isin;

import de.osthus.ambeth.annotation.NoProxy;

public interface IChildService
{
	@NoProxy
	void searchForParentWithEquals(int parentId);

	@NoProxy
	void getForParentWithIsIn(int... parentIds);
}