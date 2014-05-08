package de.osthus.ambeth.query.alternateid;

import de.osthus.ambeth.model.AbstractEntity;

public class MultiAlternateIdEntity extends AbstractEntity
{
	protected String alternateId1;

	protected String alternateId2;

	protected MultiAlternateIdEntity()
	{
		// Intended blank
	}

	public String getAlternateId1()
	{
		return alternateId1;
	}

	public void setAlternateId1(String alternateId1)
	{
		this.alternateId1 = alternateId1;
	}

	public String getAlternateId2()
	{
		return alternateId2;
	}

	public void setAlternateId2(String alternateId2)
	{
		this.alternateId2 = alternateId2;
	}
}
