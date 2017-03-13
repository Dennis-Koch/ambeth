package com.koch.ambeth.persistence.jdbc.mapping.models;

import java.util.ArrayList;
import java.util.List;

public class DetailListVO
{
	protected List<DetailVO> details = null;

	public List<DetailVO> getDetails()
	{
		if (details == null)
		{
			details = new ArrayList<DetailVO>();
		}
		return details;
	}
}
