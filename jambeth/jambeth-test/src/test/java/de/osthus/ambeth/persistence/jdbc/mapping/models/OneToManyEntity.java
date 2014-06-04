package de.osthus.ambeth.persistence.jdbc.mapping.models;

import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import de.osthus.ambeth.model.AbstractEntity;
import de.osthus.ambeth.persistence.xml.model.TestEmbeddedType;

public class OneToManyEntity extends AbstractEntity
{
	protected String buid;

	protected String name;

	protected List<String> nicknames;

	private Date needsSpecialMapping;

	protected TestEmbeddedType myEmbedded;

	protected List<OneToManyEntity> oneToManyEntities;

	protected List<OneToManyEntity> byListType;

	protected List<OneToManyEntity> byRefListType;

	protected Set<SelfReferencingEntity> selfReferencingEntities;

	protected OneToManyEntity()
	{
		// Intended blank
	}

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

	public Date getNeedsSpecialMapping()
	{
		return needsSpecialMapping;
	}

	public void setNeedsSpecialMapping(Date needsSpecialMapping)
	{
		this.needsSpecialMapping = needsSpecialMapping;
	}

	public TestEmbeddedType getMyEmbedded()
	{
		return myEmbedded;
	}

	public void setMyEmbedded(TestEmbeddedType myEmbedded)
	{
		this.myEmbedded = myEmbedded;
	}

	public List<OneToManyEntity> getOneToManyEntities()
	{
		return oneToManyEntities;
	}

	public void setOneToManyEntities(List<OneToManyEntity> oneToManyEntities)
	{
		this.oneToManyEntities = oneToManyEntities;
	}

	public List<OneToManyEntity> getByListType()
	{
		return byListType;
	}

	public void setByListType(List<OneToManyEntity> byListType)
	{
		this.byListType = byListType;
	}

	public List<OneToManyEntity> getByRefListType()
	{
		return byRefListType;
	}

	public void setByRefListType(List<OneToManyEntity> byRefListType)
	{
		this.byRefListType = byRefListType;
	}

	public Set<SelfReferencingEntity> getSelfReferencingEntities()
	{
		ensureSelfReferencingEntities();
		return selfReferencingEntities;
	}

	public void setSelfReferencingEntities(Set<SelfReferencingEntity> selfReferencingEntities)
	{
		removeAllSelfReferencingEntities();
		if (selfReferencingEntities != null)
		{
			for (Iterator<SelfReferencingEntity> iter = selfReferencingEntities.iterator(); iter.hasNext();)
			{
				this.selfReferencingEntities.add(iter.next());
			}
		}
	}

	public void removeAllSelfReferencingEntities()
	{
		ensureSelfReferencingEntities();
		selfReferencingEntities.clear();
	}

	protected void ensureSelfReferencingEntities()
	{
		if (selfReferencingEntities == null)
		{
			selfReferencingEntities = new HashSet<SelfReferencingEntity>(0);
		}
	}
}