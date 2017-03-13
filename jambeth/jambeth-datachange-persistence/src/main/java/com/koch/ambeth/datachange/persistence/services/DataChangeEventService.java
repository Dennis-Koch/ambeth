package com.koch.ambeth.datachange.persistence.services;

import java.util.List;

import com.koch.ambeth.datachange.model.IDataChange;
import com.koch.ambeth.datachange.model.IDataChangeEntry;
import com.koch.ambeth.datachange.persistence.config.DataChangePersistenceConfigurationConstants;
import com.koch.ambeth.datachange.persistence.model.DataChangeEntryBO;
import com.koch.ambeth.datachange.persistence.model.DataChangeEventBO;
import com.koch.ambeth.datachange.persistence.model.EntityType;
import com.koch.ambeth.datachange.transfer.DataChangeEntry;
import com.koch.ambeth.datachange.transfer.DataChangeEvent;
import com.koch.ambeth.event.server.IEventStore;
import com.koch.ambeth.ioc.IStartingBean;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.config.Property;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.merge.IEntityFactory;
import com.koch.ambeth.merge.cache.CacheDirective;
import com.koch.ambeth.merge.cache.ICache;
import com.koch.ambeth.merge.transfer.ObjRef;
import com.koch.ambeth.merge.util.IPrefetchHandle;
import com.koch.ambeth.merge.util.IPrefetchHelper;
import com.koch.ambeth.service.merge.IEntityMetaDataProvider;
import com.koch.ambeth.service.merge.model.IEntityMetaData;
import com.koch.ambeth.service.merge.model.IObjRef;
import com.koch.ambeth.service.metadata.Member;
import com.koch.ambeth.util.IConversionHelper;
import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.collections.HashMap;
import com.koch.ambeth.util.collections.HashSet;
import com.koch.ambeth.util.collections.IList;
import com.koch.ambeth.util.collections.IMap;
import com.koch.ambeth.util.collections.ISet;

public class DataChangeEventService implements IDataChangeEventService, IStartingBean
{
	private static final long KEEP_EVENTS_FOREVER = -1;

	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected ICache cache;

	@Autowired
	protected IConversionHelper conversionHelper;

	@Autowired
	protected IDataChangeEventDAO dataChangeEventDAO;

	@Autowired
	protected IEntityFactory entityFactory;

	@Autowired
	protected IEntityMetaDataProvider entityMetaDataProvider;

	@Autowired
	protected IEventStore eventStore;

	@Autowired
	protected IPrefetchHelper prefetchHelper;

	@Property(name = DataChangePersistenceConfigurationConstants.KeepEventsForMillis, defaultValue = KEEP_EVENTS_FOREVER + "")
	protected long keepEventsForMillis = KEEP_EVENTS_FOREVER;

	@Override
	public void afterStarted() throws Throwable
	{
		removeExpired();
		IList<IDataChange> eventObjects = retrieveAll();
		@SuppressWarnings("unchecked")
		List<Object> asObjectList = (List<Object>) (Object) eventObjects;
		eventStore.addEvents(asObjectList);
	}

	@Override
	public void save(IDataChange dataChange)
	{
		HashSet<Class<?>> entityTypes = new HashSet<Class<?>>();
		addAllEntityTypes(entityTypes, dataChange.getInserts());
		addAllEntityTypes(entityTypes, dataChange.getUpdates());
		addAllEntityTypes(entityTypes, dataChange.getDeletes());

		IMap<Class<?>, EntityType> classToEntityType = loadEntityTypes(entityTypes);

		DataChangeEventBO bo = entityFactory.createEntity(DataChangeEventBO.class);
		bo.setChangeTime(dataChange.getChangeTime());
		bo.setInserts(toDataChangeEntryList(dataChange.getInserts(), classToEntityType));
		bo.setUpdates(toDataChangeEntryList(dataChange.getUpdates(), classToEntityType));
		bo.setDeletes(toDataChangeEntryList(dataChange.getDeletes(), classToEntityType));

		dataChangeEventDAO.save(bo);
	}

	protected IList<IDataChange> retrieveAll()
	{
		List<DataChangeEventBO> bos = dataChangeEventDAO.retrieveAll();
		ArrayList<IDataChange> retrieved = new ArrayList<IDataChange>();

		IPrefetchHandle prefetchHandle = prefetchHelper.createPrefetch().add(DataChangeEventBO.class, "Inserts.EntityType")
				.add(DataChangeEventBO.class, "Updates.EntityType").add(DataChangeEventBO.class, "Deletes.EntityType").build();
		prefetchHandle.prefetch(bos);
		for (int i = 0, size = bos.size(); i < size; i++)
		{
			DataChangeEventBO bo = bos.get(i);
			DataChangeEvent entity = toEntity(bo);
			retrieved.add(entity);
		}

		return retrieved;
	}

