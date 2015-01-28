package de.osthus.ambeth.query;

import de.osthus.ambeth.annotation.Cascade;
import de.osthus.ambeth.annotation.CascadeLoadMode;
import de.osthus.ambeth.model.AbstractEntity;

public class QueryEntity extends AbstractEntity
{
	public static final String Name1 = "Name1";

	public static final String Fk = "Fk";

	public static final String LinkTableEntity = "LinkTableEntity";

	@Cascade(load = CascadeLoadMode.EAGER_VERSION)
	protected JoinQueryEntity fk;

	protected LinkTableEntity linkTableEntity;

	protected String name1;

	protected String name2;

	protected String ftname1;

	protected String ftname2;

	protected QueryEntity()
	{
		// Intended blank
	}

	public JoinQueryEntity getFk()
	{
		return fk;
	}

	public void setFk(JoinQueryEntity fk)
	{
		this.fk = fk;
	}

	public LinkTableEntity getLinkTableEntity()
	{
		return linkTableEntity;
	}

	public void setLinkTableEntity(LinkTableEntity linkTableEntity)
	{
		this.linkTableEntity = linkTableEntity;
	}

	public String getName1()
	{
		return name1;
	}

	public void setName1(String name1)
	{
		this.name1 = name1;
	}

	public String getName2()
	{
		return name2;
	}

	public void setName2(String name2)
	{
		this.name2 = name2;
	}

	public void setFtname1(String ftname1)
	{
		this.ftname1 = ftname1;
	}

	public String getFtname1()
	{
		return ftname1;
	}

	public void setFtname2(String ftname2)
	{
		this.ftname2 = ftname2;
	}

	public String getFtname2()
	{
		return ftname2;
	}
}
