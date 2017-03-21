package com.koch.ambeth.audit.server;

/*-
 * #%L
 * jambeth-audit-server
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

import java.util.concurrent.locks.Lock;

import com.koch.ambeth.audit.server.config.AuditConfigurationConstants;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.config.Property;
import com.koch.ambeth.ioc.extendable.MapExtendableContainer;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.security.audit.model.Audited;
import com.koch.ambeth.service.merge.IEntityMetaDataProvider;
import com.koch.ambeth.service.merge.model.IEntityMetaData;
import com.koch.ambeth.service.metadata.Member;
import com.koch.ambeth.service.metadata.PrimitiveMember;
import com.koch.ambeth.service.metadata.RelationMember;
import com.koch.ambeth.util.annotation.AnnotationUtil;
import com.koch.ambeth.util.collections.IdentityHashMap;

public class AuditConfigurationProvider implements IAuditConfigurationProvider, IAuditConfigurationExtendable
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected IEntityMetaDataProvider entityMetaDataProvider;

	@Property(name = AuditConfigurationConstants.AuditedEntityDefaultModeActive, defaultValue = "true")
	protected boolean auditedEntityDefaultModeActive;

	@Property(name = AuditConfigurationConstants.AuditReasonRequiredDefault, defaultValue = "false")
	protected boolean auditReasonRequiredDefault;

	@Property(name = AuditConfigurationConstants.AuditedEntityPropertyDefaultModeActive, defaultValue = "true")
	protected boolean auditedEntityPropertyDefaultModeActive;

	protected final MapExtendableContainer<Class<?>, IAuditConfiguration> entityTypeToAuditConfigurationMap = new MapExtendableContainer<Class<?>, IAuditConfiguration>(
			"auditConfiguration", "entityType");

	@Override
	public IAuditConfiguration getAuditConfiguration(Class<?> entityType)
	{
		IAuditConfiguration auditConfiguration = entityTypeToAuditConfigurationMap.getExtension(entityType);
		if (auditConfiguration != null)
		{
			return auditConfiguration;
		}
		Lock writeLock = entityTypeToAuditConfigurationMap.getWriteLock();
		writeLock.lock();
		try
		{
			auditConfiguration = entityTypeToAuditConfigurationMap.getExtension(entityType);
			if (auditConfiguration != null)
			{
				// concurrent thread might have been faster
				return auditConfiguration;
			}
			auditConfiguration = buildAuditConfiguration(entityType);
			entityTypeToAuditConfigurationMap.register(auditConfiguration, entityType);
			return auditConfiguration;
		}
		finally
		{
			writeLock.unlock();
		}
	}

	protected IAuditConfiguration buildAuditConfiguration(Class<?> entityType)
	{
		IEntityMetaData metaData = entityMetaDataProvider.getMetaData(entityType);

		Audited audited = AnnotationUtil.getAnnotation(Audited.class, metaData.getEnhancedType(), true);
		AuditReasonRequired auditReasonRequired = AnnotationUtil.getAnnotation(AuditReasonRequired.class, metaData.getEnhancedType(), true);

		boolean auditActive = audited != null ? audited.value() : auditedEntityDefaultModeActive;

		boolean reasonRequired = auditReasonRequired != null ? auditReasonRequired.value() : auditReasonRequiredDefault;

		IdentityHashMap<Member, IAuditMemberConfiguration> memberToConfigurationMap = new IdentityHashMap<Member, IAuditMemberConfiguration>(0.5f);
		for (PrimitiveMember member : metaData.getPrimitiveMembers())
		{
			memberToConfigurationMap.put(member, resolveMemberConfiguration(metaData, member));
		}
		for (RelationMember member : metaData.getRelationMembers())
		{
			memberToConfigurationMap.put(member, resolveMemberConfiguration(metaData, member));
		}
		if (metaData.getVersionMember() != null)
		{
			memberToConfigurationMap.put(metaData.getVersionMember(), resolveMemberConfiguration(metaData, metaData.getVersionMember()));
		}
		return new AuditConfiguration(auditActive, reasonRequired, memberToConfigurationMap);
	}

	protected IAuditMemberConfiguration resolveMemberConfiguration(IEntityMetaData metaData, Member member)
	{
		Audited audited = member.getAnnotation(Audited.class);
		boolean auditActive = audited != null ? audited.value() : auditedEntityPropertyDefaultModeActive;
		return auditActive ? AuditMemberConfiguration.ACTIVE : AuditMemberConfiguration.INACTIVE;
	}

	@Override
	public void registerAuditConfiguration(IAuditConfiguration auditConfiguration, Class<?> entityType)
	{
		entityTypeToAuditConfigurationMap.register(auditConfiguration, entityType);
	}

	@Override
	public void unregisterAuditConfiguration(IAuditConfiguration auditConfiguration, Class<?> entityType)
	{
		entityTypeToAuditConfigurationMap.unregister(auditConfiguration, entityType);
	}
}
