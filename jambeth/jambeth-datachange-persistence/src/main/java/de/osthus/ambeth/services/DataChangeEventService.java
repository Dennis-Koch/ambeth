package de.osthus.ambeth.services;

import java.util.List;

import de.osthus.ambeth.cache.CacheDirective;
import de.osthus.ambeth.cache.ICache;
import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.HashMap;
import de.osthus.ambeth.collections.HashSet;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.collections.IMap;
import de.osthus.ambeth.collections.ISet;
import de.osthus.ambeth.config.DataChangePersistenceConfigurationConstants;
import de.osthus.ambeth.config.Property;
import de.osthus.ambeth.datachange.model.IDataChange;
import de.osthus.ambeth.datachange.model.IDataChangeEntry;
import de.osthus.ambeth.datachange.transfer.DataChangeEntry;
import de.osthus.ambeth.datachange.transfer.DataChangeEvent;
import de.osthus.ambeth.event.IEventStore;
import de.osthus.ambeth.ioc.IStartingBean;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.merge.IEntityFactory;
import de.osthus.ambeth.merge.IEntityMetaDataProvider;
import de.osthus.ambeth.merge.model.IEntityMetaData;
import de.osthus.ambeth.merge.model.IObjRef;
import de.osthus.ambeth.merge.transfer.ObjRef;
import de.osthus.ambeth.model.DataChangeEntryBO;
import de.osthus.ambeth.model.DataChangeEventBO;
import de.osthus.ambeth.model.EntityType;
import de.osthus.ambeth.typeinfo.ITypeInfoItem;
import de.osthus.ambeth.util.IConversionHelper;
import de.osthus.ambeth.util.IPrefetchHandle;
import de.osthus.ambeth.util.IPrefetchHelper;

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
			ITypeInfoItem idMember = metaData.getIdMemberByIdIndex(idIndex);
			id = conversionHelper.convertValueToType(idMember.getRealType(), id);
			entry.setId(id);
			Object version = bo.getObjectVersion();
			ITypeInfoItem versionMember = metaData.getVersionMember();
			version = conversionHelper.convertValueToType(versionMember.getRealType(), version);
			entry.setVersion(version);

			dataChangeEntries.add(entry);
		}

		return dataChangeEntries;
	}
}
