package de.osthus.ambeth.merge;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;

import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.HashMap;
import de.osthus.ambeth.collections.HashSet;
import de.osthus.ambeth.collections.ILinkedMap;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.collections.ISet;
import de.osthus.ambeth.collections.IdentityHashSet;
import de.osthus.ambeth.database.IDatabaseMappedListener;
import de.osthus.ambeth.event.EntityMetaDataAddedEvent;
import de.osthus.ambeth.event.EntityMetaDataRemovedEvent;
import de.osthus.ambeth.event.IEventDispatcher;
import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.ioc.IServiceContext;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.ioc.extendable.ClassExtendableContainer;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.merge.model.EntityMetaData;
import de.osthus.ambeth.merge.model.IEntityMetaData;
import de.osthus.ambeth.persistence.IDatabase;
import de.osthus.ambeth.persistence.IDirectedLink;
import de.osthus.ambeth.persistence.IField;
import de.osthus.ambeth.persistence.ITable;
import de.osthus.ambeth.persistence.Table;
import de.osthus.ambeth.typeinfo.IRelationInfoItem;
import de.osthus.ambeth.typeinfo.ITypeInfoItem;
import de.osthus.ambeth.util.ParamChecker;
import de.osthus.ambeth.xml.IXmlTypeHelper;

