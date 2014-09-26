using System;
using System.Collections.Generic;
using De.Osthus.Ambeth.Collections;
using De.Osthus.Ambeth.Ioc.Annotation;
using De.Osthus.Ambeth.Log;
using De.Osthus.Ambeth.Merge;
using De.Osthus.Ambeth.Merge.Model;
using De.Osthus.Ambeth.Merge.Transfer;
using De.Osthus.Ambeth.Typeinfo;
using De.Osthus.Ambeth.Util;
using De.Osthus.Ambeth.Metadata;
using De.Osthus.Ambeth.Cache;

namespace De.Osthus.Ambeth.Converter.Merge
{
    public class EntityMetaDataConverter : IDedicatedConverter
    {
	    [LogInstance]
		public ILogger Log { private get; set; }

        [Autowired]
        public ICacheModification CacheModification { protected get; set; }

        [Autowired]
        public IEntityFactory EntityFactory { protected get; set; }

        [Autowired]
        public IMemberTypeProvider MemberTypeProvider { protected get; set; }
        
        [Autowired]
        public IProxyHelper ProxyHelper { protected get; set; }
                
	    public Object ConvertValueToType(Type expectedType, Type sourceType, Object value, Object additionalInformation)
	    {
            if (sourceType.IsAssignableFrom(typeof(EntityMetaData)))
		    {
			    EntityMetaData source = (EntityMetaData) value;

			    EntityMetaDataTransfer target = new EntityMetaDataTransfer();
			    target.EntityType = source.EntityType;
			    target.IdMemberName = GetNameOfMember(source.IdMember);
			    target.VersionMemberName = GetNameOfMember(source.VersionMember);
			    target.CreatedByMemberName = GetNameOfMember(source.CreatedByMember);
			    target.CreatedOnMemberName = GetNameOfMember(source.CreatedOnMember);
			    target.UpdatedByMemberName = GetNameOfMember(source.UpdatedByMember);
			    target.UpdatedOnMemberName = GetNameOfMember(source.UpdatedOnMember);
			    target.AlternateIdMemberNames = GetNamesOfMembers(source.AlternateIdMembers);
			    target.PrimitiveMemberNames = GetNamesOfMembers(source.PrimitiveMembers);
			    target.RelationMemberNames = GetNamesOfMembers(source.RelationMembers);
			    target.AlternateIdMemberIndicesInPrimitives = source.AlternateIdMemberIndicesInPrimitives;
			    target.TypesRelatingToThis = source.TypesRelatingToThis;
			    target.TypesToCascadeDelete = ListUtil.ToArray(source.CascadeDeleteTypes);
                PrimitiveMember[] primitiveMembers = source.PrimitiveMembers;
			    RelationMember[] relationMembers = source.RelationMembers;
			    IList<String> mergeRelevantNames = new List<String>();
			    for (int a = primitiveMembers.Length; a-- > 0;)
			    {
                    PrimitiveMember member = primitiveMembers[a];
				    if (source.IsMergeRelevant(member))
				    {
					    mergeRelevantNames.Add(GetNameOfMember(member));
				    }
			    }
			    for (int a = relationMembers.Length; a-- > 0;)
			    {
                    RelationMember member = relationMembers[a];
				    if (source.IsMergeRelevant(member))
				    {
					    mergeRelevantNames.Add(GetNameOfMember(member));
				    }
			    }
			    target.MergeRelevantNames = ListUtil.ToArray<String>(mergeRelevantNames);
			    return target;
		    }
            else if (sourceType.IsAssignableFrom(typeof(EntityMetaDataTransfer)))
		    {
			    EntityMetaDataTransfer source = (EntityMetaDataTransfer) value;

                HashMap<String, Member> nameToMemberDict = new HashMap<String, Member>();

                EntityMetaData target = new EntityMetaData();
                Type entityType = source.EntityType;
                Type realType = ProxyHelper.GetRealType(entityType);
                target.EntityType = entityType;
                target.RealType = realType;
                target.IdMember = GetPrimitiveMember(entityType, source.IdMemberName, nameToMemberDict);
                target.VersionMember = GetPrimitiveMember(entityType, source.VersionMemberName, nameToMemberDict);
                target.CreatedByMember = GetPrimitiveMember(entityType, source.CreatedByMemberName, nameToMemberDict);
                target.CreatedOnMember = GetPrimitiveMember(entityType, source.CreatedOnMemberName, nameToMemberDict);
                target.UpdatedByMember = GetPrimitiveMember(entityType, source.UpdatedByMemberName, nameToMemberDict);
                target.UpdatedOnMember = GetPrimitiveMember(entityType, source.UpdatedOnMemberName, nameToMemberDict);
                target.AlternateIdMembers = GetPrimitiveMembers(entityType, source.AlternateIdMemberNames, nameToMemberDict);
                target.PrimitiveMembers = GetPrimitiveMembers(entityType, source.PrimitiveMemberNames, nameToMemberDict);
                target.RelationMembers = GetRelationMembers(entityType, source.RelationMemberNames, nameToMemberDict);
			    target.AlternateIdMemberIndicesInPrimitives = source.AlternateIdMemberIndicesInPrimitives;
			    target.TypesRelatingToThis = source.TypesRelatingToThis;
			    Type[] typesToCascadeDelete = source.TypesToCascadeDelete;
			    for (int a = 0, size = typesToCascadeDelete.Length; a < size; a++)
			    {
				    target.CascadeDeleteTypes.Add(typesToCascadeDelete[a]);
			    }

                String[] mergeRelevantNames = source.MergeRelevantNames;
                if (mergeRelevantNames != null)
                {
                    for (int a = mergeRelevantNames.Length; a-- > 0; )
                    {
                        Member resolvedMember = nameToMemberDict.Get(mergeRelevantNames[a]);
                        target.SetMergeRelevant(resolvedMember, true);
                    }
                }
                SetMergeRelevant(target, target.CreatedByMember, false);
                SetMergeRelevant(target, target.CreatedOnMember, false);
                SetMergeRelevant(target, target.UpdatedByMember, false);
                SetMergeRelevant(target, target.UpdatedOnMember, false);
                SetMergeRelevant(target, target.IdMember, false);
                SetMergeRelevant(target, target.VersionMember, false);
                target.Initialize(CacheModification, EntityFactory);
			    return target;
		    }
		    throw new Exception("Source of type " + sourceType.Name + " not supported");
	    }

