package de.osthus.ambeth.merge.config;

import java.util.Arrays;
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
import de.osthus.ambeth.collections.LinkedHashMap;
import de.osthus.ambeth.collections.LinkedHashSet;
import de.osthus.ambeth.compositeid.CompositeIdMember;
import de.osthus.ambeth.compositeid.ICompositeIdFactory;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.merge.model.EntityMetaData;
import de.osthus.ambeth.merge.model.IEntityMetaData;
import de.osthus.ambeth.metadata.EmbeddedMember;
import de.osthus.ambeth.metadata.IEmbeddedMember;
import de.osthus.ambeth.metadata.IIntermediateMemberTypeProvider;
import de.osthus.ambeth.metadata.Member;
import de.osthus.ambeth.metadata.PrimitiveMember;
import de.osthus.ambeth.metadata.RelationMember;
import de.osthus.ambeth.orm.CompositeMemberConfig;
import de.osthus.ambeth.orm.EntityConfig;
import de.osthus.ambeth.orm.IMemberConfig;
import de.osthus.ambeth.orm.IOrmConfig;
import de.osthus.ambeth.orm.IRelationConfig;
import de.osthus.ambeth.orm.MemberConfig;
import de.osthus.ambeth.typeinfo.IPropertyInfo;
import de.osthus.ambeth.typeinfo.IPropertyInfoProvider;
import de.osthus.ambeth.typeinfo.IRelationProvider;
import de.osthus.ambeth.typeinfo.MethodPropertyInfo;
import de.osthus.ambeth.typeinfo.TypeInfoItemUtil;

public class EntityMetaDataReader implements IEntityMetaDataReader
{
	@LogInstance
	private ILogger log;

	private static final Pattern containsDot = Pattern.compile("\\.");

	@Autowired
	protected ICompositeIdFactory compositeIdFactory;

	@Autowired
	protected IIntermediateMemberTypeProvider intermediateMemberTypeProvider;

	@Autowired
	protected IPropertyInfoProvider propertyInfoProvider;

	@Autowired
	protected IRelationProvider relationProvider;

