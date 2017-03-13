package com.koch.ambeth.server.helloworld.transfer;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.koch.ambeth.util.annotation.ParentChild;

@XmlRootElement(namespace = "HelloWorld")
@XmlAccessorType(XmlAccessType.FIELD)
public class EmbeddedObject
{
	@XmlElement
	protected TestEntity2 relationOfEmbeddedObject;

	@XmlElement
	protected String name;

	@XmlElement
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

	@ParentChild
	public TestEntity2 getRelationOfEmbeddedObject()
	{
		return relationOfEmbeddedObject;
	}

	public void setRelationOfEmbeddedObject(TestEntity2 relationOfEmbeddedObject)
	{
		this.relationOfEmbeddedObject = relationOfEmbeddedObject;
	}
}
