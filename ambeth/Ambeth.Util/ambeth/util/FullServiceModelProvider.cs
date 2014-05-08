using System;
using System.Collections.Generic;
using System.Reflection;
using System.Runtime.Serialization;
using System.Text.RegularExpressions;
using De.Osthus.Ambeth.Util;
using Castle.DynamicProxy;
using System.Collections;
using System.IO.IsolatedStorage;
using System.IO;

namespace De.Osthus.Ambeth.Service
{
    public class FullServiceModelProvider
    {
        protected static readonly Type listType = typeof(List<Object>).GetGenericTypeDefinition();

        protected static readonly Type listInterfaceType = typeof(IList<Object>).GetGenericTypeDefinition();

        protected static ISet<Type> serviceModelTypes = new HashSet<Type>();

        protected static IDictionary<Type, Type> listToTypeCollectionContractType = new Dictionary<Type, Type>();

        protected static ISet<Type> proxyServiceModelTypes;
        
        public static void ShareServiceModel(Assembly assembly, params String[] namespacePatterns)
        {
            Regex[] regexes = new Regex[namespacePatterns.Length];
            for (int a = namespacePatterns.Length; a-- > 0; )
            {
                regexes[a] = new Regex(namespacePatterns[a]);
            }

            foreach (Type type in assembly.GetTypes())
            {
                String typeNamespace = type.Namespace;
                if (typeNamespace == null)
                {
                    // This may happen on some internal generated classes of .NET
                    continue;
                }
                foreach (Regex regex in regexes)
                {
                    if (!regex.IsMatch(typeNamespace))
                    {
                        continue;
                    }
                    if (type.GetCustomAttributes(typeof(DataContractAttribute), false).Length == 0
                        && type.GetCustomAttributes(typeof(CollectionDataContractAttribute), false).Length == 0)
                    {
                        continue;
                    }
                    serviceModelTypes.Add(type);

                    if (type.GetCustomAttributes(typeof(CollectionDataContractAttribute), false).Length > 0)
                    {
                        Type[] genArguments = type.BaseType.GetGenericArguments();
                        if (genArguments.Length == 1)
                        {
                            Type genArgument = genArguments[0];

                            Type genTypedListType = listType.MakeGenericType(genArgument);
                            if (!listToTypeCollectionContractType.ContainsKey(genTypedListType))
                            {
                                listToTypeCollectionContractType.Add(genTypedListType, type);
                            }
                        }
                    }
                    Type typedListType = listType.MakeGenericType(type);

                    serviceModelTypes.Add(typedListType); // Register list version of transfer type
                    break;
                }
            }
        }

        public static T ConvertToCollectionContract<T>(T list)
        {
            if (list == null)
            {
                return default(T);
            }
            Type collectionContractType = DictionaryExtension.ValueOrDefault(listToTypeCollectionContractType, list.GetType());
            if (collectionContractType == null)
            {
                return list;
            }
            return (T)Activator.CreateInstance(collectionContractType, list);
        }

        public static IList<T> ConvertToCollectionContract<T>(IList<T> list)
        {
            Type collectionContractType = DictionaryExtension.ValueOrDefault(listToTypeCollectionContractType, list.GetType());
            if (collectionContractType == null)
            {
                return list;
            }
            return (IList<T>)Activator.CreateInstance(collectionContractType, list);
        }

        public static IEnumerable<Type> RegisterKnownTypes(ICustomAttributeProvider provider)
        {
            if (proxyServiceModelTypes == null)
            {
                proxyServiceModelTypes = new HashSet<Type>();

//#if !SILVERLIGHT
//                ModuleScope scope = new ModuleScope(true, true, ModuleScope.DEFAULT_ASSEMBLY_NAME, ModuleScope.DEFAULT_FILE_NAME, "Ambeth.Proxy", "Ambeth.Proxy.dll");
//#else
//                ModuleScope scope = new ModuleScope(true, ModuleScope.DEFAULT_ASSEMBLY_NAME, ModuleScope.DEFAULT_FILE_NAME, "Ambeth.Proxy", "Ambeth.Proxy.dll");
//#endif
//                IProxyBuilder builder = new DefaultProxyBuilder(scope);
//                ProxyGenerator proxyGenerator = new ProxyGenerator(builder);

//                foreach (Type type in serviceModelTypes)
//                {
//                    try
//                    {
//                        if (type.IsInterface)
//                        {
//                            Type proxyType = builder.CreateInterfaceProxyTypeWithoutTarget(type, new Type[0], new ProxyGenerationOptions());
//                            proxyServiceModelTypes.Add(proxyType);
//                        }
//                        else if (type.IsClass && !type.HasElementType && !type.IsGenericType && !typeof(IList).IsAssignableFrom(type))
//                        {
//                            Type proxyType = proxyGenerator.CreateClassProxy(type).GetType();
//                            proxyServiceModelTypes.Add(proxyType);
//                        }
//                    }
//                    catch (Exception e)
//                    {
//                        String stackTrace = e.StackTrace;
//                        throw;
//                    }
//                }
//#if !SILVERLIGHT
//                //scope.SaveAssembly(false);
//#else
//#endif
                serviceModelTypes.UnionWith(proxyServiceModelTypes);
                serviceModelTypes.Add(typeof(List<String>));
            }
            return serviceModelTypes;
        }
    }
}