	@Override
	public void addMembers(EntityMetaData metaData, EntityConfig entityConfig)
	{
		Class<?> realType = entityConfig.getRealType();

		HashSet<String> memberNamesToIgnore = new HashSet<String>();
		ArrayList<IMemberConfig> embeddedMembers = new ArrayList<IMemberConfig>();
		HashMap<String, IMemberConfig> nameToMemberConfig = new HashMap<String, IMemberConfig>();
		HashMap<String, IRelationConfig> nameToRelationConfig = new HashMap<String, IRelationConfig>();
		LinkedHashMap<String, Member> nameToMemberMap = new LinkedHashMap<String, Member>();

		fillNameCollections(entityConfig, memberNamesToIgnore, embeddedMembers, nameToMemberConfig, nameToRelationConfig);

		LinkedHashSet<PrimitiveMember> alternateIdMembers = new LinkedHashSet<PrimitiveMember>();
		LinkedHashSet<PrimitiveMember> primitiveMembers = new LinkedHashSet<PrimitiveMember>();
		LinkedHashSet<RelationMember> relationMembers = new LinkedHashSet<RelationMember>();
		LinkedHashSet<Member> notMergeRelevant = new IdentityLinkedSet<Member>();

		LinkedHashSet<Member> containedInAlternateIdMember = new IdentityLinkedSet<Member>();

		IPropertyInfo[] properties = propertyInfoProvider.getProperties(realType);

		IdentityLinkedMap<IOrmConfig, Member> memberConfigToInfoItem = new IdentityLinkedMap<IOrmConfig, Member>();

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

		IdentityHashSet<Member> idMembers = new IdentityHashSet<Member>();
		Member idMember = metaData.getIdMember();
		if (idMember instanceof CompositeIdMember)
		{
			idMembers.addAll(((CompositeIdMember) idMember).getMembers());
		}
		else if (idMember != null)
		{
			idMembers.add(idMember);
		}

		// Handle all explicitly configurated members
		for (Entry<IOrmConfig, Member> entry : memberConfigToInfoItem)
		{
			IOrmConfig ormConfig = entry.getKey();
			Member member = entry.getValue();

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
				if (!relationMembers.add((RelationMember) member))
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
				if (!alternateIdMembers.add((PrimitiveMember) member))
				{
					throw new IllegalStateException("Member has been registered as alternate id multiple times: " + member.getName());
				}
				if (member instanceof CompositeIdMember)
				{
					Member[] containedMembers = ((CompositeIdMember) member).getMembers();
					containedInAlternateIdMember.addAll(containedMembers);
				}
			}
			if (!(member instanceof CompositeIdMember) && metaData.getVersionMember() != member)
			{
				// Alternate Ids are normally primitives, too. But Composite Alternate Ids not - only their composite
				// items are primitives
				primitiveMembers.add((PrimitiveMember) member);
			}
		}
		IdentityHashSet<String> explicitTypeInfoItems = IdentityHashSet.<String> create(memberConfigToInfoItem.size());
		for (Entry<IOrmConfig, Member> entry : memberConfigToInfoItem)
		{
			Member member = entry.getValue();
			explicitTypeInfoItems.add(member.getName());
			if (member instanceof IEmbeddedMember)
			{
				explicitTypeInfoItems.add(((IEmbeddedMember) member).getMemberPath()[0].getName());
			}
		}
		// Go through the available members to look for potential auto-mapping (simple, no embedded)
		for (int i = 0; i < properties.length; i++)
		{
			IPropertyInfo property = properties[i];
			String memberName = property.getName();
			if (memberNamesToIgnore.contains(memberName))
			{
				continue;
			}
			if (explicitTypeInfoItems.contains(memberName))
			{
				// already configured, no auto mapping needed for this member
				continue;
			}
			MethodPropertyInfo mProperty = (MethodPropertyInfo) property;
			Class<?> elementType = TypeInfoItemUtil.getElementTypeUsingReflection(mProperty.getGetter().getReturnType(), mProperty.getGetter()
					.getGenericReturnType());
			if ((nameToMemberMap.get(property.getName()) instanceof RelationMember) || relationProvider.isEntityType(elementType))
			{
				RelationMember member = getRelationMember(metaData.getEntityType(), property, nameToMemberMap);
				relationMembers.add(member);
				continue;
			}
			PrimitiveMember member = getPrimitiveMember(metaData.getEntityType(), property, nameToMemberMap);
			if (metaData.getIdMember() == null && memberName.equals(EntityMetaData.DEFAULT_NAME_ID))
			{
				metaData.setIdMember(member);
				continue;
			}
			if (idMembers.contains(member) && !alternateIdMembers.contains(member) && !containedInAlternateIdMember.contains(member))
			{
				continue;
			}
			if (member.equals(metaData.getIdMember()) || member.equals(metaData.getVersionMember()) || member.equals(metaData.getCreatedByMember())
					|| member.equals(metaData.getCreatedOnMember()) || member.equals(metaData.getUpdatedByMember())
					|| member.equals(metaData.getUpdatedOnMember()))
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
			primitiveMembers.add(member);
		}
		for (PrimitiveMember member : primitiveMembers)
		{
			String memberName = member.getName();
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
		}
		filterWrongRelationMappings(relationMembers);
		// Order of setter calls is important
		PrimitiveMember[] primitives = primitiveMembers.toArray(PrimitiveMember.class);
		PrimitiveMember[] alternateIds = alternateIdMembers.toArray(PrimitiveMember.class);
		RelationMember[] relations = relationMembers.toArray(RelationMember.class);
		Arrays.sort(primitives);
		Arrays.sort(alternateIds);
		Arrays.sort(relations);
		metaData.setPrimitiveMembers(primitives);
		metaData.setAlternateIdMembers(alternateIds);
		metaData.setRelationMembers(relations);

