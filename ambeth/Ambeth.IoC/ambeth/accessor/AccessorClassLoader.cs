using De.Osthus.Ambeth.Collections;
using De.Osthus.Ambeth.Util;
using System;
using System.Collections.Generic;
using System.Reflection;
using System.Reflection.Emit;
using System.Threading;
namespace De.Osthus.Ambeth.Accessor
{
    public class AccessorClassLoader
    {
        private static readonly WeakDictionary<Assembly, AccessorClassLoader> accessClassLoaders = new WeakDictionary<Assembly, AccessorClassLoader>();

        private static readonly Object writeLock = new Object();

        public static AccessorClassLoader Get(Type type)
        {
            Assembly parent = type.Assembly;
            lock (writeLock)
            {
                List<KeyValuePair<Assembly, AccessorClassLoader>> toBeRemoved = null;
                foreach (KeyValuePair<Assembly, AccessorClassLoader> entry in accessClassLoaders)
                {
                    Assembly assembly = entry.Key;
                    if (assembly == null)
                    {
                        // Current ClassLoader is invalidated
                        if (toBeRemoved == null)
                        {
                            toBeRemoved = new List<KeyValuePair<Assembly,AccessorClassLoader>>();
                        }
                        toBeRemoved.Add(entry);
                        continue;
                    }
                    if (Object.ReferenceEquals(assembly, parent))
                    {
                        return entry.Value;
                    }
                }
                if (toBeRemoved != null)
                {
                    foreach (KeyValuePair<Assembly, AccessorClassLoader> entry in toBeRemoved)
                    {
                        accessClassLoaders.Remove(entry);
                    }
                }
                AccessorClassLoader accessClassLoader = new AccessorClassLoader(parent);
                accessClassLoaders.Add(parent, accessClassLoader);
                return accessClassLoader;
            }
        }

        protected readonly Assembly parent;

        protected readonly ModuleBuilder moduleBuilder;

        protected readonly AssemblyBuilder assemblyBuilder;

        protected readonly HashMap<String, Type> nameToTypeMap = new HashMap<String, Type>(); 

        private AccessorClassLoader(Assembly parent)
        {
            this.parent = parent;
            String name = parent.FullName.Replace('\\',' ') + "_ACCESSOR_" + new Random().Next();
            AssemblyName assemblyName = new AssemblyName { Name = name };
            AssemblyBuilderAccess access;
#if SILVERLIGHT
            access = AssemblyBuilderAccess.Run;
#else
            access = AssemblyBuilderAccess.RunAndCollect;
#endif
            AssemblyBuilder assemblyBuilder = Thread.GetDomain().DefineDynamicAssembly(assemblyName, access);
            moduleBuilder = assemblyBuilder.DefineDynamicModule(name, true);
            this.assemblyBuilder = assemblyBuilder;
        }

        public TypeBuilder CreateNewType(TypeAttributes access, String name, Type parentType, Type[] interfaces)
        {
            try
            {
                return moduleBuilder.DefineType(name, access, parentType, interfaces);
            }
            catch (Exception e)
            {
                throw;
            }
        }

        public Type GetType(String typeName, TypeBuilder tb)
        {
            Type type = tb.CreateType();
            nameToTypeMap.Put(typeName, type);
            return type;
        }

        public Type LoadClass(String typeName)
        {
            Type type = nameToTypeMap.Get(typeName);
            if (type != null)
            {
                return type;
            }
            return moduleBuilder.GetType(typeName, false, false);
        }
    }
}