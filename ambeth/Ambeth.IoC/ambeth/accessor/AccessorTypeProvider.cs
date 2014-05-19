using De.Osthus.Ambeth.Collections;
using De.Osthus.Ambeth.Ioc;
using De.Osthus.Ambeth.Log;
using De.Osthus.Ambeth.Util;
using System;
using System.Reflection;
using System.Reflection.Emit;

namespace De.Osthus.Ambeth.Accessor
{
    public class AccessorTypeProvider : IAccessorTypeProvider, IInitializingBean
    {
        protected static readonly ConstructorInfo ci = typeof(DefaultAccessor).GetConstructor(new Type[] { typeof(Type), typeof(String) });

        [LogInstance]
        public ILogger Log { private get; set; }

        protected readonly Tuple2KeyHashMap<Type, String, AbstractAccessor> typeToAccessorMap = new Tuple2KeyHashMap<Type, String, AbstractAccessor>();

        protected readonly HashMap<Type, Object> typeToConstructorMap = new HashMap<Type, Object>();

        protected readonly Object writeLock = new Object();

        public void AfterPropertiesSet()
        {
            // Intended blank
        }

        public AbstractAccessor GetAccessorType(Type type, String propertyName)
        {
            AbstractAccessor accessor = typeToAccessorMap.Get(type, propertyName);
            if (accessor != null)
            {
                return accessor;
            }
            Object writeLock = this.writeLock;
            lock (writeLock)
            {
                // concurrent thread might have been faster
                accessor = typeToAccessorMap.Get(type, propertyName);
                if (accessor != null)
                {
                    return accessor;
                }
                try
                {
                    Type enhancedType = GetAccessorTypeIntern(type, propertyName);
                    if (enhancedType != typeof(AbstractAccessor))
                    {
                        ConstructorInfo constructor = enhancedType.GetConstructor(new Type[] { typeof(Type), typeof(String) });
                        accessor = (AbstractAccessor)constructor.Invoke(new Object[] { type, propertyName });
                    }
                }
                catch (Exception e)
                {
                    if (Log.WarnEnabled)
                    {
                        Log.Warn(e);
                    }
                }
                if (accessor == null)
                {
                    // something serious happened during enhancement: continue with a fallback
                    accessor = (AbstractAccessor)ci.Invoke(new Object[] { type, propertyName });
                }
                typeToAccessorMap.Put(type, propertyName, accessor);
                return accessor;
            }
        }

        public V GetConstructorType<V>(Type targetType)
        {
            Object constructorDelegate = typeToConstructorMap.Get(targetType);
            if (constructorDelegate != null)
            {
                return (V)constructorDelegate;
            }
            Object writeLock = this.writeLock;
            lock (writeLock)
            {
                // concurrent thread might have been faster
                constructorDelegate = typeToConstructorMap.Get(targetType);
                if (constructorDelegate != null)
                {
                    return (V)constructorDelegate;
                }
                Type enhancedType = GetConstructorTypeIntern(typeof(V), targetType);
                constructorDelegate = Activator.CreateInstance(enhancedType);
                typeToConstructorMap.Put(targetType, constructorDelegate);
                return (V)constructorDelegate;
            }
        }

        protected Type GetConstructorTypeIntern(Type delegateType, Type targetType)
        {
            String constructorClassName = targetType.FullName + "$FastConstructor$" + delegateType.FullName;
            lock (writeLock)
            {
                AccessorClassLoader loader = AccessorClassLoader.Get(targetType);
                Type type = loader.LoadClass(constructorClassName);
                if (type != null)
                {
                    return type;
                }
                return CreateConstructorType(loader, constructorClassName, delegateType, targetType);
            }
        }

        protected Type GetAccessorTypeIntern(Type targetType, String propertyName)
        {
            String accessClassName = targetType.FullName + "$" + typeof(AbstractAccessor).Name + "$" + propertyName;
            lock (writeLock)
            {
                AccessorClassLoader loader = AccessorClassLoader.Get(targetType);
                Type type = loader.LoadClass(accessClassName);
                if (type != null)
                {
                    return type;
                }
                return CreateType(loader, accessClassName, targetType, propertyName);
            }
        }
        
