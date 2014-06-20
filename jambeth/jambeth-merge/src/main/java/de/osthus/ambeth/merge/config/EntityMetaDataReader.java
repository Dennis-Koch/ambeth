package de.osthus.ambeth.merge.config;

import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.HashMap;
import de.osthus.ambeth.collections.HashSet;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.collections.IMap;
import de.osthus.ambeth.collections.ISet;
import de.osthus.ambeth.collections.IdentityHashSet;
import de.osthus.ambeth.collections.IdentityLinkedMap;
import de.osthus.ambeth.collections.IdentityLinkedSet;
import de.osthus.ambeth.compositeid.CompositeIdTypeInfoItem;
import de.osthus.ambeth.compositeid.ICompositeIdFactory;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.merge.model.EntityMetaData;
import de.osthus.ambeth.merge.model.IEntityMetaData;
import de.osthus.ambeth.orm.CompositeMemberConfig;
import de.osthus.ambeth.orm.EntityConfig;
import de.osthus.ambeth.orm.IMemberConfig;
import de.osthus.ambeth.orm.IOrmConfig;
import de.osthus.ambeth.orm.IRelationConfig;
import de.osthus.ambeth.orm.MemberConfig;
import de.osthus.ambeth.typeinfo.IEmbeddedTypeInfoItem;
import de.osthus.ambeth.typeinfo.IRelationInfoItem;
import de.osthus.ambeth.typeinfo.IRelationProvider;
import de.osthus.ambeth.typeinfo.ITypeInfo;
import de.osthus.ambeth.typeinfo.ITypeInfoItem;
import de.osthus.ambeth.typeinfo.ITypeInfoProvider;

public class EntityMetaDataReader implements IEntityMetaDataReader
{
	@LogInstance
	private ILogger log;

	private static final Pattern containsDot = Pattern.compile("\\.");

	@Autowired
	protected ICompositeIdFactory compositeIdFactory;

	@Autowired
	protected IRelationProvider relationProvider;

	@Autowired
	protected ITypeInfoProvider typeInfoProvider;

