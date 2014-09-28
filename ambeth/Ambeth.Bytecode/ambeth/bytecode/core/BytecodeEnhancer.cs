using System;
using System.Collections.Generic;
using System.Text;
using De.Osthus.Ambeth.Bytecode.Behavior;
using De.Osthus.Ambeth.Bytecode.Visitor;
using De.Osthus.Ambeth.Collections;
using De.Osthus.Ambeth.Exceptions;
using De.Osthus.Ambeth.Ioc;
using De.Osthus.Ambeth.Ioc.Extendable;
using De.Osthus.Ambeth.Log;
using De.Osthus.Ambeth.Proxy;
using De.Osthus.Ambeth.Util;
using System.Reflection;
using De.Osthus.Ambeth.Config;
using De.Osthus.Ambeth.Bytecode.Config;
using System.IO;

namespace De.Osthus.Ambeth.Bytecode.Core
{
    public class BytecodeEnhancer : IBytecodeEnhancer, IBytecodeBehaviorExtendable
    {
        public class ValueType : SmartCopyMap<IEnhancementHint, WeakReference>
        {
            private volatile int changeCount;

            public void AddChangeCount()
            {
                changeCount++;
            }

            public int ChangeCount
            {
                get
                {
                    return changeCount;
                }
            }
        }

        [LogInstance]
        public ILogger Log { private get; set; }

        public IServiceContext BeanContext { protected get; set; }

        public IBytecodeClassLoader BytecodeClassLoader { protected get; set; }

        [Property(BytecodeConfigurationConstants.EnhancementTraceDirectory, Mandatory = false)]
        public String TraceDir { protected get; set; }

        protected readonly WeakDictionary<Type, ValueType> typeToExtendedType = new WeakDictionary<Type, ValueType>();

        protected readonly WeakDictionary<Type, WeakReference> extendedTypeToType = new WeakDictionary<Type, WeakReference>();

        protected readonly IdentityHashSet<Object> hardRefToTypes = new IdentityHashSet<Object>();

        protected readonly CHashSet<Type> supportedEnhancements = new CHashSet<Type>(0.5f);

        protected readonly Object writeLock = new Object();

        protected readonly IExtendableContainer<IBytecodeBehavior> bytecodeBehaviorExtensions = new ExtendableContainer<IBytecodeBehavior>("bytecodeBehavior");

        public BytecodeEnhancer()
        {
            //extendedTypeToType.setAutoCleanupReference(true);
        }

        public bool SupportsEnhancement(Type enhancementType)
        {
            lock (writeLock)
            {
                return supportedEnhancements.Contains(enhancementType);
            }
        }

        public bool IsEnhancedType(Type entityType)
        {
            return typeof(IEnhancedType).IsAssignableFrom(entityType);
        }

        public Type GetBaseType(Type enhancedType)
        {
            if (!IsEnhancedType(enhancedType))
            {
                return null;
            }
            lock (writeLock)
            {
                WeakReference typeR = DictionaryExtension.ValueOrDefault(extendedTypeToType, enhancedType);
                if (typeR == null)
                {
                    throw new Exception("Must never happen");
                }
                Type type = (Type)typeR.Target;
                if (type == null)
                {
                    throw new Exception("Must never happen");
                }
                return type;
            }
        }

        protected Type GetEnhancedTypeIntern(Type entityType, IEnhancementHint enhancementHint)
        {
            lock (writeLock)
            {
                WeakReference existingBaseTypeR = DictionaryExtension.ValueOrDefault(extendedTypeToType, entityType);
		        if (existingBaseTypeR != null)
		        {
			        Type existingBaseType = (Type) existingBaseTypeR.Target;
			        if (existingBaseType != null)
			        {
				        // there is already an enhancement of the given baseType. Now we check if the existing enhancement is made with the same enhancementHint
				        ValueType valueType2 = DictionaryExtension.ValueOrDefault(typeToExtendedType, existingBaseType);
				        if (valueType2 != null && valueType2.ContainsKey(enhancementHint))
				        {
					        // do nothing: the given entity is already the result of the enhancement of the existingBaseType with the given enhancementHint
					        // it is not possible to enhance the same two times
					        return entityType;
				        }
			        }
		        }
                ValueType valueType = DictionaryExtension.ValueOrDefault(typeToExtendedType, entityType);
                if (valueType == null)
                {
                    return null;
                }
                WeakReference extendedTypeR = valueType.Get(enhancementHint);
                if (extendedTypeR == null)
                {
                    return null;
                }
                return (Type)extendedTypeR.Target;
            }
        }