        protected void SetMergeRelevant(EntityMetaData metaData, Member member, bool value)
        {
            if (member != null)
            {
                metaData.SetMergeRelevant(member, value);
            }
        }

        protected PrimitiveMember GetPrimitiveMember(Type entityType, String memberName, IMap<String, Member> nameToMemberDict)
	    {
            if (memberName == null)
            {
                return null;
            }
            PrimitiveMember member = (PrimitiveMember)nameToMemberDict.Get(memberName);
            if (member != null)
            {
                return member;
            }
            member = MemberTypeProvider.GetPrimitiveMember(entityType, memberName);
            if (member == null)
            {
                throw new Exception("No member with name '" + memberName + "' found on entity type '" + entityType.FullName + "'");
            }
            nameToMemberDict.Put(memberName, member);
            return member;
	    }

	    protected RelationMember GetRelationMember(Type entityType, String memberName, IMap<String, Member> nameToMemberDict)
	    {
		    RelationMember member = (RelationMember) nameToMemberDict.Get(memberName);
		    if (member != null)
		    {
			    return member;
		    }
		    member = MemberTypeProvider.GetRelationMember(entityType, memberName);
		    if (member == null)
		    {
			    throw new Exception("No member with name '" + memberName + "' found on entity type '" + entityType.FullName + "'");
		    }
		    nameToMemberDict.Put(memberName, member);
		    return member;
	    }

        protected PrimitiveMember[] GetPrimitiveMembers(Type entityType, String[] memberNames, IMap<String, Member> nameToMemberDict)
	    {
		    if (memberNames == null)
		    {
			    return EntityMetaData.EmptyPrimitiveMembers;
		    }
            PrimitiveMember[] members = new PrimitiveMember[memberNames.Length];
		    for (int a = memberNames.Length; a-- > 0;)
		    {
                members[a] = GetPrimitiveMember(entityType, memberNames[a], nameToMemberDict);
		    }
		    return members;
	    }

        protected RelationMember[] GetRelationMembers(Type entityType, String[] memberNames, IMap<String, Member> nameToMemberDict)
	    {
		    if (memberNames == null)
		    {
			    return EntityMetaData.EmptyRelationMembers;
		    }
            RelationMember[] members = new RelationMember[memberNames.Length];
		    for (int a = memberNames.Length; a-- > 0;)
		    {
			    members[a] = GetRelationMember(entityType, memberNames[a], nameToMemberDict);
		    }
		    return members;
	    }

        protected String GetNameOfMember(Member member)
	    {
		    if (member == null)
		    {
			    return null;
		    }
		    return member.Name;
	    }

        protected String[] GetNamesOfMembers(Member[] members)
	    {
		    if (members == null)
		    {
			    return null;
		    }
		    String[] names = new String[members.Length];
		    for (int a = members.Length; a-- > 0;)
		    {
			    names[a] = GetNameOfMember(members[a]);
		    }
		    return names;
	    }
    }
}