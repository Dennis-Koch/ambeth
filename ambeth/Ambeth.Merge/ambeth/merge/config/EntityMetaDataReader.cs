using System;
using System.Collections;
using System.Collections.Generic;
using System.Linq;
using System.Xml.Linq;
using De.Osthus.Ambeth.Collections;
using De.Osthus.Ambeth.Config;
using De.Osthus.Ambeth.Ioc;
using De.Osthus.Ambeth.Log;
using De.Osthus.Ambeth.Merge.Model;
using De.Osthus.Ambeth.Typeinfo;
using De.Osthus.Ambeth.Util;
using De.Osthus.Ambeth.Util.Xml;
using De.Osthus.Ambeth.Event;
using De.Osthus.Ambeth.Orm;
using De.Osthus.Ambeth.CompositeId;
using De.Osthus.Ambeth.Ioc.Annotation;

namespace De.Osthus.Ambeth.Merge.Config
{
    public class EntityMetaDataReader : IEntityMetaDataReader
    {
        private static readonly char[] dot = { '.' };

        [LogInstance]
        public ILogger Log { private get; set; }

        [Autowired]
	    public ICompositeIdFactory CompositeIdFactory { protected get; set; }

        [Autowired]
	    public IRelationProvider RelationProvider { protected get; set; }

        [Autowired]
	    public ITypeInfoProvider TypeInfoProvider { protected get; set; }
        
