package com.koch.ambeth.server.helloworld.vo;


public class EmbeddedObjectVO
{
	protected TestEntity2VO relationOfEmbeddedObject;

	protected String name;

	protected int value;

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public int getValue()
	{
		return value;
	}

	public void setValue(int value)
	{
		this.value = value;
	}

	public TestEntity2VO getRelationOfEmbeddedObject()
	{
		return relationOfEmbeddedObject;
	}

	public void setRelationOfEmbeddedObject(TestEntity2VO relationOfEmbeddedObject)
	{
		this.relationOfEmbeddedObject = relationOfEmbeddedObject;
	}
}
