using De.Osthus.Ambeth.Accessor;
using De.Osthus.Ambeth.Collections;
using De.Osthus.Ambeth.Ioc;
using De.Osthus.Ambeth.Ioc.Annotation;
using De.Osthus.Ambeth.Log;
using De.Osthus.Ambeth.Proxy;
using De.Osthus.Ambeth.Util;
using System;
using System.Collections.Generic;
using System.Reflection;
using System.Reflection.Emit;

namespace De.Osthus.Ambeth.Garbageproxy
{
    public class GarbageProxyFactoryConstructorMap : Tuple2KeyHashMap<Type, Type[], IGarbageProxyConstructor>
    {
        protected override bool EqualKeys(Type key1, Type[] key2, Tuple2KeyEntry<Type, Type[], IGarbageProxyConstructor> entry)
        {
            return key1.Equals(entry.GetKey1()) && Arrays.Equals(key2, entry.GetKey2());
        }

		protected override int ExtractHash(Type key1, Type[] key2)
		{
			return (key1 != null ? key1.GetHashCode() : 3) ^ (key2 != null ? Arrays.GetHashCode(key2) : 5);
		}
    }

    public class GarbageProxyFactory : IGarbageProxyFactory, IInitializingBean
    {
        [LogInstance]
        public ILogger Log { private get; set; }

        [Autowired]
        public IAccessorTypeProvider AccessorTypeProvider { protected get; set; }

        protected readonly Tuple2KeyHashMap<Type, Type[], IGarbageProxyConstructor> interfaceTypesToConstructorMap = new GarbageProxyFactoryConstructorMap();

        protected readonly Object writeLock = new Object();

        public void AfterPropertiesSet()
        {
            ParamChecker.AssertNotNull(AccessorTypeProvider, "AccessorTypeProvider");
        }

        public IGarbageProxyConstructor<T> CreateGarbageProxyConstructor<T>(params Type[] additionalInterfaceTypes)
        {
            Object writeLock = this.writeLock;
            lock (writeLock)
            {
                IGarbageProxyConstructor gpContructor = interfaceTypesToConstructorMap.Get(typeof(T), additionalInterfaceTypes);
                if (gpContructor != null)
                {
                    return (IGarbageProxyConstructor<T>)gpContructor;
                }
                Type gpType = LoadClass(typeof(GCProxy), typeof(T), additionalInterfaceTypes);
                gpContructor = AccessorTypeProvider.GetConstructorType<IGarbageProxyConstructor<T>>(gpType);
                interfaceTypesToConstructorMap.Put(typeof(T), additionalInterfaceTypes, gpContructor);
                return (IGarbageProxyConstructor<T>)gpContructor;
            }
        }

        public T CreateGarbageProxy<T>(IDisposable target, params Type[] additionalInterfaceTypes)
        {
            return CreateGarbageProxy<T>(target, target, additionalInterfaceTypes);
        }

        public T CreateGarbageProxy<T>(Object target, IDisposable disposable, params Type[] additionalInterfaceTypes)
        {
            return CreateGarbageProxyConstructor<T>(additionalInterfaceTypes).CreateInstance(target, disposable);
        }

        protected Type LoadClass(Type baseType, Type interfaceType, Type[] additionalInterfaceTypes)
        {
            String className = interfaceType.FullName + "$" + baseType.Name + "$" + Arrays.GetHashCode(additionalInterfaceTypes);
            if (className.StartsWith("java."))
            {
                className = "ambeth." + className;
            }
            lock (writeLock)
            {
                AccessorClassLoader loader = AccessorClassLoader.Get(interfaceType);
                Type type = loader.LoadClass(className);
                if (type != null)
                {
                    return type;
                }
                return CreateGpType(loader, interfaceType, additionalInterfaceTypes, className);
            }
        }

        protected Type CreateGpType(AccessorClassLoader loader, Type proxyType, Type[] additionalProxyTypes, String className)
        {
            String classNameInternal = className.Replace('.', '/');
            Type abstractType = typeof(GCProxy);

            List<Type> interfaceClasses = new List<Type>();
            interfaceClasses.Add(proxyType);
            foreach (Type additionalProxyType in additionalProxyTypes)
            {
                interfaceClasses.Add(additionalProxyType);
            }

            TypeBuilder cw = loader.CreateNewType(TypeAttributes.Public, classNameInternal, abstractType, Type.EmptyTypes);
            {
                ConstructorInfo baseConstructor = abstractType.GetConstructor(BindingFlags.Instance | BindingFlags.Public | BindingFlags.NonPublic, null, new Type[] { typeof(Object), typeof(IDisposable) }, null);
                if (baseConstructor == null)
                {
                    throw new Exception("Constructor not found: " + abstractType.FullName);
                }
				ILGenerator mv = cw.DefineConstructor(MethodAttributes.Public, CallingConventions.HasThis, TypeUtil.GetParameterTypesToTypes(baseConstructor.GetParameters())).GetILGenerator();
                mv.Emit(OpCodes.Ldarg_0);
                mv.Emit(OpCodes.Ldarg_1);
                mv.Emit(OpCodes.Ldarg_2);
                mv.Emit(OpCodes.Call, baseConstructor);
                mv.Emit(OpCodes.Ret);
            }
            {
                ConstructorInfo baseConstructor = abstractType.GetConstructor(BindingFlags.Instance | BindingFlags.Public | BindingFlags.NonPublic, null, new Type[] { typeof(IDisposable) }, null);
                if (baseConstructor == null)
                {
                    throw new Exception("Constructor not found: " + abstractType.FullName);
                }
                ILGenerator mv = cw.DefineConstructor(MethodAttributes.Public, CallingConventions.HasThis, TypeUtil.GetParameterTypesToTypes(baseConstructor.GetParameters())).GetILGenerator();
                mv.Emit(OpCodes.Ldarg_0);
                mv.Emit(OpCodes.Ldarg_1);
                mv.Emit(OpCodes.Call, baseConstructor);
                mv.Emit(OpCodes.Ret);
            }
            MethodInfo targetMethod = ReflectUtil.GetDeclaredMethod(false, typeof(GCProxy), typeof(Object), "ResolveTarget");

			CHashSet<MethodInfo> alreadyImplementedMethods = new CHashSet<MethodInfo>();
            foreach (Type interfaceClass in interfaceClasses)
            {
                MethodInfo[] methods = interfaceClass.GetMethods();
                foreach (MethodInfo method in methods)
                {
                    if (GCProxy.disposeMethod.Equals(method))
                    {
                        // will remain implemented by the GCProxy class
                        continue;
                    }
                    if (!alreadyImplementedMethods.Add(method))
                    {
                        continue;
                    }
                    MethodAttributes attributes = 0;

                    Type[] paramTypes = TypeUtil.GetParameterTypesToTypes(method.GetParameters());
                    ILGenerator mv = cw.DefineMethod(method.Name, attributes, CallingConventions.HasThis, method.ReturnType, paramTypes).GetILGenerator();
                    mv.Emit(OpCodes.Ldarg_0);
                    mv.Emit(OpCodes.Callvirt, targetMethod);
                    mv.Emit(OpCodes.Castclass, method.DeclaringType);
                    for (int a = 0, size = paramTypes.Length; a < size; a++)
                    {
                        mv.Emit(OpCodes.Ldarg, a + 1);
                    }
                    mv.Emit(OpCodes.Callvirt, method);
                    mv.Emit(OpCodes.Ret);
                }
            }
            return loader.GetType(classNameInternal, cw);
        }
    }
}