        protected Type CreateType(AccessorClassLoader loader, String accessClassName, Type targetType, String propertyName)
        {
            if (Log.DebugEnabled)
            {
                Log.Debug("Creating accessor for " + targetType.FullName + "." + propertyName);
            }
            Type abstractAccessorType = typeof(AbstractAccessor);
            Type objType = typeof(Object);

            TypeBuilder cw = loader.CreateNewType(TypeAttributes.Public, accessClassName, abstractAccessorType, Type.EmptyTypes);
            {
                ConstructorInfo baseConstructor = abstractAccessorType.GetConstructor(BindingFlags.Instance | BindingFlags.Public | BindingFlags.NonPublic, null, new Type[] { typeof(Type), typeof(String) }, null);
                ILGenerator mv = cw.DefineConstructor(MethodAttributes.Public, CallingConventions.HasThis, new Type[] { typeof(Type), typeof(String) }).GetILGenerator();
                mv.Emit(OpCodes.Ldarg_0);
                mv.Emit(OpCodes.Ldarg_1);
                mv.Emit(OpCodes.Ldarg_2);
                mv.Emit(OpCodes.Call, baseConstructor);
                mv.Emit(OpCodes.Ret);
            }
            MethodInfo r_get = ReflectUtil.GetDeclaredMethod(true, targetType, "get_" + propertyName, new Type[0]);
            if (r_get == null)
            {
                r_get = ReflectUtil.GetDeclaredMethod(true, targetType, "Get" + propertyName, new Type[0]);
            }
            if (r_get == null)
            {
                r_get = ReflectUtil.GetDeclaredMethod(true, targetType, "Is" + propertyName, new Type[0]);
            }
            MethodInfo r_set = ReflectUtil.GetDeclaredMethod(true, targetType, "set_" + propertyName, new Type[] { null });
            if (r_set == null)
            {
                r_set = ReflectUtil.GetDeclaredMethod(true, targetType, "Set" + propertyName, new Type[] { null });
            }
            {
                ILGenerator mv = cw.DefineMethod("get_CanRead", MethodAttributes.Public | MethodAttributes.Virtual | MethodAttributes.HideBySig, CallingConventions.HasThis, typeof(bool), Type.EmptyTypes).GetILGenerator();
                mv.Emit(r_get != null && r_get.IsPublic ? OpCodes.Ldc_I4_1 : OpCodes.Ldc_I4_0);
                mv.Emit(OpCodes.Ret);
            }
            {
                ILGenerator mv = cw.DefineMethod("get_CanWrite", MethodAttributes.Public | MethodAttributes.Virtual | MethodAttributes.HideBySig, CallingConventions.HasThis, typeof(bool), Type.EmptyTypes).GetILGenerator();
                mv.Emit(r_set != null && r_set.IsPublic ? OpCodes.Ldc_I4_1 : OpCodes.Ldc_I4_0);
                mv.Emit(OpCodes.Ret);
            }
            {
                ILGenerator mv = cw.DefineMethod("GetValue", MethodAttributes.Public | MethodAttributes.Virtual | MethodAttributes.HideBySig, CallingConventions.HasThis, typeof(Object), new Type[] { typeof(Object) }).GetILGenerator();

                if (r_get == null)
                {
                    mv.Emit(OpCodes.Ldstr, "Property not readable: " + targetType.FullName + "." + propertyName);
                    mv.ThrowException(typeof(NotSupportedException));
                }
                else
                {
                    Type owner = r_get.DeclaringType;
                    mv.Emit(OpCodes.Ldarg_1);
                    mv.Emit(OpCodes.Castclass, owner);
                    mv.Emit(OpCodes.Callvirt, r_get);
                    if (r_get.ReturnType.IsValueType)
                    {
                        mv.Emit(OpCodes.Box, r_get.ReturnType);
                    }
                }
                mv.Emit(OpCodes.Ret);
            }
            {
                MethodAttributes access = MethodAttributes.Public | MethodAttributes.Virtual | MethodAttributes.HideBySig;
                access |= MethodAttributes.ReuseSlot;
                access &= ~MethodAttributes.VtableLayoutMask;

                ILGenerator mv = cw.DefineMethod("SetValue", access, CallingConventions.HasThis, typeof(void), new Type[] { typeof(Object), typeof(Object) }).GetILGenerator();
                
                if (r_set == null)
                {
                    mv.Emit(OpCodes.Ldstr, "Property not writable: " + targetType.FullName + "." + propertyName);
                    mv.ThrowException(typeof(NotSupportedException));
                }
                else
                {
                    Type owner = r_get.DeclaringType;
                    mv.Emit(OpCodes.Ldarg_1);
                    mv.Emit(OpCodes.Castclass, owner);
                    mv.Emit(OpCodes.Ldarg_2);
                    Type paramType = r_set.GetParameters()[0].ParameterType;
                    if (!objType.Equals(paramType))
                    {
                        if (paramType.IsValueType)
                        {
                            mv.Emit(OpCodes.Unbox_Any, paramType);
                        }
                        else
                        {
                            mv.Emit(OpCodes.Castclass, paramType);
                        }
                    }
                    mv.Emit(OpCodes.Callvirt, r_set);
                }
                mv.Emit(OpCodes.Ret);
            }
            return loader.GetType(accessClassName, cw);
        }

