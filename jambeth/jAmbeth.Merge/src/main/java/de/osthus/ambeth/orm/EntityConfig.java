package de.osthus.ambeth.orm;

import java.util.LinkedHashSet;
import java.util.Set;

public class EntityConfig
{
	private final Class<?> entityType, realType;

	private boolean local;

	private String tableName;

	private String sequenceName;

	private IMemberConfig idMemberConfig;

	private IMemberConfig versionMemberConfig;

	private boolean versionRequired = true;

	private IMemberConfig createdByMemberConfig;

	private IMemberConfig createdOnMemberConfig;

	private IMemberConfig updatedByMemberConfig;

	private IMemberConfig updatedOnMemberConfig;

	private final Set<IMemberConfig> memberConfigs = new LinkedHashSet<IMemberConfig>();

	private final Set<IRelationConfig> relationConfigs = new LinkedHashSet<IRelationConfig>();

	@Deprecated
	public EntityConfig(Class<?> entityType)
	{
		this(entityType, entityType);
	}

	public EntityConfig(Class<?> entityType, Class<?> realType)
	{
		this.entityType = entityType;
		this.realType = realType;
	}

	public Class<?> getEntityType()
	{
		return entityType;
	}

	public Class<?> getRealType()
	{
		return realType;
	}

	public boolean isLocal()
	{
		return local;
	}

	public void setLocal(boolean local)
	{
		this.local = local;
	}

	public String getTableName()
	{
		return tableName;
	}

	public void setTableName(String tableName)
	{
		this.tableName = tableName;
	}

	public String getSequenceName()
	{
		return sequenceName;
	}

	public void setSequenceName(String sequenceName)
	{
		this.sequenceName = sequenceName;
	}

	public IMemberConfig getIdMemberConfig()
	{
		return idMemberConfig;
	}

	public void setIdMemberConfig(IMemberConfig idMemberInfo)
	{
		idMemberConfig = idMemberInfo;
	}

	public IMemberConfig getVersionMemberConfig()
	{
		return versionMemberConfig;
	}

	public void setVersionMemberConfig(IMemberConfig versionMemberInfo)
	{
		versionMemberConfig = versionMemberInfo;
	}

	public boolean isVersionRequired()
	{
		return versionRequired;
	}

	public void setVersionRequired(boolean versionRequired)
	{
		this.versionRequired = versionRequired;
	}

	public IMemberConfig getCreatedByMemberConfig()
	{
		return createdByMemberConfig;
	}

	public void setCreatedByMemberConfig(IMemberConfig createdByMemberConfig)
	{
		this.createdByMemberConfig = createdByMemberConfig;
	}

	public IMemberConfig getCreatedOnMemberConfig()
	{
		return createdOnMemberConfig;
	}

	public void setCreatedOnMemberConfig(IMemberConfig createdOnMemberConfig)
	{
		this.createdOnMemberConfig = createdOnMemberConfig;
	}

	public IMemberConfig getUpdatedByMemberConfig()
	{
		return updatedByMemberConfig;
	}

	public void setUpdatedByMemberConfig(IMemberConfig updatedByMemberConfig)
	{
		this.updatedByMemberConfig = updatedByMemberConfig;
	}

	public IMemberConfig getUpdatedOnMemberConfig()
	{
		return updatedOnMemberConfig;
	}

	public void setUpdatedOnMemberConfig(IMemberConfig updatedOnMemberConfig)
	{
		this.updatedOnMemberConfig = updatedOnMemberConfig;
	}

	public Iterable<IMemberConfig> getMemberConfigIterable()
	{
		return memberConfigs;
	}

	public void addMemberConfig(IMemberConfig memberConfig)
	{
		if (!memberConfigs.add(memberConfig))
		{
			throw new IllegalStateException("Duplicate member configuration for '" + entityType.getName() + "'.'" + memberConfig.getName() + "'");
		}
	}

	public Iterable<IRelationConfig> getRelationConfigIterable()
	{
		return relationConfigs;
	}

	public void addRelationConfig(IRelationConfig relationConfig)
	{
		if (!relationConfigs.add(relationConfig))
		{
			throw new IllegalStateException("Duplicate relation configuration for '" + entityType.getName() + "'.'" + relationConfig.getName() + "'");
		}
	}

	@Override
	public int hashCode()
	{
		return entityType.hashCode();
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj instanceof EntityConfig)
		{
			EntityConfig other = (EntityConfig) obj;
			return entityType.equals(other.getEntityType());
		}
		else
		{
			return false;
		}
	}
}