        public Type GetEnhancedType(Type typeToEnhance, IEnhancementHint hint)
        {
            ITargetNameEnhancementHint targetNameHint = hint.Unwrap<ITargetNameEnhancementHint>();
            if (targetNameHint == null && hint is ITargetNameEnhancementHint)
		    {
			    targetNameHint = (ITargetNameEnhancementHint) hint;
		    }
            String newTypeNamePrefix = typeToEnhance.FullName;
            if (targetNameHint != null)
            {
                newTypeNamePrefix = targetNameHint.GetTargetName(typeToEnhance);
            }
            return GetEnhancedType(typeToEnhance, newTypeNamePrefix, hint);
        }

        public Type GetEnhancedType(Type typeToEnhance, String newTypeNamePrefix, IEnhancementHint hint)
        {
            Type extendedType = GetEnhancedTypeIntern(typeToEnhance, hint);
            if (extendedType != null)
            {
                return extendedType;
            }
            lock (writeLock)
            {
                // Concurrent thread may have been faster
                extendedType = GetEnhancedTypeIntern(typeToEnhance, hint);
                if (extendedType != null)
                {
                    return extendedType;
                }
                if (Log.InfoEnabled)
                {
                    Log.Info("Enhancing " + typeToEnhance + " with hint: " + hint);
                }
                ValueType valueType = DictionaryExtension.ValueOrDefault(typeToExtendedType, typeToEnhance);
                if (valueType == null)
                {
                    valueType = new ValueType();
                    typeToExtendedType.Add(typeToEnhance, valueType);
                }
                else
                {
                    valueType.AddChangeCount();
                    newTypeNamePrefix += "_O" + valueType.ChangeCount;
                }

                List<IBytecodeBehavior> pendingBehaviors = new List<IBytecodeBehavior>();

                IBytecodeBehavior[] extensions = bytecodeBehaviorExtensions.GetExtensions();
                pendingBehaviors.AddRange(extensions);

                Type enhancedType;
                if (pendingBehaviors.Count > 0)
                {
                    enhancedType = EnhanceTypeIntern(typeToEnhance, newTypeNamePrefix, pendingBehaviors, hint);
                }
                else
                {
                    enhancedType = typeToEnhance;
                }
                WeakReference entityTypeR = typeToExtendedType.GetWeakReferenceEntry(typeToEnhance);
			    if (entityTypeR == null)
			    {
				    throw new Exception("Must never happen");
			    }
                hardRefToTypes.Add(enhancedType);
                hardRefToTypes.Add(typeToEnhance);

                if (TraceDir != null)
                {
                    String printableBytecode = BytecodeClassLoader.ToPrintableBytecode(enhancedType);

                    String outputFileDir = TraceDir + "/" + GetType().FullName;
                    FileStream outputFile = new FileStream(outputFileDir + "/" + enhancedType.FullName + ".txt", FileMode.Create, FileAccess.Write, FileShare.Read);
                    try
                    {
                        StreamWriter fw = new StreamWriter(outputFile);
                        try
                        {
                            fw.Write(printableBytecode);
                        }
                        finally
                        {
                            fw.Close();
                        }
                    }
                    finally
                    {
                        outputFile.Close();
                    }
                }
                else if (Log.DebugEnabled)
                {
                    // note that this intentionally will only be logged to the console if the traceDir is NOT specified already
                    Log.Debug(BytecodeClassLoader.ToPrintableBytecode(enhancedType));
                }
                try
                {
                    CheckEnhancedTypeConsistency(enhancedType);
                }
                catch (Exception e)
                {
                    if (Log.ErrorEnabled)
                    {
                        Log.Error(BytecodeClassLoader.ToPrintableBytecode(enhancedType), e);
                    }
                    BytecodeClassLoader.Save();
                    throw;
                }
                WeakReference enhancedEntityTypeR = new WeakReference(enhancedType);
                valueType.Put(hint, enhancedEntityTypeR);
                extendedTypeToType.Add(enhancedType, entityTypeR);
                if (Log.InfoEnabled)
                {
                    Log.Info("Enhancement finished successfully with type: " + enhancedType);
                }
                return enhancedType;
            }
        }