        protected Type CreateConstructorType(AccessorClassLoader loader, String constructorClassName, Type delegateType, Type targetType)
        {
            if (Log.DebugEnabled)
            {
                Log.Debug("Creating fast constructor handle for " + targetType.FullName);
            }
            Type delegateTypeHandle = delegateType;
            Type objType = typeof(Object);
            Type superType;

            TypeBuilder cw;
            if (delegateType.IsInterface)
            {
                superType = objType;
                cw = loader.CreateNewType(TypeAttributes.Public, constructorClassName, superType, new Type[] { delegateTypeHandle });
            }
            else
            {
                superType = delegateTypeHandle;
                cw = loader.CreateNewType(TypeAttributes.Public, constructorClassName, superType, Type.EmptyTypes);
            }
            {
                ConstructorInfo baseConstructor = superType.GetConstructor(Type.EmptyTypes);
                ILGenerator mv = cw.DefineConstructor(MethodAttributes.Public, CallingConventions.HasThis, Type.EmptyTypes).GetILGenerator();
                mv.Emit(OpCodes.Ldarg_0);
                mv.Emit(OpCodes.Call, baseConstructor);
                mv.Emit(OpCodes.Ret);
            }
            MethodInfo[] r_methods = delegateType.GetMethods();

            ConstructorInfo[] constructors = targetType.GetConstructors();
            foreach (ConstructorInfo constructor in constructors)
            {
                ParameterInfo[] constructorParams = constructor.GetParameters();
                MethodInfo r_selectedMethod = null;
                for (int a = r_methods.Length; a-- > 0; )
                {
                    MethodInfo r_method = r_methods[a];
                    if (r_method == null)
                    {
                        // already handled
                        continue;
                    }
                    if (!delegateType.IsInterface && !r_method.IsAbstract)
                    {
                        // only handle abstract methods
                        r_methods[a] = null;
                        continue;
                    }
                    ParameterInfo[] methodParams = r_method.GetParameters();
                    if (constructorParams.Length != methodParams.Length)
                    {
                        // no match
                        continue;
                    }
                    bool paramsEqual = true;
                    for (int b = constructorParams.Length; b-- > 0; )
                    {
                        if (!constructorParams[b].ParameterType.Equals(methodParams[b].ParameterType))
                        {
                            paramsEqual = false;
                            break;
                        }
                    }
                    if (!paramsEqual)
                    {
                        // no match
                        continue;
                    }
                    r_methods[a] = null;
                    r_selectedMethod = r_method;
                    break;
                }
                if (r_selectedMethod == null)
                {
                    // no delegate method found to invoke constructor
                    continue;
                }
                MethodAttributes access = MethodAttributes.Public | MethodAttributes.Virtual | MethodAttributes.HideBySig;
                access |= MethodAttributes.ReuseSlot;
                access &= ~MethodAttributes.VtableLayoutMask;

                Type[] paramTypes = TypeUtil.GetClassesToTypesNative(r_selectedMethod.GetParameters());
                MethodInfo method = r_selectedMethod;
                ILGenerator mv = cw.DefineMethod(method.Name, access, CallingConventions.HasThis, method.ReturnType, paramTypes).GetILGenerator();
                for (int b = 0, sizeB = paramTypes.Length; b < sizeB; b++)
                {
                    mv.Emit(OpCodes.Ldarg, b + 1);
                }
                mv.Emit(OpCodes.Newobj, constructor);
                mv.Emit(OpCodes.Ret);
            }
            foreach (MethodInfo r_method in r_methods)
            {
                if (r_method != null)
                {
                    throw new ArgumentException("No matching constructor found on " + targetType.FullName + " to map on delegate method "
                            + r_method.ToString());
                }
            }
            return loader.GetType(constructorClassName, cw);
        }
    }
}