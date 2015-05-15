using System;
using System.Reflection;
using System.Text.RegularExpressions;
using De.Osthus.Ambeth.Accessor;
using De.Osthus.Ambeth.Annotation;
using De.Osthus.Ambeth.Collections;
using De.Osthus.Ambeth.Exceptions;
using De.Osthus.Ambeth.Ioc;
using De.Osthus.Ambeth.Ioc.Annotation;
using De.Osthus.Ambeth.Util;
using System.Collections.Generic;

namespace De.Osthus.Ambeth.Typeinfo
{
    public class PropertyInfoProvider : SmartCopyMap<Type, PropertyInfoEntry>, IPropertyInfoProvider, IInitializingBean
    {
        private static readonly Regex getSetIsPattern = new Regex("(get_|set_|Get|Set|Is)([A-ZÄÖÜ].*)");

        public IAccessorTypeProvider AccessorTypeProvider { protected get; set; }

        public virtual void AfterPropertiesSet()
        {
            ParamChecker.AssertNotNull(AccessorTypeProvider, "AccessorTypeProvider");
        }

        public IPropertyInfo GetProperty(Object obj, String propertyName)
        {
            return GetProperty(obj.GetType(), propertyName);
        }

        public IPropertyInfo GetProperty(Type type, String propertyName)
        {
            IMap<String, IPropertyInfo> map = GetPropertyMap(type);
            return map.Get(propertyName);
        }

        public IPropertyInfo[] GetProperties(Object obj)
        {
            return GetProperties(obj.GetType());
        }

        public IPropertyInfo[] GetProperties(Type type)
        {
            return GetPropertyEntry(type).properties;
        }

        public IMap<String, IPropertyInfo> GetPropertyMap(Object obj)
        {
            return GetPropertyMap(obj.GetType());
        }

        public IMap<String, IPropertyInfo> GetPropertyMap(Type type)
        {
            return GetPropertyEntry(type).map;
        }

        protected PropertyInfoEntry GetPropertyEntry(Type type)
        {
            ParamChecker.AssertParamNotNull(type, "type");
            PropertyInfoEntry propertyEntry = Get(type);
            if (propertyEntry != null)
            {
                return propertyEntry;
            }
            Object writeLock = GetWriteLock();
            lock (writeLock)
            {
                propertyEntry = Get(type);
                if (propertyEntry != null)
                {
                    // Concurrent thread might have been faster
                    return propertyEntry;
                }

                HashMap<String, HashMap<Type, HashMap<String, MethodInfo>>> sortedMethods = new HashMap<String, HashMap<Type, HashMap<String, MethodInfo>>>();
                MethodInfo[] methods = ReflectUtil.GetMethods(type);

                foreach (MethodInfo method in methods)
                {
                    if (method.DeclaringType.Equals(typeof(Object)))
                    {
                        continue;
                    }
                    try
                    {
                        String propName = GetPropertyNameFor(method);
                        if (propName.Length == 0 || method.IsStatic)
                        {
                            continue;
                        }

                        HashMap<Type, HashMap<String, MethodInfo>> sortedMethod = sortedMethods.Get(propName);
                        if (sortedMethod == null)
                        {
                            sortedMethod = HashMap<Type, HashMap<String, MethodInfo>>.Create(1);
                            sortedMethods.Put(propName, sortedMethod);
                        }

                        ParameterInfo[] parameterInfos = method.GetParameters();
                        Type propertyType;
                        String prefix;
                        if (parameterInfos.Length == 1)
                        {
                            propertyType = parameterInfos[0].ParameterType;
                            prefix = "set";
                        }
                        else if (parameterInfos.Length == 0)
                        {
                            propertyType = method.ReturnType;
                            prefix = "get";
                        }
                        else
                        {
                            throw new Exception("Method is not an accessor: " + method);
                        }

                        HashMap<String, MethodInfo> methodPerType = sortedMethod.Get(propertyType);
                        if (methodPerType == null)
                        {
                            methodPerType = HashMap<String, MethodInfo>.Create(2);
                            sortedMethod.Put(propertyType, methodPerType);
                        }

                        methodPerType.Put(prefix, method);
                    }
                    catch (Exception e)
                    {
                        throw RuntimeExceptionUtil.Mask(e, "Error occured while processing " + method);
                    }
                }

                HashMap<String, HashMap<String, MethodInfo>> filteredMethods = FilterOverriddenMethods(sortedMethods, type);

                HashMap<String, IPropertyInfo> propertyMap = new HashMap<String, IPropertyInfo>(0.5f);
                foreach (MapEntry<String, HashMap<String, MethodInfo>> propertyData in filteredMethods)
                {
                    String propertyName = propertyData.Key;

                    HashMap<String, MethodInfo> propertyMethods = propertyData.Value;
                    MethodInfo getter = propertyMethods.Get("get");
                    MethodInfo setter = propertyMethods.Get("set");

                    MethodPropertyInfo propertyInfo = new MethodPropertyInfo(type, propertyName, getter, setter);
                    propertyMap.Put(propertyInfo.Name, propertyInfo);
                }

                Type[] interfaces = type.GetInterfaces();
                List<Type> typesToSearch = new List<Type>(interfaces);
                typesToSearch.Add(type);
                foreach (Type typeToSearch in typesToSearch)
                {
                    PropertyInfo[] properties = typeToSearch.GetProperties(BindingFlags.FlattenHierarchy | BindingFlags.Static | BindingFlags.Instance | BindingFlags.Public);
                    foreach (PropertyInfo property in properties)
                    {
                        if (property.GetGetMethod() != null && property.GetGetMethod().GetParameters().Length != 0)
                        {
                            continue;
                        }
                        if (property.GetSetMethod() != null && property.GetSetMethod().GetParameters().Length != 1)
                        {
                            continue;
                        }
                        MethodInfo getter = null;
                        MethodInfo setter = null;

                        MethodPropertyInfo propertyInfo = (MethodPropertyInfo)propertyMap.Get(property.Name);
                        if (propertyInfo != null)
                        {
                            getter = propertyInfo.Getter;
                            setter = propertyInfo.Setter;
                        }
                        if (getter == null)
                        {
                            getter = property.GetGetMethod();
                        }
                        if (setter == null)
                        {
                            setter = property.GetSetMethod();
                        }
                        propertyInfo = new MethodPropertyInfo(type, property.Name, getter, setter);
                        propertyInfo.PutAnnotations(property);
                        propertyMap.Put(propertyInfo.Name, propertyInfo);
                    }
                }

                FieldInfo[] fields = ReflectUtil.GetDeclaredFieldsInHierarchy(type);
                foreach (FieldInfo field in fields)
                {
                    if (!AnnotationUtil.IsAnnotationPresent<AutowiredAttribute>(field, false))
                    {
                        continue;
                    }
                    String propertyName = GetPropertyNameFor(field);
                    IPropertyInfo existingProperty = propertyMap.Get(propertyName);
                    if (existingProperty != null && existingProperty.IsWritable)
                    {
                        // Ignore field injection if the already resolved (method-)property is writable
                        continue;
                    }
                    IPropertyInfo propertyInfo = new FieldPropertyInfo(type, propertyName, field);
                    propertyMap.Put(propertyInfo.Name, propertyInfo);
                }
                propertyEntry = new PropertyInfoEntry(propertyMap);
                Put(type, propertyEntry);
                return propertyEntry;
            }
        }

