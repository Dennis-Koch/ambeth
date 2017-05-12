using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Reflection;
using System.Text.RegularExpressions;
using De.Osthus.Ambeth.Annotation;
#if !SILVERLIGHT
using System.Threading;
using System.IO;
#else
#endif

namespace De.Osthus.Ambeth.Util
{
    public class AssemblyHelper
    {
        public static readonly Regex assemblyNameRegEx = new Regex("([^\\,]+)\\,.+");

        public static readonly ISet<Assembly> Assemblies = new HashSet<Assembly>();

        protected static readonly ISet<Assembly> registeredAssemblies = new HashSet<Assembly>();

        protected static ThreadLocal<IDictionary<String, Type>> nameToTypeDictTL = new ThreadLocal<IDictionary<String, Type>>(delegate() {
            return new Dictionary<String, Type>();
        });

        protected static IDictionary<String, Type> nameToTypeDict = new Dictionary<String, Type>();

        protected static IList<Type> typesFromCurrentDomain;

        public static void InitAssemblies(params String[] matchingAssemblies)
        {
            if (matchingAssemblies == null || matchingAssemblies.Length == 0)
            {
                return;
            }
            Regex[] regexes = new Regex[matchingAssemblies.Length];
            for (int a = matchingAssemblies.Length; a-- > 0; )
            {
                regexes[a] = new Regex(matchingAssemblies[a]);
            }
            ISet<Type> types = new HashSet<Type>();
            AppDomain currentDomain = AppDomain.CurrentDomain;
            
            ISet<Assembly> allAssemblies = LoadAllBinDirectoryAssemblies();

            foreach (Assembly assembly in registeredAssemblies)
            {
                allAssemblies.Add(assembly);
            }
#if !SILVERLIGHT
            Assembly[] currentAssemblies = currentDomain.GetAssemblies();
            foreach (Assembly assembly in currentAssemblies)
            {
                allAssemblies.Add(assembly);
            }
#endif
            foreach (Assembly assembly in allAssemblies)
            {
                String name = GetSimpleName(assembly).ToLower();
                foreach (Regex regex in regexes)
                {
                    if (regex.IsMatch(name))
                    {
                        Assemblies.Add(assembly);
                        foreach (Type type in assembly.GetTypes())
                        {
                            types.Add(type);
                        }
                        break;
                    }
                }
            }
            lock (nameToTypeDict)
            {
                foreach (Type type in types)
                {
                    nameToTypeDict[type.FullName] = type;
                }
            }
            typesFromCurrentDomain = ListUtil.ToList(types);
        }

        public static String GetSimpleName(Assembly assembly)
        {
            Match match = assemblyNameRegEx.Match(assembly.FullName);
            if (match.Success)
            {
                return match.Groups[1].Value;
            }
            return assembly.FullName;
        }

        protected static ISet<Assembly> LoadAllBinDirectoryAssemblies()
        {
            ISet<Assembly> assemblies = new HashSet<Assembly>();
#if !SILVERLIGHT
            String path = System.AppDomain.CurrentDomain.BaseDirectory;

            foreach (string dll in Directory.GetFiles(path, "*.dll", SearchOption.AllDirectories))
            {
                try
                {
                    Assembly loadedAssembly = Assembly.LoadFile(dll);
                    assemblies.Add(loadedAssembly);
                }
                catch (FileLoadException)
                { } // The Assembly has already been loaded.
                catch (BadImageFormatException)
                { } // If a BadImageFormatException exception is thrown, the file is not an assembly.

            } // foreach dll
#endif
            return assemblies;
        }

        public static void RegisterAssemblyFromType(Type type)
        {
            lock (registeredAssemblies)
            {
                registeredAssemblies.Add(type.Assembly);
                Assemblies.Add(type.Assembly);
            }
        }

        public static void RegisterExecutingAssembly()
        {
            //assemblies.Add(Assembly.GetCallingAssembly());
        }

        public static Type GetTypeFromAssemblies(String typeName)
        {
            IDictionary<String, Type> nameToTypeDict = nameToTypeDictTL.Value;
            Type type = DictionaryExtension.ValueOrDefault(nameToTypeDict, typeName);
            if (type != null)
            {
                return type;
            }
            if (nameToTypeDict.ContainsKey(typeName))
            {
                return null;
            }
            type = Type.GetType(typeName);
            if (type != null)
            {
                nameToTypeDict.Add(typeName, type);
                return type;
            }
            lock (Assemblies)
            {
                foreach (Assembly assembly in Assemblies)
                {
                    type = assembly.GetType(typeName, false);
                    if (type != null)
                    {
                        nameToTypeDict.Add(typeName, type);
                        return type;
                    }
                }
            }
            // GN: If we add to the nameToTypeDict here, we have the problem, that we return in line 141 the next time, even if the type is available by that time.
            // Concrete example: ValueObjectConfigReader handles the configuration of "ProcessingRequest" from the delivery domain. This entity has a relation to
            // "ProcessingStepType" from the processing domain, hence the ValueObjectConfigReader calls this method to determine the VO type. As we are in a warehouse
            // screen that needs no procesing entities, the type cannot be found. Now we switch to another warehouse screen that has dependencies to processing, but we
            // return in line 141, although the assembly is now available.
            //nameToTypeDict.Add(typeName, null);
            return null;
        }

        public static IList<Type> GetTypesFromCurrentDomain()
        {
            return typesFromCurrentDomain;
        }

        public static Type GetTypeFromCurrentDomain(String typeName)
        {
            return DictionaryExtension.ValueOrDefault(nameToTypeDict, typeName);
        }

        public static void HandleTypesFromCurrentDomain<T>(TypeHandleDelegate typeHandleDelegate) where T : class
        {
            HandleTypesFromCurrentDomain<T>(typeHandleDelegate, false);
        }

        public static void HandleTypesFromCurrentDomain<T>(TypeHandleDelegate typeHandleDelegate, bool includeInterfaces) where T : class
        {
            Type lookForType = typeof(T);
            foreach (Type type in typesFromCurrentDomain)
            {
                if (!lookForType.IsAssignableFrom(type))
                {
                    continue;
                }
                if (!includeInterfaces && type.IsInterface)
                {
                    continue;
                }
                typeHandleDelegate(type);
            }
        }

        public static void HandleTypesFromCurrentDomainWithAnnotation<T>(TypeHandleDelegate typeHandleDelegate) where T : Attribute
        {
            foreach (Type type in typesFromCurrentDomain)
            {
                if (!AnnotationUtil.IsAnnotationPresent<T>(type, true))
                {
                    continue;
                }
                typeHandleDelegate(type);
            }
        }

        public static void HandleAttributedTypesFromCurrentDomain<T>(AttributedTypeHandleDelegate typeHandleDelegate) where T : Attribute
        {
            Type lookForType = typeof(T);
            foreach (Type type in typesFromCurrentDomain)
            {
                Object[] attributes = type.GetCustomAttributes(lookForType, true);
                if (attributes == null || attributes.Length == 0)
                {
                    continue;
                }
                typeHandleDelegate(type, attributes);
            }
        }
    }

    public delegate void TypeHandleDelegate(Type type);

    public delegate void AttributedTypeHandleDelegate(Type type, Object[] attributes);
}
