package de.osthus.ambeth.audit;

import de.osthus.ambeth.collections.IdentityHashMap;
import de.osthus.ambeth.config.AuditConfigurationConstants;
import de.osthus.ambeth.config.Property;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.ioc.extendable.MapExtendableContainer;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.merge.IEntityMetaDataProvider;
import de.osthus.ambeth.merge.model.IEntityMetaData;
import de.osthus.ambeth.metadata.Member;
import de.osthus.ambeth.metadata.PrimitiveMember;
import de.osthus.ambeth.metadata.RelationMember;

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

	private final MapExtendableContainer<Class<?>, IAuditConfiguration> entityTypeToAuditConfigurationMap = new MapExtendableContainer<Class<?>, IAuditConfiguration>(
			"auditConfiguration", "entityType");

	@Override
	public IAuditConfiguration getAuditConfiguration(Class<?> entityType)
	{
		IAuditConfiguration auditConfiguration = entityTypeToAuditConfigurationMap.getExtension(entityType);
		if (auditConfiguration != null)
		{
			return auditConfiguration;
		}
		auditConfiguration = buildAuditConfiguration(entityType);
		entityTypeToAuditConfigurationMap.register(auditConfiguration, entityType);
		return auditConfiguration;
	}

	protected IAuditConfiguration buildAuditConfiguration(Class<?> entityType)
	{
		IEntityMetaData metaData = entityMetaDataProvider.getMetaData(entityType);

		// TODO: DM to DeK: You used "getEnhancedType" but Annotations are not inherited and so were not present at the enhanced classes. Is the usage of
		// getEntityType correct?
		Audited audited = metaData.getEntityType().getAnnotation(Audited.class);
		AuditReasonRequired auditReasonRequired = metaData.getEntityType().getAnnotation(AuditReasonRequired.class);

		boolean auditActive = audited != null ? audited.value() : auditedEntityDefaultModeActive;

		boolean reasonRequired = auditReasonRequired != null ? auditReasonRequired.value() : auditReasonRequiredDefault;

		IdentityHashMap<Member, IAuditMemberConfiguration> memberToConfigurationMap = new IdentityHashMap<Member, IAuditMemberConfiguration>(0.5f);
		for (PrimitiveMember member : metaData.getPrimitiveMembers())
		{
			memberToConfigurationMap.put(member, resolveMemberConfiguration(member));
		}
		for (RelationMember member : metaData.getRelationMembers())
		{
			memberToConfigurationMap.put(member, resolveMemberConfiguration(member));
		}
		return new AuditConfiguration(auditActive, reasonRequired, memberToConfigurationMap);
	}

	protected IAuditMemberConfiguration resolveMemberConfiguration(Member member)
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