        protected HashMap<String, HashMap<String, MethodInfo>> FilterOverriddenMethods(HashMap<String, HashMap<Type, HashMap<String, MethodInfo>>> sortedMethods,
                Type entityType)
        {
            HashMap<String, HashMap<String, MethodInfo>> filteredMethods = HashMap<String, HashMap<String, MethodInfo>>.Create(sortedMethods.Count);

            foreach (MapEntry<String, HashMap<Type, HashMap<String, MethodInfo>>> entry in sortedMethods)
            {
                String propName = entry.Key;
                HashMap<Type, HashMap<String, MethodInfo>> typedHashMap = entry.Value;

                if (typedHashMap.Count == 1)
                {
                    IEnumerator<HashMap<String, MethodInfo>> iter = typedHashMap.Values().GetEnumerator();
                    iter.MoveNext();
                    HashMap<String, MethodInfo> accessorMap = iter.Current;
                    filteredMethods.Put(propName, accessorMap);
                    continue;
                }

                Type mostConcreteType = null;
                Type mostConcreteGetterType = null;
                Type mostConcreteSetterType = null;
                foreach (Entry<Type, HashMap<String, MethodInfo>> typedEntries in typedHashMap)
                {
                    Type currentType = typedEntries.Key;
                    HashMap<String, MethodInfo> accessorMap = typedEntries.Value;
                    if (accessorMap.Count != 2)
                    {
                        if (accessorMap.Get("get") != null)
                        {
                            if (mostConcreteGetterType == null || mostConcreteGetterType.IsAssignableFrom(currentType))
                            {
                                mostConcreteGetterType = currentType;
                            }
                        }
                        else
                        {
                            if (mostConcreteSetterType == null || mostConcreteSetterType.IsAssignableFrom(currentType))
                            {
                                mostConcreteSetterType = currentType;
                            }
                        }
                        continue;
                    }

                    if (mostConcreteType == null || mostConcreteType.IsAssignableFrom(currentType))
                    {
                        mostConcreteType = currentType;
                    }
                }
                if (mostConcreteType != null)
                {
                    HashMap<String, MethodInfo> accessorMap = typedHashMap.Get(mostConcreteType);
                    filteredMethods.Put(propName, accessorMap);
                }
                else if (mostConcreteGetterType != null)
                {
                    HashMap<String, MethodInfo> accessorMap = typedHashMap.Get(mostConcreteGetterType);
                    filteredMethods.Put(propName, accessorMap);
                }
                else if (mostConcreteSetterType != null)
                {
                    HashMap<String, MethodInfo> accessorMap = typedHashMap.Get(mostConcreteSetterType);
                    filteredMethods.Put(propName, accessorMap);
                }
            }

            return filteredMethods;
        }

        public String GetPropertyNameFor(FieldInfo field)
        {
            return StringBuilderUtil.UpperCaseFirst(field.Name);
        }

        public String GetPropertyNameFor(MethodInfo method)
        {
            PropertyAccessor propertyAccessor = AnnotationUtil.GetAnnotation<PropertyAccessor>(method, true);
            if (propertyAccessor != null)
            {
                return propertyAccessor.PropertyName;
            }
            Match matcher = getSetIsPattern.Match(method.Name);
            if (!matcher.Success)
            {
                return "";
            }
            int paramLength = method.GetParameters().Length;
            String getSetIs = matcher.Groups[1].Value;
            if (("get_".Equals(getSetIs) || "Get".Equals(getSetIs)) && (0 != paramLength || typeof(void).Equals(method.ReturnType)))
            {
                return "";
            }
            else if (("set_".Equals(getSetIs) || "Set".Equals(getSetIs)) && 1 != paramLength)
            {
                return "";
            }
            else if ("Is".Equals(getSetIs) && (0 != paramLength || typeof(void).Equals(method.ReturnType)))
            {
                return "";
            }
            String name = matcher.Groups[2].Value;
            return StringConversionHelper.UpperCaseFirst(name);
        }
    }
}