        public void AddMembers(EntityMetaData metaData, EntityConfig entityConfig)
        {
		    Type realType = entityConfig.RealType;

            ISet<String> memberNamesToIgnore = new HashSet<String>();
            IList<IMemberConfig> embeddedMembers = new List<IMemberConfig>();
            IMap<String, IMemberConfig> nameToMemberConfig = new HashMap<String, IMemberConfig>();
            IMap<String, IRelationConfig> nameToRelationConfig = new HashMap<String, IRelationConfig>();

            FillNameCollections(entityConfig, memberNamesToIgnore, embeddedMembers, nameToMemberConfig, nameToRelationConfig);

            IdentityLinkedSet<ITypeInfoItem> alternateIdMembers = new IdentityLinkedSet<ITypeInfoItem>();
            IdentityLinkedSet<ITypeInfoItem> primitiveMembers = new IdentityLinkedSet<ITypeInfoItem>();
            IdentityLinkedSet<IRelationInfoItem> relationMembers = new IdentityLinkedSet<IRelationInfoItem>();
            IdentityLinkedSet<ITypeInfoItem> notMergeRelevant = new IdentityLinkedSet<ITypeInfoItem>();

            IdentityLinkedSet<ITypeInfoItem> containedInAlternateIdMember = new IdentityLinkedSet<ITypeInfoItem>();

            ITypeInfo typeInfo = TypeInfoProvider.GetTypeInfo(realType);

            IdentityLinkedMap<IOrmConfig, ITypeInfoItem> memberConfigToInfoItem = new IdentityLinkedMap<IOrmConfig, ITypeInfoItem>();

            // Resolve members for all explicit configurations - both simple and composite ones, each with embedded
            // functionality (dot-member-path)
            foreach (IMemberConfig memberConfig in entityConfig.GetMemberConfigIterable())
		    {
			    if (memberConfig.Ignore)
			    {
				    continue;
			    }
			    HandleMemberConfig(metaData, realType, memberConfig, memberConfigToInfoItem);
		    }
		    foreach (IRelationConfig relationConfig in entityConfig.GetRelationConfigIterable())
		    {
			    HandleRelationConfig(realType, relationConfig, memberConfigToInfoItem);
		    }
		    metaData.IdMember = HandleMemberConfig(metaData, realType, entityConfig.IdMemberConfig, memberConfigToInfoItem);
		    metaData.VersionMember = HandleMemberConfig(metaData, realType, entityConfig.VersionMemberConfig, memberConfigToInfoItem);
		    metaData.CreatedByMember = HandleMemberConfig(metaData, realType, entityConfig.CreatedByMemberConfig, memberConfigToInfoItem);
		    metaData.CreatedOnMember = HandleMemberConfig(metaData, realType, entityConfig.CreatedOnMemberConfig, memberConfigToInfoItem);
		    metaData.UpdatedByMember = HandleMemberConfig(metaData, realType, entityConfig.UpdatedByMemberConfig, memberConfigToInfoItem);
		    metaData.UpdatedOnMember = HandleMemberConfig(metaData, realType, entityConfig.UpdatedOnMemberConfig, memberConfigToInfoItem);

		    IdentityHashSet<ITypeInfoItem> idMembers = new IdentityHashSet<ITypeInfoItem>();
		    ITypeInfoItem idMember = metaData.IdMember;
		    if (idMember is CompositeIdTypeInfoItem)
		    {
			    idMembers.AddAll(((CompositeIdTypeInfoItem) idMember).Members);
		    }
		    else
		    {
			    idMembers.Add(idMember);
		    }

		    // Handle all explicitly configurated members
		    foreach (Entry<IOrmConfig, ITypeInfoItem> entry in memberConfigToInfoItem)
		    {
			    IOrmConfig ormConfig = entry.Key;
			    ITypeInfoItem member = entry.Value;

			    if (idMembers.Contains(member))
			    {
				    continue;
			    }
			    if (ormConfig.ExplicitlyNotMergeRelevant)
			    {
				    notMergeRelevant.Add(member);
			    }
			    if (ormConfig is IRelationConfig)
			    {
				    if (!relationMembers.Add((IRelationInfoItem) member))
				    {
					    throw new Exception("Member has been registered as relation multiple times: " + member.Name);
				    }
				    continue;
			    }
			    if (!(ormConfig is IMemberConfig))
			    {
				    continue;
			    }
			    if (((IMemberConfig) ormConfig).AlternateId)
			    {
				    if (!alternateIdMembers.Add(member))
				    {
					    throw new Exception("Member has been registered as alternate id multiple times: " + member.Name);
				    }
				    if (member is CompositeMemberConfig)
				    {
					    ITypeInfoItem[] containedMembers = ((CompositeIdTypeInfoItem) member).Members;
					    containedInAlternateIdMember.AddAll(containedMembers);
				    }
			    }
			    if (!(member is CompositeIdTypeInfoItem) && metaData.VersionMember != member)
			    {
				    // Alternate Ids are normally primitives, too. But Composite Alternate Ids not - only their composite
				    // items are primitives
				    primitiveMembers.Add(member);
			    }
		    }
		    IdentityHashSet<ITypeInfoItem> explicitTypeInfoItems = IdentityHashSet<ITypeInfoItem>.Create(memberConfigToInfoItem.Count);
		    foreach (Entry<IOrmConfig, ITypeInfoItem> entry in memberConfigToInfoItem)
		    {
			    ITypeInfoItem member = entry.Value;
			    explicitTypeInfoItems.Add(member);
			    if (member is IEmbeddedTypeInfoItem)
			    {
				    explicitTypeInfoItems.Add(((IEmbeddedTypeInfoItem) member).MemberPath[0]);
			    }
		    }
		    // Go through the available members to look for potential auto-mapping (simple, no embedded)
		    ITypeInfoItem[] members = typeInfo.Members;
		    for (int i = 0; i < members.Length; i++)
		    {
			    ITypeInfoItem member = members[i];
			    String memberName = member.Name;

			    if (memberNamesToIgnore.Contains(memberName))
			    {
				    continue;
			    }
			    if (explicitTypeInfoItems.Contains(member))
			    {
				    // already configured, no auto mapping needed for this member
				    continue;
			    }
			    if (metaData.IdMember == null && memberName.Equals(EntityMetaData.DEFAULT_NAME_ID))
			    {
				    metaData.IdMember = member;
				    continue;
			    }
			    if (idMembers.Contains(member) && !alternateIdMembers.Contains(member) && !containedInAlternateIdMember.Contains(member))
			    {
				    continue;
			    }
			    if (metaData.VersionMember == null && memberName.Equals(EntityMetaData.DEFAULT_NAME_VERSION))
			    {
				    metaData.VersionMember = member;
				    continue;
			    }
			    if (metaData.CreatedByMember == null && memberName.Equals(EntityMetaData.DEFAULT_NAME_CREATED_BY))
			    {
				    metaData.CreatedByMember = member;
			    }
			    else if (metaData.CreatedOnMember == null && memberName.Equals(EntityMetaData.DEFAULT_NAME_CREATED_ON))
			    {
				    metaData.CreatedOnMember = member;
			    }
			    else if (metaData.UpdatedByMember == null && memberName.Equals(EntityMetaData.DEFAULT_NAME_UPDATED_BY))
			    {
				    metaData.UpdatedByMember = member;
			    }
			    else if (metaData.UpdatedOnMember == null && memberName.Equals(EntityMetaData.DEFAULT_NAME_UPDATED_ON))
			    {
				    metaData.UpdatedOnMember = member;
			    }
			    if (!member.CanWrite)
			    {
				    continue;
			    }
			    if (RelationProvider.IsEntityType(member.ElementType))
			    {
				    relationMembers.Add((IRelationInfoItem) member);
				    continue;
			    }
			    primitiveMembers.Add(member);
		    }
		    // Order of setter calls is important
		    metaData.PrimitiveMembers = primitiveMembers.ToArray();
		    metaData.AlternateIdMembers = alternateIdMembers.ToArray();
		    metaData.RelationMembers = relationMembers.ToArray();

		    foreach (ITypeInfoItem member in notMergeRelevant)
		    {
			    metaData.SetMergeRelevant(member, false);
		    }
            if (metaData.IdMember == null)
            {
                throw new Exception("No ID member could be resolved for entity of type " + metaData.RealType);
            }
        }

