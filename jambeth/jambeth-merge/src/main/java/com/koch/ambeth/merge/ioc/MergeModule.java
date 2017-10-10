package com.koch.ambeth.merge.ioc;

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

import com.koch.ambeth.event.IEventListenerExtendable;
import com.koch.ambeth.ioc.IInitializingModule;
import com.koch.ambeth.ioc.annotation.FrameworkModule;
import com.koch.ambeth.ioc.config.IBeanConfiguration;
import com.koch.ambeth.ioc.config.PrecedenceType;
import com.koch.ambeth.ioc.config.Property;
import com.koch.ambeth.ioc.extendable.ExtendableBean;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;
import com.koch.ambeth.ioc.util.DedicatedConverterUtil;
import com.koch.ambeth.merge.CUDResultApplier;
import com.koch.ambeth.merge.CUDResultComparer;
import com.koch.ambeth.merge.CUDResultHelper;
import com.koch.ambeth.merge.DeepScanRecursion;
import com.koch.ambeth.merge.EntityMetaDataClient;
import com.koch.ambeth.merge.EntityMetaDataProvider;
import com.koch.ambeth.merge.ICUDResultApplier;
import com.koch.ambeth.merge.ICUDResultComparer;
import com.koch.ambeth.merge.ICUDResultExtendable;
import com.koch.ambeth.merge.ICUDResultHelper;
import com.koch.ambeth.merge.IDeepScanRecursion;
import com.koch.ambeth.merge.IEntityFactory;
import com.koch.ambeth.merge.IEntityInstantiationExtensionExtendable;
import com.koch.ambeth.merge.IEntityMetaDataExtendable;
import com.koch.ambeth.merge.IFimExtensionExtendable;
import com.koch.ambeth.merge.IMergeController;
import com.koch.ambeth.merge.IMergeListenerExtendable;
import com.koch.ambeth.merge.IMergeProcess;
import com.koch.ambeth.merge.IMergeServiceExtension;
import com.koch.ambeth.merge.IMergeServiceExtensionExtendable;
import com.koch.ambeth.merge.IMergeTimeProvider;
import com.koch.ambeth.merge.IObjRefHelper;
import com.koch.ambeth.merge.ITechnicalEntityTypeExtendable;
import com.koch.ambeth.merge.IValueObjectConfigExtendable;
import com.koch.ambeth.merge.MergeController;
import com.koch.ambeth.merge.MergeProcess;
import com.koch.ambeth.merge.MergeServiceRegistry;
import com.koch.ambeth.merge.ObjRefHelper;
import com.koch.ambeth.merge.ValueObjectMap;
import com.koch.ambeth.merge.cache.CacheModification;
import com.koch.ambeth.merge.cache.ICacheModification;
import com.koch.ambeth.merge.config.EntityMetaDataReader;
import com.koch.ambeth.merge.config.IEntityMetaDataReader;
import com.koch.ambeth.merge.config.IndependentEntityMetaDataReader;
import com.koch.ambeth.merge.config.MergeConfigurationConstants;
import com.koch.ambeth.merge.converter.EntityMetaDataConverter;
import com.koch.ambeth.merge.copy.IObjectCopierExtendable;
import com.koch.ambeth.merge.metadata.IIntermediateMemberTypeProvider;
import com.koch.ambeth.merge.metadata.IMemberTypeProvider;
import com.koch.ambeth.merge.metadata.IObjRefFactory;
import com.koch.ambeth.merge.metadata.MemberTypeProvider;
import com.koch.ambeth.merge.metadata.ObjRefFactory;
import com.koch.ambeth.merge.metadata.ObjRefObjectCopierExtension;
import com.koch.ambeth.merge.mixin.CompositeIdMixin;
import com.koch.ambeth.merge.mixin.EmbeddedMemberMixin;
import com.koch.ambeth.merge.mixin.ObjRefMixin;
import com.koch.ambeth.merge.mixin.ObjRefTypeMixin;
import com.koch.ambeth.merge.model.EntityMetaData;
import com.koch.ambeth.merge.objrefstore.IObjRefStoreEntryProvider;
import com.koch.ambeth.merge.objrefstore.ObjRefStoreEntryProvider;
import com.koch.ambeth.merge.orm.DefaultOrmEntityEntityProvider;
import com.koch.ambeth.merge.orm.IOrmConfigGroupProvider;
import com.koch.ambeth.merge.orm.IOrmEntityTypeProvider;
import com.koch.ambeth.merge.orm.IOrmXmlReaderExtendable;
import com.koch.ambeth.merge.orm.IOrmXmlReaderRegistry;
import com.koch.ambeth.merge.orm.OrmConfigGroupProvider;
import com.koch.ambeth.merge.orm.OrmXmlReader20;
import com.koch.ambeth.merge.orm.OrmXmlReaderLegathy;
import com.koch.ambeth.merge.propertychange.PropertyChangeInstantiationProcessor;
import com.koch.ambeth.merge.proxy.EntityFactory;
import com.koch.ambeth.merge.security.ISecurityActivation;
import com.koch.ambeth.merge.security.ISecurityScopeChangeListenerExtendable;
import com.koch.ambeth.merge.security.ISecurityScopeProvider;
import com.koch.ambeth.merge.security.SecurityActivation;
import com.koch.ambeth.merge.security.SecurityScopeProvider;
import com.koch.ambeth.merge.service.IMergeService;
import com.koch.ambeth.merge.transfer.EntityMetaDataTransfer;
import com.koch.ambeth.merge.typeinfo.RelationProvider;
import com.koch.ambeth.merge.util.XmlConfigUtil;
import com.koch.ambeth.service.cache.ClearAllCachesEvent;
import com.koch.ambeth.service.config.ServiceConfigurationConstants;
import com.koch.ambeth.service.merge.IEntityMetaDataProvider;
import com.koch.ambeth.service.merge.IEntityMetaDataRefresher;
import com.koch.ambeth.service.merge.model.IEntityLifecycleExtendable;
import com.koch.ambeth.service.merge.model.IObjRef;
import com.koch.ambeth.service.remote.ClientServiceBean;
import com.koch.ambeth.util.typeinfo.INoEntityTypeExtendable;
import com.koch.ambeth.util.typeinfo.IRelationProvider;
import com.koch.ambeth.util.xml.IXmlConfigUtil;

