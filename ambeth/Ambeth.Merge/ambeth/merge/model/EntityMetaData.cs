using De.Osthus.Ambeth.Annotation;
using De.Osthus.Ambeth.Collections;
using De.Osthus.Ambeth.CompositeId;
using De.Osthus.Ambeth.Merge.Transfer;
using De.Osthus.Ambeth.Typeinfo;
using De.Osthus.Ambeth.Util;
using System;
using System.Collections.Generic;
using System.Reflection;

namespace De.Osthus.Ambeth.Merge.Model
{
    public class EntityMetaData : IEntityMetaData
    {
        public const String DEFAULT_NAME_ID = "Id";

        public const String DEFAULT_NAME_VERSION = "Version";

        public const String DEFAULT_NAME_CREATED_BY = "CreatedBy";

        public const String DEFAULT_NAME_CREATED_ON = "CreatedOn";

        public const String DEFAULT_NAME_UPDATED_BY = "UpdatedBy";

        public const String DEFAULT_NAME_UPDATED_ON = "UpdatedOn";

        private static readonly int[][] EmptyShortArray = new int[0][];

        public static readonly ITypeInfoItem[] EmptyTypeInfoItems = new ITypeInfoItem[0];

        public static readonly IRelationInfoItem[] EmptyRelationInfoItems = new IRelationInfoItem[0];

        public static readonly IEntityLifecycleExtension[] EmptyEntityLifecycleExtensions = new IEntityLifecycleExtension[0];

        public Type EntityType { get; set; }

        public Type RealType { get; set; }

        public bool LocalEntity { get; set; }

        protected readonly ISet<Type> cascadeDeleteTypes = new HashSet<Type>();

        public ISet<Type> CascadeDeleteTypes { get { return cascadeDeleteTypes; } }

        public int[][] AlternateIdMemberIndicesInPrimitives { get; set; }

        public ITypeInfoItem IdMember { get; set; }

        public ITypeInfoItem VersionMember { get; set; }

        public ITypeInfoItem CreatedByMember { get; set; }

        public ITypeInfoItem CreatedOnMember { get; set; }

        public ITypeInfoItem UpdatedByMember { get; set; }

        public ITypeInfoItem UpdatedOnMember { get; set; }

        public ITypeInfoItem[] PrimitiveMembers { get; set; }

        public IRelationInfoItem[] RelationMembers { get; set; }

        public ITypeInfoItem[] AlternateIdMembers { get; set; }

        public Type[] TypesRelatingToThis { get; set; }

        protected IEntityLifecycleExtension[] entityLifecycleExtensions = EmptyEntityLifecycleExtensions;

        public IEntityLifecycleExtension[] EntityLifecycleExtensions
        {
            get
            {
                return entityLifecycleExtensions;
            }
            set
            {
                if (value == null || value.Length == 0)
                {
                    value = EmptyEntityLifecycleExtensions;
                }
                entityLifecycleExtensions = value;
            }
        }

        protected readonly ISet<Type> typesRelatingToThisSet = new HashSet<Type>();

        protected readonly Dictionary<String, ITypeInfoItem> nameToMemberDict = new Dictionary<String, ITypeInfoItem>();

        protected readonly Dictionary<String, int?> relMemberNameToIndexDict = new Dictionary<String, int?>();

        protected readonly Dictionary<String, int?> primMemberNameToIndexDict = new Dictionary<String, int?>();

        protected readonly Dictionary<IRelationInfoItem, int?> relMemberToIndexDict = new Dictionary<IRelationInfoItem, int?>();

        protected readonly Dictionary<ITypeInfoItem, int?> primMemberToIndexDict = new Dictionary<ITypeInfoItem, int?>();

        protected readonly Dictionary<String, sbyte?> memberNameToIdIndexDict = new Dictionary<String, sbyte?>();

        protected readonly HashMap<ITypeInfoItem, bool?> memberToMergeRelevanceDict = new HashMap<ITypeInfoItem, bool?>(0.5f);

        protected IEntityFactory entityFactory;

        public EntityMetaData()
        {
            this.AlternateIdMemberIndicesInPrimitives = EmptyShortArray;
        }

