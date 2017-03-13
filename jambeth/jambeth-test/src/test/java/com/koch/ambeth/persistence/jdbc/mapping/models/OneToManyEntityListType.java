package com.koch.ambeth.persistence.jdbc.mapping.models;

import java.util.ArrayList;
import java.util.List;

public class OneToManyEntityListType
{
	protected List<OneToManyEntityVO> oneToManyEntities = null;

	public List<OneToManyEntityVO> getOneToManyEntities()
	{
		if (oneToManyEntities == null)
		{
			oneToManyEntities = new ArrayList<OneToManyEntityVO>();
		}
		return oneToManyEntities;
	}
}