        protected void CheckEnhancedTypeConsistency(Type type)
	    {
		    IdentityHashSet<MethodInfo> allMethods = new IdentityHashSet<MethodInfo>();
		    foreach (Type interf in type.GetInterfaces())
		    {
			    allMethods.AddAll(interf.GetMethods());
		    }
		    Type currType = type;
		    while (currType != typeof(Object) && currType != null)
		    {
                allMethods.AddAll(currType.GetMethods(BindingFlags.Public | BindingFlags.NonPublic | BindingFlags.Instance | BindingFlags.Static | BindingFlags.DeclaredOnly));
			    currType = currType.BaseType;
		    }
		    if (allMethods.Count == 0)
		    {
			    throw new Exception("Type invalid (not a single method): " + type);
		    }
            if (type.GetConstructors(BindingFlags.Instance | BindingFlags.Public | BindingFlags.NonPublic | BindingFlags.DeclaredOnly).Length == 0)
		    {
			    throw new Exception("Type invalid (not a single constructor): " + type);
		    }
		    if (!type.IsAbstract)
		    {
			    foreach (MethodInfo method in allMethods)
			    {
				    MethodInfo method2 = ReflectUtil.GetDeclaredMethod(true, type, method.ReturnType, method.Name, TypeUtil.GetParameterTypesToTypes(method.GetParameters()));
				    if (method2 == null || method2.IsAbstract)
				    {
					    throw new Exception("Type is not abstract but has at least one abstract method: " + method);
				    }
			    }
		    }
		    Type[] interfaces = type.GetInterfaces();
		    foreach (Type interf in interfaces)
		    {
			    MethodInfo[] interfaceMethods = ReflectUtil.GetDeclaredMethods(interf);
			    foreach (MethodInfo interfaceMethod in interfaceMethods)
			    {
				    try
				    {
                        if (type.GetMethod(interfaceMethod.Name, TypeUtil.GetParameterTypesToTypes(interfaceMethod.GetParameters())) == null)
                        {
                            throw new Exception("Type is not abstract but has at least one abstract method: " + interfaceMethod);
                        }
				    }
				    catch (Exception e)
				    {
					    throw new Exception("Type is not abstract but has at least one abstract method: " + interfaceMethod, e);
				    }
			    }
		    }
	    }

