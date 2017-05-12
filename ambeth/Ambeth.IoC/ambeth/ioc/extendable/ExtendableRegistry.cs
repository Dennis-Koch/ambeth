using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Reflection;
using De.Osthus.Ambeth.Collections;
using De.Osthus.Ambeth.Ioc.Exceptions;
using De.Osthus.Ambeth.Log;
using De.Osthus.Ambeth.Util;

namespace De.Osthus.Ambeth.Ioc.Extendable
{
    public class ExtendableRegistry : SmartCopyMap<ExtendableRegistry.KeyItem, MethodInfo[]>, IExtendableRegistry, IInitializingBean
    {
        public class KeyItem
        {
            public KeyItem()
            {
                // Intended blank
            }

            public KeyItem(Type extendableInterface, String propertyName, int argumentCount)
            {
                ExtendableInterface = extendableInterface;
                PropertyName = propertyName;
                ArgumentCount = argumentCount;
            }

            public Type ExtendableInterface { get; set; }

            public String PropertyName { get; set; }

            public int ArgumentCount { get; set; }

            public override int GetHashCode()
            {
                int hashCode = ExtendableInterface.GetHashCode() ^ ArgumentCount;
                if (PropertyName != null)
                {
                    hashCode ^= PropertyName.GetHashCode();
                }
                return hashCode;
            }

            public override bool Equals(Object obj)
            {
                if (Object.ReferenceEquals(obj, this))
                {
                    return true;
                }
                if (!(obj is KeyItem))
                {
                    return false;
                }
                KeyItem other = (KeyItem)obj;
                return Object.Equals(ExtendableInterface, other.ExtendableInterface) && ArgumentCount == other.ArgumentCount
                    && Object.Equals(PropertyName, other.PropertyName);
            }
        }

        public class EventKeyItem
        {
            public EventKeyItem(Type type, String eventName)
            {
                this.type = type;
                this.eventName = eventName;
            }

            protected Type type;

            protected String eventName;

            public override int GetHashCode()
            {
                return type.GetHashCode() ^ (eventName != null ? eventName.GetHashCode() : 1);
            }

            public override bool Equals(Object obj)
            {
                if (Object.ReferenceEquals(obj, this))
                {
                    return true;
                }
                if (!(obj is EventKeyItem))
                {
                    return false;
                }
                EventKeyItem other = (EventKeyItem)obj;
                return Object.Equals(type, other.type) && Object.Equals(eventName, other.eventName);
            }
        }

        [ThreadStatic]
        protected static KeyItem keyItem;

        public static KeyItem KeyItemProp
        {
            get
            {
                if (keyItem == null)
                {
                    keyItem = new KeyItem();
                }
                return keyItem;
            }
        }

        [LogInstance]
        public ILogger Log { private get; set; }

        protected readonly IDictionary<Object, MethodInfo[]> typeToAddRemoveMethodsMapOld = new Dictionary<Object, MethodInfo[]>();

        public void AfterPropertiesSet()
        {
            // Intended blank
        }

        protected Object[] CreateArgumentArray(Object[] args)
        {
            Object[] realArguments = new Object[args.Length + 1];
            Array.Copy(args, 0, realArguments, 1, args.Length);
            return realArguments;
        }

        [Obsolete]
        protected Object[] createArgumentArray(Object[] args)
        {
            return CreateArgumentArray(args);
        }

        public void CleanupThreadLocal()
        {
            // Intentionally cleanup static field
            keyItem = null;
        }

        public MethodInfo[] GetAddRemoveMethods(Type type, String eventName, Object[] arguments, out Object[] linkArguments)
        {
            ParamChecker.AssertParamNotNull(type, "type");
            ParamChecker.AssertParamNotNull(eventName, "eventName");
            Type[] argumentTypes = ArgsToTypesAndLinkArgs(arguments, out linkArguments);
            return GetAddRemoveMethodsIntern(type, eventName, argumentTypes);
        }

        public MethodInfo[] GetAddRemoveMethods(Type extendableInterface, Object[] arguments, out Object[] linkArguments)
        {
            ParamChecker.AssertParamNotNull(extendableInterface, "extendableInterface");
            Type[] argumentTypes = ArgsToTypesAndLinkArgs(arguments, out linkArguments);
            return GetAddRemoveMethodsIntern(extendableInterface, null, argumentTypes);
        }

        public MethodInfo[] GetAddRemoveMethods(Type extendableInterface, Type[] argumentTypes)
        {
            ParamChecker.AssertParamNotNull(extendableInterface, "extendableInterface");
            return GetAddRemoveMethodsIntern(extendableInterface, null, argumentTypes);
        }

