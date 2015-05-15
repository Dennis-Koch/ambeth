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
using De.Osthus.Ambeth.Metadata;

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
        public IIntermediateMemberTypeProvider IntermediateMemberTypeProvider { protected get; set; }

        [Autowired]
        public IPropertyInfoProvider PropertyInfoProvider { protected get; set; }

        [Autowired]
	    public IRelationProvider RelationProvider { protected get; set; }
                
        public void AddMembers(EntityMetaData metaData, EntityConfig entityConfig)
        {
		    Type realType = entityConfig.RealType;

            ISet<String> memberNamesToIgnore = new HashSet<String>();
            IList<IMemberConfig> embeddedMembers = new List<IMemberConfig>();
            IMap<String, IMemberConfig> nameToMemberConfig = new HashMap<String, IMemberConfig>();
            IMap<String, IRelationConfig> nameToRelationConfig = new HashMap<String, IRelationConfig>();
            IdentityLinkedMap<String, Member> nameToMemberMap = new IdentityLinkedMap<String, Member>();

            FillNameCollections(entityConfig, memberNamesToIgnore, embeddedMembers, nameToMemberConfig, nameToRelationConfig);

            IdentityLinkedSet<PrimitiveMember> alternateIdMembers = new IdentityLinkedSet<PrimitiveMember>();
            IdentityLinkedSet<PrimitiveMember> primitiveMembers = new IdentityLinkedSet<PrimitiveMember>();
            IdentityLinkedSet<RelationMember> relationMembers = new IdentityLinkedSet<RelationMember>();
            IdentityLinkedSet<Member> notMergeRelevant = new IdentityLinkedSet<Member>();

            IdentityLinkedSet<Member> containedInAlternateIdMember = new IdentityLinkedSet<Member>();

            IPropertyInfo[] properties = PropertyInfoProvider.GetProperties(realType);

			IdentityLinkedMap<String, Member> explicitlyConfiguredMemberNameToMember = new IdentityLinkedMap<String, Member>();

			HashMap<String, IOrmConfig> nameToConfigMap = new HashMap<String, IOrmConfig>();
            // Resolve members for all explicit configurations - both simple and composite ones, each with embedded
            // functionality (dot-member-path)
            foreach (IMemberConfig memberConfig in entityConfig.GetMemberConfigIterable())
		    {
				PutNameToConfigMap(memberConfig, nameToConfigMap);
			    if (memberConfig.Ignore)
			    {
				    continue;
			    }
				HandleMemberConfig(metaData, realType, memberConfig, explicitlyConfiguredMemberNameToMember, nameToMemberMap);
		    }
		    foreach (IRelationConfig relationConfig in entityConfig.GetRelationConfigIterable())
		    {
				PutNameToConfigMap(relationConfig, nameToConfigMap);
			    HandleRelationConfig(realType, relationConfig, explicitlyConfiguredMemberNameToMember);
		    }
			PutNameToConfigMap(entityConfig.IdMemberConfig, nameToConfigMap);
			PutNameToConfigMap(entityConfig.VersionMemberConfig, nameToConfigMap);
			PutNameToConfigMap(entityConfig.CreatedByMemberConfig, nameToConfigMap);
			PutNameToConfigMap(entityConfig.CreatedOnMemberConfig, nameToConfigMap);
			PutNameToConfigMap(entityConfig.UpdatedByMemberConfig, nameToConfigMap);
			PutNameToConfigMap(entityConfig.UpdatedOnMemberConfig, nameToConfigMap);

			metaData.IdMember = HandleMemberConfig(metaData, realType, entityConfig.IdMemberConfig, explicitlyConfiguredMemberNameToMember, nameToMemberMap);
			metaData.VersionMember = HandleMemberConfig(metaData, realType, entityConfig.VersionMemberConfig, explicitlyConfiguredMemberNameToMember, nameToMemberMap);
			metaData.CreatedByMember = HandleMemberConfig(metaData, realType, entityConfig.CreatedByMemberConfig, explicitlyConfiguredMemberNameToMember, nameToMemberMap);
			metaData.CreatedOnMember = HandleMemberConfig(metaData, realType, entityConfig.CreatedOnMemberConfig, explicitlyConfiguredMemberNameToMember, nameToMemberMap);
			metaData.UpdatedByMember = HandleMemberConfig(metaData, realType, entityConfig.UpdatedByMemberConfig, explicitlyConfiguredMemberNameToMember, nameToMemberMap);
			metaData.UpdatedOnMember = HandleMemberConfig(metaData, realType, entityConfig.UpdatedOnMemberConfig, explicitlyConfiguredMemberNameToMember, nameToMemberMap);

            IdentityHashSet<Member> idMembers = new IdentityHashSet<Member>();
            Member idMember = metaData.IdMember;
		    if (idMember is CompositeIdMember)
		    {
			    idMembers.AddAll(((CompositeIdMember) idMember).Members);
		    }
		    else
		    {
			    idMembers.Add(idMember);
		    }

		    // Handle all explicitly configurated members
            foreach (Entry<IOrmConfig, Member> entry in explicitlyConfiguredMemberNameToMember)
		    {
			    IOrmConfig ormConfig = entry.Key;
                Member member = entry.Value;

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
                    if (!relationMembers.Add((RelationMember)member))
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
                    if (!alternateIdMembers.Add((PrimitiveMember)member))
				    {
					    throw new Exception("Member has been registered as alternate id multiple times: " + member.Name);
				    }
                    if (member is CompositeIdMember)
				    {
                        Member[] containedMembers = ((CompositeIdMember)member).Members;
					    containedInAlternateIdMember.AddAll(containedMembers);
				    }
			    }
			    if (!(member is CompositeIdMember) && metaData.VersionMember != member)
			    {
				    // Alternate Ids are normally primitives, too. But Composite Alternate Ids not - only their composite
				    // items are primitives
                    primitiveMembers.Add((PrimitiveMember)member);
			    }
		    }
            IdentityHashSet<String> explicitTypeInfoItems = IdentityHashSet<String>.Create(explicitlyConfiguredMemberNameToMember.Count);
            foreach (Entry<IOrmConfig, Member> entry in explicitlyConfiguredMemberNameToMember)
		    {
                Member member = entry.Value;
			    explicitTypeInfoItems.Add(member.Name);
                if (member is IEmbeddedMember)
			    {
                    explicitTypeInfoItems.Add(((IEmbeddedMember)member).GetMemberPath()[0].Name);
			    }
		    }
		    // Go through the available members to look for potential auto-mapping (simple, no embedded)
            for (int i = 0; i < properties.Length; i++)
		    {
                IPropertyInfo property = properties[i];
                String memberName = property.Name;
			    if (memberNamesToIgnore.Contains(memberName))
			    {
				    continue;
			    }
                if (explicitTypeInfoItems.Contains(memberName))
			    {
				    // already configured, no auto mapping needed for this member
				    continue;
			    }
                MethodPropertyInfo mProperty = (MethodPropertyInfo) property;
			    Type elementType = TypeInfoItemUtil.GetElementTypeUsingReflection(mProperty.Getter.ReturnType, null);
			    if ((nameToMemberMap.Get(property.Name) is RelationMember) || RelationProvider.IsEntityType(elementType))
			    {
				    RelationMember member = GetRelationMember(metaData.EntityType, property, nameToMemberMap);
				    relationMembers.Add(member);
				    continue;
			    }
                PrimitiveMember member2 = GetPrimitiveMember(metaData.EntityType, property, nameToMemberMap);
			    if (metaData.IdMember == null && memberName.Equals(EntityMetaData.DEFAULT_NAME_ID))
			    {
				    metaData.IdMember = member2;
				    continue;
			    }
			    if (idMembers.Contains(member2) && !alternateIdMembers.Contains(member2) && !containedInAlternateIdMember.Contains(member2))
			    {
				    continue;
			    }
                if (member2.Equals(metaData.IdMember) || member2.Equals(metaData.VersionMember) || member2.Equals(metaData.CreatedByMember)
                    || member2.Equals(metaData.CreatedOnMember) || member2.Equals(metaData.UpdatedByMember) || member2.Equals(metaData.UpdatedOnMember))
                {
                    continue;
                }
			    if (metaData.VersionMember == null && memberName.Equals(EntityMetaData.DEFAULT_NAME_VERSION))
			    {
				    metaData.VersionMember = member2;
				    continue;
			    }
			    if (metaData.CreatedByMember == null && memberName.Equals(EntityMetaData.DEFAULT_NAME_CREATED_BY))
			    {
				    metaData.CreatedByMember = member2;
			    }
			    else if (metaData.CreatedOnMember == null && memberName.Equals(EntityMetaData.DEFAULT_NAME_CREATED_ON))
			    {
				    metaData.CreatedOnMember = member2;
			    }
			    else if (metaData.UpdatedByMember == null && memberName.Equals(EntityMetaData.DEFAULT_NAME_UPDATED_BY))
			    {
				    metaData.UpdatedByMember = member2;
			    }
			    else if (metaData.UpdatedOnMember == null && memberName.Equals(EntityMetaData.DEFAULT_NAME_UPDATED_ON))
			    {
				    metaData.UpdatedOnMember = member2;
			    }
			    primitiveMembers.Add(member2);
		    }
            foreach (PrimitiveMember member in primitiveMembers)
		    {
			    String memberName = member.Name;
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
		    }
            FilterWrongRelationMappings(relationMembers);
		    // Order of setter calls is important
		    PrimitiveMember[] primitives = primitiveMembers.ToArray();
		    PrimitiveMember[] alternateIds = alternateIdMembers.ToArray();
		    RelationMember[] relations = relationMembers.ToArray();
		    Array.Sort(primitives);
            Array.Sort(alternateIds);
		    Array.Sort(relations);
		    metaData.PrimitiveMembers = primitives;
		    metaData.AlternateIdMembers = alternateIds;
		    metaData.RelationMembers = relations;

		    foreach (Member member in notMergeRelevant)
		    {
			    metaData.SetMergeRelevant(member, false);
		    }
            if (metaData.IdMember == null)
            {
                throw new Exception("No ID member could be resolved for entity of type " + metaData.RealType);
            }
        }

		protected void PutNameToConfigMap(IOrmConfig config, IMap<String, IOrmConfig> nameToConfigMap)
		{
			if (config == null)
			{
				return;
			}
			nameToConfigMap.Put(config.Name, config);
			if (config is CompositeMemberConfig)
			{
				foreach (MemberConfig member in ((CompositeMemberConfig) config).GetMembers())
				{
					PutNameToConfigMap(member, nameToConfigMap);
				}
			}
		}

        protected void FilterWrongRelationMappings(IISet<RelationMember> relationMembers)
        {
            // filter all relations which can not be a relation because of explicit embedded property mapping
            IdentityHashSet<RelationMember> toRemove = new IdentityHashSet<RelationMember>();
            foreach (RelationMember relationMember in relationMembers)
            {
                String[] memberPath = EmbeddedMember.Split(relationMember.Name);
                foreach (RelationMember otherRelationMember in relationMembers)
                {
                    if (Object.ReferenceEquals(relationMember, otherRelationMember) || toRemove.Contains(otherRelationMember))
                    {
                        continue;
                    }
                    if (!(otherRelationMember is IEmbeddedMember))
                    {
                        // only embedded members can help identifying other wrong relation members
                        continue;
                    }
                    String[] otherMemberPath = ((IEmbeddedMember)otherRelationMember).GetMemberPathToken();
                    if (memberPath.Length > otherMemberPath.Length)
                    {
                        continue;
                    }
                    bool match = true;
                    for (int a = 0, size = memberPath.Length; a < size; a++)
                    {
                        if (!memberPath[a].Equals(otherMemberPath[a]))
                        {
                            match = false;
                            break;
                        }
                    }
                    if (match)
                    {
                        toRemove.Add(relationMember);
                        break;
                    }
                }
            }
            relationMembers.RemoveAll(toRemove);
        }

        protected PrimitiveMember GetPrimitiveMember(Type entityType, IPropertyInfo property, IMap<String, Member> nameToMemberMap)
	    {
		    PrimitiveMember member = (PrimitiveMember) nameToMemberMap.Get(property.Name);
		    if (member != null)
		    {
			    return member;
		    }
            member = IntermediateMemberTypeProvider.GetIntermediatePrimitiveMember(entityType, property.Name);
		    nameToMemberMap.Put(property.Name, member);
		    return member;
	    }

        protected RelationMember GetRelationMember(Type entityType, IPropertyInfo property, IMap<String, Member> nameToMemberMap)
	    {
            RelationMember member = (RelationMember)nameToMemberMap.Get(property.Name);
		    if (member != null)
		    {
			    return member;
		    }
            member = IntermediateMemberTypeProvider.GetIntermediateRelationMember(entityType, property.Name);
		    nameToMemberMap.Put(property.Name, member);
		    return member;
	    }

		protected PrimitiveMember HandleMemberConfigIfNew(Type entityType, String memberName, IMap<String, Member> memberConfigToInfoItem)
	    {
			PrimitiveMember member = (PrimitiveMember)memberConfigToInfoItem.Get(memberName);
		    if (member != null)
		    {
			    return member;
		    }
			member = IntermediateMemberTypeProvider.GetIntermediatePrimitiveMember(entityType, memberName);
		    if (member == null)
		    {
				throw new Exception("No member with name '" + memberName + "' found on entity type '" + entityType.Name + "'");
		    }
			memberConfigToInfoItem.Put(memberName, member);
		    return member;
	    }

        protected PrimitiveMember HandleMemberConfig(IEntityMetaData metaData, Type realType, IMemberConfig memberConfig,
			IMap<String, Member> explicitMemberNameToMember, IMap<String, Member> allMemberNameToMember)
	    {
		    if (memberConfig == null)
		    {
			    return null;
		    }
		    if (!(memberConfig is CompositeMemberConfig))
		    {
				PrimitiveMember member = HandleMemberConfigIfNew(realType, memberConfig.Name, explicitMemberNameToMember);
				explicitMemberNameToMember.Put(memberConfig.Name, member);
				((IPrimitiveMemberWrite)member).SetTransient(memberConfig.Transient);

				PrimitiveMember definedBy = memberConfig.DefinedBy != null ? HandleMemberConfigIfNew(realType, memberConfig.DefinedBy,
					allMemberNameToMember) : null;
				((IPrimitiveMemberWrite)member).SetDefinedBy(definedBy);
				return member;
		    }
		    MemberConfig[] memberConfigs = ((CompositeMemberConfig) memberConfig).GetMembers();
            PrimitiveMember[] members = new PrimitiveMember[memberConfigs.Length];
		    for (int a = memberConfigs.Length; a-- > 0;)
		    {
			    MemberConfig memberPart = memberConfigs[a];
				PrimitiveMember member = HandleMemberConfigIfNew(realType, memberPart.Name, explicitMemberNameToMember);
			    members[a] = member;
		    }
            PrimitiveMember compositeIdMember = CompositeIdFactory.CreateCompositeIdMember(metaData, members);
			explicitMemberNameToMember.Put(memberConfig.Name, compositeIdMember);
			((IPrimitiveMemberWrite)compositeIdMember).SetTransient(memberConfig.Transient);

			PrimitiveMember definedBy2 = memberConfig.DefinedBy != null ? HandleMemberConfigIfNew(realType, memberConfig.DefinedBy, allMemberNameToMember)
				: null;
			((IPrimitiveMemberWrite)compositeIdMember).SetDefinedBy(definedBy2);
			return compositeIdMember;
	    }

        protected Member HandleRelationConfig(Type realType, IRelationConfig relationConfig, IMap<String, Member> relationConfigToInfoItem)
	    {
		    if (relationConfig == null)
		    {
			    return null;
		    }
            Member member = relationConfigToInfoItem.Get(relationConfig.Name);
		    if (member != null)
		    {
			    return member;
		    }
            member = IntermediateMemberTypeProvider.GetIntermediateRelationMember(realType, relationConfig.Name);
		    if (member == null)
		    {
                throw new Exception("No member with name '" + relationConfig.Name + "' found on entity type '" + realType.Name + "'");
		    }
		    relationConfigToInfoItem.Put(relationConfig.Name, member);
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
