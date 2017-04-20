package com.koch.ambeth.merge;

/*-
 * #%L
 * jambeth-merge
 * %%
 * Copyright (C) 2017 Koch Softwaredevelopment
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 * #L%
 */

import java.io.IOException;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;

import javax.persistence.PostLoad;
import javax.persistence.PrePersist;

import com.koch.ambeth.dot.DotWriter;
import com.koch.ambeth.dot.IDotNode;
import com.koch.ambeth.dot.IDotWriter;
import com.koch.ambeth.event.IEventDispatcher;
import com.koch.ambeth.ioc.IInitializingBean;
import com.koch.ambeth.ioc.IServiceContext;
import com.koch.ambeth.ioc.accessor.IAccessorTypeProvider;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.bytecode.IBytecodeEnhancer;
import com.koch.ambeth.ioc.extendable.ClassExtendableContainer;
import com.koch.ambeth.ioc.extendable.ClassExtendableListContainer;
import com.koch.ambeth.ioc.extendable.MapExtendableContainer;
import com.koch.ambeth.ioc.util.ImmutableTypeSet;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.merge.bytecode.EntityEnhancementHint;
import com.koch.ambeth.merge.cache.ICacheModification;
import com.koch.ambeth.merge.event.EntityMetaDataAddedEvent;
import com.koch.ambeth.merge.event.EntityMetaDataRemovedEvent;
import com.koch.ambeth.merge.ioc.MergeModule;
import com.koch.ambeth.merge.metadata.IMemberTypeProvider;
import com.koch.ambeth.merge.model.EntityMetaData;
import com.koch.ambeth.merge.model.PostLoadMethodLifecycleExtension;
import com.koch.ambeth.merge.model.PrePersistMethodLifecycleExtension;
import com.koch.ambeth.service.merge.IEntityMetaDataProvider;
import com.koch.ambeth.service.merge.IEntityMetaDataRefresher;
import com.koch.ambeth.service.merge.IValueObjectConfig;
import com.koch.ambeth.service.merge.model.IEntityLifecycleExtendable;
import com.koch.ambeth.service.merge.model.IEntityLifecycleExtension;
import com.koch.ambeth.service.merge.model.IEntityMetaData;
import com.koch.ambeth.service.metadata.IDTOType;
import com.koch.ambeth.service.metadata.IPrimitiveMemberWrite;
import com.koch.ambeth.service.metadata.Member;
import com.koch.ambeth.service.metadata.PrimitiveMember;
import com.koch.ambeth.service.metadata.RelationMember;
import com.koch.ambeth.service.xml.IXmlTypeHelper;
import com.koch.ambeth.util.EqualsUtil;
import com.koch.ambeth.util.ReflectUtil;
import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.collections.HashMap;
import com.koch.ambeth.util.collections.HashSet;
import com.koch.ambeth.util.collections.ILinkedMap;
import com.koch.ambeth.util.collections.IList;
import com.koch.ambeth.util.collections.IMap;
import com.koch.ambeth.util.collections.ISet;
import com.koch.ambeth.util.collections.IdentityHashSet;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;
import com.koch.ambeth.util.objectcollector.IThreadLocalObjectCollector;
import com.koch.ambeth.util.proxy.IProxyFactory;
import com.koch.ambeth.util.typeinfo.IPropertyInfo;
import com.koch.ambeth.util.typeinfo.IPropertyInfoProvider;
import com.koch.ambeth.util.typeinfo.ITypeInfoItem;
import com.koch.ambeth.util.typeinfo.ITypeInfoProvider;

