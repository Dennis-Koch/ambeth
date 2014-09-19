using De.Osthus.Ambeth.Annotation;
using De.Osthus.Ambeth.Bytecode;
using De.Osthus.Ambeth.Collections;
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
    public class MemberTypeProvider : IMemberTypeProvider
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
            String[] memberPath = memberName.Split('.');
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

        [LogInstance]
        public ILogger Log { private get; set; }

        protected readonly TypeAndStringWeakMap<PrimitiveMember> typeToPrimitiveMemberMap = new TypeAndStringWeakMap<PrimitiveMember>();

        protected readonly TypeAndStringWeakMap<Member> typeToMemberMap = new TypeAndStringWeakMap<Member>();

        protected readonly TypeAndStringWeakMap<RelationMember> typeToRelationMemberMap = new TypeAndStringWeakMap<RelationMember>();

        protected readonly Object writeLock = new Object();

        [Autowired]
        public IBytecodeEnhancer BytecodeEnhancer { protected get; set; }

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
            T member = accessorR != null ? (T)accessorR.Target : null;
            if (member != null)
            {
                return member;
            }
            Object writeLock = this.writeLock;
            lock (writeLock)
            {
                // concurrent thread might have been faster
                accessorR = map.Get(type, propertyName);
                member = accessorR != null ? (T)accessorR.Target : null;
                if (member != null)
                {
                    return member;
                }
                member = (T)GetMemberIntern(type, propertyName, baseType);
                if (member is RelationMember)
                {
                    CascadeLoadMode? cascadeLoadMode = null;
                    CascadeAttribute cascadeAnnotation = member.GetAnnotation<CascadeAttribute>();
                    if (cascadeAnnotation != null)
                    {
                        cascadeLoadMode = cascadeAnnotation.Load;
                    }
                    if (cascadeLoadMode == null || CascadeLoadMode.DEFAULT.Equals(cascadeLoadMode))
                    {
                        cascadeLoadMode = (CascadeLoadMode) Enum.Parse(typeof(CascadeLoadMode), Properties.GetString(
                                ((RelationMember)(dynamic)member).IsToMany ? ServiceConfigurationConstants.ToManyDefaultCascadeLoadMode
                                        : ServiceConfigurationConstants.ToOneDefaultCascadeLoadMode, CascadeLoadMode.DEFAULT.ToString()));
                    }
                    if (cascadeLoadMode == null || CascadeLoadMode.DEFAULT.Equals(cascadeLoadMode))
                    {
                        cascadeLoadMode = CascadeLoadMode.LAZY;
                    }
                    ((IRelationMemberWrite)member).SetCascadeLoadMode(cascadeLoadMode.Value);
                }
                map.Put(type, propertyName, new WeakReference(member));
                return member;
            }
        }

        protected Member GetMemberIntern(Type type, String propertyName, Type baseType)
        {
            String[] memberNameSplit = propertyName.Split('.');
            if (memberNameSplit.Length > 1)
            {
                Member[] memberPath = new Member[memberNameSplit.Length - 1];
                StringBuilder memberPathString = new StringBuilder();
                Type currType = type;
                for (int a = 0, size = memberPath.Length; a < size; a++)
                {
                    memberPath[a] = GetMember(currType, memberNameSplit[a]);
                    Type parentObjectType = currType;
                    currType = memberPath[a].RealType;
                    if (a > 0)
                    {
                        memberPathString.Append('.');
                    }
                    memberPathString.Append(memberPath[a].Name);
                    if (BytecodeEnhancer.IsEnhancedType(type))
                    {
                        currType = BytecodeEnhancer.GetEnhancedType(currType, new EmbeddedEnhancementHint(type, parentObjectType, memberPathString.ToString()));
                    }
                }
                if (baseType == typeof(RelationMember))
                {
                    RelationMember lastRelationMember = GetRelationMember(currType, memberNameSplit[memberNameSplit.Length - 1]);
                    return new EmbeddedRelationMember(propertyName, lastRelationMember, memberPath);
                }
                else if (baseType == typeof(PrimitiveMember))
                {
                    PrimitiveMember lastMember = GetPrimitiveMember(currType, memberNameSplit[memberNameSplit.Length - 1]);
                    return new EmbeddedPrimitiveMember(propertyName, lastMember, memberPath);
                }
                else if (baseType == typeof(Member))
                {
                    Member lastMember = GetMember(currType, memberNameSplit[memberNameSplit.Length - 1]);
                    return new EmbeddedMember(propertyName, lastMember, memberPath);
                }
                throw new Exception("Must never happen");
            }
            Type enhancedType = GetMemberTypeIntern(type, propertyName, baseType);
            if (enhancedType == baseType)
            {
                throw new Exception("Must never happen. No enhancement for " + baseType + " has been done");
            }
            ConstructorInfo constructor = enhancedType.GetConstructor(new Type[] { typeof(Type), typeof(IPropertyInfo) });
            return (Member)constructor.Invoke(new Object[] { type, null });
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
    }
}