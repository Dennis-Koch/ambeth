package com.koch.ambeth.server.helloworld.transfer;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.koch.ambeth.util.annotation.ParentChild;

@XmlRootElement(namespace = "HelloWorld")
@XmlAccessorType(XmlAccessType.FIELD)
public class TestEntity extends AbstractEntity
{
	@XmlElement
	protected TestEntity2 relation;

	@XmlElement
	protected List<TestEntity3> relations;

	@XmlElement
	protected int myValue;

	@XmlElement
	protected int myValueUnique;

	@XmlElement
	protected EmbeddedObject embeddedObject;

	public EmbeddedObject getEmbeddedObject()
	{
		if (embeddedObject == null)
		{
			embeddedObject = new EmbeddedObject();
		}
		return embeddedObject;
	}

	public void setEmbeddedObject(EmbeddedObject embeddedObject)
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

	public void setRelations(List<TestEntity3> relations)
	{
		this.relations = relations;
	}

	public List<TestEntity3> getRelations()
	{
		return relations;
	}

	public void setRelation(TestEntity2 relation)
	{
		this.relation = relation;
	}

	@ParentChild
	public TestEntity2 getRelation()
	{
		return relation;
	}
}
