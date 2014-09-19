using De.Osthus.Ambeth.Annotation;
using De.Osthus.Ambeth.Collections;
using De.Osthus.Ambeth.CompositeId;
using De.Osthus.Ambeth.Merge.Transfer;
using De.Osthus.Ambeth.Metadata;
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

        public static readonly PrimitiveMember[] EmptyPrimitiveMembers = new PrimitiveMember[0];

        public static readonly RelationMember[] EmptyRelationMembers = new RelationMember[0];

        public static readonly IEntityLifecycleExtension[] EmptyEntityLifecycleExtensions = new IEntityLifecycleExtension[0];

        public Type EntityType { get; set; }

        public Type RealType { get; set; }

        public Type EnhancedType { get; set; }

        public bool LocalEntity { get; set; }

        protected readonly ISet<Type> cascadeDeleteTypes = new HashSet<Type>();

        public ISet<Type> CascadeDeleteTypes { get { return cascadeDeleteTypes; } }

        public int[][] AlternateIdMemberIndicesInPrimitives { get; set; }

        public PrimitiveMember IdMember { get; set; }

        public PrimitiveMember VersionMember { get; set; }

        public PrimitiveMember CreatedByMember { get; set; }

        public PrimitiveMember CreatedOnMember { get; set; }

        public PrimitiveMember UpdatedByMember { get; set; }

        public PrimitiveMember UpdatedOnMember { get; set; }

        public PrimitiveMember[] PrimitiveMembers { get; set; }

        public RelationMember[] RelationMembers { get; set; }

        public PrimitiveMember[] AlternateIdMembers { get; set; }

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

        protected readonly SmartCopySet<Member> interningMemberSet = new SmartCopySet<Member>(0.5f);

        protected readonly Dictionary<String, Member> nameToMemberDict = new Dictionary<String, Member>();

        protected readonly Dictionary<String, int?> relMemberNameToIndexDict = new Dictionary<String, int?>();

        protected readonly Dictionary<String, int?> primMemberNameToIndexDict = new Dictionary<String, int?>();

        protected readonly Dictionary<Member, int?> relMemberToIndexDict = new Dictionary<Member, int?>();

        protected readonly Dictionary<Member, int?> primMemberToIndexDict = new Dictionary<Member, int?>();

        protected readonly Dictionary<String, sbyte?> memberNameToIdIndexDict = new Dictionary<String, sbyte?>();

        protected readonly HashMap<Member, bool?> memberToMergeRelevanceDict = new HashMap<Member, bool?>(0.5f);

        protected IEntityFactory entityFactory;

        public EntityMetaData()
        {
            this.AlternateIdMemberIndicesInPrimitives = EmptyShortArray;
        }

        public PrimitiveMember GetIdMemberByIdIndex(sbyte idIndex)
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

        public bool HasInterningBehavior(PrimitiveMember primitiveMember)
	    {
		    return interningMemberSet.Contains(primitiveMember);
	    }

        public void ChangeInterningBehavior(PrimitiveMember primitiveMember, bool state)
	    {
		    if (state)
		    {
			    interningMemberSet.Add(primitiveMember);
		    }
		    else
		    {
			    interningMemberSet.Remove(primitiveMember);
		    }
	    }

        public int GetAlternateIdCount()
        {
            return AlternateIdMembers.Length;
        }

        public bool IsMergeRelevant(Member member)
        {
            bool? relevance = memberToMergeRelevanceDict.Get(member);
            return !relevance.HasValue || relevance.Value;
        }

        public void SetMergeRelevant(Member member, bool relevant)
        {
            memberToMergeRelevanceDict.Put(member, relevant);
        }

        public Member GetMemberByName(String memberName)
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

	    public bool IsPrimitiveMember(String primitiveMemberName)
	    {
		    return primMemberNameToIndexDict.ContainsKey(primitiveMemberName);
	    }

	    public bool IsRelationMember(String relationMemberName)
	    {
		    return relMemberNameToIndexDict.ContainsKey(relationMemberName);
	    }

        public int GetIndexByRelation(Member relationMember)
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

        public int GetIndexByPrimitive(Member primitiveMember)
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

        public void addCascadeDeleteType(Type type)
	    {
		    cascadeDeleteTypes.Add(type);
	    }

	    public void PostProcessNewEntity(Object newEntity)
	    {
		    foreach (IEntityLifecycleExtension entityLifecycleExtension in entityLifecycleExtensions)
		    {
			    entityLifecycleExtension.PostCreate(this, newEntity);
		    }
	    }

        public void PostLoad(Object entity)
	    {
		    foreach (IEntityLifecycleExtension entityLifecycleExtension in entityLifecycleExtensions)
		    {
			    entityLifecycleExtension.PostLoad(this, entity);
		    }
	    }

        public void PrePersist(Object entity)
        {
            foreach (IEntityLifecycleExtension entityLifecycleExtension in entityLifecycleExtensions)
            {
                entityLifecycleExtension.PrePersist(this, entity);
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
                PrimitiveMembers = EmptyPrimitiveMembers;
            }
            else
            {
                // Array.Sort<INamed>(PrimitiveMembers, namedItemComparer);
            }

            if (RelationMembers == null)
            {
                RelationMembers = EmptyRelationMembers;
            }
            else
            {
                // Array.Sort<INamed>(RelationMembers, namedItemComparer);
            }

            if (AlternateIdMembers == null)
            {
                AlternateIdMembers = EmptyPrimitiveMembers;
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
            }
            if (VersionMember != null)
            {
                nameToMemberDict.Add(VersionMember.Name, VersionMember);
            }
            for (int a = PrimitiveMembers.Length; a-- > 0; )
            {
                Member member = PrimitiveMembers[a];
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
                RelationMember member = RelationMembers[a];
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
                Member alternateIdMember = AlternateIdMembers[idIndex];
                Member[] memberItems;
                if (alternateIdMember is CompositeIdMember)
                {
                    memberItems = ((CompositeIdMember)alternateIdMember).Members;
                }
                else
                {
                    memberItems = new Member[] { alternateIdMember };
                }
                compositeIndex = new int[memberItems.Length];

                for (int compositePosition = compositeIndex.Length; compositePosition-- > 0; )
                {
                    compositeIndex[compositePosition] = -1;
                    Member memberItem = memberItems[compositePosition];
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
            if (CreatedByMember != null)
            {
                ChangeInterningBehavior(CreatedByMember, true);
            }
            if (UpdatedByMember != null)
            {
                ChangeInterningBehavior(UpdatedByMember, true);
            }
            SetTechnicalMember(IdMember);
            SetTechnicalMember(VersionMember);
            SetTechnicalMember(CreatedByMember);
            SetTechnicalMember(CreatedOnMember);
            SetTechnicalMember(UpdatedByMember);
            SetTechnicalMember(UpdatedOnMember);
        }

        protected void SetTechnicalMember(PrimitiveMember member)
        {
            if (member == null)
            {
                return;
            }
            ((IPrimitiveMemberWrite)member).SetTechnicalMember(true);
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