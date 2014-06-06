package de.osthus.ambeth.merge;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;

import javax.persistence.PostLoad;
import javax.persistence.PrePersist;

import de.osthus.ambeth.accessor.AbstractAccessor;
import de.osthus.ambeth.accessor.IAccessorTypeProvider;
import de.osthus.ambeth.bytecode.EntityEnhancementHint;
import de.osthus.ambeth.bytecode.IBytecodeEnhancer;
import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.HashMap;
import de.osthus.ambeth.collections.HashSet;
import de.osthus.ambeth.collections.ILinkedMap;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.collections.IMap;
import de.osthus.ambeth.collections.ISet;
import de.osthus.ambeth.collections.IdentityHashSet;
import de.osthus.ambeth.event.EntityMetaDataAddedEvent;
import de.osthus.ambeth.event.EntityMetaDataRemovedEvent;
import de.osthus.ambeth.event.IEventDispatcher;
import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.ioc.IServiceContext;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.ioc.extendable.ClassExtendableContainer;
import de.osthus.ambeth.ioc.extendable.ClassExtendableListContainer;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.merge.model.EntityMetaData;
import de.osthus.ambeth.merge.model.IEntityLifecycleExtendable;
import de.osthus.ambeth.merge.model.IEntityLifecycleExtension;
import de.osthus.ambeth.merge.model.IEntityMetaData;
import de.osthus.ambeth.merge.model.PostLoadMethodLifecycleExtension;
import de.osthus.ambeth.merge.model.PrePersistMethodLifecycleExtension;
import de.osthus.ambeth.typeinfo.AbstractPropertyInfo;
import de.osthus.ambeth.typeinfo.IPropertyInfo;
import de.osthus.ambeth.typeinfo.IPropertyInfoProvider;
import de.osthus.ambeth.typeinfo.IRelationInfoItem;
import de.osthus.ambeth.typeinfo.ITypeInfoItem;
import de.osthus.ambeth.typeinfo.ITypeInfoProvider;
import de.osthus.ambeth.typeinfo.MethodPropertyInfoASM2;
import de.osthus.ambeth.typeinfo.PropertyInfoItem;
import de.osthus.ambeth.util.EqualsUtil;
import de.osthus.ambeth.util.ImmutableTypeSet;
import de.osthus.ambeth.util.ReflectUtil;
import de.osthus.ambeth.xml.IXmlTypeHelper;