	protected void removeExpired()
	{
		if (keepEventsForMillis == KEEP_EVENTS_FOREVER)
		{
			return;
		}
		long time = System.currentTimeMillis() - keepEventsForMillis;
		dataChangeEventDAO.removeBefore(time);
	}

	protected void addAllEntityTypes(ISet<Class<?>> entityTypes, List<IDataChangeEntry> dataChangeEntries)
	{
		if (dataChangeEntries == null)
		{
			return;
		}
		for (int i = dataChangeEntries.size(); i-- > 0;)
		{
			IDataChangeEntry dataChangeEntry = dataChangeEntries.get(i);
			entityTypes.add(dataChangeEntry.getEntityType());
		}
	}

	protected IMap<Class<?>, EntityType> loadEntityTypes(ISet<Class<?>> entityTypes)
	{
		HashMap<Class<?>, EntityType> classToEntityType = new HashMap<Class<?>, EntityType>();

		IEntityMetaData metaData = entityMetaDataProvider.getMetaData(EntityType.class);
		byte idIndex = metaData.getIdIndexByMemberName("Type");
		IObjRef[] orisToGet = new IObjRef[entityTypes.size()];
		int index = 0;
		for (Class<?> entry : entityTypes)
		{
			IObjRef objRef = new ObjRef(EntityType.class, idIndex, entry, null);
			orisToGet[index++] = objRef;
		}
		IList<Object> retrieved = cache.getObjects(orisToGet, CacheDirective.none());
		for (int i = retrieved.size(); i-- > 0;)
		{
			EntityType entityType = (EntityType) retrieved.get(i);
			classToEntityType.put(entityType.getType(), entityType);
		}

		return classToEntityType;
	}

	private List<DataChangeEntryBO> toDataChangeEntryList(List<IDataChangeEntry> dataChangeEntries, IMap<Class<?>, EntityType> classToEntityType)
	{
		if (dataChangeEntries == null)
		{
			return null;
		}
		IEntityFactory entityFactory = this.entityFactory;
		List<DataChangeEntryBO> dataChangeEntryList = new ArrayList<DataChangeEntryBO>(dataChangeEntries.size());
		for (int i = 0, size = dataChangeEntries.size(); i < size; i++)
		{
			IDataChangeEntry dataChangeEntry = dataChangeEntries.get(i);

			DataChangeEntryBO bo = entityFactory.createEntity(DataChangeEntryBO.class);
			Class<?> entityClass = dataChangeEntry.getEntityType();
			EntityType entityType = classToEntityType.get(entityClass);
			if (entityType == null)
			{
				entityType = entityFactory.createEntity(EntityType.class);
				entityType.setType(entityClass);
				classToEntityType.put(entityClass, entityType);
			}
			bo.setEntityType(entityType);
			bo.setIdIndex(dataChangeEntry.getIdNameIndex());
			bo.setObjectId(dataChangeEntry.getId().toString());
			bo.setObjectVersion(dataChangeEntry.getVersion().toString());

			dataChangeEntryList.add(bo);
		}

		return dataChangeEntryList;
	}

	private DataChangeEvent toEntity(DataChangeEventBO bo)
	{
		DataChangeEvent dce = new DataChangeEvent();
		dce.setChangeTime(bo.getChangeTime());
		dce.setInserts(toDataChangeEntries(bo.getInserts()));
		dce.setUpdates(toDataChangeEntries(bo.getUpdates()));
		dce.setDeletes(toDataChangeEntries(bo.getDeletes()));

		return dce;
	}

	private List<IDataChangeEntry> toDataChangeEntries(List<DataChangeEntryBO> bos)
	{
		List<IDataChangeEntry> dataChangeEntries = new ArrayList<IDataChangeEntry>(bos.size());

		for (int i = 0, size = bos.size(); i < size; i++)
		{
			DataChangeEntryBO bo = bos.get(i);
			DataChangeEntry entry = new DataChangeEntry();
			Class<?> entityType = bo.getEntityType().getType();
			entry.setEntityType(entityType);
			byte idIndex = bo.getIdIndex();
			entry.setIdNameIndex(idIndex);

			IEntityMetaData metaData = entityMetaDataProvider.getMetaData(entityType);
			Object id = bo.getObjectId();
			Member idMember = metaData.getIdMemberByIdIndex(idIndex);
			id = conversionHelper.convertValueToType(idMember.getRealType(), id);
			entry.setId(id);
			Object version = bo.getObjectVersion();
			Member versionMember = metaData.getVersionMember();
			version = conversionHelper.convertValueToType(versionMember.getRealType(), version);
			entry.setVersion(version);

			dataChangeEntries.add(entry);
		}

		return dataChangeEntries;
	}
}
