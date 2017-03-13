package com.koch.ambeth.query.isin;

import com.koch.ambeth.util.annotation.NoProxy;

public interface IChildService
{
	@NoProxy
	void searchForParentWithEquals(int parentId);

	@NoProxy
	void getForParentWithIsIn(int... parentIds);
}