        public ITypeInfoItem GetIdMemberByIdIndex(sbyte idIndex)
        {
            if (idIndex == ObjRef.PRIMARY_KEY_INDEX)
            {
                return IdMember;
            }
            return AlternateIdMembers[idIndex];
        }

        public sbyte GetIdIndexByMemberName(String memberName)
        {
            sbyte? index = DictionaryExtension.ValueOrDefault(memberNameToIdIndexDict, memberName);
            if (!index.HasValue)
            {
                throw new ArgumentException("No alternate id index found for member name '" + memberName + "'");
            }
            return index.Value;
        }

        public int GetAlternateIdCount()
        {
            return AlternateIdMembers.Length;
        }

        public ITypeInfoItem GetMemberByName(String memberName)
        {
            return DictionaryExtension.ValueOrDefault(nameToMemberDict, memberName);
        }

        public int GetIndexByRelationName(String relationMemberName)
        {
            int? index = DictionaryExtension.ValueOrDefault(relMemberNameToIndexDict, relationMemberName);
            if (!index.HasValue)
            {
                throw new ArgumentException("No index found for relation member: " + relationMemberName);
            }
            return index.Value;
        }

        public int GetIndexByRelation(IRelationInfoItem relationMember)
        {
            int? index = DictionaryExtension.ValueOrDefault(relMemberToIndexDict, relationMember);
            if (!index.HasValue)
            {
                throw new ArgumentException("No index found for relation member: " + relationMember);
            }
            return index.Value;
        }

        public int GetIndexByPrimitiveName(String primitiveMemberName)
        {
            int? index = DictionaryExtension.ValueOrDefault(primMemberNameToIndexDict, primitiveMemberName);
            if (!index.HasValue)
            {
                throw new ArgumentException("No index found for primitive member: " + primitiveMemberName);
            }
            return index.Value;
        }

        public int GetIndexByPrimitive(ITypeInfoItem primitiveMember)
        {
            int? index = DictionaryExtension.ValueOrDefault(primMemberToIndexDict, primitiveMember);
            if (!index.HasValue)
            {
                throw new ArgumentException("No index found for primitive member: " + primitiveMember);
            }
            return index.Value;
        }

        public bool IsRelatingToThis(Type childType)
        {
            return typesRelatingToThisSet.Contains(childType);
        }

        public bool IsCascadeDelete(Type type)
        {
            return cascadeDeleteTypes.Contains(type);
        }

        public bool IsMergeRelevant(ITypeInfoItem member)
        {
            bool? relevance = memberToMergeRelevanceDict.Get(member);
            return !relevance.HasValue || relevance.Value;
        }

        public void SetMergeRelevant(ITypeInfoItem member, bool relevant)
        {
            memberToMergeRelevanceDict.Put(member, relevant);
        }

        public void PostLoad(Object entity)
	    {
		    foreach (IEntityLifecycleExtension entityLifecycleExtension in entityLifecycleExtensions)
		    {
			    entityLifecycleExtension.PostLoad(entity);
		    }
	    }

        public void PrePersist(Object entity)
        {
            foreach (IEntityLifecycleExtension entityLifecycleExtension in entityLifecycleExtensions)
            {
                entityLifecycleExtension.PrePersist(entity);
            }
        }

