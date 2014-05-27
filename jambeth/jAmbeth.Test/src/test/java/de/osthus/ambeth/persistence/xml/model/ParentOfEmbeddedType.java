package de.osthus.ambeth.persistence.xml.model;

import javax.persistence.Embedded;

import de.osthus.ambeth.model.AbstractEntity;

public class ParentOfEmbeddedType extends AbstractEntity
{
	@Embedded
	protected TestEmbeddedType myEmbeddedType;

	protected ParentOfEmbeddedType()
	{
		// Intended blank
	}

	public TestEmbeddedType getMyEmbeddedType()
	{
		return myEmbeddedType;
	}

	public void setMyEmbeddedType(TestEmbeddedType myEmbeddedType)
	{
		this.myEmbeddedType = myEmbeddedType;
	}
}