        protected Type[] ArgsToTypesAndLinkArgs(Object[] arguments, out Object[] linkArguments)
        {
            if (arguments == null)
            {
                arguments = EmptyList.EmptyArray<Object>();
            }
            Type[] argumentTypes = new Type[arguments.Length];
            for (int i = 0, size = arguments.Length; i < size; i++)
            {
                Object argument = arguments[i];
                if (argument != null)
                {
                    argumentTypes[i] = argument.GetType();
                }
            }
            linkArguments = CreateArgumentArray(arguments);
            return argumentTypes;
        }

        protected MethodInfo[] GetAddRemoveMethodsIntern(Type extendableInterface, String propertyName, Type[] argumentTypes)
        {
            int expectedParamCount = argumentTypes.Length + 1;

            KeyItem keyItem = KeyItemProp;
            keyItem.ExtendableInterface = extendableInterface;
            keyItem.PropertyName = propertyName;
            keyItem.ArgumentCount = expectedParamCount;

            MethodInfo[] addRemoveMethods = Get(keyItem);
            if (addRemoveMethods != null)
            {
                return addRemoveMethods;
            }
            keyItem = new KeyItem(extendableInterface, propertyName, expectedParamCount);
            Object writeLock = GetWriteLock();
            lock (writeLock)
            {
                addRemoveMethods = Get(keyItem);
                if (addRemoveMethods != null)
                {
                    // Concurrent thread might have been faster
                    return addRemoveMethods;
                }
                String registerMethodName1 = null, registerMethodName2 = null;
                String unregisterMethodName1 = null, unregisterMethodName2 = null;
                if (propertyName != null)
                {
                    registerMethodName1 = StringBuilderUtil.Concat("register", propertyName).ToLowerInvariant();
                    registerMethodName2 = StringBuilderUtil.Concat("add_", propertyName).ToLowerInvariant();
                    unregisterMethodName1 = StringBuilderUtil.Concat("remove_", propertyName).ToLowerInvariant();
                    unregisterMethodName2 = StringBuilderUtil.Concat("unregister", propertyName).ToLowerInvariant();
                }
                MethodInfo[] methods = extendableInterface.GetMethods();
                MethodInfo addMethod = null, removeMethod = null;
                foreach (MethodInfo method in methods)
                {
                    ParameterInfo[] paramInfos = method.GetParameters();
                    if (paramInfos.Length != expectedParamCount)
                    {
                        continue;
                    }
                    String methodName = method.Name.ToLower();
                    if (propertyName != null && !methodName.Equals(registerMethodName1) && !methodName.Equals(registerMethodName2)
                        && !methodName.Equals(unregisterMethodName1) && !methodName.Equals(unregisterMethodName2))
                    {
                        continue;
                    }
                    if (methodName.StartsWith("register") || methodName.StartsWith("add"))
                    {
                        bool match = true;
                        for (int a = paramInfos.Length; a-- > 1; )
                        {
                            ParameterInfo paramInfo = paramInfos[a];
                            Type argumentType = argumentTypes[a - 1];
                            if (argumentType != null && !UnboxType(paramInfo.ParameterType).IsAssignableFrom(UnboxType(argumentType)))
                            {
                                match = false;
                                break;
                            }
                        }
                        if (match)
                        {
                            addMethod = method;
                        }
                    }
                    else if (methodName.StartsWith("unregister") || methodName.StartsWith("remove"))
                    {
                        bool match = true;
                        for (int a = paramInfos.Length; a-- > 1; )
                        {
                            ParameterInfo paramInfo = paramInfos[a];
                            Type argumentType = argumentTypes[a - 1];
                            if (argumentType != null && !UnboxType(paramInfo.ParameterType).IsAssignableFrom(UnboxType(argumentType)))
                            {
                                match = false;
                                break;
                            }
                        }
                        if (match)
                        {
                            removeMethod = method;
                        }
                    }
                }
                if (addMethod == null || removeMethod == null)
                {
                    throw new ExtendableException("No extendable methods pair like 'add/remove' or 'register/unregister' found on interface "
                            + extendableInterface.FullName + " to add extension signature with exactly " + expectedParamCount + " argument(s)");
                }
                addRemoveMethods = new MethodInfo[] { addMethod, removeMethod };
                Put(keyItem, addRemoveMethods);
                return addRemoveMethods;
            }
        }

        protected Type UnboxType(Type type)
        {
            Type unwrappedType = ImmutableTypeSet.GetUnwrappedType(type);
		    if (unwrappedType != null)
		    {
			    return unwrappedType;
		    }
		    return type;
        }