        public void Initialize(IEntityFactory entityFactory)
        {
            this.entityFactory = entityFactory;
            if (AlternateIdMemberIndicesInPrimitives == null)
            {
                AlternateIdMemberIndicesInPrimitives = EmptyShortArray;
            }

            if (PrimitiveMembers == null)
            {
                PrimitiveMembers = EmptyTypeInfoItems;
            }
            else
            {
                // Array.Sort<INamed>(PrimitiveMembers, namedItemComparer);
            }

            if (RelationMembers == null)
            {
                RelationMembers = EmptyRelationInfoItems;
            }
            else
            {
                // Array.Sort<INamed>(RelationMembers, namedItemComparer);
            }

            if (AlternateIdMembers == null)
            {
                AlternateIdMembers = EmptyTypeInfoItems;
            }
            else
            {
                // Array.Sort<INamed>(AlternateIdMembers, namedItemComparer);
            }

            nameToMemberDict.Clear();
            relMemberToIndexDict.Clear();
            relMemberNameToIndexDict.Clear();
            primMemberToIndexDict.Clear();
            primMemberNameToIndexDict.Clear();
            if (IdMember != null)
            {
                nameToMemberDict.Add(IdMember.Name, IdMember);
                IdMember.TechnicalMember = true;
            }
            if (VersionMember != null)
            {
                nameToMemberDict.Add(VersionMember.Name, VersionMember);
                VersionMember.TechnicalMember = true;
            }
            if (CreatedByMember != null)
            {
                CreatedByMember.TechnicalMember = true;
            }
            if (CreatedOnMember != null)
            {
                CreatedOnMember.TechnicalMember = true;
            }
            if (UpdatedByMember != null)
            {
                UpdatedByMember.TechnicalMember = true;
            }
            if (UpdatedOnMember != null)
            {
                UpdatedOnMember.TechnicalMember = true;
            }
            for (int a = PrimitiveMembers.Length; a-- > 0; )
            {
                ITypeInfoItem member = PrimitiveMembers[a];
                nameToMemberDict.Add(member.Name, member);
                primMemberNameToIndexDict.Add(member.Name, a);
                primMemberToIndexDict.Add(member, a);
                if (Object.ReferenceEquals(member, IdMember) || Object.ReferenceEquals(member, VersionMember) || Object.ReferenceEquals(member, UpdatedByMember)
                    || Object.ReferenceEquals(member, UpdatedOnMember) || Object.ReferenceEquals(member, CreatedByMember) || Object.ReferenceEquals(member, CreatedOnMember))
                {
                    // technical members must never be merge relevant
                    SetMergeRelevant(member, false);
                }
            }
            for (int a = RelationMembers.Length; a-- > 0; )
            {
                IRelationInfoItem member = RelationMembers[a];
                nameToMemberDict.Add(member.Name, member);
                relMemberNameToIndexDict.Add(member.Name, a);
                relMemberToIndexDict.Add(member, a);
            }
            memberNameToIdIndexDict.Clear();
            if (IdMember != null)
            {
                memberNameToIdIndexDict.Add(IdMember.Name, ObjRef.PRIMARY_KEY_INDEX);
            }
            AlternateIdMemberIndicesInPrimitives = new int[AlternateIdMembers.Length][];
            for (int idIndex = AlternateIdMembers.Length; idIndex-- > 0; )
            {
                int[] compositeIndex = null;
                ITypeInfoItem alternateIdMember = AlternateIdMembers[idIndex];
                ITypeInfoItem[] memberItems;
                if (alternateIdMember is CompositeIdTypeInfoItem)
                {
                    memberItems = ((CompositeIdTypeInfoItem)alternateIdMember).Members;
                }
                else
                {
                    memberItems = new ITypeInfoItem[] { alternateIdMember };
                }
                compositeIndex = new int[memberItems.Length];

                for (int compositePosition = compositeIndex.Length; compositePosition-- > 0; )
                {
                    ITypeInfoItem memberItem = memberItems[compositePosition];
                    for (int primitiveIndex = PrimitiveMembers.Length; primitiveIndex-- > 0; )
                    {
                        if (Object.ReferenceEquals(memberItem, PrimitiveMembers[primitiveIndex]))
                        {
                            compositeIndex[compositePosition] = primitiveIndex;
                            break;
                        }
                    }
                    if (compositeIndex[compositePosition] == -1)
                    {
                        throw new Exception("AlternateId is not a primitive: " + memberItem);
                    }
                }
                AlternateIdMemberIndicesInPrimitives[idIndex] = compositeIndex;
                memberNameToIdIndexDict.Add(alternateIdMember.Name, (sbyte)idIndex);
            }

            if (TypesRelatingToThis != null && TypesRelatingToThis.Length > 0)
            {
                for (int i = TypesRelatingToThis.Length; i-- > 0; )
                {
                    typesRelatingToThisSet.Add(TypesRelatingToThis[i]);
                }
            }
        }
                
        public override String ToString()
        {
            return GetType().Name + ": " + EntityType.FullName;
        }

        public Object NewInstance()
        {
            return entityFactory.CreateEntity(this);
        }
    }
}