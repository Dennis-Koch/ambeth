using System;
using System.Collections.Generic;
using System.Linq;
using System.Reflection;
using System.Runtime.Serialization;
using System.Text.RegularExpressions;
#if !SILVERLIGHT
using System.Threading;
#endif
using De.Osthus.Ambeth.Collections;
using De.Osthus.Ambeth.Config;
using De.Osthus.Ambeth.Util;

namespace De.Osthus.Ambeth.Typeinfo
{
    public class TypeInfoProvider : SmartCopyMap<Type, TypeInfo>, ITypeInfoProvider
    {
        private static readonly NamedItemComparer namedItemComparer = new NamedItemComparer();

        private static readonly Regex memberPathSplitPattern = new Regex("\\.");

        private static readonly Comparison<ITypeInfoItem> typeInfoItemComparer = delegate(ITypeInfoItem item1, ITypeInfoItem item2)
        {
            return namedItemComparer.Compare(item1, item2);
        };

        public static Type GetElementTypeOfCollection(Type collType)
        {
            while (collType != null)
            {
                Type[] genericArguments = collType.GetGenericArguments();

                if (genericArguments != null && genericArguments.Length > 0)
                {
                    return genericArguments[0];
                }
                collType = collType.BaseType;
            }
            return null;
        }

        protected readonly ThreadLocal<HashMap<Type, TypeInfo>> tempTypeInfoMapTL = new ThreadLocal<HashMap<Type, TypeInfo>>();

        public IProperties Properties { protected get; set; }

        public IPropertyInfoProvider PropertyInfoProvider { protected get; set; }

        public ITypeInfoProvider GetInstance()
	    {
		    return this;
	    }

        public virtual ITypeInfoItem GetHierarchicMember(Type entityType, String hierarchicMemberName)
        {
            String[] memberPath = memberPathSplitPattern.Split(hierarchicMemberName);
            ITypeInfoItem[] members = new ITypeInfoItem[memberPath.Length];
            Type currentType = entityType;
            for (int a = 0, size = memberPath.Length; a < size; a++)
            {
                String memberName = memberPath[a];
                ITypeInfo typeInfo = GetTypeInfo(currentType);
                ITypeInfoItem member = typeInfo.GetMemberByName(memberName);
                if (member == null)
                {
                    String memberNameLower = memberName.ToLower();
                    IList<ITypeInfoItem> allMembers = typeInfo.Members;
                    for (int b = allMembers.Count; b-- > 0; )
                    {
                        if (allMembers[b].Name.ToLower().Equals(memberNameLower))
                        {
                            member = allMembers[b];
                            break;
                        }
                    }
                    if (member == null)
                    {
                        return null;
                    }
                }
                currentType = member.RealType;
                members[a] = member;
            }
            if (members.Length == 1)
            {
                return members[0];
            }
            else if (members.Length > 1)
            {
                ITypeInfoItem[] memberForParentPath = new ITypeInfoItem[members.Length - 1];
                Array.Copy(members, 0, memberForParentPath, 0, memberForParentPath.Length);
                ITypeInfoItem lastMember = members[members.Length - 1];
			    return new EmbeddedTypeInfoItem(hierarchicMemberName, lastMember, memberForParentPath);
            }
            else
            {
                throw new ArgumentException("Must never happen");
            }
        }

        public virtual ITypeInfoItem GetMember(Type entityType, PropertyInfo propertyInfo)
        {
            return GetMember(entityType, PropertyInfoProvider.GetProperty(entityType, propertyInfo.Name));
        }

        public virtual ITypeInfoItem GetMember(Type entityType, IPropertyInfo propertyInfo)
        {
            return new PropertyInfoItem(propertyInfo);
        }

        public virtual ITypeInfoItem GetMember(FieldInfo field)
        {
            return new FieldInfoItem(field);
        }

        public virtual ITypeInfoItem GetMember(String propertyName, FieldInfo field)
        {
            return new FieldInfoItem(field);
        }

