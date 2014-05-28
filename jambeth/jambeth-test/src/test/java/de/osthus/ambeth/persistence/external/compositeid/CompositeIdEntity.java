package de.osthus.ambeth.persistence.external.compositeid;

public class CompositeIdEntity
{
	protected int id1;

	protected String id2;

	protected short aid1;

	protected long aid2;

	protected String name;

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

	public short getAid1()
	{
		return aid1;
	}

	public void setAid1(short aid1)
	{
		this.aid1 = aid1;
	}

	public long getAid2()
	{
		return aid2;
	}

	public void setAid2(long aid2)
	{
		this.aid2 = aid2;
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
