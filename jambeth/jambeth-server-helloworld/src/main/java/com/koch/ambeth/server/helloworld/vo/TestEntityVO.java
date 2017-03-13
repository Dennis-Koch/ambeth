package com.koch.ambeth.server.helloworld.vo;

import java.util.List;

public class TestEntityVO extends AbstractEntityVO
{
	protected TestEntity2VO relation;

	protected List<TestEntity3VO> relations;

	protected int myValue;

	protected int myValueUnique;

	protected EmbeddedObjectVO embeddedObject;

	public EmbeddedObjectVO getEmbeddedObject()
	{
		return embeddedObject;
	}

	public void setEmbeddedObject(EmbeddedObjectVO embeddedObject)
	{
		this.embeddedObject = embeddedObject;
	}

	public void setMyValue(int myValue)
	{
		this.myValue = myValue;
	}

	public int getMyValue()
	{
		return myValue;
	}

	public void setMyValueUnique(int myValueUnique)
	{
		this.myValueUnique = myValueUnique;
	}

	public int getMyValueUnique()
	{
		return myValueUnique;
	}

	public TestEntity2VO getRelation()
	{
		return relation;
	}

	public void setRelation(TestEntity2VO relation)
	{
		this.relation = relation;
	}

	public List<TestEntity3VO> getRelations()
	{
		return relations;
	}

	public void setRelations(List<TestEntity3VO> relations)
	{
		this.relations = relations;
	}
}