        public virtual ITypeInfo GetTypeInfo(Type type)
        {
            TypeInfo typeInfo = Get(type);
            if (typeInfo != null)
            {
                return typeInfo;
            }
            bool tempTypeInfoMapCreated = false;
            try
            {
                Object writeLock = GetWriteLock();
                lock (writeLock)
                {
                    typeInfo = Get(type);
                    if (typeInfo != null)
                    {
                        // Concurrent thread might have been faster
                        return typeInfo;
                    }
                    HashMap<Type, TypeInfo> tempTypeInfoMap = tempTypeInfoMapTL.Value;
                    if (tempTypeInfoMap != null)
                    {
                        typeInfo = tempTypeInfoMap.Get(type);
                        if (typeInfo != null)
                        {
                            // Current thread is currently created cascaded TypeInfo instances
                            return typeInfo;
                        }
                    }
                    else
                    {
                        tempTypeInfoMap = new HashMap<Type, TypeInfo>();
                        tempTypeInfoMapTL.Value = tempTypeInfoMap;
                        tempTypeInfoMapCreated = true;
                    }
                    List<FieldInfo> allFields = new List<FieldInfo>(0);
                    List<IPropertyInfo> allProperties = new List<IPropertyInfo>();

                    FindFields(allFields, type);
                    allProperties.AddRange(PropertyInfoProvider.GetProperties(type));

                    List<ITypeInfoItem> memberList = new List<ITypeInfoItem>(allProperties.Count + allFields.Count);

                    for (int a = allFields.Count; a-- > 0; )
                    {
                        FieldInfo field = allFields[a];

                        String fieldNameLower = field.Name.ToLower();
                        for (int b = allProperties.Count; b-- > 0; )
                        {
                            IPropertyInfo property = allProperties[b];
                            if (property.PropertyType.Equals(field.FieldType) && fieldNameLower.StartsWith(property.Name.ToLower()))
                            {
                                allFields.RemoveAt(a);
                                break;
                            }
                        }
                    }
                    typeInfo = new TypeInfo(type);
                    tempTypeInfoMap.Put(type, typeInfo);

                    foreach (IPropertyInfo property in allProperties)
                    {
						int modifiers = property.Modifiers;
						if (Modifier.IsTransient(modifiers) || property.GetAnnotation<IgnoreDataMemberAttribute>() != null)
						{
							continue; // Can not handle non datamember properties
						}
						if (Modifier.IsFinal(modifiers))
						{
							continue;
						}
						if (!Modifier.IsPublic(modifiers))
						{
							continue;
						}
                        memberList.Add(GetMember(type, property));
                    }
                    foreach (FieldInfo field in allFields)
                    {
                        if (field.GetCustomAttributes(typeof(IgnoreDataMemberAttribute), true).Length > 0)
                        {
                            continue; // Can not handle non datamember fields
                        }
                        memberList.Add(GetMember(field));
                    }

                    memberList.Sort(typeInfoItemComparer);

                    typeInfo.PostInit(memberList.ToArray());

                    PutAll(tempTypeInfoMap);
                    return typeInfo;
                }
            }
            finally
            {
                if (tempTypeInfoMapCreated)
                {
                    tempTypeInfoMapTL.Value = null;
                }
            }
        }

        protected void FindFields(ICollection<FieldInfo> fieldList, Type type)
        {
            if (typeof(Object).Equals(type))
            {
                return;
            }
            BindingFlags flags = BindingFlags.Public | BindingFlags.Instance | BindingFlags.DeclaredOnly;

            FieldInfo[] fields = type.GetFields(flags);
            for (int a = fields.Length; a-- > 0; )
            {
                FieldInfo field = fields[a];
                if (field.IsInitOnly)
                {
                    continue;
                }
                if (field.Name.Contains('<') && field.Name.Contains(">k__BackingField"))
                {// Ignore private fields which are automatically generated by properties
                    continue;
                }
                Object[] attributes;
#if !SILVERLIGHT
                attributes = field.GetType().GetCustomAttributes(typeof(NonSerializedAttribute), false);
                if (attributes != null && attributes.Length > 0)
                {
                    continue;
                }
#endif
                attributes = field.GetType().GetCustomAttributes(typeof(IgnoreDataMemberAttribute), false);
                if (attributes != null && attributes.Length > 0)
                {
                    continue;
                }
                fieldList.Add(field);
            }
            Type baseType = type.BaseType;
            if (baseType != null)
            {
                FindFields(fieldList, baseType);
            }
        }

        protected void FindProperties(ICollection<PropertyInfo> propertyList, Type type)
        {
            BindingFlags flags = BindingFlags.Public | BindingFlags.Instance;// | BindingFlags.DeclaredOnly;

            PropertyInfo[] properties = type.GetProperties(flags);
            for (int a = properties.Length; a-- > 0; )
            {
                PropertyInfo property = properties[a];
                if (property.GetGetMethod() == null || property.GetSetMethod() == null)
                {
                    continue;
                }
                Object[] attributes;
#if !SILVERLIGHT
                attributes = property.GetType().GetCustomAttributes(typeof(NonSerializedAttribute), false);
                if (attributes != null && attributes.Length > 0)
                {
                    continue;
                }
#endif
                attributes = property.GetType().GetCustomAttributes(typeof(IgnoreDataMemberAttribute), false);
                if (attributes != null && attributes.Length > 0)
                {
                    continue;
                }
                propertyList.Add(property);
            }
            //Type baseType = type.BaseType;
            //if (baseType != null)
            //{
            //    FindProperties(propertyList, baseType);
            //}
        }

        public static void FindAllProperties(ICollection<PropertyInfo> propertyList, Type type)
        {
            BindingFlags flags = BindingFlags.Public | BindingFlags.Instance;

            PropertyInfo[] properties = type.GetProperties(flags);
            for (int a = properties.Length; a-- > 0; )
            {
                PropertyInfo property = properties[a];
                // Ignore inherited fields.
                //if (property.DeclaringType == type)
                {
                    propertyList.Add(property);
                }
            }
            Type baseType = type.BaseType;
            if (baseType != null)
            {
                FindAllProperties(propertyList, baseType);
            }
        }
    }
}