        public MethodInfo[] GetAddRemoveMethods(Type extendableInterface)
        {
            Object writeLock = GetWriteLock();
            lock (writeLock)
            {
                MethodInfo[] methods = extendableInterface.GetMethods();
                MethodInfo addMethod = null, removeMethod = null;
                ParameterInfo[] foundParamInfos = null;
                foreach (MethodInfo method in methods)
                {
                    ParameterInfo[] paramInfos = method.GetParameters();
                    String methodName = method.Name.ToLower();
                    if (methodName.StartsWith("register") || methodName.StartsWith("add"))
                    {
                        if (addMethod != null)
                        {
                            throw new ExtendableException("No unique extendable methods pair like 'add/remove' or 'register/unregister' found on interface '"
                                    + extendableInterface.FullName + "' to add extension signature");
                        }
                        if (foundParamInfos == null)
                        {
                            foundParamInfos = paramInfos;
                        }
                        else
                        {
                            CompareParamInfos(paramInfos, foundParamInfos, extendableInterface);
                        }
                        addMethod = method;
                    }
                    else if (methodName.StartsWith("unregister") || methodName.StartsWith("remove"))
                    {
                        if (removeMethod != null)
                        {
                            throw new ExtendableException("No unique extendable methods pair like 'add/remove' or 'register/unregister' found on interface '"
                                    + extendableInterface.FullName + "' to add extension signature");
                        }
                        if (foundParamInfos == null)
                        {
                            foundParamInfos = paramInfos;
                        }
                        else
                        {
                            CompareParamInfos(paramInfos, foundParamInfos, extendableInterface);
                        }
                        removeMethod = method;
                    }
                }

                if (addMethod == null || removeMethod == null)
                {
                    throw new ExtendableException("No unique extendable methods pair like 'add/remove' or 'register/unregister' found on interface '"
                            + extendableInterface.FullName + "' to add extension signature");
                }
                KeyItem keyItem = new KeyItem(extendableInterface, null, addMethod.GetParameters().Length);
                MethodInfo[] addRemoveMethods = new MethodInfo[] { addMethod, removeMethod };

                Put(keyItem, addRemoveMethods);

                return addRemoveMethods;
            }
        }

        protected void CompareParamInfos(ParameterInfo[] paramInfos, ParameterInfo[] foundParamInfos, Type extendableInterface)
        {
            for (int a = 0, size = paramInfos.Length; a < size; a++)
            {
                ParameterInfo paramInfo = paramInfos[a];
                ParameterInfo foundParamInfo = foundParamInfos[a];
                if (!paramInfo.ParameterType.Equals(foundParamInfo.ParameterType))
                {
                    throw new ExtendableException("No unique extendable methods pair like 'add/remove' or 'register/unregister' found on interface '"
                            + extendableInterface.FullName + "' to add extension signature");
                }
            }
        }

        [Obsolete]
        public MethodInfo[] getAddRemoveMethods(Type extendableInterface, Object[] arguments, out Object[] linkArguments)
        {
            ParamChecker.AssertParamNotNull(extendableInterface, "extendableInterface");

            if (arguments == null)
            {
                // This is expected to be an .NET event link
                linkArguments = new Object[1];
                return getAddRemoveMethodsForEvent(extendableInterface);
            }
            linkArguments = createArgumentArray(arguments);

            int expectedParamCount = arguments.Length + 1;

            MethodInfo[] addRemoveMethods;
            KeyItem keyItem = new KeyItem(extendableInterface, null, expectedParamCount);
            lock (typeToAddRemoveMethodsMapOld)
            {
                addRemoveMethods = DictionaryExtension.ValueOrDefault(typeToAddRemoveMethodsMapOld, keyItem);
                if (addRemoveMethods != null)
                {
                    return addRemoveMethods;
                }
            }
            MethodInfo[] methods = extendableInterface.GetMethods();
            MethodInfo addMethod = null, removeMethod = null;
            foreach (MethodInfo method in methods)
            {
                ParameterInfo[] paramInfos = method.GetParameters();
                if (paramInfos.Length != expectedParamCount)
                {
                    continue;
                }
                String methodName = method.Name.ToLower();
                if (methodName.StartsWith("register") || methodName.StartsWith("add"))
                {
                    bool match = true;
                    for (int a = paramInfos.Length; a-- > 1; )
                    {
                        ParameterInfo paramInfo = paramInfos[a];
                        Object argument = arguments[a - 1];
                        if (argument != null && !paramInfo.ParameterType.IsAssignableFrom(argument.GetType()))
                        {
                            match = false;
                            break;
                        }
                    }
                    if (match)
                    {
                        addMethod = method;
                    }
                }
                else if (methodName.StartsWith("unregister") || methodName.StartsWith("remove"))
                {
                    bool match = true;
                    for (int a = paramInfos.Length; a-- > 1; )
                    {
                        ParameterInfo paramInfo = paramInfos[a];
                        Object argument = arguments[a - 1];
                        if (argument != null && !paramInfo.ParameterType.IsAssignableFrom(argument.GetType()))
                        {
                            match = false;
                            break;
                        }
                    }
                    if (match)
                    {
                        removeMethod = method;
                    }
                }
            }
            if (addMethod == null || removeMethod == null)
            {
                throw new ExtendableException("No extendable methods pair like 'add/remove' or 'register/unregister' found on interface "
                        + extendableInterface.Name + " to add extension signature with exactly " + expectedParamCount + " argument(s)");
            }
            addRemoveMethods = new MethodInfo[] { addMethod, removeMethod };

            lock (typeToAddRemoveMethodsMapOld)
            {
                if (!typeToAddRemoveMethodsMapOld.ContainsKey(keyItem))
                {
                    typeToAddRemoveMethodsMapOld.Add(keyItem, addRemoveMethods);
                }
            }
            return addRemoveMethods;
        }

