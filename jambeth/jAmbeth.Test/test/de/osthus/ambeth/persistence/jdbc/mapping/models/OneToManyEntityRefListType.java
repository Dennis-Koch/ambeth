package de.osthus.ambeth.persistence.jdbc.mapping.models;

import java.util.List;

public class OneToManyEntityRefListType
{
	protected List<String> buids = null;

	public List<String> getBUID()
	{
		return buids;
	}

	public void setBUID(List<String> buids)
	{
		this.buids = buids;
	}
}