@FrameworkModule
public class MergeModule implements IInitializingModule {
	public static final String INDEPENDENT_META_DATA_READER = "independentEntityMetaDataReader";

	public static final String REMOTE_ENTITY_METADATA_PROVIDER = "entityMetaDataProvider.remote";

	public static final String DEFAULT_MERGE_SERVICE_EXTENSION = "mergeServiceExtension.default";

	@Property(name = ServiceConfigurationConstants.IndependentMetaData, defaultValue = "false")
	protected boolean independentMetaData;

	@Property(name = MergeConfigurationConstants.EntityFactoryType, mandatory = false)
	protected Class<?> entityFactoryType;

	@Property(name = ServiceConfigurationConstants.NetworkClientMode, defaultValue = "false")
	protected boolean isNetworkClientMode;

	@Property(name = MergeConfigurationConstants.MergeServiceBeanActive, defaultValue = "true")
	protected boolean isMergeServiceBeanActive;


	@Override
	public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable {
		beanContextFactory.registerAutowireableBean(IDeepScanRecursion.class, DeepScanRecursion.class);
		beanContextFactory.registerAutowireableBean(IMergeController.class, MergeController.class);
		beanContextFactory.registerAutowireableBean(IMergeProcess.class, MergeProcess.class);
		beanContextFactory.registerAutowireableBean(ICUDResultApplier.class,
				CUDResultApplier.class);
		beanContextFactory.registerAutowireableBean(ICUDResultComparer.class,
				CUDResultComparer.class);

		beanContextFactory.registerAutowireableBean(CompositeIdMixin.class, CompositeIdMixin.class);
		beanContextFactory.registerAutowireableBean(ObjRefMixin.class, ObjRefMixin.class);
		beanContextFactory.registerBean(ObjRefTypeMixin.class).autowireable(ObjRefTypeMixin.class);

		beanContextFactory.registerBean(SecurityScopeProvider.class).autowireable(
				ISecurityScopeProvider.class, ISecurityScopeChangeListenerExtendable.class);
		beanContextFactory.registerBean(SecurityActivation.class)
				.autowireable(ISecurityActivation.class);

		beanContextFactory.registerBean(PropertyChangeInstantiationProcessor.class);

		beanContextFactory.registerBean(CacheModification.class)
				.autowireable(ICacheModification.class);

		beanContextFactory.registerAutowireableBean(IObjRefHelper.class, ObjRefHelper.class);
		beanContextFactory.registerBean(CUDResultHelper.class).autowireable(ICUDResultHelper.class,
				ICUDResultExtendable.class);

		beanContextFactory.registerBean(EntityMetaDataReader.class)
				.autowireable(IEntityMetaDataReader.class);

		beanContextFactory.registerBean(MergeServiceRegistry.class).autowireable(
				IMergeService.class, IMergeServiceExtensionExtendable.class,
				IMergeListenerExtendable.class, IMergeTimeProvider.class);

		IBeanConfiguration valueObjectMap = beanContextFactory.registerBean(ValueObjectMap.class);

		beanContextFactory.registerBean(EntityMetaDataProvider.class)
				.propertyRef("ValueObjectMap", valueObjectMap)
				.autowireable(IEntityMetaDataProvider.class, IEntityMetaDataRefresher.class,
						IValueObjectConfigExtendable.class, IEntityLifecycleExtendable.class,
						ITechnicalEntityTypeExtendable.class, IEntityMetaDataExtendable.class,
						EntityMetaDataProvider.class, IEntityInstantiationExtensionExtendable.class,
						IFimExtensionExtendable.class);

		beanContextFactory
				.registerBean(INDEPENDENT_META_DATA_READER, IndependentEntityMetaDataReader.class)
				.precedence(PrecedenceType.HIGH);

		IBeanConfiguration entityMetaDataConverter =
				beanContextFactory.registerBean(EntityMetaDataConverter.class);
		DedicatedConverterUtil.biLink(beanContextFactory, entityMetaDataConverter,
				EntityMetaData.class, EntityMetaDataTransfer.class);

		if (!independentMetaData && isNetworkClientMode) {
			beanContextFactory.registerBean(REMOTE_ENTITY_METADATA_PROVIDER,
					EntityMetaDataClient.class);
		}

		IBeanConfiguration ormConfigGroupProvider =
				beanContextFactory.registerBean(OrmConfigGroupProvider.class)
						.autowireable(IOrmConfigGroupProvider.class);
		beanContextFactory
				.link(ormConfigGroupProvider, OrmConfigGroupProvider.handleClearAllCachesEvent)
				.to(IEventListenerExtendable.class).with(ClearAllCachesEvent.class);

		beanContextFactory.registerBean(DefaultOrmEntityEntityProvider.class)
				.autowireable(IOrmEntityTypeProvider.class);

		IBeanConfiguration ormXmlReaderLegathy =
				beanContextFactory.registerBean(OrmXmlReaderLegathy.class);
		ExtendableBean
				.registerExtendableBean(beanContextFactory, IOrmXmlReaderRegistry.class,
						IOrmXmlReaderExtendable.class, IOrmXmlReaderRegistry.class.getClassLoader())//
				.propertyRef(ExtendableBean.P_DEFAULT_BEAN, ormXmlReaderLegathy);
		IBeanConfiguration ormXmlReader20BC = beanContextFactory.registerBean(OrmXmlReader20.class);
		beanContextFactory.link(ormXmlReader20BC).to(IOrmXmlReaderExtendable.class)
				.with(OrmXmlReader20.ORM_XML_NS);

		beanContextFactory.registerBean(XmlConfigUtil.class).autowireable(IXmlConfigUtil.class);

		beanContextFactory.registerBean(RelationProvider.class)
				.autowireable(IRelationProvider.class, INoEntityTypeExtendable.class);

		beanContextFactory.registerBean(MemberTypeProvider.class)
				.autowireable(IMemberTypeProvider.class, IIntermediateMemberTypeProvider.class);
		beanContextFactory.registerBean(EmbeddedMemberMixin.class)
				.autowireable(EmbeddedMemberMixin.class);

		beanContextFactory.registerBean(ObjRefFactory.class).autowireable(IObjRefFactory.class);
		IBeanConfiguration objRefObjectCopierExtension =
				beanContextFactory.registerBean(ObjRefObjectCopierExtension.class);
		beanContextFactory.link(objRefObjectCopierExtension).to(IObjectCopierExtendable.class)
				.with(IObjRef.class).optional();

		Class<?> entityFactoryType = this.entityFactoryType;
		if (entityFactoryType == null) {
			entityFactoryType = EntityFactory.class;
		}
		beanContextFactory.registerBean("entityFactory", entityFactoryType)
				.autowireable(IEntityFactory.class);

		beanContextFactory.registerBean(ObjRefStoreEntryProvider.class)
				.autowireable(IObjRefStoreEntryProvider.class);

		if (isNetworkClientMode && isMergeServiceBeanActive) {
			IBeanConfiguration remoteMergeServiceExtension = beanContextFactory
					.registerBean(DEFAULT_MERGE_SERVICE_EXTENSION, ClientServiceBean.class)
					.propertyValue(ClientServiceBean.INTERFACE_PROP_NAME,
							IMergeServiceExtension.class)//
					.propertyValue(ClientServiceBean.SYNC_REMOTE_INTERFACE_PROP_NAME,
							IMergeService.class);

			// register to all entities in a "most-weak" manner
			beanContextFactory.link(remoteMergeServiceExtension)
					.to(IMergeServiceExtensionExtendable.class).with(Object.class);
		}
	}
}
