package com.koch.ambeth.merge.proxy;

import java.util.ArrayList;

public class DefaultList<T> extends ArrayList<T> implements IDefaultCollection
{
	private static final long serialVersionUID = -2835256244356556171L;

	@Override
	public boolean hasDefaultState()
	{
		return this.modCount == 0 && size() == 0;
	}
}