        protected Type EnhanceTypeIntern(Type originalType, String newTypeNamePrefix, IList<IBytecodeBehavior> pendingBehaviors,
                IEnhancementHint hint)
        {
            if (pendingBehaviors.Count == 0)
            {
                return originalType;
            }
            WeakReference originalTypeR = typeToExtendedType.GetWeakReferenceEntry(originalType);
            if (originalTypeR == null)
		    {
			    throw new Exception("Must never happen");
		    }
            newTypeNamePrefix = newTypeNamePrefix.Replace('.', '/');
            StringBuilder sw = new StringBuilder();
            try
            {
                Type currentType = originalType;
                if (currentType.IsInterface)
                {
                    currentType = typeof(Object);
                }
                for (int a = 0, size = pendingBehaviors.Count; a < size; a++)
                {
                    Type newCurrentType = pendingBehaviors[a].GetTypeToExtendFrom(originalType, currentType, hint);
                    if (newCurrentType != null)
                    {
                        currentType = newCurrentType;
                    }
                }
                int iterationCount = 0;

                List<BytecodeBehaviorState> pendingStatesToPostProcess = new List<BytecodeBehaviorState>();
                Type currentContent = currentType; //BytecodeClassLoader.ReadTypeAsBinary(currentType);
                while (pendingBehaviors.Count > 0)
                {
                    iterationCount++;

                    NewType newTypeHandle = NewType.GetObjectType(newTypeNamePrefix + "$A" + iterationCount);

                    IBytecodeBehavior[] currentPendingBehaviors = ListUtil.ToArray(pendingBehaviors);
                    pendingBehaviors.Clear();

                    if (currentPendingBehaviors.Length > 0 && Log.DebugEnabled)
                    {
                        Log.Debug("Applying behaviors on " + newTypeHandle.ClassName + ": " + Arrays.ToString(currentPendingBehaviors));
                    }
                    BytecodeEnhancer This = this;
                    Type fCurrentContent = currentContent;

                    BytecodeBehaviorState acquiredState = null;
                    Type newContent = BytecodeBehaviorState.SetState(originalType, currentType, newTypeHandle, BeanContext, hint,
                            delegate()
                            {
                                acquiredState = (BytecodeBehaviorState)BytecodeBehaviorState.State;
                                return This.ExecutePendingBehaviors(fCurrentContent, sw, currentPendingBehaviors, pendingBehaviors);
                            });
                    if (newContent == null)
                    {
                        if (pendingBehaviors.Count > 0)
                        {
                            // "fix" the iterationCount to have a consistent class name hierarchy
                            iterationCount--;
                            continue;
                        }
                        return currentType;
                    }
                    Type newType = newContent;// BytecodeClassLoader.LoadClass(newTypeHandle.InternalName, newContent);
                    extendedTypeToType.Add(newType, originalTypeR);
                    pendingStatesToPostProcess.Add(acquiredState);
                    currentContent = newContent;
                    currentType = newType;
                }
                for (int a = 0, size = pendingStatesToPostProcess.Count; a < size; a++)
                {
                    pendingStatesToPostProcess[a].PostProcessCreatedType(currentType);
                }
                return currentType;
            }
            catch (Exception e)
            {
                BytecodeClassLoader.Save();
                String classByteCode = sw.ToString();
                if (classByteCode.Length > 0)
                {
                    throw RuntimeExceptionUtil.Mask(e, "Bytecode:\n" + classByteCode);
                }
                throw;
            }
        }

        public Type ExecutePendingBehaviors(Type currentContent, StringBuilder sw, IBytecodeBehavior[] pendingBehaviors,
                IList<IBytecodeBehavior> cascadePendingBehaviors)
        {
            IBytecodeBehaviorState state = BytecodeBehaviorState.State;
            Type content = BytecodeClassLoader.BuildTypeFromParent(state.NewType.ClassName, currentContent, sw, delegate(IClassVisitor cv)
                {
                    IBytecodeBehavior[] currPendingBehaviors = pendingBehaviors;
                    for (int a = 0; a < currPendingBehaviors.Length; a++)
                    {
                        List<IBytecodeBehavior> remainingPendingBehaviors = new List<IBytecodeBehavior>();
                        for (int b = a + 1, sizeB = currPendingBehaviors.Length; b < sizeB; b++)
                        {
                            remainingPendingBehaviors.Add(currPendingBehaviors[b]);
                        }
                        IClassVisitor newCv = currPendingBehaviors[a].Extend(cv, state, remainingPendingBehaviors, cascadePendingBehaviors);
                        currPendingBehaviors = remainingPendingBehaviors.ToArray();
                        a = -1;
                        if (newCv != null)
                        {
                            cv = newCv;
                        }
                    }
                    return cv;
                });
            return content;
        }

        public void RegisterBytecodeBehavior(IBytecodeBehavior bytecodeBehavior)
        {
            bytecodeBehaviorExtensions.Register(bytecodeBehavior);
            RefreshSupportedEnhancements();
        }

        public void UnregisterBytecodeBehavior(IBytecodeBehavior bytecodeBehavior)
        {
            bytecodeBehaviorExtensions.Unregister(bytecodeBehavior);
            RefreshSupportedEnhancements();
        }

        protected void RefreshSupportedEnhancements()
	    {
		    lock (writeLock)
		    {
			    supportedEnhancements.Clear();
			    foreach (IBytecodeBehavior bytecodeBehavior in bytecodeBehaviorExtensions.GetExtensions())
			    {
				    supportedEnhancements.AddAll(bytecodeBehavior.GetEnhancements());
			    }
		    }
	    }
    }
}