using De.Osthus.Ambeth.Annotation;
using De.Osthus.Ambeth.Bytecode;
using De.Osthus.Ambeth.Collections;
using De.Osthus.Ambeth.CompositeId;
using De.Osthus.Ambeth.Config;
using De.Osthus.Ambeth.Ioc.Annotation;
using De.Osthus.Ambeth.Log;
using De.Osthus.Ambeth.Typeinfo;
using De.Osthus.Ambeth.Util;
using System;
using System.Collections.Generic;
using System.Reflection;
using System.Text;

namespace De.Osthus.Ambeth.Metadata
{
    public class MemberTypeProvider : IMemberTypeProvider, IIntermediateMemberTypeProvider
    {
        public class TypeAndStringWeakMap<T> : Tuple2KeyHashMap<Type, String, WeakReference>
        {
            protected override void Transfer(Tuple2KeyEntry<Type, String, WeakReference>[] newTable)
            {
                int newCapacityMinus1 = newTable.Length - 1;
                Tuple2KeyEntry<Type, String, WeakReference>[] table = this.table;

                for (int a = table.Length; a-- > 0; )
                {
                    Tuple2KeyEntry<Type, String, WeakReference> entry = table[a], next;
                    while (entry != null)
                    {
                        next = entry.GetNextEntry();

                        // only handle this entry if it has still a valid value
                        if (entry.GetValue().Target != null)
                        {
                            int i = entry.GetHash() & newCapacityMinus1;
                            entry.SetNextEntry(newTable[i]);
                            newTable[i] = entry;
                        }
                        entry = next;
                    }
                }
            }
        }

        public static IPropertyInfo[] BuildPropertyPath(Type entityType, String memberName, IPropertyInfoProvider propertyInfoProvider)
        {
            String[] memberPath = EmbeddedMember.Split(memberName);
            Type currType = entityType;
            IPropertyInfo[] propertyPath = new IPropertyInfo[memberPath.Length];
            for (int a = 0, size = propertyPath.Length; a < size; a++)
            {
                IPropertyInfo property = propertyInfoProvider.GetProperty(currType, memberPath[a]);
                if (property == null)
                {
                    FieldInfo[] fields = ReflectUtil.GetDeclaredFieldInHierarchy(currType, memberPath[a]);
                    if (fields.Length == 0)
                    {
                        throw new Exception("Path illegal: " + memberName);
                    }
                    property = new FieldPropertyInfo(currType, memberPath[a], fields[a]);
                }
                propertyPath[a] = property;
                currType = property.PropertyType;
            }
            return propertyPath;
        }

        protected static readonly Object[] EMPTY_OBJECTS = new Object[0];

        [LogInstance]
        public ILogger Log { private get; set; }

        protected readonly TypeAndStringWeakMap<PrimitiveMember> typeToPrimitiveMemberMap = new TypeAndStringWeakMap<PrimitiveMember>();

        protected readonly TypeAndStringWeakMap<Member> typeToMemberMap = new TypeAndStringWeakMap<Member>();

        protected readonly TypeAndStringWeakMap<RelationMember> typeToRelationMemberMap = new TypeAndStringWeakMap<RelationMember>();

        protected readonly Object writeLock = new Object();

        [Autowired]
        public IBytecodeEnhancer BytecodeEnhancer { protected get; set; }

        [Autowired]
        public ICompositeIdFactory CompositeIdFactory { protected get; set; }

        [Autowired]
        public IProperties Properties { protected get; set; }

        [Autowired]
        public IPropertyInfoProvider PropertyInfoProvider { protected get; set; }

        public RelationMember GetRelationMember(Type type, String propertyName)
        {
            return GetMemberIntern(type, propertyName, typeToRelationMemberMap, typeof(RelationMember));
        }

        public PrimitiveMember GetPrimitiveMember(Type type, String propertyName)
        {
            return GetMemberIntern(type, propertyName, typeToPrimitiveMemberMap, typeof(PrimitiveMember));
        }

        public Member GetMember(Type type, String propertyName)
        {
            return GetMemberIntern(type, propertyName, typeToMemberMap, typeof(Member));
        }

        protected T GetMemberIntern<T>(Type type, String propertyName, TypeAndStringWeakMap<T> map, Type baseType) where T : Member
        {
            WeakReference accessorR = map.Get(type, propertyName);
            Member member = accessorR != null ? (Member)accessorR.Target : null;
            if (member != null)
            {
                return (T)member;
            }
            member = (T)GetMemberIntern(type, propertyName, baseType);
            if (member is RelationMember)
            {
                CascadeLoadMode? cascadeLoadMode = null;
                Cascade cascadeAnnotation = (Cascade) member.GetAnnotation(typeof(Cascade));
                if (cascadeAnnotation != null)
                {
                    cascadeLoadMode = cascadeAnnotation.Load;
                }
                if (cascadeLoadMode == null || CascadeLoadMode.DEFAULT.Equals(cascadeLoadMode))
                {
                    cascadeLoadMode = (CascadeLoadMode) Enum.Parse(typeof(CascadeLoadMode), Properties.GetString(
                            ((RelationMember)member).IsToMany ? ServiceConfigurationConstants.ToManyDefaultCascadeLoadMode
                                    : ServiceConfigurationConstants.ToOneDefaultCascadeLoadMode, CascadeLoadMode.DEFAULT.ToString()), false);
                }
                if (cascadeLoadMode == null || CascadeLoadMode.DEFAULT.Equals(cascadeLoadMode))
                {
                    cascadeLoadMode = CascadeLoadMode.LAZY;
                }
                ((IRelationMemberWrite)member).SetCascadeLoadMode(cascadeLoadMode.Value);
            }
            Object writeLock = this.writeLock;
            lock (writeLock)
            {
                // concurrent thread might have been faster
                accessorR = map.Get(type, propertyName);
                Object existingMember = accessorR != null ? (T)accessorR.Target : null;
                if (existingMember != null)
                {
                    return (T)existingMember;
                }
                map.Put(type, propertyName, new WeakReference(member));
                return (T)member;
            }
        }