public class EntityMetaDataProvider extends ClassExtendableContainer<IEntityMetaData>
		implements IEntityMetaDataProvider, IEntityMetaDataRefresher, IEntityMetaDataExtendable,
		IEntityLifecycleExtendable, ITechnicalEntityTypeExtendable,
		IEntityInstantiationExtensionExtendable, IValueObjectConfigExtendable, IInitializingBean {

	private final class StringBuilderWriter extends Writer {
		private final StringBuilder sb;

		private StringBuilderWriter(StringBuilder sb) {
			this.sb = sb;
		}

		@Override
		public void write(int c) throws IOException {
			sb.append(c);
		}

		@Override
		public void write(String str) throws IOException {
			sb.append(str);
		}

		@Override
		public void write(char[] cbuf, int off, int len) throws IOException {
			sb.append(cbuf, off, len);
		}

		@Override
		public Writer append(char c) throws IOException {
			sb.append(c);
			return this;
		}

		@Override
		public Writer append(CharSequence csq) throws IOException {
			sb.append(csq);
			return this;
		}

		@Override
		public Writer append(CharSequence csq, int start, int end) throws IOException {
			sb.append(csq, start, end);
			return this;
		}

		@Override
		public void flush() throws IOException {
			// intended blank
		}

		@Override
		public void close() throws IOException {
			// intended blank
		}
	}

	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected IAccessorTypeProvider accessorTypeProvider;

	@Autowired
	protected IServiceContext beanContext;

	@Autowired
	protected IBytecodeEnhancer bytecodeEnhancer;

	@Autowired
	protected ICacheModification cacheModification;

	@Autowired(optional = true)
	protected IEntityFactory entityFactory;

	@Autowired
	protected IEventDispatcher eventDispatcher;

	@Autowired
	protected IMemberTypeProvider memberTypeProvider;

	@Autowired
	protected IThreadLocalObjectCollector objectCollector;

	@Autowired
	protected IPropertyInfoProvider propertyInfoProvider;

	@Autowired
	protected IProxyFactory proxyFactory;

	@Autowired(value = MergeModule.REMOTE_ENTITY_METADATA_PROVIDER, optional = true)
	protected IEntityMetaDataProvider remoteEntityMetaDataProvider;

	@Autowired
	protected ITypeInfoProvider typeInfoProvider;

	@Autowired
	protected IXmlTypeHelper xmlTypeHelper;

	@Autowired
	protected ValueObjectMap valueObjectMap;

	protected final ThreadLocal<ClassExtendableContainer<IEntityMetaData>> pendingToRefreshMetaDatasTL =
			new ThreadLocal<>();

	protected IEntityMetaData alreadyHandled;

	protected Class<?>[] businessObjectSaveOrder;

	protected final ClassExtendableContainer<IEntityInstantiationExtension> entityInstantiationExtensions =
			new ClassExtendableContainer<>("entityFactoryExtension", "entityType");

	protected final HashMap<Class<?>, IMap<String, ITypeInfoItem>> typeToPropertyMap =
			new HashMap<>();

	protected final ClassExtendableListContainer<IEntityLifecycleExtension> entityLifecycleExtensions =
			new ClassExtendableListContainer<>("entityLifecycleExtension", "entityType");

	protected final MapExtendableContainer<Class<?>, Class<?>> technicalEntityTypes =
			new MapExtendableContainer<>("technicalEntityType", "entityType");

	public EntityMetaDataProvider() {
		super("entity meta data", "entity class");
	}

	@Override
	public void afterPropertiesSet() throws Throwable {
		alreadyHandled = proxyFactory.createProxy(IEntityMetaData.class);
	}

	protected void addTypeRelatedByTypes(Map<Class<?>, ISet<Class<?>>> typeRelatedByTypes,
			Class<?> relating, Class<?> relatedTo) {
		IEntityMetaData metaData = getMetaData(relatedTo, true);
		if (metaData != null) {
			relatedTo = metaData.getEntityType();
		}
		ISet<Class<?>> relatedByTypes = typeRelatedByTypes.get(relatedTo);
		if (relatedByTypes == null) {
			relatedByTypes = new HashSet<>();
			typeRelatedByTypes.put(relatedTo, relatedByTypes);
		}
		relatedByTypes.add(relating);
	}

	protected void initialize() {
		HashMap<Class<?>, ISet<Class<?>>> typeRelatedByTypes = new HashMap<>();
		IdentityHashSet<IEntityMetaData> extensions = new IdentityHashSet<>(getExtensions().values());
		for (IEntityMetaData metaData : extensions) {
			if (metaData == alreadyHandled) {
				continue;
			}
			for (RelationMember relationMember : metaData.getRelationMembers()) {
				addTypeRelatedByTypes(typeRelatedByTypes, metaData.getEntityType(),
						relationMember.getElementType());
			}
		}
		for (IEntityMetaData metaData : extensions) {
			if (metaData == alreadyHandled) {
				continue;
			}
			Class<?> entityType = metaData.getEntityType();
			ISet<Class<?>> relatedByTypes = typeRelatedByTypes.get(entityType);
			if (relatedByTypes == null) {
				relatedByTypes = new HashSet<>();
			}
			((EntityMetaData) metaData).setTypesRelatingToThis(relatedByTypes.toArray(Class.class));
			refreshMembers(metaData);
		}
	}

	@Override
	public void toDotGraph(Writer writer) {
		IThreadLocalObjectCollector objectCollector = this.objectCollector.getCurrent();
		final StringBuilder sb = objectCollector.create(StringBuilder.class);
		try {
			IEntityMetaData[] extensions =
					new IdentityHashSet<>(getExtensions().values()).toArray(IEntityMetaData.class);

			IDotWriter dotWriter = new DotWriter(new StringBuilderWriter(sb));

			try {
				// writer.write("\n\tgraph [truecolor=true mindist=2 overlap=prism];");
				// writer.write("\n\tedge [len=4];");
				for (IEntityMetaData metaData : extensions) {
					if (metaData == alreadyHandled) {
						continue;
					}
					{
						IDotNode node = dotWriter.openNode(metaData);
						node.attribute("label", metaData.getEntityType().getSimpleName());
						node.attribute("shape", "ellipse");
						node.attribute("style", "filled");
						node.attribute("fontcolor", "#ffffffff");
						node.attribute("fillcolor", metaData.isLocalEntity() ? "#d0771eff" : "#ffff00ff");
						node.endNode();
					}

					for (Member member : metaData.getPrimitiveMembers()) {
						IDotNode node = dotWriter.openNode(member);
						node.attribute("label", member.getName());
						node.attribute("shape", "ellipse");
						node.attribute("style", "filled");
						node.attribute("fontcolor", "#ffffffff");
						node.attribute("fillcolor", "#0066ffff");
						node.endNode();
					}
					for (Member member : metaData.getRelationMembers()) {
						IDotNode node = dotWriter.openNode(member);
						node.attribute("label", member.getName());
						node.attribute("shape", "ellipse");
						node.attribute("style", "filled");
						node.attribute("fontcolor", "#ffffffff");
						node.attribute("fillcolor", "#0066ffff");
						node.endNode();
					}
				}
				for (IEntityMetaData metaData : extensions) {
					if (metaData == alreadyHandled) {
						continue;
					}
					for (Member member : metaData.getPrimitiveMembers()) {
						dotWriter.openEdge(metaData, member).attribute("arrowhead", "none").endEdge();
					}
					for (Member member : metaData.getRelationMembers()) {
						dotWriter.openEdge(metaData, member).attribute("arrowhead", "none").endEdge();

						IEntityMetaData targetMetaData = getMetaData(member.getElementType(), true);
						if (targetMetaData != null) {
							dotWriter.openEdge(member, targetMetaData).endEdge();
						}
					}
				}
			}
			finally {
				try {
					dotWriter.close();
				}
				catch (Throwable e) {
					throw RuntimeExceptionUtil.mask(e);
				}
			}
		}
		catch (Throwable e) {
			throw RuntimeExceptionUtil.mask(e);
		}
		finally {
			objectCollector.dispose(sb);
		}
	}

	protected String getNodeName(Object handle, IMap<Object, String> handleToNodeIdMap) {
		String nodeId = handleToNodeIdMap.get(handle);
		if (nodeId != null) {
			return nodeId;
		}
		nodeId = Integer.valueOf(handleToNodeIdMap.size() + 1).toString();
		handleToNodeIdMap.put(handle, nodeId);
		return nodeId;
	}

	@Override
	public void refreshMembers(IEntityMetaData metaData) {
		if (metaData.getEnhancedType() == null) {
			((EntityMetaData) metaData).initialize(cacheModification, entityFactory);
			IEntityInstantiationExtension eie =
					entityInstantiationExtensions.getExtension(metaData.getEntityType());
			Class<?> baseType = eie != null ? eie.getMappedEntityType(metaData.getEntityType())
					: metaData.getEntityType();
			((EntityMetaData) metaData).setEnhancedType(
					bytecodeEnhancer.getEnhancedType(baseType, EntityEnhancementHint.Instance));
		}
		RelationMember[] relationMembers = metaData.getRelationMembers();
		for (int a = relationMembers.length; a-- > 0;) {
			relationMembers[a] = (RelationMember) refreshMember(metaData, relationMembers[a]);
		}
		PrimitiveMember[] primitiveMembers = metaData.getPrimitiveMembers();
		for (int a = primitiveMembers.length; a-- > 0;) {
			primitiveMembers[a] = (PrimitiveMember) refreshMember(metaData, primitiveMembers[a]);
		}

		HashMap<String, PrimitiveMember> nameToPrimitiveMember = new HashMap<>();
		for (int a = primitiveMembers.length; a-- > 0;) {
			PrimitiveMember member = primitiveMembers[a];
			nameToPrimitiveMember.put(member.getName(), member);
		}
		PrimitiveMember[] alternateIdMembers = metaData.getAlternateIdMembers();
		for (int a = alternateIdMembers.length; a-- > 0;) {
			alternateIdMembers[a] = (PrimitiveMember) refreshMember(metaData, alternateIdMembers[a]);
		}

		((EntityMetaData) metaData).setIdMember(refreshDefinedBy(
				(PrimitiveMember) refreshMember(metaData, metaData.getIdMember()), nameToPrimitiveMember));
		((EntityMetaData) metaData).setVersionMember(
				refreshDefinedBy((PrimitiveMember) refreshMember(metaData, metaData.getVersionMember()),
						nameToPrimitiveMember));

		((EntityMetaData) metaData)
				.setUpdatedByMember(getIfExists(metaData.getUpdatedByMember(), nameToPrimitiveMember));
		((EntityMetaData) metaData)
				.setUpdatedOnMember(getIfExists(metaData.getUpdatedOnMember(), nameToPrimitiveMember));
		((EntityMetaData) metaData)
				.setCreatedByMember(getIfExists(metaData.getCreatedByMember(), nameToPrimitiveMember));
		((EntityMetaData) metaData)
				.setCreatedOnMember(getIfExists(metaData.getCreatedOnMember(), nameToPrimitiveMember));

		for (int a = primitiveMembers.length; a-- > 0;) {
			refreshDefinedBy(primitiveMembers[a], nameToPrimitiveMember);
		}
		for (int a = alternateIdMembers.length; a-- > 0;) {
			refreshDefinedBy(alternateIdMembers[a], nameToPrimitiveMember);
		}
		updateEntityMetaDataWithLifecycleExtensions(metaData);
		((EntityMetaData) metaData).initialize(cacheModification, entityFactory);
	}

	protected PrimitiveMember getIfExists(PrimitiveMember memberToRefresh,
			IMap<String, PrimitiveMember> nameToPrimitiveMember) {
		if (memberToRefresh == null) {
			return null;
		}
		return nameToPrimitiveMember.get(memberToRefresh.getName());
	}

	protected PrimitiveMember refreshDefinedBy(PrimitiveMember member,
			IMap<String, PrimitiveMember> nameToPrimitiveMember) {
		if (member == null) {
			return member;
		}
		PrimitiveMember definedBy = member.getDefinedBy();
		if (definedBy == null) {
			return member;
		}
		PrimitiveMember refreshedDefinedBy = nameToPrimitiveMember.get(definedBy.getName());
		if (refreshedDefinedBy == null) {
			throw new IllegalStateException("Must never happen");
		}
		((IPrimitiveMemberWrite) member).setDefinedBy(refreshedDefinedBy);
		return member;
	}

	protected Member refreshMember(IEntityMetaData metaData, Member member) {
		if (member == null) {
			return null;
		}
		if (member instanceof RelationMember) {
			return memberTypeProvider.getRelationMember(metaData.getEnhancedType(), member.getName());
		}
		PrimitiveMember refreshedMember =
				memberTypeProvider.getPrimitiveMember(metaData.getEnhancedType(), member.getName());
		((IPrimitiveMemberWrite) refreshedMember)
				.setTechnicalMember(((PrimitiveMember) member).isTechnicalMember());
		((IPrimitiveMemberWrite) refreshedMember)
				.setTransient(((PrimitiveMember) member).isTransient());
		((IPrimitiveMemberWrite) refreshedMember)
				.setDefinedBy(((PrimitiveMember) member).getDefinedBy());
		return refreshedMember;
	}

	protected IList<Class<?>> addLoadedMetaData(List<Class<?>> entityTypes,
			List<IEntityMetaData> loadedMetaData) {
		HashSet<Class<?>> cascadeMissingEntityTypes = null;
		Lock writeLock = getWriteLock();
		writeLock.lock();
		try {
			for (int a = loadedMetaData.size(); a-- > 0;) {
				IEntityMetaData missingMetaDataItem = loadedMetaData.get(a);
				Class<?> entityType = missingMetaDataItem.getEntityType();
				IEntityMetaData existingMetaData = getExtensionHardKey(entityType);
				if (existingMetaData != null && existingMetaData != alreadyHandled) {
					continue;
				}
				pendingToRefreshMetaDatasTL.get().register(missingMetaDataItem, entityType);
			}
			for (int a = loadedMetaData.size(); a-- > 0;) {
				IEntityMetaData missingMetaDataItem = loadedMetaData.get(a);
				for (RelationMember relationMember : missingMetaDataItem.getRelationMembers()) {
					Class<?> relationMemberType = relationMember.getElementType();
					if (!containsKey(relationMemberType)) {
						if (cascadeMissingEntityTypes == null) {
							cascadeMissingEntityTypes = new HashSet<>();
						}
						cascadeMissingEntityTypes.add(relationMemberType);
					}
				}
			}
			return cascadeMissingEntityTypes != null ? cascadeMissingEntityTypes.toList() : null;
		}
		finally {
			writeLock.unlock();
		}
	}

	@Override
	public IEntityMetaData getExtensionHardKey(Class<?> key) {
		if (ImmutableTypeSet.isImmutableType(key) || key.isArray()
				|| IDTOType.class.isAssignableFrom(key) || Collection.class.isAssignableFrom(key)) {
			return alreadyHandled;
		}
		IEntityMetaData metaData = super.getExtensionHardKey(key);
		if (metaData != null) {
			return metaData;
		}
		ClassExtendableContainer<IEntityMetaData> pendingToRefreshMetaDatas =
				pendingToRefreshMetaDatasTL.get();
		if (pendingToRefreshMetaDatas == null) {
			return null;
		}
		return pendingToRefreshMetaDatas.getExtensionHardKey(key);
	}

	public IEntityMetaData getExtensionHardKeyGlobalOnly(Class<?> key) {
		return super.getExtensionHardKey(key);
	}

	@Override
	public IEntityMetaData getExtension(Class<?> key) {
		IEntityMetaData metaData = super.getExtension(key);
		if (metaData != null) {
			return metaData;
		}
		ClassExtendableContainer<IEntityMetaData> pendingToRefreshMetaDatas =
				pendingToRefreshMetaDatasTL.get();
		if (pendingToRefreshMetaDatas == null) {
			return null;
		}
		return pendingToRefreshMetaDatas.getExtension(key);
	}

	@Override
	public IList<IEntityMetaData> getExtensions(Class<?> key) {
		throw new UnsupportedOperationException();
	}

	public boolean containsKey(Class<?> key) {
		boolean contains = super.containsKey(key);
		if (contains) {
			return true;
		}
		ClassExtendableContainer<IEntityMetaData> pendingToRefreshMetaDatas =
				pendingToRefreshMetaDatasTL.get();
		if (pendingToRefreshMetaDatas == null) {
			return contains;
		}
		return pendingToRefreshMetaDatas.containsKey(key);
	}

	@Override
	public IEntityMetaData getMetaData(Class<?> entityType) {
		return getMetaData(entityType, false);
	}

	@Override
	public IEntityMetaData getMetaData(Class<?> entityType, boolean tryOnly) {
		IEntityMetaData metaDataItem = getExtensionHardKey(entityType);
		if (metaDataItem != null) {
			if (metaDataItem == alreadyHandled) {
				if (tryOnly) {
					return null;
				}
				throw new IllegalArgumentException(
						"No metadata found for entity of type " + entityType.getName());
			}
			return metaDataItem;
		}
		ArrayList<Class<?>> missingEntityTypes = new ArrayList<>(1);
		missingEntityTypes.add(entityType);
		IList<IEntityMetaData> missingMetaDatas = getMetaData(missingEntityTypes);
		if (missingMetaDatas.size() > 0) {
			IEntityMetaData metaData = missingMetaDatas.get(0);
			if (metaData != null) {
				return metaData;
			}
		}
		if (tryOnly) {
			return null;
		}
		throw new IllegalArgumentException(
				"No metadata found for entity of type " + entityType.getName());
	}

	@Override
	public IList<IEntityMetaData> getMetaData(List<Class<?>> entityTypes) {
		return getMetaData(entityTypes, true);
	}

	protected IList<IEntityMetaData> getMetaData(List<Class<?>> entityTypes,
			boolean askRemoteOnMiss) {
		ArrayList<IEntityMetaData> result = new ArrayList<>(entityTypes.size());
		IList<Class<?>> missingEntityTypes = null;
		for (int a = entityTypes.size(); a-- > 0;) {
			Class<?> entityType = entityTypes.get(a);
			IEntityMetaData metaDataItem = getExtension(entityType);
			if (metaDataItem == alreadyHandled) {
				metaDataItem = getExtensionHardKey(entityType);
				if (metaDataItem == null && askRemoteOnMiss) {
					if (missingEntityTypes == null) {
						missingEntityTypes = new ArrayList<>();
					}
					missingEntityTypes.add(entityType);
				}
				continue;
			}
			if (metaDataItem == null) {
				if (askRemoteOnMiss) {
					if (missingEntityTypes == null) {
						missingEntityTypes = new ArrayList<>();
					}
					missingEntityTypes.add(entityType);
				}
				continue;
			}
			result.add(metaDataItem);
		}
		if (missingEntityTypes == null || remoteEntityMetaDataProvider == null) {
			return result;
		}
		boolean handlePendingMetaData = false;
		try {
			ClassExtendableContainer<IEntityMetaData> pendingToRefreshMetaDatas =
					pendingToRefreshMetaDatasTL.get();
			if (pendingToRefreshMetaDatas == null) {
				pendingToRefreshMetaDatas = new ClassExtendableContainer<>("metaData", "entityType");
				pendingToRefreshMetaDatasTL.set(pendingToRefreshMetaDatas);
				handlePendingMetaData = true;
			}
			while (missingEntityTypes != null && missingEntityTypes.size() > 0) {
				IList<IEntityMetaData> loadedMetaData =
						remoteEntityMetaDataProvider.getMetaData(missingEntityTypes);

				IList<Class<?>> cascadeMissingEntityTypes =
						addLoadedMetaData(missingEntityTypes, loadedMetaData);

				if (cascadeMissingEntityTypes != null && cascadeMissingEntityTypes.size() > 0) {
					missingEntityTypes = cascadeMissingEntityTypes;
				}
				else {
					missingEntityTypes.clear();
				}
			}
			if (handlePendingMetaData) {
				ILinkedMap<Class<?>, IEntityMetaData> extensions =
						pendingToRefreshMetaDatas.getExtensions();
				for (Entry<Class<?>, IEntityMetaData> entry : extensions) {
					IEntityMetaData metaData = entry.getValue();
					if (metaData == alreadyHandled) {
						continue;
					}
					refreshMembers(metaData);
				}
				Lock writeLock = getWriteLock();
				writeLock.lock();
				try {
					for (Entry<Class<?>, IEntityMetaData> entry : pendingToRefreshMetaDatas.getExtensions()) {
						Class<?> entityType = entry.getKey();
						IEntityMetaData existingMetaData = getExtensionHardKeyGlobalOnly(entityType);
						if (existingMetaData != null && existingMetaData != alreadyHandled) {
							// existing entry is already a valid non-null entry
							continue;
						}
						IEntityMetaData ownMetaData = entry.getValue();
						if (existingMetaData == ownMetaData) {
							// existing entry is already a null-entry and our entry is a null-entry, too - so
							// nothing to do
							continue;
						}
						if (existingMetaData == alreadyHandled) {
							unregister(alreadyHandled, entityType);
						}
						register(ownMetaData, entityType);
					}
				}
				finally {
					writeLock.unlock();
				}
			}
		}
		finally {
			if (handlePendingMetaData) {
				pendingToRefreshMetaDatasTL.remove();
			}
		}
		return getMetaData(entityTypes, false);
	}

	@Override
	public void registerValueObjectConfig(IValueObjectConfig config) {
		valueObjectMap.register(config, config.getValueType());
	}

	@Override
	public void unregisterValueObjectConfig(IValueObjectConfig config) {
		valueObjectMap.unregister(config, config.getValueType());
	}

	@Override
	public IValueObjectConfig getValueObjectConfig(Class<?> valueType) {
		return valueObjectMap.getExtension(valueType);
	}

	@Override
	public IValueObjectConfig getValueObjectConfig(String xmlTypeName) {
		Class<?> valueType = xmlTypeHelper.getType(xmlTypeName);
		return getValueObjectConfig(valueType);
	}

	@Override
	public List<Class<?>> getValueObjectTypesByEntityType(Class<?> entityType) {
		List<Class<?>> valueObjectTypes = valueObjectMap.getValueObjectTypesByEntityType(entityType);
		if (valueObjectTypes == null) {
			valueObjectTypes = Collections.emptyList();
		}
		return valueObjectTypes;
	}

	@Override
	public void registerEntityMetaData(IEntityMetaData entityMetaData) {
		registerEntityMetaData(entityMetaData, entityMetaData.getEntityType());
	}

	@Override
	public void registerEntityMetaData(IEntityMetaData entityMetaData, Class<?> entityType) {
		Lock writeLock = getWriteLock();
		writeLock.lock();
		try {
			register(entityMetaData, entityType);
			initialize();
		}
		finally {
			writeLock.unlock();
		}
		eventDispatcher.dispatchEvent(new EntityMetaDataAddedEvent(entityType));
	}

	@Override
	public void unregisterEntityMetaData(IEntityMetaData entityMetaData) {
		unregisterEntityMetaData(entityMetaData, entityMetaData.getEntityType());
	}

	@Override
	public void unregisterEntityMetaData(IEntityMetaData entityMetaData, Class<?> entityType) {
		Lock writeLock = getWriteLock();
		writeLock.lock();
		try {
			unregister(entityMetaData, entityType);
			initialize();
		}
		finally {
			writeLock.unlock();
		}
		eventDispatcher.dispatchEvent(new EntityMetaDataRemovedEvent(entityType));
	}

	@Override
	public void register(IEntityMetaData extension, Class<?> entityType) {
		Lock writeLock = getWriteLock();
		writeLock.lock();
		try {
			super.register(extension, entityType);
			updateEntityMetaDataWithLifecycleExtensions(extension);
			Class<?> technicalEntityType = technicalEntityTypes.getExtension(entityType);
			if (technicalEntityType != null) {
				super.register(extension, technicalEntityType);
			}
		}
		finally {
			writeLock.unlock();
		}
	}

	@Override
	public void unregister(IEntityMetaData extension, Class<?> entityType) {
		Lock writeLock = getWriteLock();
		writeLock.lock();
		try {
			Class<?> technicalEntityType = technicalEntityTypes.getExtension(entityType);
			if (technicalEntityType != null) {
				super.unregister(extension, technicalEntityType);
			}
			super.unregister(extension, entityType);
			cleanEntityMetaDataFromLifecycleExtensions(extension);
		}
		finally {
			writeLock.unlock();
		}
	}

	@Override
	public Class<?> getEntityTypeForTechnicalEntity(Class<?> technicalEntitiyType) {
		IEntityMetaData metaData = this.getMetaData(technicalEntitiyType);
		return metaData == null ? null : metaData.getEntityType();
	}

	@Override
	public void registerTechnicalEntityType(Class<?> technicalEntityType, Class<?> entityType) {
		Lock writeLock = getWriteLock();
		writeLock.lock();
		try {
			technicalEntityTypes.register(technicalEntityType, entityType);
			IEntityMetaData metaData = getExtensionHardKey(entityType);
			if (metaData != null) {
				super.register(metaData, technicalEntityType);
			}
		}
		finally {
			writeLock.unlock();
		}
	}

	@Override
	public void unregisterTechnicalEntityType(Class<?> technicalEntityType, Class<?> entityType) {
		Lock writeLock = getWriteLock();
		writeLock.lock();
		try {
			technicalEntityTypes.unregister(technicalEntityType, entityType);
			IEntityMetaData metaData = getExtensionHardKey(entityType);
			if (metaData != null) {
				super.unregister(metaData, technicalEntityType);
			}
		}
		finally {
			writeLock.unlock();
		}
	}

	@Override
	public void registerEntityLifecycleExtension(IEntityLifecycleExtension entityLifecycleExtension,
			Class<?> entityType) {
		Lock writeLock = getWriteLock();
		writeLock.lock();
		try {
			entityLifecycleExtensions.register(entityLifecycleExtension, entityType);
			updateAllEntityMetaDataWithLifecycleExtensions();
		}
		finally {
			writeLock.unlock();
		}
	}

	@Override
	public void unregisterEntityLifecycleExtension(IEntityLifecycleExtension entityLifecycleExtension,
			Class<?> entityType) {
		Lock writeLock = getWriteLock();
		writeLock.lock();
		try {
			entityLifecycleExtensions.unregister(entityLifecycleExtension, entityType);
			updateAllEntityMetaDataWithLifecycleExtensions();
		}
		finally {
			writeLock.unlock();
		}
	}

	protected void cleanEntityMetaDataFromLifecycleExtensions(IEntityMetaData entityMetaData) {
		((EntityMetaData) entityMetaData).setEntityLifecycleExtensions(null);
	}

	@SuppressWarnings("unchecked")
	protected void updateEntityMetaDataWithLifecycleExtensions(IEntityMetaData entityMetaData) {
		if (entityMetaData == alreadyHandled) {
			return;
		}
		if (entityMetaData.getEnhancedType() == null) {
			return;
		}
		IList<IEntityLifecycleExtension> extensionList =
				entityLifecycleExtensions.getExtensions(entityMetaData.getEnhancedType());
		ArrayList<IEntityLifecycleExtension> allExtensions = new ArrayList<>(extensionList);
		ArrayList<Method> prePersistMethods = new ArrayList<>();
		fillMethodsAnnotatedWith(entityMetaData.getEnhancedType(), prePersistMethods, PrePersist.class);

		ArrayList<Method> postLoadMethods = new ArrayList<>();
		fillMethodsAnnotatedWith(entityMetaData.getEnhancedType(), postLoadMethods, PostLoad.class);

		for (Method prePersistMethod : prePersistMethods) {
			PrePersistMethodLifecycleExtension extension =
					beanContext.registerBean(PrePersistMethodLifecycleExtension.class)
							.propertyValue("Method", prePersistMethod).finish();
			allExtensions.add(extension);
		}
		for (Method postLoadMethod : postLoadMethods) {
			PostLoadMethodLifecycleExtension extension =
					beanContext.registerBean(PostLoadMethodLifecycleExtension.class)
							.propertyValue("Method", postLoadMethod).finish();
			allExtensions.add(extension);
		}
		((EntityMetaData) entityMetaData)
				.setEntityLifecycleExtensions(allExtensions.toArray(IEntityLifecycleExtension.class));
	}

	protected void updateAllEntityMetaDataWithLifecycleExtensions() {
		ILinkedMap<Class<?>, IEntityMetaData> typeToMetaDataMap = getExtensions();
		for (Entry<Class<?>, IEntityMetaData> entry : typeToMetaDataMap) {
			updateEntityMetaDataWithLifecycleExtensions(entry.getValue());
		}
	}

	protected void fillMethodsAnnotatedWith(Class<?> type, List<Method> methods,
			Class<? extends Annotation>... annotations) {
		if (type == null || Object.class.equals(type)) {
			return;
		}
		fillMethodsAnnotatedWith(type.getSuperclass(), methods, annotations);
		Method[] allMethodsOfThisType = ReflectUtil.getDeclaredMethods(type);
		for (int a = 0, size = allMethodsOfThisType.length; a < size; a++) {
			Method currentMethod = allMethodsOfThisType[a];
			for (int b = annotations.length; b-- > 0;) {
				if (!currentMethod.isAnnotationPresent(annotations[b])) {
					continue;
				}
				if (currentMethod.getParameterTypes().length != 0) {
					throw new IllegalArgumentException("It is not allowed to annotated methods without "
							+ annotations[b].getName() + " having 0 arguments: " + currentMethod.toString());
				}
				currentMethod.setAccessible(true);
				methods.add(currentMethod);
			}
		}
	}

	protected void initializeValueObjectMapping() {
		Lock writeLock = getWriteLock();
		writeLock.lock();
		try {
			businessObjectSaveOrder = null;

			HashMap<Class<?>, ISet<Class<?>>> boTypeToBeforeBoTypes = new HashMap<>();
			HashMap<Class<?>, ISet<Class<?>>> boTypeToAfterBoTypes = new HashMap<>();

			for (Entry<Class<?>, IValueObjectConfig> entry : valueObjectMap.getExtensions()) {
				IValueObjectConfig voConfig = entry.getValue();
				Class<?> entityType = voConfig.getEntityType();
				Class<?> valueType = voConfig.getValueType();
				IEntityMetaData metaData = getMetaData(entityType);

				if (metaData == null) {
					// Currently no bo metadata found. We can do nothing here
					return;
				}
				Map<String, ITypeInfoItem> boNameToVoMember = getTypeInfoMapForVo(valueType);

				for (RelationMember boMember : metaData.getRelationMembers()) {
					String boMemberName = boMember.getName();
					String voMemberName = voConfig.getValueObjectMemberName(boMemberName);
					ITypeInfoItem voMember = boNameToVoMember.get(boMemberName);
					if (voConfig.isIgnoredMember(voMemberName) || voMember == null) {
						continue;
					}
					Class<?> voMemberRealType = voMember.getRealType();
					if (voConfig.holdsListType(voMember.getName())) {
						IPropertyInfo[] properties = propertyInfoProvider.getProperties(voMemberRealType);
						if (properties.length != 1) {
							throw new IllegalArgumentException("ListTypes must have exactly one property");
						}
						voMemberRealType =
								typeInfoProvider.getMember(voMemberRealType, properties[0]).getRealType();
					}
					if (!ImmutableTypeSet.isImmutableType(voMemberRealType)) {
						// vo member is either a list or a single direct relation to another VO
						// This implies that a potential service can handle both VO types as new objects at once
						continue;
					}
					// vo member only holds a id reference which implies that the related VO has to be
					// persisted first to
					// contain an id which can be referred to. But we do NOT know the related VO here, but we
					// know
					// the related BO where ALL potential VOs will be derived from:
					Class<?> boMemberElementType = boMember.getElementType();

					if (EqualsUtil.equals(entityType, boMemberElementType)) {
						continue;
					}

					addBoTypeAfter(entityType, boMemberElementType, boTypeToBeforeBoTypes,
							boTypeToAfterBoTypes);
					addBoTypeBefore(entityType, boMemberElementType, boTypeToBeforeBoTypes,
							boTypeToAfterBoTypes);
				}
			}
			List<Class<?>> businessObjectSaveOrder = new ArrayList<>();

			for (Class<?> boType : boTypeToBeforeBoTypes.keySet()) {
				// BeforeBoType are types which have to be saved BEFORE saving the boType
				boolean added = false;
				for (int a = 0, size = businessObjectSaveOrder.size(); a < size; a++) {
					Class<?> orderedBoType = businessObjectSaveOrder.get(a);

					// OrderedBoType is the type currently inserted at the correct position in the save order
					// - as far as the keyset
					// has been traversed, yet

					ISet<Class<?>> typesBeforeOrderedType = boTypeToBeforeBoTypes.get(orderedBoType);
					// typesBeforeOrderedType are types which have to be

					boolean orderedHasToBeAfterCurrent =
							typesBeforeOrderedType != null && typesBeforeOrderedType.contains(boType);

					if (!orderedHasToBeAfterCurrent) {
						// our boType has nothing to do with the orderedBoType. So we let is be at it is
						continue;
					}
					businessObjectSaveOrder.add(a, boType);
					added = true;
					break;
				}
				if (!added) {
					businessObjectSaveOrder.add(boType);
				}
			}
			for (Class<?> boType : boTypeToAfterBoTypes.keySet()) {
				if (boTypeToBeforeBoTypes.containsKey(boType)) {
					// already handled in the previous loop
					continue;
				}
				boolean added = false;
				for (int a = businessObjectSaveOrder.size(); a-- > 0;) {
					Class<?> orderedBoType = businessObjectSaveOrder.get(a);

					// OrderedBoType is the type currently inserted at the correct position in the save order
					// - as far as the keyset
					// has been traversed, yet

					ISet<Class<?>> typesBeforeOrderedType = boTypeToBeforeBoTypes.get(orderedBoType);

					boolean orderedHasToBeAfterCurrent =
							typesBeforeOrderedType != null && typesBeforeOrderedType.contains(boType);

					if (!orderedHasToBeAfterCurrent) {
						// our boType has nothing to do with the orderedBoType. So we let it be as it is
						continue;
					}
					businessObjectSaveOrder.add(a, boType);
					added = true;
					break;
				}
				if (!added) {
					businessObjectSaveOrder.add(boType);
				}
			}
			this.businessObjectSaveOrder =
					businessObjectSaveOrder.toArray(new Class[businessObjectSaveOrder.size()]);
		}
		finally {
			writeLock.unlock();
		}
	}

	protected void addBoTypeBefore(Class<?> boType, Class<?> beforeBoType,
			Map<Class<?>, ISet<Class<?>>> boTypeToBeforeBoTypes,
			Map<Class<?>, ISet<Class<?>>> boTypeToAfterBoTypes) {
		ISet<Class<?>> beforeBoTypes = boTypeToBeforeBoTypes.get(boType);
		if (beforeBoTypes == null) {
			beforeBoTypes = new HashSet<>();
			boTypeToBeforeBoTypes.put(boType, beforeBoTypes);
		}
		beforeBoTypes.add(beforeBoType);

		ISet<Class<?>> afterBoTypes = boTypeToAfterBoTypes.get(boType);
		if (afterBoTypes != null) {
			// Add boType as a after BO for all BOs which are BEFORE afterBoType (similar to: if 1<3 and
			// 3<4 then 1<4)
			for (Class<?> afterBoType : afterBoTypes) {
				addBoTypeBefore(afterBoType, beforeBoType, boTypeToBeforeBoTypes, boTypeToAfterBoTypes);
			}
		}
	}

	protected void addBoTypeAfter(Class<?> boType, Class<?> afterBoType,
			Map<Class<?>, ISet<Class<?>>> boTypeBeforeBoTypes,
			Map<Class<?>, ISet<Class<?>>> boTypeToAfterBoTypes) {
		ISet<Class<?>> afterBoTypes = boTypeToAfterBoTypes.get(afterBoType);
		if (afterBoTypes == null) {
			afterBoTypes = new HashSet<>();
			boTypeToAfterBoTypes.put(afterBoType, afterBoTypes);
		}
		afterBoTypes.add(boType);

		ISet<Class<?>> beforeBoTypes = boTypeBeforeBoTypes.get(afterBoType);
		if (beforeBoTypes != null) {
			// Add afterBoType as a after BO for all BOs which are BEFORE boType (similar to: if 1<3 and
			// 3<4 then 1<4)
			for (Class<?> beforeBoType : beforeBoTypes) {
				addBoTypeAfter(beforeBoType, afterBoType, boTypeBeforeBoTypes, boTypeToAfterBoTypes);
			}
		}
	}

	@Override
	public IList<Class<?>> findMappableEntityTypes() {
		ILinkedMap<Class<?>, IValueObjectConfig> targetExtensionMap = valueObjectMap.getExtensions();
		HashSet<Class<?>> mappableEntitiesSet = HashSet.create(targetExtensionMap.size());
		for (Entry<Class<?>, IValueObjectConfig> entry : targetExtensionMap) {
			IValueObjectConfig voConfig = entry.getValue();
			mappableEntitiesSet.add(voConfig.getEntityType());
		}
		return new ArrayList<>(mappableEntitiesSet);
	}

	public IMap<String, ITypeInfoItem> getTypeInfoMapForVo(Class<?> valueType) {
		IValueObjectConfig config = getValueObjectConfig(valueType);
		if (config == null) {
			return null;
		}
		IMap<String, ITypeInfoItem> typeInfoMap = typeToPropertyMap.get(valueType);
		if (typeInfoMap == null) {
			typeInfoMap = new HashMap<>();
			IEntityMetaData boMetaData = getMetaData(config.getEntityType());
			StringBuilder sb = new StringBuilder();

			addTypeInfoMapping(typeInfoMap, config, boMetaData.getIdMember().getName(), sb);
			if (boMetaData.getVersionMember() != null) {
				addTypeInfoMapping(typeInfoMap, config, boMetaData.getVersionMember().getName(), sb);
			}
			for (Member primitiveMember : boMetaData.getPrimitiveMembers()) {
				addTypeInfoMapping(typeInfoMap, config, primitiveMember.getName(), sb);
			}
			for (RelationMember relationMember : boMetaData.getRelationMembers()) {
				addTypeInfoMapping(typeInfoMap, config, relationMember.getName(), null);
			}

			if (!typeToPropertyMap.putIfNotExists(config.getValueType(), typeInfoMap)) {
				throw new IllegalStateException("Key already exists " + config.getValueType());
			}
		}
		return typeInfoMap;
	}

	protected void addTypeInfoMapping(IMap<String, ITypeInfoItem> typeInfoMap,
			IValueObjectConfig config, String boMemberName, StringBuilder sb) {
		String voMemberName = config.getValueObjectMemberName(boMemberName);
		ITypeInfoItem voMember =
				typeInfoProvider.getHierarchicMember(config.getValueType(), voMemberName);
		if (voMember == null) {
			return;
		}
		typeInfoMap.put(boMemberName, voMember);
		if (sb == null) {
			return;
		}
		sb.setLength(0);
		String voSpecifiedName = sb.append(voMemberName).append("Specified").toString();
		ITypeInfoItem voSpecifiedMember =
				typeInfoProvider.getHierarchicMember(config.getValueType(), voSpecifiedName);
		if (voSpecifiedMember == null) {
			return;
		}
		sb.setLength(0);
		String boSpecifiedName = sb.append(boMemberName).append("Specified").toString();
		typeInfoMap.put(boSpecifiedName, voSpecifiedMember);
	}

	@Override
	public Class<?>[] getEntityPersistOrder() {
		return businessObjectSaveOrder;
	}

	@Override
	public void registerEntityInstantiationExtension(
			IEntityInstantiationExtension entityInstantiationExtension, Class<?> type) {
		entityInstantiationExtensions.register(entityInstantiationExtension, type);
		initialize();
	}

	@Override
	public void unregisterEntityInstantiationExtension(
			IEntityInstantiationExtension entityInstantiationExtension, Class<?> type) {
		entityInstantiationExtensions.unregister(entityInstantiationExtension, type);
		initialize();
	}
}
