package de.osthus.ambeth.orm;

import java.util.LinkedHashSet;

public class EntityConfig implements IEntityConfig
{
	private final Class<?> entityType, realType;

	private boolean local;

	private String tableName;

	private String permissionGroupName;

	private String sequenceName;

	private IMemberConfig idMemberConfig;

	private IMemberConfig versionMemberConfig;

	private boolean versionRequired = true;

	private IMemberConfig createdByMemberConfig;

	private IMemberConfig createdOnMemberConfig;

	private IMemberConfig updatedByMemberConfig;

	private IMemberConfig updatedOnMemberConfig;

	private final LinkedHashSet<IMemberConfig> memberConfigs = new LinkedHashSet<IMemberConfig>();

	private final LinkedHashSet<IRelationConfig> relationConfigs = new LinkedHashSet<IRelationConfig>();

	private String descriminatorName;

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

	@Override
	public Class<?> getEntityType()
	{
		return entityType;
	}

	@Override
	public Class<?> getRealType()
	{
		return realType;
	}

	@Override
	public boolean isLocal()
	{
		return local;
	}

	public void setLocal(boolean local)
	{
		this.local = local;
	}

	@Override
	public String getTableName()
	{
		return tableName;
	}

	public void setTableName(String tableName)
	{
		this.tableName = tableName;
	}

	@Override
	public String getPermissionGroupName()
	{
		return permissionGroupName;
	}

	public void setPermissionGroupName(String permissionGroupName)
	{
		this.permissionGroupName = permissionGroupName;
	}

	@Override
	public String getSequenceName()
	{
		return sequenceName;
	}

	public void setSequenceName(String sequenceName)
	{
		this.sequenceName = sequenceName;
	}

	@Override
	public IMemberConfig getIdMemberConfig()
	{
		return idMemberConfig;
	}

	public void setIdMemberConfig(IMemberConfig idMemberInfo)
	{
		idMemberConfig = idMemberInfo;
	}

	@Override
	public IMemberConfig getVersionMemberConfig()
	{
		return versionMemberConfig;
	}

	public void setVersionMemberConfig(IMemberConfig versionMemberInfo)
	{
		versionMemberConfig = versionMemberInfo;
	}

	@Override
	public String getDescriminatorName()
	{
		return descriminatorName;
	}

	public void setDescriminatorName(String descriminatorName)
	{
		this.descriminatorName = descriminatorName;
	}

	@Override
	public boolean isVersionRequired()
	{
		return versionRequired;
	}

	public void setVersionRequired(boolean versionRequired)
	{
		this.versionRequired = versionRequired;
	}

	@Override
	public IMemberConfig getCreatedByMemberConfig()
	{
		return createdByMemberConfig;
	}

	public void setCreatedByMemberConfig(IMemberConfig createdByMemberConfig)
	{
		this.createdByMemberConfig = createdByMemberConfig;
	}

	@Override
	public IMemberConfig getCreatedOnMemberConfig()
	{
		return createdOnMemberConfig;
	}

	public void setCreatedOnMemberConfig(IMemberConfig createdOnMemberConfig)
	{
		this.createdOnMemberConfig = createdOnMemberConfig;
	}

	@Override
	public IMemberConfig getUpdatedByMemberConfig()
	{
		return updatedByMemberConfig;
	}

	public void setUpdatedByMemberConfig(IMemberConfig updatedByMemberConfig)
	{
		this.updatedByMemberConfig = updatedByMemberConfig;
	}

	@Override
	public IMemberConfig getUpdatedOnMemberConfig()
	{
		return updatedOnMemberConfig;
	}

	public void setUpdatedOnMemberConfig(IMemberConfig updatedOnMemberConfig)
	{
		this.updatedOnMemberConfig = updatedOnMemberConfig;
	}

	@Override
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

	@Override
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
			IEntityConfig other = (IEntityConfig) obj;
			return entityType.equals(other.getEntityType());
		}
		else
		{
			return false;
		}
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + ": " + getEntityType();
	}
}