public class EntityMetaDataProvider extends ClassExtendableContainer<IEntityMetaData> implements IEntityMetaDataProvider, IEntityMetaDataExtendable,
		IEntityLifecycleExtendable, IValueObjectConfigExtendable, IInitializingBean
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected IAccessorTypeProvider accessorTypeProvider;

	@Autowired
	protected IServiceContext beanContext;

	@Autowired
	protected IBytecodeEnhancer bytecodeEnhancer;

	@Autowired(optional = true)
	protected IEntityFactory entityFactory;

	@Autowired
	protected IEventDispatcher eventDispatcher;

	@Autowired
	protected IPropertyInfoProvider propertyInfoProvider;

	@Autowired
	protected ITypeInfoProvider typeInfoProvider;

	@Autowired
	protected IXmlTypeHelper xmlTypeHelper;

	@Autowired
	protected ValueObjectMap valueObjectMap;

	protected Class<?>[] businessObjectSaveOrder;

	protected final IMap<Class<?>, IMap<String, ITypeInfoItem>> typeToPropertyMap = new HashMap<Class<?>, IMap<String, ITypeInfoItem>>();

	protected final ClassExtendableListContainer<IEntityLifecycleExtension> entityLifecycleExtensions = new ClassExtendableListContainer<IEntityLifecycleExtension>(
			"entityLifecycleExtension", "entityType");

	public EntityMetaDataProvider()
	{
		super("entity meta data", "entity class");
	}

	@Override
	public void afterPropertiesSet() throws Throwable
	{
	}

	protected void addTypeRelatedByTypes(Map<Class<?>, ISet<Class<?>>> typeRelatedByTypes, Class<?> relating, Class<?> relatedTo)
	{
		ISet<Class<?>> relatedByTypes = typeRelatedByTypes.get(relatedTo);
		if (relatedByTypes == null)
		{
			relatedByTypes = new HashSet<Class<?>>();
			typeRelatedByTypes.put(relatedTo, relatedByTypes);
		}
		relatedByTypes.add(relating);
	}

	protected void initialize()
	{
		HashMap<Class<?>, ISet<Class<?>>> typeRelatedByTypes = new HashMap<Class<?>, ISet<Class<?>>>();
		IdentityHashSet<IEntityMetaData> extensions = new IdentityHashSet<IEntityMetaData>(getExtensions().values());
		for (IEntityMetaData metaData : extensions)
		{
			for (IRelationInfoItem relationMember : metaData.getRelationMembers())
			{
				addTypeRelatedByTypes(typeRelatedByTypes, metaData.getEntityType(), relationMember.getElementType());
			}
		}
		for (IEntityMetaData metaData : extensions)
		{
			Class<?> entityType = metaData.getEntityType();
			ISet<Class<?>> relatedByTypes = typeRelatedByTypes.get(entityType);
			if (relatedByTypes == null)
			{
				relatedByTypes = new HashSet<Class<?>>();
			}
			((EntityMetaData) metaData).setTypesRelatingToThis(relatedByTypes.toArray(Class.class));
			((EntityMetaData) metaData).setRealType(bytecodeEnhancer.getEnhancedType(entityType, EntityEnhancementHint.HOOK));

			refreshMembers(metaData);

			((EntityMetaData) metaData).initialize(entityFactory);

		}
	}

	protected void refreshMembers(IEntityMetaData metaData)
	{
		for (ITypeInfoItem member : metaData.getRelationMembers())
		{
			refreshMember(metaData, member);
		}
		for (ITypeInfoItem member : metaData.getPrimitiveMembers())
		{
			refreshMember(metaData, member);
		}
		refreshMember(metaData, metaData.getIdMember());
		refreshMember(metaData, metaData.getVersionMember());

		updateEntityMetaDataWithLifecycleExtensions(metaData);
	}

	protected void refreshMember(IEntityMetaData metaData, ITypeInfoItem member)
	{
		if (!(member instanceof PropertyInfoItem))
		{
			return;
		}
		PropertyInfoItem pMember = (PropertyInfoItem) member;
		AbstractPropertyInfo propertyInfo = (AbstractPropertyInfo) pMember.getProperty();
		propertyInfo.refreshAccessors(metaData.getRealType());
		if (propertyInfo instanceof MethodPropertyInfoASM2)
		{
			AbstractAccessor accessor = accessorTypeProvider.getAccessorType(metaData.getRealType(), member.getName());
			((MethodPropertyInfoASM2) propertyInfo).setAccessor(accessor);
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
		IEntityMetaData metaDataItem = getExtension(entityType);

		if (metaDataItem == null)
		{
			if (tryOnly)
			{
				return null;
			}
			throw new IllegalArgumentException("No metadata found for entity of type " + entityType);
		}
		return metaDataItem;
	}

	@Override
	public IList<IEntityMetaData> getMetaData(List<Class<?>> entityTypes)
	{
		ArrayList<IEntityMetaData> entityMetaData = new ArrayList<IEntityMetaData>(entityTypes.size());
		ArrayList<Class<?>> notFoundEntityTypes = new ArrayList<Class<?>>();
		for (Class<?> entityType : entityTypes)
		{
			IEntityMetaData metaDataItem = getExtension(entityType);

			if (metaDataItem != null)
			{
				entityMetaData.add(metaDataItem);
			}
			else
			{
				notFoundEntityTypes.add(entityType);
			}
		}
		if (notFoundEntityTypes.size() > 0 && log.isWarnEnabled())
		{
			Collections.sort(notFoundEntityTypes, new Comparator<Class<?>>()
			{
				@Override
				public int compare(Class<?> o1, Class<?> o2)
				{
					return o1.getName().compareTo(o2.getName());
				}
			});
			StringBuilder sb = new StringBuilder();
			sb.append("No metadata found for ").append(notFoundEntityTypes.size()).append(" type(s):");
			for (Class<?> notFoundType : notFoundEntityTypes)
			{
				sb.append("\n\t").append(notFoundType.getName());
			}
			log.warn(sb);
		}
		return entityMetaData;
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
	public IValueObjectConfig getValueObjectConfig(String xmlTypeName)
	{
		Class<?> valueType = xmlTypeHelper.getType(xmlTypeName);
		return getValueObjectConfig(valueType);
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
	public void register(IEntityMetaData extension, Class<?> key)
	{
		Lock writeLock = getWriteLock();
		writeLock.lock();
		try
		{
			super.register(extension, key);
			updateEntityMetaDataWithLifecycleExtensions(extension);
		}
		finally
		{
			writeLock.unlock();
		}
	}

	@Override
	public void unregister(IEntityMetaData extension, Class<?> key)
	{
		Lock writeLock = getWriteLock();
		writeLock.lock();
		try
		{
			super.unregister(extension, key);
			cleanEntityMetaDataFromLifecycleExtensions(extension);
		}
		finally
		{
			writeLock.unlock();
		}
	}

	@Override
	public void registerEntityLifecycleExtension(IEntityLifecycleExtension entityLifecycleExtension, Class<?> entityType)
	{
		Lock writeLock = getWriteLock();
		writeLock.lock();
		try
		{
			entityLifecycleExtensions.register(entityLifecycleExtension, entityType);
			updateAllEntityMetaDataWithLifecycleExtensions();
		}
		finally
		{
			writeLock.unlock();
		}
	}

	@Override
	public void unregisterEntityLifecycleExtension(IEntityLifecycleExtension entityLifecycleExtension, Class<?> entityType)
	{
		Lock writeLock = getWriteLock();
		writeLock.lock();
		try
		{
			entityLifecycleExtensions.unregister(entityLifecycleExtension, entityType);
			updateAllEntityMetaDataWithLifecycleExtensions();
		}
		finally
		{
			writeLock.unlock();
		}
	}

	protected void cleanEntityMetaDataFromLifecycleExtensions(IEntityMetaData entityMetaData)
	{
		((EntityMetaData) entityMetaData).setEntityLifecycleExtensions(null);
	}

	@SuppressWarnings("unchecked")
	protected void updateEntityMetaDataWithLifecycleExtensions(IEntityMetaData entityMetaData)
	{
		IList<IEntityLifecycleExtension> extensionList = entityLifecycleExtensions.getExtensions(entityMetaData.getEntityType());
		ArrayList<IEntityLifecycleExtension> allExtensions = new ArrayList<IEntityLifecycleExtension>(extensionList);
		ArrayList<Method> prePersistMethods = new ArrayList<Method>();
		fillMethodsAnnotatedWith(entityMetaData.getRealType(), prePersistMethods, PrePersist.class);

		ArrayList<Method> postLoadMethods = new ArrayList<Method>();
		fillMethodsAnnotatedWith(entityMetaData.getRealType(), postLoadMethods, PostLoad.class);

		for (Method prePersistMethod : prePersistMethods)
		{
			PrePersistMethodLifecycleExtension extension = beanContext.registerAnonymousBean(PrePersistMethodLifecycleExtension.class)
					.propertyValue("Method", prePersistMethod).finish();
			allExtensions.add(extension);
		}
		for (Method postLoadMethod : postLoadMethods)
		{
			PostLoadMethodLifecycleExtension extension = beanContext.registerAnonymousBean(PostLoadMethodLifecycleExtension.class)
					.propertyValue("Method", postLoadMethod).finish();
			allExtensions.add(extension);
		}
		((EntityMetaData) entityMetaData).setEntityLifecycleExtensions(allExtensions.toArray(IEntityLifecycleExtension.class));
	}

	protected void updateAllEntityMetaDataWithLifecycleExtensions()
	{
		ILinkedMap<Class<?>, IEntityMetaData> typeToMetaDataMap = getExtensions();
		for (Entry<Class<?>, IEntityMetaData> entry : typeToMetaDataMap)
		{
			updateEntityMetaDataWithLifecycleExtensions(entry.getValue());
		}
	}

	protected void fillMethodsAnnotatedWith(Class<?> type, List<Method> methods, Class<? extends Annotation>... annotations)
	{
		if (type == null || Object.class.equals(type))
		{
			return;
		}
		fillMethodsAnnotatedWith(type.getSuperclass(), methods, annotations);
		Method[] allMethodsOfThisType = ReflectUtil.getDeclaredMethods(type);
		for (int a = 0, size = allMethodsOfThisType.length; a < size; a++)
		{
			Method currentMethod = allMethodsOfThisType[a];
			for (int b = annotations.length; b-- > 0;)
			{
				if (!currentMethod.isAnnotationPresent(annotations[b]))
				{
					continue;
				}
				if (currentMethod.getParameterTypes().length != 0)
				{
					throw new IllegalArgumentException("It is not allowed to annotated methods without " + annotations[b].getName() + " having 0 arguments: "
							+ currentMethod.toString());
				}
				currentMethod.setAccessible(true);
				methods.add(currentMethod);
			}
		}
	}

	protected void initializeValueObjectMapping()
	{
		Lock writeLock = getWriteLock();
		writeLock.lock();
		try
		{
			this.businessObjectSaveOrder = null;

			HashMap<Class<?>, ISet<Class<?>>> boTypeToBeforeBoTypes = new HashMap<Class<?>, ISet<Class<?>>>();
			HashMap<Class<?>, ISet<Class<?>>> boTypeToAfterBoTypes = new HashMap<Class<?>, ISet<Class<?>>>();

			for (Entry<Class<?>, IValueObjectConfig> entry : valueObjectMap.getExtensions())
			{
				IValueObjectConfig voConfig = entry.getValue();
				Class<?> entityType = voConfig.getEntityType();
				Class<?> valueType = voConfig.getValueType();
				IEntityMetaData metaData = getMetaData(entityType);

				if (metaData == null)
				{
					// Currently no bo metadata found. We can do nothing here
					return;
				}
				Map<String, ITypeInfoItem> boNameToVoMember = getTypeInfoMapForVo(valueType);

				for (IRelationInfoItem boMember : metaData.getRelationMembers())
				{
					String boMemberName = boMember.getName();
					String voMemberName = voConfig.getValueObjectMemberName(boMemberName);
					ITypeInfoItem voMember = boNameToVoMember.get(boMemberName);
					if (voConfig.isIgnoredMember(voMemberName) || voMember == null)
					{
						continue;
					}
					Class<?> voMemberRealType = voMember.getRealType();
					if (voConfig.holdsListType(voMember.getName()))
					{
						IPropertyInfo[] properties = propertyInfoProvider.getProperties(voMemberRealType);
						if (properties.length != 1)
						{
							throw new IllegalArgumentException("ListTypes must have exactly one property");
						}
						voMemberRealType = typeInfoProvider.getMember(voMemberRealType, properties[0]).getRealType();
					}
					if (!ImmutableTypeSet.isImmutableType(voMemberRealType))
					{
						// vo member is either a list or a single direct relation to another VO
						// This implies that a potential service can handle both VO types as new objects at once
						continue;
					}
					// vo member only holds a id reference which implies that the related VO has to be persisted first to
					// contain an id which can be referred to. But we do NOT know the related VO here, but we know
					// the related BO where ALL potential VOs will be derived from:
					Class<?> boMemberElementType = boMember.getElementType();

					if (EqualsUtil.equals(entityType, boMemberElementType))
					{
						continue;
					}

					addBoTypeAfter(entityType, boMemberElementType, boTypeToBeforeBoTypes, boTypeToAfterBoTypes);
					addBoTypeBefore(entityType, boMemberElementType, boTypeToBeforeBoTypes, boTypeToAfterBoTypes);
				}
			}
			List<Class<?>> businessObjectSaveOrder = new ArrayList<Class<?>>();

			for (Class<?> boType : boTypeToBeforeBoTypes.keySet())
			{
				// BeforeBoType are types which have to be saved BEFORE saving the boType
				boolean added = false;
				for (int a = 0, size = businessObjectSaveOrder.size(); a < size; a++)
				{
					Class<?> orderedBoType = businessObjectSaveOrder.get(a);

					// OrderedBoType is the type currently inserted at the correct position in the save order - as far as the keyset
					// has been traversed, yet

					ISet<Class<?>> typesBeforeOrderedType = boTypeToBeforeBoTypes.get(orderedBoType);
					// typesBeforeOrderedType are types which have to be

					boolean orderedHasToBeAfterCurrent = typesBeforeOrderedType != null && typesBeforeOrderedType.contains(boType);

					if (!orderedHasToBeAfterCurrent)
					{
						// our boType has nothing to do with the orderedBoType. So we let is be at it is
						continue;
					}
					businessObjectSaveOrder.add(a, boType);
					added = true;
					break;
				}
				if (!added)
				{
					businessObjectSaveOrder.add(boType);
				}
			}
			for (Class<?> boType : boTypeToAfterBoTypes.keySet())
			{
				if (boTypeToBeforeBoTypes.containsKey(boType))
				{
					// already handled in the previous loop
					continue;
				}
				boolean added = false;
				for (int a = businessObjectSaveOrder.size(); a-- > 0;)
				{
					Class<?> orderedBoType = businessObjectSaveOrder.get(a);

					// OrderedBoType is the type currently inserted at the correct position in the save order - as far as the keyset
					// has been traversed, yet

					ISet<Class<?>> typesBeforeOrderedType = boTypeToBeforeBoTypes.get(orderedBoType);

					boolean orderedHasToBeAfterCurrent = typesBeforeOrderedType != null && typesBeforeOrderedType.contains(boType);

					if (!orderedHasToBeAfterCurrent)
					{
						// our boType has nothing to do with the orderedBoType. So we let it be as it is
						continue;
					}
					businessObjectSaveOrder.add(a, boType);
					added = true;
					break;
				}
				if (!added)
				{
					businessObjectSaveOrder.add(boType);
				}
			}
			this.businessObjectSaveOrder = businessObjectSaveOrder.toArray(new Class[businessObjectSaveOrder.size()]);
		}
		finally
		{
			writeLock.unlock();
		}
	}

	protected void addBoTypeBefore(Class<?> boType, Class<?> beforeBoType, Map<Class<?>, ISet<Class<?>>> boTypeToBeforeBoTypes,
			Map<Class<?>, ISet<Class<?>>> boTypeToAfterBoTypes)
	{
		ISet<Class<?>> beforeBoTypes = boTypeToBeforeBoTypes.get(boType);
		if (beforeBoTypes == null)
		{
			beforeBoTypes = new HashSet<Class<?>>();
			boTypeToBeforeBoTypes.put(boType, beforeBoTypes);
		}
		beforeBoTypes.add(beforeBoType);

		ISet<Class<?>> afterBoTypes = boTypeToAfterBoTypes.get(boType);
		if (afterBoTypes != null)
		{
			// Add boType as a after BO for all BOs which are BEFORE afterBoType (similar to: if 1<3 and 3<4 then 1<4)
			for (Class<?> afterBoType : afterBoTypes)
			{
				addBoTypeBefore(afterBoType, beforeBoType, boTypeToBeforeBoTypes, boTypeToAfterBoTypes);
			}
		}
	}

	protected void addBoTypeAfter(Class<?> boType, Class<?> afterBoType, Map<Class<?>, ISet<Class<?>>> boTypeBeforeBoTypes,
			Map<Class<?>, ISet<Class<?>>> boTypeToAfterBoTypes)
	{
		ISet<Class<?>> afterBoTypes = boTypeToAfterBoTypes.get(afterBoType);
		if (afterBoTypes == null)
		{
			afterBoTypes = new HashSet<Class<?>>();
			boTypeToAfterBoTypes.put(afterBoType, afterBoTypes);
		}
		afterBoTypes.add(boType);

		ISet<Class<?>> beforeBoTypes = boTypeBeforeBoTypes.get(afterBoType);
		if (beforeBoTypes != null)
		{
			// Add afterBoType as a after BO for all BOs which are BEFORE boType (similar to: if 1<3 and 3<4 then 1<4)
			for (Class<?> beforeBoType : beforeBoTypes)
			{
				addBoTypeAfter(beforeBoType, afterBoType, boTypeBeforeBoTypes, boTypeToAfterBoTypes);
			}
		}
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

	public IMap<String, ITypeInfoItem> getTypeInfoMapForVo(Class<?> valueType)
	{
		IValueObjectConfig config = getValueObjectConfig(valueType);
		if (config == null)
		{
			return null;
		}
		IMap<String, ITypeInfoItem> typeInfoMap = typeToPropertyMap.get(valueType);
		if (typeInfoMap == null)
		{
			typeInfoMap = new HashMap<String, ITypeInfoItem>();
			IEntityMetaData boMetaData = getMetaData(config.getEntityType());
			StringBuilder sb = new StringBuilder();

			addTypeInfoMapping(typeInfoMap, config, boMetaData.getIdMember().getName(), sb);
			if (boMetaData.getVersionMember() != null)
			{
				addTypeInfoMapping(typeInfoMap, config, boMetaData.getVersionMember().getName(), sb);
			}
			for (ITypeInfoItem primitiveMember : boMetaData.getPrimitiveMembers())
			{
				addTypeInfoMapping(typeInfoMap, config, primitiveMember.getName(), sb);
			}
			for (ITypeInfoItem relationMember : boMetaData.getRelationMembers())
			{
				addTypeInfoMapping(typeInfoMap, config, relationMember.getName(), null);
			}

			if (!typeToPropertyMap.putIfNotExists(config.getValueType(), typeInfoMap))
			{
				throw new IllegalStateException("Key already exists " + config.getValueType());
			}
		}
		return typeInfoMap;
	}

	protected void addTypeInfoMapping(IMap<String, ITypeInfoItem> typeInfoMap, IValueObjectConfig config, String boMemberName, StringBuilder sb)
	{
		String voMemberName = config.getValueObjectMemberName(boMemberName);
		ITypeInfoItem voMember = typeInfoProvider.getHierarchicMember(config.getValueType(), voMemberName);
		if (voMember == null)
		{
			return;
		}
		typeInfoMap.put(boMemberName, voMember);
		if (sb == null)
		{
			return;
		}
		sb.setLength(0);
		String voSpecifiedName = sb.append(voMemberName).append("Specified").toString();
		ITypeInfoItem voSpecifiedMember = typeInfoProvider.getHierarchicMember(config.getValueType(), voSpecifiedName);
		if (voSpecifiedMember == null)
		{
			return;
		}
		sb.setLength(0);
		String boSpecifiedName = sb.append(boMemberName).append("Specified").toString();
		typeInfoMap.put(boSpecifiedName, voSpecifiedMember);
	}

	@Override
	public Class<?>[] getEntityPersistOrder()
	{
		return businessObjectSaveOrder;
	}
}