        [Obsolete]
        protected MethodInfo[] getAddRemoveMethodsForEvent(Type targetType)
        {
            ParamChecker.AssertParamNotNull(targetType, "targetType");

            MethodInfo[] addRemoveMethods;
            EventKeyItem keyItem = new EventKeyItem(targetType, null);
            lock (typeToAddRemoveMethodsMapOld)
            {
                addRemoveMethods = DictionaryExtension.ValueOrDefault(typeToAddRemoveMethodsMapOld, keyItem);
                if (addRemoveMethods != null)
                {
                    return addRemoveMethods;
                }
            }

            MethodInfo[] methods = targetType.GetMethods();

            MethodInfo addMethod = null, removeMethod = null;
            foreach (MethodInfo method in methods)
            {
                if (!method.IsSpecialName)
                {
                    // Look for special methods autogenerated by the 'event' keyword
                    continue;
                }
                ParameterInfo[] paramInfos = method.GetParameters();
                if (paramInfos.Length != 1)
                {
                    // The autogenerated methods by the 'event' keyword always have exactly 1 argument
                    continue;
                }
                String methodName = method.Name;
                if (methodName.StartsWith("add_"))
                {
                    if (addMethod != null)
                    {
                        throw new ExtendableException("Autogenerated event methods not uniquely resolvable. Maybe there are more than exactly 1 members like 'public event <EventType> <EventName>;' on type " + targetType.FullName + "?");
                    }
                    addMethod = method;
                }
                else if (methodName.StartsWith("remove_"))
                {
                    if (removeMethod != null)
                    {
                        throw new ExtendableException("Autogenerated event methods not uniquely resolvable. Maybe there are more than exactly 1 members like 'public event <EventType> <EventName>;' on type " + targetType.FullName + "?");
                    }
                    removeMethod = method;
                }
            }
            if (addMethod == null || removeMethod == null)
            {
                throw new ExtendableException("No autogenerated event methods found. Looked for a member like 'public event <EventType> <EventName>;' on type " + targetType.FullName);
            }
            addRemoveMethods = new MethodInfo[] { addMethod, removeMethod };

            lock (typeToAddRemoveMethodsMapOld)
            {
                if (!typeToAddRemoveMethodsMapOld.ContainsKey(keyItem))
                {
                    typeToAddRemoveMethodsMapOld.Add(keyItem, addRemoveMethods);
                }
            }
            return addRemoveMethods;
        }

        [Obsolete]
        public MethodInfo[] getAddRemoveMethodsForEvent(Type targetType, String eventName)
        {
            ParamChecker.AssertParamNotNull(targetType, "targetType");
            ParamChecker.AssertParamNotNull(eventName, "eventName");

            MethodInfo[] addRemoveMethods;
            EventKeyItem keyItem = new EventKeyItem(targetType, eventName);
            lock (typeToAddRemoveMethodsMapOld)
            {
                addRemoveMethods = DictionaryExtension.ValueOrDefault(typeToAddRemoveMethodsMapOld, keyItem);
                if (addRemoveMethods != null)
                {
                    return addRemoveMethods;
                }
            }
            MethodInfo addMethod = targetType.GetMethod("add_" + eventName);
            MethodInfo removeMethod = targetType.GetMethod("remove_" + eventName);

            if (addMethod == null || removeMethod == null)
            {
                throw new ExtendableException("No autogenerated event methods for event field '" + eventName + "' found on type " + targetType.Name
                    + ". Looked for a member like 'public event " + typeof(PropertyChangedEventHandler).Name + " " + eventName + ";");
            }
            addRemoveMethods = new MethodInfo[] { addMethod, removeMethod };

            lock (typeToAddRemoveMethodsMapOld)
            {
                if (!typeToAddRemoveMethodsMapOld.ContainsKey(keyItem))
                {
                    typeToAddRemoveMethodsMapOld.Add(keyItem, addRemoveMethods);
                }
            }
            return addRemoveMethods;
        }
    }
}