	@Override
	public void addMembers(EntityMetaData metaData, EntityConfig entityConfig)
	{
		Class<?> realType = entityConfig.getRealType();

		ISet<String> memberNamesToIgnore = new HashSet<String>();
		IList<IMemberConfig> embeddedMembers = new ArrayList<IMemberConfig>();
		IMap<String, IMemberConfig> nameToMemberConfig = new HashMap<String, IMemberConfig>();
		IMap<String, IRelationConfig> nameToRelationConfig = new HashMap<String, IRelationConfig>();

		fillNameCollections(entityConfig, memberNamesToIgnore, embeddedMembers, nameToMemberConfig, nameToRelationConfig);

		IdentityLinkedSet<ITypeInfoItem> alternateIdMembers = new IdentityLinkedSet<ITypeInfoItem>();
		IdentityLinkedSet<ITypeInfoItem> primitiveMembers = new IdentityLinkedSet<ITypeInfoItem>();
		IdentityLinkedSet<IRelationInfoItem> relationMembers = new IdentityLinkedSet<IRelationInfoItem>();
		IdentityLinkedSet<ITypeInfoItem> notMergeRelevant = new IdentityLinkedSet<ITypeInfoItem>();

		IdentityLinkedSet<ITypeInfoItem> containedInAlternateIdMember = new IdentityLinkedSet<ITypeInfoItem>();

		ITypeInfo typeInfo = typeInfoProvider.getTypeInfo(realType);

		IdentityLinkedMap<IOrmConfig, ITypeInfoItem> memberConfigToInfoItem = new IdentityLinkedMap<IOrmConfig, ITypeInfoItem>();

		// Resolve members for all explicit configurations - both simple and composite ones, each with embedded
		// functionality (dot-member-path)
		for (IMemberConfig memberConfig : entityConfig.getMemberConfigIterable())
		{
			if (memberConfig.isIgnore())
			{
				continue;
			}
			handleMemberConfig(metaData, realType, memberConfig, memberConfigToInfoItem);
		}
		for (IRelationConfig relationConfig : entityConfig.getRelationConfigIterable())
		{
			handleRelationConfig(realType, relationConfig, memberConfigToInfoItem);
		}
		metaData.setIdMember(handleMemberConfig(metaData, realType, entityConfig.getIdMemberConfig(), memberConfigToInfoItem));
		metaData.setVersionMember(handleMemberConfig(metaData, realType, entityConfig.getVersionMemberConfig(), memberConfigToInfoItem));
		metaData.setCreatedByMember(handleMemberConfig(metaData, realType, entityConfig.getCreatedByMemberConfig(), memberConfigToInfoItem));
		metaData.setCreatedOnMember(handleMemberConfig(metaData, realType, entityConfig.getCreatedOnMemberConfig(), memberConfigToInfoItem));
		metaData.setUpdatedByMember(handleMemberConfig(metaData, realType, entityConfig.getUpdatedByMemberConfig(), memberConfigToInfoItem));
		metaData.setUpdatedOnMember(handleMemberConfig(metaData, realType, entityConfig.getUpdatedOnMemberConfig(), memberConfigToInfoItem));

		IdentityHashSet<ITypeInfoItem> idMembers = new IdentityHashSet<ITypeInfoItem>();
		ITypeInfoItem idMember = metaData.getIdMember();
		if (idMember instanceof CompositeIdTypeInfoItem)
		{
			idMembers.addAll(((CompositeIdTypeInfoItem) idMember).getMembers());
		}
		else if (idMember != null)
		{
			idMembers.add(idMember);
		}

		// Handle all explicitly configurated members
		for (Entry<IOrmConfig, ITypeInfoItem> entry : memberConfigToInfoItem)
		{
			IOrmConfig ormConfig = entry.getKey();
			ITypeInfoItem member = entry.getValue();

			if (idMembers.contains(member))
			{
				continue;
			}
			if (ormConfig.isExplicitlyNotMergeRelevant())
			{
				notMergeRelevant.add(member);
			}
			if (ormConfig instanceof IRelationConfig)
			{
				if (!relationMembers.add((IRelationInfoItem) member))
				{
					throw new IllegalStateException("Member has been registered as relation multiple times: " + member.getName());
				}
				continue;
			}
			if (!(ormConfig instanceof IMemberConfig))
			{
				continue;
			}
			if (((IMemberConfig) ormConfig).isAlternateId())
			{
				if (!alternateIdMembers.add(member))
				{
					throw new IllegalStateException("Member has been registered as alternate id multiple times: " + member.getName());
				}
				if (member instanceof CompositeIdTypeInfoItem)
				{
					ITypeInfoItem[] containedMembers = ((CompositeIdTypeInfoItem) member).getMembers();
					containedInAlternateIdMember.addAll(containedMembers);
				}
			}
			if (!(member instanceof CompositeIdTypeInfoItem) && metaData.getVersionMember() != member)
			{
				// Alternate Ids are normally primitives, too. But Composite Alternate Ids not - only their composite
				// items are primitives
				primitiveMembers.add(member);
			}
		}
		IdentityHashSet<ITypeInfoItem> explicitTypeInfoItems = IdentityHashSet.<ITypeInfoItem> create(memberConfigToInfoItem.size());
		for (Entry<IOrmConfig, ITypeInfoItem> entry : memberConfigToInfoItem)
		{
			ITypeInfoItem member = entry.getValue();
			explicitTypeInfoItems.add(member);
			if (member instanceof IEmbeddedTypeInfoItem)
			{
				explicitTypeInfoItems.add(((IEmbeddedTypeInfoItem) member).getMemberPath()[0]);
			}
		}
		// Go through the available members to look for potential auto-mapping (simple, no embedded)
		ITypeInfoItem[] members = typeInfo.getMembers();
		for (int i = 0; i < members.length; i++)
		{
			ITypeInfoItem member = members[i];
			String memberName = member.getName();

			if (memberNamesToIgnore.contains(memberName))
			{
				continue;
			}
			if (explicitTypeInfoItems.contains(member))
			{
				// already configured, no auto mapping needed for this member
				continue;
			}
			if (metaData.getIdMember() == null && memberName.equals(EntityMetaData.DEFAULT_NAME_ID))
			{
				metaData.setIdMember(member);
				continue;
			}
			if (idMembers.contains(member) && !alternateIdMembers.contains(member) && !containedInAlternateIdMember.contains(member))
			{
				continue;
			}
			if (metaData.getVersionMember() == null && memberName.equals(EntityMetaData.DEFAULT_NAME_VERSION))
			{
				metaData.setVersionMember(member);
				continue;
			}
			if (metaData.getCreatedByMember() == null && memberName.equals(EntityMetaData.DEFAULT_NAME_CREATED_BY))
			{
				metaData.setCreatedByMember(member);
			}
			else if (metaData.getCreatedOnMember() == null && memberName.equals(EntityMetaData.DEFAULT_NAME_CREATED_ON))
			{
				metaData.setCreatedOnMember(member);
			}
			else if (metaData.getUpdatedByMember() == null && memberName.equals(EntityMetaData.DEFAULT_NAME_UPDATED_BY))
			{
				metaData.setUpdatedByMember(member);
			}
			else if (metaData.getUpdatedOnMember() == null && memberName.equals(EntityMetaData.DEFAULT_NAME_UPDATED_ON))
			{
				metaData.setUpdatedOnMember(member);
			}
			if (!member.canWrite())
			{
				continue;
			}
			if (relationProvider.isEntityType(member.getElementType()))
			{
				relationMembers.add((IRelationInfoItem) member);
				continue;
			}
			primitiveMembers.add(member);
		}
		// Order of setter calls is important
		metaData.setPrimitiveMembers(primitiveMembers.toArray(ITypeInfoItem.class));
		metaData.setAlternateIdMembers(alternateIdMembers.toArray(ITypeInfoItem.class));
		metaData.setRelationMembers(relationMembers.toArray(IRelationInfoItem.class));

		for (ITypeInfoItem member : notMergeRelevant)
		{
			metaData.setMergeRelevant(member, false);
		}
		if (metaData.getIdMember() == null)
		{
			throw new IllegalStateException("No ID member could be resolved for entity of type " + metaData.getRealType());
		}
	}

