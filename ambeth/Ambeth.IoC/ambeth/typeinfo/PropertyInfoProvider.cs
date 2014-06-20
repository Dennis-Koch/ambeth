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
                HashMap<String, IMap<String, MethodInfo>> sortedMethods = new HashMap<String, IMap<String, MethodInfo>>();
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
                        IMap<String, MethodInfo> sortedMethod = sortedMethods.Get(propName);
                        if (sortedMethod == null)
                        {
                            sortedMethod = HashMap<String, MethodInfo>.Create(2);
                            sortedMethods.Put(propName, sortedMethod);
                        }
                        if (method.GetParameters().Length == 1)
                        {
                            sortedMethod.Put("set", method);
                        }
                        else if (method.GetParameters().Length == 0)
                        {
                            sortedMethod.Put("get", method);
                        }
                        else
                        {
                            throw new Exception("Method is not an accessor: " + method);
                        }
                    }
                    catch (Exception e)
                    {
                        throw RuntimeExceptionUtil.Mask(e, "Error occured while processing " + method);
                    }
                }
                HashMap<String, IPropertyInfo> propertyMap = new HashMap<String, IPropertyInfo>(0.5f);
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
                        AbstractPropertyInfo propertyInfo = new MethodPropertyInfo(type, property.Name, property.GetGetMethod(), property.GetSetMethod());
                        propertyInfo.PutAnnotations(property);
                        propertyMap.Put(propertyInfo.Name, propertyInfo);
                    }
                }

                foreach (Entry<String, IMap<String, MethodInfo>> propertyData in sortedMethods)
                {
                    String propertyName = propertyData.Key;

                    if (propertyMap.ContainsKey(propertyName))
                    {
                        // already handled by properties directly
                        continue;
                    }
                    IMap<String, MethodInfo> propertyMethods = propertyData.Value;
                    MethodInfo getter = propertyMethods.Get("get");
                    MethodInfo setter = propertyMethods.Get("set");

                    IPropertyInfo propertyInfo;
                    if ((getter == null || !getter.IsAbstract) && (setter == null || !setter.IsAbstract))
                    {
                        Type propertyType = getter != null ? getter.ReturnType : setter.GetParameters()[0].ParameterType;
                        AbstractAccessor accessor = AccessorTypeProvider.GetAccessorType(type, propertyName, propertyType);
                        propertyInfo = new MethodPropertyInfoASM2(type, propertyName, getter, setter, accessor);
                    }
                    else
                    {
                        propertyInfo = new MethodPropertyInfo(type, propertyName, getter, setter);
                    }
                    propertyMap.Put(propertyInfo.Name, propertyInfo);
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