		for (Member member : notMergeRelevant)
		{
			metaData.setMergeRelevant(member, false);
		}
		if (metaData.getIdMember() == null)
		{
			throw new IllegalStateException("No ID member could be resolved for entity of type " + metaData.getRealType());
		}
	}

	protected void filterWrongRelationMappings(ISet<RelationMember> relationMembers)
	{
		// filter all relations which can not be a relation because of explicit embedded property mapping
		IdentityHashSet<RelationMember> toRemove = new IdentityHashSet<RelationMember>();
		for (RelationMember relationMember : relationMembers)
		{
			String[] memberPath = EmbeddedMember.split(relationMember.getName());
			for (RelationMember otherRelationMember : relationMembers)
			{
				if (relationMember == otherRelationMember || toRemove.contains(otherRelationMember))
				{
					continue;
				}
				if (!(otherRelationMember instanceof IEmbeddedMember))
				{
					// only embedded members can help identifying other wrong relation members
					continue;
				}
				String[] otherMemberPath = ((IEmbeddedMember) otherRelationMember).getMemberPathToken();
				if (memberPath.length > otherMemberPath.length)
				{
					continue;
				}
				boolean match = true;
				for (int a = 0, size = memberPath.length; a < size; a++)
				{
					if (!memberPath[a].equals(otherMemberPath[a]))
					{
						match = false;
						break;
					}
				}
				if (match)
				{
					toRemove.add(relationMember);
					break;
				}
			}
		}
		relationMembers.removeAll(toRemove);
	}

	protected PrimitiveMember getPrimitiveMember(Class<?> entityType, IPropertyInfo property, Map<String, Member> nameToMemberMap)
	{
		PrimitiveMember member = (PrimitiveMember) nameToMemberMap.get(property.getName());
		if (member != null)
		{
			return member;
		}
		member = intermediateMemberTypeProvider.getIntermediatePrimitiveMember(entityType, property.getName());
		nameToMemberMap.put(property.getName(), member);
		return member;
	}

	protected RelationMember getRelationMember(Class<?> entityType, IPropertyInfo property, Map<String, Member> nameToMemberMap)
	{
		RelationMember member = (RelationMember) nameToMemberMap.get(property.getName());
		if (member != null)
		{
			return member;
		}
		member = intermediateMemberTypeProvider.getIntermediateRelationMember(entityType, property.getName());
		nameToMemberMap.put(property.getName(), member);
		return member;
	}

	protected PrimitiveMember handleMemberConfigIfNew(Class<?> entityType, IMemberConfig itemConfig, Map<IOrmConfig, Member> memberConfigToInfoItem)
	{
		PrimitiveMember member = (PrimitiveMember) memberConfigToInfoItem.get(itemConfig);
		if (member != null)
		{
			return member;
		}
		member = intermediateMemberTypeProvider.getIntermediatePrimitiveMember(entityType, itemConfig.getName());
		if (member == null)
		{
			throw new RuntimeException("No member with name '" + itemConfig.getName() + "' found on entity type '" + entityType.getName() + "'");
		}
		memberConfigToInfoItem.put(itemConfig, member);
		return member;
	}

	protected PrimitiveMember handleMemberConfig(IEntityMetaData metaData, Class<?> realType, IMemberConfig memberConfig,
			Map<IOrmConfig, Member> memberConfigToInfoItem)
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
		PrimitiveMember[] members = new PrimitiveMember[memberConfigs.length];
		for (int a = memberConfigs.length; a-- > 0;)
		{
			MemberConfig memberPart = memberConfigs[a];
			PrimitiveMember member = handleMemberConfigIfNew(realType, memberPart, memberConfigToInfoItem);
			members[a] = member;
		}
		PrimitiveMember compositeIdMember = compositeIdFactory.createCompositeIdMember(metaData, members);
		memberConfigToInfoItem.put(memberConfig, compositeIdMember);
		return compositeIdMember;
	}

	protected Member handleRelationConfig(Class<?> realType, IRelationConfig relationConfig, Map<IOrmConfig, Member> relationConfigToInfoItem)
	{
		if (relationConfig == null)
		{
			return null;
		}
		Member member = relationConfigToInfoItem.get(relationConfig);
		if (member != null)
		{
			return member;
		}
		member = intermediateMemberTypeProvider.getIntermediateRelationMember(realType, relationConfig.getName());
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