        protected Member GetMemberIntern(Type type, String propertyName, Type baseType)
        {
            if (propertyName.Contains("&"))
            {
                String[] compositePropertyNames = propertyName.Split('&');
                PrimitiveMember[] members = new PrimitiveMember[compositePropertyNames.Length];
                for (int a = compositePropertyNames.Length; a-- > 0; )
                {
                    members[a] = (PrimitiveMember)GetMemberIntern(type, compositePropertyNames[a], baseType);
                }
                return CompositeIdFactory.CreateCompositeIdMember(type, members);
            }
            Type enhancedType = GetMemberTypeIntern(type, propertyName, baseType);
            if (enhancedType == baseType)
            {
                throw new Exception("Must never happen. No enhancement for " + baseType + " has been done");
            }
            ConstructorInfo constructor = enhancedType.GetConstructor(Type.EmptyTypes);
            return (Member)constructor.Invoke(EMPTY_OBJECTS);
        }

        protected Type GetMemberTypeIntern(Type targetType, String propertyName, Type baseType)
        {
            String memberTypeName = targetType.Name + "$" + baseType.Name + "$" + propertyName.Replace('.', '$');
            //if (memberTypeName.StartsWith("java."))
            //{
            //    memberTypeName = "ambeth." + memberTypeName;
            //}
            if (baseType == typeof(RelationMember))
            {
                return BytecodeEnhancer.GetEnhancedType(baseType, new RelationMemberEnhancementHint(targetType, propertyName));
            }
            return BytecodeEnhancer.GetEnhancedType(baseType, new MemberEnhancementHint(targetType, propertyName));
        }

	    public IntermediatePrimitiveMember GetIntermediatePrimitiveMember(Type entityType, String propertyName)
	    {
		    String[] memberNamePath = EmbeddedMember.Split(propertyName);
		    Type currDeclaringType = entityType;
		    Member[] members = new Member[memberNamePath.Length];
		    for (int a = 0, size = memberNamePath.Length; a < size; a++)
		    {
			    IPropertyInfo property = PropertyInfoProvider.GetProperty(currDeclaringType, memberNamePath[a]);
			    if (property == null)
			    {
				    return null;
			    }
			    members[a] = new IntermediatePrimitiveMember(currDeclaringType, entityType, property.PropertyType, property.ElementType,
					    property.Name, property.GetAnnotations());
			    currDeclaringType = property.PropertyType;
		    }
		    if (members.Length > 1)
		    {
			    Member[] memberPath = new Member[members.Length - 1];
			    Array.Copy(members, 0, memberPath, 0, memberPath.Length);
                PrimitiveMember lastMember = (PrimitiveMember)members[memberPath.Length];
			    return new IntermediateEmbeddedPrimitiveMember(entityType, lastMember.RealType, lastMember.ElementType, propertyName, memberPath,
					    lastMember);
		    }
		    return (IntermediatePrimitiveMember) members[0];
	    }

	    public IntermediateRelationMember GetIntermediateRelationMember(Type entityType, String propertyName)
	    {
		    String[] memberNamePath = EmbeddedMember.Split(propertyName);
            Type currDeclaringType = entityType;
		    Member[] members = new Member[memberNamePath.Length];
		    for (int a = 0, size = memberNamePath.Length; a < size; a++)
		    {
			    IPropertyInfo property = PropertyInfoProvider.GetProperty(currDeclaringType, memberNamePath[a]);
			    if (property == null)
			    {
				    return null;
			    }
			    members[a] = new IntermediateRelationMember(currDeclaringType, entityType, property.PropertyType, property.ElementType,
					    property.Name, property.GetAnnotations());
			    currDeclaringType = property.PropertyType;
		    }
		    if (members.Length > 1)
		    {
			    Member[] memberPath = new Member[members.Length - 1];
                Array.Copy(members, 0, memberPath, 0, memberPath.Length);
			    Member lastMember = members[memberPath.Length];
			    return new IntermediateEmbeddedRelationMember(entityType, lastMember.RealType, lastMember.ElementType, propertyName, memberPath,
					    lastMember);
		    }
		    return (IntermediateRelationMember) members[0];
	    }
    }
}