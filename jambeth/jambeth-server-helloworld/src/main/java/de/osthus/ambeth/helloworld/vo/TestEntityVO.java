package de.osthus.ambeth.helloworld.vo;


public class TestEntityVO extends AbstractEntityVO
{
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
}