public class EntityMetaDataServer extends ClassExtendableContainer<IEntityMetaData> implements IEntityMetaDataProvider, IValueObjectConfigExtendable,
		IEntityMetaDataExtendable, IInitializingBean, IDatabaseMappedListener
{
	@LogInstance
	private ILogger log;

	private static final Class<?>[] EMPTY_TYPES = new Class[0];

	protected ValueObjectMap valueObjectMap;

	protected boolean firstMapping = true;

	@Autowired
	protected IServiceContext beanContext;

	@Autowired
	protected IEntityFactory entityFactory;

	@Autowired
	protected IEventDispatcher eventDispatcher;

	@Autowired
	protected IProxyHelper proxyHelper;

	@Autowired
	protected IXmlTypeHelper xmlTypeHelper;

	public EntityMetaDataServer()
	{
		super("entity meta data", "entity class");
	}

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		ParamChecker.assertNotNull(valueObjectMap, "ValueObjectMap");
	}

	public void setValueObjectMap(ValueObjectMap valueObjectMap)
	{
		this.valueObjectMap = valueObjectMap;
	}

	@Override
	public synchronized void databaseMapped(IDatabase database)
	{
		if (!firstMapping)
		{
			return;
		}
		firstMapping = false;
		HashSet<IField> alreadyHandledFields = new HashSet<IField>();
		List<Class<?>> newEntityTypes = new ArrayList<Class<?>>();
		Lock writeLock = getWriteLock();
		writeLock.lock();
		try
		{
			for (ITable table : database.getTables())
			{
				Class<?> entityType = table.getEntityType();
				Table tableImpl = (Table) table;
				if (entityType == null || table.isArchive())
				{
					continue;
				}
				Class<?> realType = proxyHelper.getRealType(entityType);
				EntityMetaData metaData = new EntityMetaData();
				metaData.setEntityType(entityType);
				metaData.setRealType(realType);
				metaData.setIdMember(table.getIdField().getMember());
				alreadyHandledFields.add(table.getIdField());
				IField versionField = table.getVersionField();
				if (versionField != null)
				{
					alreadyHandledFields.add(table.getVersionField());
					metaData.setVersionMember(versionField.getMember());
				}

				if (table.getCreatedOnField() != null)
				{
					metaData.setCreatedOnMember(table.getCreatedOnField().getMember());
				}
				if (table.getCreatedByField() != null)
				{
					metaData.setCreatedByMember(table.getCreatedByField().getMember());
				}
				if (table.getUpdatedOnField() != null)
				{
					metaData.setUpdatedOnMember(table.getUpdatedOnField().getMember());
				}
				if (table.getUpdatedByField() != null)
				{
					metaData.setUpdatedByMember(table.getUpdatedByField().getMember());
				}

				IField[] alternateIdFields = table.getAlternateIdFields();
				ITypeInfoItem[] alternateIdMembers = new ITypeInfoItem[alternateIdFields.length];
				for (int b = alternateIdFields.length; b-- > 0;)
				{
					IField alternateIdField = alternateIdFields[b];
					alternateIdMembers[b] = alternateIdField.getMember();
				}
				ArrayList<ITypeInfoItem> fulltextMembers = new ArrayList<ITypeInfoItem>();
				ArrayList<ITypeInfoItem> primitiveMembers = new ArrayList<ITypeInfoItem>();
				HashSet<IRelationInfoItem> relationMembers = new HashSet<IRelationInfoItem>();

				// IField[] fulltextFieldsContains = table.getFulltextFieldsContains();
				// for (int a = 0; a < fulltextFieldsContains.length; a++)
				// {
				// IField field = fulltextFieldsContains[a];
				// ITypeInfoItem member = field.getMember();
				// if (member != null)
				// {
				// fulltextMembers.add(member);
				// }
				// }
				// IField[] fulltextFieldsCatsearch = table.getFulltextFieldsCatsearch();
				// for (int a = 0; a < fulltextFieldsCatsearch.length; a++)
				// {
				// IField field = fulltextFieldsCatsearch[a];
				List<IField> fulltextFields = table.getFulltextFields();
				for (int a = 0; a < fulltextFields.size(); a++)
				{
					IField field = fulltextFields.get(a);
					ITypeInfoItem member = field.getMember();
					if (member != null)
					{
						fulltextMembers.add(member);
					}
				}

				List<IField> fields = table.getPrimitiveFields();
				for (int a = 0; a < fields.size(); a++)
				{
					IField field = fields.get(a);

					ITypeInfoItem member = field.getMember();
					if (member == null)
					{
						continue;
					}

					if (!alreadyHandledFields.contains(field))
					{
						primitiveMembers.add(member);
					}
				}

				List<IDirectedLink> links = table.getLinks();
				for (int a = 0; a < links.size(); a++)
				{
					IDirectedLink link = links.get(a);
					if (link.getMember() == null)
					{
						continue;
					}
					Class<?> otherType = link.getToEntityType();
					relationMembers.add(link.getMember());
					if (link.getReverse().isCascadeDelete())
					{
						metaData.addCascadeDeleteType(otherType);
					}
				}

				IList<IRelationInfoItem> relationMembersList = relationMembers.toList();
				Collections.sort(relationMembersList, new Comparator<IRelationInfoItem>()
				{

					@Override
					public int compare(IRelationInfoItem o1, IRelationInfoItem o2)
					{
						return o1.getName().compareTo(o2.getName());
					}
				});

				// Order of setter calls is important
				metaData.setPrimitiveMembers(primitiveMembers.toArray(ITypeInfoItem.class));
				metaData.setFulltextMembers(fulltextMembers.toArray(ITypeInfoItem.class));
				metaData.setAlternateIdMembers(alternateIdMembers);
				metaData.setRelationMembers(relationMembersList.toArray(IRelationInfoItem.class));

				register(metaData, metaData.getEntityType());
				newEntityTypes.add(metaData.getEntityType());
			}
			initialize();
		}
		finally
		{
			writeLock.unlock();
		}
		if (newEntityTypes.size() > 0)
		{
			eventDispatcher.dispatchEvent(new EntityMetaDataAddedEvent(newEntityTypes.toArray(new Class<?>[newEntityTypes.size()])));
		}
	}

	protected void initialize()
	{
		HashMap<Class<?>, ISet<Class<?>>> typeRelatedByTypes = new HashMap<Class<?>, ISet<Class<?>>>();
		IdentityHashSet<IEntityMetaData> extensions = new IdentityHashSet<IEntityMetaData>(getExtensions().values());
		for (IEntityMetaData metaData : extensions)
		{
			ITypeInfoItem[] relationMembers = metaData.getRelationMembers();
			for (int j = relationMembers.length; j-- > 0;)
			{
				addTypeRelatedByTypes(typeRelatedByTypes, metaData.getEntityType(), relationMembers[j].getElementType());
			}
		}
		for (IEntityMetaData metaData : extensions)
		{
			ISet<Class<?>> relatedByTypes = typeRelatedByTypes.get(metaData.getEntityType());
			if (relatedByTypes == null)
			{
				relatedByTypes = new HashSet<Class<?>>();
			}
			((EntityMetaData) metaData).setTypesRelatingToThis(relatedByTypes.toArray(Class.class));
			((EntityMetaData) metaData).initialize(entityFactory);
		}
	}

	protected static void addTypeRelatedByTypes(Map<Class<?>, ISet<Class<?>>> typeRelatedByTypes, Class<?> relating, Class<?> relatedTo)
	{
		ISet<Class<?>> relatedByTypes = typeRelatedByTypes.get(relatedTo);
		if (relatedByTypes == null)
		{
			relatedByTypes = new HashSet<Class<?>>();
			typeRelatedByTypes.put(relatedTo, relatedByTypes);
		}
		relatedByTypes.add(relating);
	}

	@Override
	public IEntityMetaData getMetaData(Class<?> entityType)
	{
		return getMetaData(entityType, false);
	}

	@Override
	public IEntityMetaData getMetaData(Class<?> entityType, boolean tryOnly)
	{
		IEntityMetaData metaData = getExtension(entityType);
		if (metaData != null)
		{
			return metaData;
		}
		if (tryOnly)
		{
			return null;
		}
		Class<?> realEntityType = proxyHelper.getRealType(entityType);
		throw new IllegalArgumentException("No metadata found for entity of type " + realEntityType + " (" + entityType + ")");
	}

	@Override
	public IList<IEntityMetaData> getMetaData(List<Class<?>> entityTypes)
	{
		IProxyHelper proxyHelper = this.proxyHelper;
		ArrayList<IEntityMetaData> entityMetaData = new ArrayList<IEntityMetaData>(entityTypes.size());
		for (int a = 0, size = entityTypes.size(); a < size; a++)
		{
			Class<?> entityType = entityTypes.get(a);
			IEntityMetaData metaDataItem = getExtension(entityType);
			if (metaDataItem != null)
			{
				entityMetaData.add(metaDataItem);
				continue;
			}
			if (log.isDebugEnabled())
			{
				Class<?> realEntityType = proxyHelper.getRealType(entityType);
				if (entityType.equals(realEntityType))
				{
					log.debug("No metadata found for type: " + realEntityType + ".");
				}
				else
				{
					log.debug("No metadata found for type: " + realEntityType + " (" + entityType + ").");
				}
			}
		}
		return entityMetaData;
	}

	@Override
	public IList<Class<?>> findMappableEntityTypes()
	{
		ILinkedMap<Class<?>, IValueObjectConfig> targetExtensionMap = valueObjectMap.getExtensions();
		HashSet<Class<?>> mappableEntitiesSet = HashSet.create(targetExtensionMap.size());
		for (Entry<Class<?>, IValueObjectConfig> entry : targetExtensionMap)
		{
			IValueObjectConfig voConfig = entry.getValue();
			mappableEntitiesSet.add(voConfig.getEntityType());
		}
		return new ArrayList<Class<?>>(mappableEntitiesSet);
	}

	@Override
	public void registerEntityMetaData(IEntityMetaData entityMetaData)
	{
		registerEntityMetaData(entityMetaData, entityMetaData.getEntityType());
	}

	@Override
	public void registerEntityMetaData(IEntityMetaData entityMetaData, Class<?> entityType)
	{
		Lock writeLock = getWriteLock();
		writeLock.lock();
		try
		{
			register(entityMetaData, entityType);
			initialize();
		}
		finally
		{
			writeLock.unlock();
		}
		eventDispatcher.dispatchEvent(new EntityMetaDataAddedEvent(entityType));
	}

	@Override
	public void unregisterEntityMetaData(IEntityMetaData entityMetaData)
	{
		unregisterEntityMetaData(entityMetaData, entityMetaData.getEntityType());
	}

	@Override
	public void unregisterEntityMetaData(IEntityMetaData entityMetaData, Class<?> entityType)
	{
		Lock writeLock = getWriteLock();
		writeLock.lock();
		try
		{
			unregister(entityMetaData, entityType);
			initialize();
		}
		finally
		{
			writeLock.unlock();
		}
		eventDispatcher.dispatchEvent(new EntityMetaDataRemovedEvent(entityType));
	}

	@Override
	public void registerValueObjectConfig(IValueObjectConfig config)
	{
		valueObjectMap.register(config, config.getValueType());
	}

	@Override
	public void unregisterValueObjectConfig(IValueObjectConfig config)
	{
		valueObjectMap.unregister(config, config.getValueType());
	}

	@Override
	public IValueObjectConfig getValueObjectConfig(Class<?> valueType)
	{
		return valueObjectMap.getExtension(valueType);
	}

	@Override
	public IValueObjectConfig getValueObjectConfig(String xmlName)
	{
		Class<?> valueType = xmlTypeHelper.getType(xmlName);
		return getValueObjectConfig(valueType);
	}

	@Override
	public Class<?>[] getEntityPersistOrder()
	{
		return EMPTY_TYPES;
	}

	@Override
	public List<Class<?>> getValueObjectTypesByEntityType(Class<?> entityType)
	{
		List<Class<?>> valueObjectTypes = valueObjectMap.getValueObjectTypesByEntityType(entityType);
		if (valueObjectTypes == null)
		{
			valueObjectTypes = Collections.emptyList();
		}
		return valueObjectTypes;
	}
}
