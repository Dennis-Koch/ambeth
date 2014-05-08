package de.osthus.ambeth.merge;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.ILinkedMap;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.collections.LinkedHashMap;
import de.osthus.ambeth.config.IProperties;
import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.merge.model.EntityMetaData;
import de.osthus.ambeth.merge.model.IEntityMetaData;
import de.osthus.ambeth.typeinfo.IRelationInfoItem;
import de.osthus.ambeth.typeinfo.ITypeInfoItem;
import de.osthus.ambeth.typeinfo.TypeInfoItem;
import de.osthus.ambeth.util.ParamChecker;

public class EntityMetaDataFake implements IEntityMetaDataProvider, IInitializingBean, IEntityMetaDataExtendable
{
	protected final ILinkedMap<Class<?>, IEntityMetaData> typeToEntityMetaData = new LinkedHashMap<Class<?>, IEntityMetaData>();

	protected final Map<Class<?>, List<Class<?>>> typeToRelatedTypesMap = new HashMap<Class<?>, List<Class<?>>>();

	protected IEntityMetaDataFiller entityMetaDataFiller;

	protected IProperties properties;

	@Autowired(optional = true)
	protected IEntityFactory entityFactory;

	@Autowired(optional = true)
	protected IProxyHelper proxyHelper;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		ParamChecker.assertNotNull(properties, "properties");

		if (entityMetaDataFiller != null)
		{
			entityMetaDataFiller.fillMetaData(this);
		}
	}

	public void setEntityMetaDataFiller(IEntityMetaDataFiller entityMetaDataFiller)
	{
		this.entityMetaDataFiller = entityMetaDataFiller;
	}

	public void setProperties(IProperties properties)
	{
		this.properties = properties;
	}

	public void addMetaData(Class<?> entityType, ITypeInfoItem idMember, ITypeInfoItem versionMember, ITypeInfoItem[] primitiveMembers,
			IRelationInfoItem[] relationMembers)
	{
		addMetaData(entityType, idMember, versionMember, primitiveMembers, relationMembers, new ITypeInfoItem[0]);
	}

	public void addMetaData(Class<?> entityType, ITypeInfoItem idMember, ITypeInfoItem versionMember, ITypeInfoItem[] primitiveMembers,
			IRelationInfoItem[] relationMembers, ITypeInfoItem[] alternateIdMembers)
	{
		Class<?> realEntityType = proxyHelper != null ? proxyHelper.getRealType(entityType) : entityType;
		EntityMetaData metaData = new EntityMetaData();
		metaData.setEntityType(entityType);
		metaData.setRealType(realEntityType);
		metaData.setLocalEntity(false);
		metaData.setIdMember(idMember);
		metaData.setVersionMember(versionMember);

		// Order of setter calls is important
		metaData.setPrimitiveMembers(primitiveMembers);
		metaData.setAlternateIdMembers(alternateIdMembers);
		metaData.setRelationMembers(relationMembers);

		typeToEntityMetaData.put(metaData.getEntityType(), metaData);

		for (int i = relationMembers.length; i-- > 0;)
		{
			IRelationInfoItem relationMember = relationMembers[i];
			TypeInfoItem.setEntityType(relationMember.getRealType(), relationMember, properties);
			updateRelations(relationMember.getElementType(), entityType);
		}
		updateRelations(entityType, null);
		metaData.initialize(entityFactory);
	}

	protected void updateRelations(Class<?> entityType, Class<?> typeToAdd)
	{
		List<Class<?>> relateToType = typeToRelatedTypesMap.get(entityType);
		if (relateToType == null)
		{
			relateToType = new ArrayList<Class<?>>();
			typeToRelatedTypesMap.put(entityType, relateToType);
		}

		if (typeToAdd != null)
		{
			relateToType.add(typeToAdd);
		}

		EntityMetaData entityTypeMetaData = (EntityMetaData) typeToEntityMetaData.get(entityType);
		if (entityTypeMetaData != null)
		{
			entityTypeMetaData.setTypesRelatingToThis(relateToType.toArray(new Class<?>[relateToType.size()]));
		}
	}

	@Override
	public IEntityMetaData getMetaData(Class<?> entityType)
	{
		return getMetaData(entityType, false);
	}

	@Override
	public IEntityMetaData getMetaData(Class<?> entityType, boolean tryOnly)
	{
		Class<?> realEntityType = proxyHelper != null ? proxyHelper.getRealType(entityType) : entityType;
		IEntityMetaData metaData = typeToEntityMetaData.get(realEntityType);
		if (metaData != null || tryOnly)
		{
			return metaData;
		}
		throw new IllegalArgumentException("No metadata found for entity of type " + realEntityType.getName());
	}

	@Override
	public IList<IEntityMetaData> getMetaData(List<Class<?>> entityTypes)
	{
		IList<IEntityMetaData> entityMetaData = new ArrayList<IEntityMetaData>();
		for (Class<?> entityType : entityTypes)
		{
			Class<?> realEntityType = proxyHelper != null ? proxyHelper.getRealType(entityType) : entityType;
			IEntityMetaData metaDataItem = typeToEntityMetaData.get(realEntityType);

			if (metaDataItem != null)
			{
				entityMetaData.add(metaDataItem);
			}
		}
		return entityMetaData;
	}

	@Override
	public IList<Class<?>> findMappableEntityTypes()
	{
		throw new UnsupportedOperationException("Not implemented");
	}

	@Override
	public IValueObjectConfig getValueObjectConfig(Class<?> valueType)
	{
		throw new UnsupportedOperationException("Not implemented");
	}

	@Override
	public IValueObjectConfig getValueObjectConfig(String xmlTypeName)
	{
		throw new UnsupportedOperationException("Not implemented");
	}

	@Override
	public Class<?>[] getEntityPersistOrder()
	{
		return new Class[0];
	}

	@Override
	public IList<Class<?>> getValueObjectTypesByEntityType(Class<?> entityType)
	{
		throw new UnsupportedOperationException("Not implemented");
	}

	@Override
	public void registerEntityMetaData(IEntityMetaData metaData)
	{
		registerEntityMetaData(metaData, metaData.getEntityType());
	}

	@Override
	public void registerEntityMetaData(IEntityMetaData metaData, Class<?> entityType)
	{
		ITypeInfoItem[] relationMembers = metaData.getRelationMembers();
		typeToEntityMetaData.put(entityType, metaData);

		for (int i = relationMembers.length; i-- > 0;)
		{
			updateRelations(relationMembers[i].getRealType(), entityType);
		}
		updateRelations(entityType, null);
	}

	@Override
	public void unregisterEntityMetaData(IEntityMetaData entityMetaData)
	{
	}

	@Override
	public void unregisterEntityMetaData(IEntityMetaData entityMetaData, Class<?> entityType)
	{
	}
}