	protected ITypeInfoItem handleMemberConfigIfNew(Class<?> entityType, IMemberConfig itemConfig, Map<IOrmConfig, ITypeInfoItem> memberConfigToInfoItem)
	{
		ITypeInfoItem member = memberConfigToInfoItem.get(itemConfig);
		if (member != null)
		{
			return member;
		}
		member = typeInfoProvider.getHierarchicMember(entityType, itemConfig.getName());
		if (member == null)
		{
			throw new RuntimeException("No member with name '" + itemConfig.getName() + "' found on entity type '" + entityType.getName() + "'");
		}
		memberConfigToInfoItem.put(itemConfig, member);
		return member;
	}

	protected ITypeInfoItem handleMemberConfig(IEntityMetaData metaData, Class<?> realType, IMemberConfig memberConfig,
			Map<IOrmConfig, ITypeInfoItem> memberConfigToInfoItem)
	{
		if (memberConfig == null)
		{
			return null;
		}
		if (!(memberConfig instanceof CompositeMemberConfig))
		{
			return handleMemberConfigIfNew(realType, memberConfig, memberConfigToInfoItem);
		}
		MemberConfig[] memberConfigs = ((CompositeMemberConfig) memberConfig).getMembers();
		ITypeInfoItem[] members = new ITypeInfoItem[memberConfigs.length];
		for (int a = memberConfigs.length; a-- > 0;)
		{
			MemberConfig memberPart = memberConfigs[a];
			ITypeInfoItem member = handleMemberConfigIfNew(realType, memberPart, memberConfigToInfoItem);
			members[a] = member;
		}
		ITypeInfoItem compositeIdMember = compositeIdFactory.createCompositeIdMember(metaData, members);
		memberConfigToInfoItem.put(memberConfig, compositeIdMember);
		return compositeIdMember;
	}

	protected ITypeInfoItem handleRelationConfig(Class<?> realType, IRelationConfig relationConfig, Map<IOrmConfig, ITypeInfoItem> relationConfigToInfoItem)
	{
		if (relationConfig == null)
		{
			return null;
		}
		ITypeInfoItem member = relationConfigToInfoItem.get(relationConfig);
		if (member != null)
		{
			return member;
		}
		member = typeInfoProvider.getHierarchicMember(realType, relationConfig.getName());
		if (member == null)
		{
			throw new RuntimeException("No member with name '" + relationConfig.getName() + "' found on entity type '" + realType.getName() + "'");
		}
		relationConfigToInfoItem.put(relationConfig, member);
		return member;
	}

	protected void fillNameCollections(EntityConfig entityConfig, ISet<String> memberNamesToIgnore, IList<IMemberConfig> embeddedMembers,
			IMap<String, IMemberConfig> nameToMemberConfig, IMap<String, IRelationConfig> nameToRelationConfig)
	{
		for (IMemberConfig memberConfig : entityConfig.getMemberConfigIterable())
		{
			if (!(memberConfig instanceof MemberConfig) && !(memberConfig instanceof CompositeMemberConfig))
			{
				throw new IllegalStateException("Member configurations of type '" + memberConfig.getClass().getName() + "' not yet supported");
			}

			String memberName = memberConfig.getName();

			if (memberConfig.isIgnore())
			{
				memberNamesToIgnore.add(memberName);
				memberNamesToIgnore.add(memberName + "Specified");
				continue;
			}

			String[] parts = containsDot.split(memberName, 2);
			boolean isEmbeddedMember = parts.length > 1;

			if (isEmbeddedMember)
			{
				embeddedMembers.add(memberConfig);
				memberNamesToIgnore.add(parts[0]);
				memberNamesToIgnore.add(parts[0] + "Specified");
				continue;
			}
			nameToMemberConfig.put(memberName, memberConfig);
		}

		for (IRelationConfig relationConfig : entityConfig.getRelationConfigIterable())
		{
			String relationName = relationConfig.getName();

			nameToRelationConfig.put(relationName, relationConfig);
		}
	}
}
