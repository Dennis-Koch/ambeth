package de.osthus.ambeth.persistence.jdbc.mapping.models;

import java.util.List;

import de.osthus.ambeth.model.AbstractEntity;

public class OneToManyEntityVO extends AbstractEntity
{
	protected String buid;

	protected String name;

	protected List<String> nicknames;

	private double needsSpecialMapping;

	protected TestEmbeddedTypeVO myEmbeddedType;

	protected List<String> oneToManyEntities;

	protected OneToManyEntityListType byListType;

	protected OneToManyEntityRefListType byRefListType;

	protected List<SelfReferencingEntityVO> selfReferencingEntities;

	public String getBuid()
	{
		return buid;
	}

	public void setBuid(String buid)
	{
		this.buid = buid;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public List<String> getNicknames()
	{
		return nicknames;
	}

	public void setNicknames(List<String> nicknames)
	{
		this.nicknames = nicknames;
	}

	public double getNeedsSpecialMapping()
	{
		return needsSpecialMapping;
	}

	public void setNeedsSpecialMapping(double needsSpecialMapping)
	{
		this.needsSpecialMapping = needsSpecialMapping;
	}

	public TestEmbeddedTypeVO getMyEmbeddedType()
	{
		return myEmbeddedType;
	}

	public void setMyEmbeddedType(TestEmbeddedTypeVO myEmbeddedType)
	{
		this.myEmbeddedType = myEmbeddedType;
	}

	public List<String> getOneToManyEntities()
	{
		return oneToManyEntities;
	}

	public void setOneToManyEntities(List<String> oneToManyEntities)
	{
		this.oneToManyEntities = oneToManyEntities;
	}

	public OneToManyEntityListType getByListType()
	{
		return byListType;
	}

	public void setByListType(OneToManyEntityListType byListType)
	{
		this.byListType = byListType;
	}

	// Strange case to check for tolerance in BO <-> VO method name matching.
	public OneToManyEntityRefListType getByREFListType()
	{
		return byRefListType;
	}

	public void setByREFListType(OneToManyEntityRefListType byRefListType)
	{
		this.byRefListType = byRefListType;
	}

	public List<SelfReferencingEntityVO> getSelfReferencingEntities()
	{
		return selfReferencingEntities;
	}

	public void setSelfReferencingEntities(List<SelfReferencingEntityVO> selfReferencingEntities)
	{
		this.selfReferencingEntities = selfReferencingEntities;
	}
}
