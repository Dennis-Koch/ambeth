package de.osthus.ambeth.persistence.jdbc.compositeid.models;

public class CompositeIdEntity
{
	private int id1;

	private String id2;

	private int altId1;

	private String altId2;

	private int altId3;

	private String altId4;

	private String name;

	protected CompositeIdEntity()
	{
		// Intended blank
	}

	public int getId1()
	{
		return id1;
	}

	public void setId1(int id1)
	{
		this.id1 = id1;
	}

	public String getId2()
	{
		return id2;
	}

	public void setId2(String id2)
	{
		this.id2 = id2;
	}

	public int getAltId1()
	{
		return altId1;
	}

	public void setAltId1(int altId1)
	{
		this.altId1 = altId1;
	}

	public String getAltId2()
	{
		return altId2;
	}

	public void setAltId2(String altId2)
	{
		this.altId2 = altId2;
	}

	public int getAltId3()
	{
		return altId3;
	}

	public void setAltId3(int altId3)
	{
		this.altId3 = altId3;
	}

	public String getAltId4()
	{
		return altId4;
	}

	public void setAltId4(String altId4)
	{
		this.altId4 = altId4;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}
}