        protected ITypeInfoItem HandleMemberConfigIfNew(Type entityType, IMemberConfig itemConfig, IMap<IOrmConfig, ITypeInfoItem> memberConfigToInfoItem)
	    {
		    ITypeInfoItem member = memberConfigToInfoItem.Get(itemConfig);
		    if (member != null)
		    {
			    return member;
		    }
		    member = TypeInfoProvider.GetHierarchicMember(entityType, itemConfig.Name);
		    if (member == null)
		    {
			    throw new Exception("No member with name '" + itemConfig.Name + "' found on entity type '" + entityType.Name + "'");
		    }
		    memberConfigToInfoItem.Put(itemConfig, member);
		    return member;
	    }

        protected ITypeInfoItem HandleMemberConfig(IEntityMetaData metaData, Type realType, IMemberConfig memberConfig,
			IMap<IOrmConfig, ITypeInfoItem> memberConfigToInfoItem)
	    {
		    if (memberConfig == null)
		    {
			    return null;
		    }
		    if (!(memberConfig is CompositeMemberConfig))
		    {
			    return HandleMemberConfigIfNew(realType, memberConfig, memberConfigToInfoItem);
		    }
		    MemberConfig[] memberConfigs = ((CompositeMemberConfig) memberConfig).GetMembers();
		    ITypeInfoItem[] members = new ITypeInfoItem[memberConfigs.Length];
		    for (int a = memberConfigs.Length; a-- > 0;)
		    {
			    MemberConfig memberPart = memberConfigs[a];
			    ITypeInfoItem member = HandleMemberConfigIfNew(realType, memberPart, memberConfigToInfoItem);
			    members[a] = member;
		    }
		    ITypeInfoItem compositeIdMember = CompositeIdFactory.CreateCompositeIdMember(metaData, members);
		    memberConfigToInfoItem.Put(memberConfig, compositeIdMember);
		    return compositeIdMember;
	    }

	    protected ITypeInfoItem HandleRelationConfig(Type realType, IRelationConfig relationConfig, IMap<IOrmConfig, ITypeInfoItem> relationConfigToInfoItem)
	    {
		    if (relationConfig == null)
		    {
			    return null;
		    }
		    ITypeInfoItem member = relationConfigToInfoItem.Get(relationConfig);
		    if (member != null)
		    {
			    return member;
		    }
		    member = TypeInfoProvider.GetHierarchicMember(realType, relationConfig.Name);
		    if (member == null)
		    {
                throw new Exception("No member with name '" + relationConfig.Name + "' found on entity type '" + realType.Name + "'");
		    }
		    relationConfigToInfoItem.Put(relationConfig, member);
		    return member;
	    }

        protected void FillNameCollections(EntityConfig entityConfig, ISet<String> memberNamesToIgnore, IList<IMemberConfig> embeddedMembers,
                IMap<String, IMemberConfig> nameToMemberConfig, IMap<String, IRelationConfig> nameToRelationConfig)
        {
            foreach (IMemberConfig memberConfig in entityConfig.GetMemberConfigIterable())
            {
                String memberName = memberConfig.Name;

                if (memberConfig.Ignore)
                {
                    memberNamesToIgnore.Add(memberName);
                    memberNamesToIgnore.Add(memberName + "Specified");
                    continue;
                }

                String[] parts = memberName.Split(dot);
                bool isEmbeddedMember = parts.Length > 1;

                if (isEmbeddedMember)
                {
                    embeddedMembers.Add(memberConfig);
                    memberNamesToIgnore.Add(parts[0]);
                    memberNamesToIgnore.Add(parts[0] + "Specified");
                    continue;
                }

                nameToMemberConfig.Put(memberName, memberConfig);
            }

            foreach (IRelationConfig relationConfig in entityConfig.GetRelationConfigIterable())
            {
                String relationName = relationConfig.Name;

                nameToRelationConfig.Put(relationName, relationConfig);
            }
        }
    }
}
