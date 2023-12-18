package com.koch.ambeth.ioc.extendable;

/*-
 * #%L
 * jambeth-ioc
 * %%
 * Copyright (C) 2017 Koch Softwaredevelopment
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 * #L%
 */

import com.koch.ambeth.ioc.IInitializingBean;
import com.koch.ambeth.ioc.exception.ExtendableException;
import com.koch.ambeth.ioc.extendable.ExtendableRegistry.KeyItem;
import com.koch.ambeth.util.IParamHolder;
import com.koch.ambeth.util.ParamChecker;
import com.koch.ambeth.util.ReflectUtil;
import com.koch.ambeth.util.StringBuilderUtil;
import com.koch.ambeth.util.WrapperTypeSet;
import com.koch.ambeth.util.collections.SmartCopyMap;
import com.koch.ambeth.util.objectcollector.IThreadLocalObjectCollector;

import java.lang.reflect.Method;
import java.util.Objects;
import java.util.concurrent.locks.Lock;

public class ExtendableRegistry extends SmartCopyMap<KeyItem, Method[]> implements IExtendableRegistry, IInitializingBean {
    protected static final Class<?>[] emptyArgTypes = new Class<?>[0];

    protected static final Object[] emptyArgs = new Object[0];
    protected IThreadLocalObjectCollector objectCollector;

    @Override
    public void afterPropertiesSet() throws Throwable {
        ParamChecker.assertNotNull(objectCollector, "ObjectCollector");
    }

    public void setObjectCollector(IThreadLocalObjectCollector objectCollector) {
        this.objectCollector = objectCollector;
    }

    protected Object[] createArgumentArray(Object[] args) {
        var realArguments = new Object[args.length + 1];
        System.arraycopy(args, 0, realArguments, 1, args.length);
        return realArguments;
    }

    @Override
    public Method[] getAddRemoveMethods(Class<?> type, String eventName, Object[] arguments, IParamHolder<Object[]> linkArguments) {
        ParamChecker.assertParamNotNull(type, "type");
        ParamChecker.assertParamNotNull(eventName, "eventName");
        var argumentTypes = argsToTypesAndLinkArgs(arguments, linkArguments);
        return getAddRemoveMethodsIntern(type, eventName, argumentTypes);
    }

    @Override
    public Method[] getAddRemoveMethods(Class<?> extendableInterface, Object[] arguments, IParamHolder<Object[]> linkArguments) {
        ParamChecker.assertParamNotNull(extendableInterface, "extendableInterface");
        var argumentTypes = argsToTypesAndLinkArgs(arguments, linkArguments);
        return getAddRemoveMethodsIntern(extendableInterface, null, argumentTypes);
    }

    @Override
    public Method[] getAddRemoveMethods(Class<?> extendableInterface, Class<?>[] argumentTypes) {
        ParamChecker.assertParamNotNull(extendableInterface, "extendableInterface");
        return getAddRemoveMethodsIntern(extendableInterface, null, argumentTypes);
    }

    protected Class<?>[] argsToTypesAndLinkArgs(Object[] arguments, IParamHolder<Object[]> linkArguments) {
        if (arguments == null) {
            arguments = emptyArgs;
        }
        var argumentTypes = new Class<?>[arguments.length];
        for (int i = 0, size = arguments.length; i < size; i++) {
            Object argument = arguments[i];
            if (argument != null) {
                argumentTypes[i] = argument.getClass();
            }
        }
        var argumentArray = createArgumentArray(arguments);
        if (linkArguments != null) {
            linkArguments.setValue(argumentArray);
        }
        return argumentTypes;
    }

    protected Method[] getAddRemoveMethodsIntern(Class<?> extendableInterface, String propertyName, Class<?>[] argumentTypes) {
        if (argumentTypes == null) {
            argumentTypes = emptyArgTypes;
        }
        var expectedParamCount = argumentTypes.length + 1;

        var keyItem = new KeyItem(extendableInterface, propertyName, expectedParamCount);
        var addRemoveMethods = get(keyItem);
        if (addRemoveMethods != null) {
            return addRemoveMethods;
        }
        var writeLock = getWriteLock();
        writeLock.lock();
        try {
            addRemoveMethods = get(keyItem);
            if (addRemoveMethods != null) {
                // Concurrent thread may have been faster
                return addRemoveMethods;
            }
            var tlObjectCollector = objectCollector.getCurrent();
            String registerMethodName1 = null, registerMethodName2 = null;
            String unregisterMethodName1 = null, unregisterMethodName2 = null;
            if (propertyName != null) {
                registerMethodName1 = StringBuilderUtil.concat(tlObjectCollector, "register", propertyName).toLowerCase();
                registerMethodName2 = StringBuilderUtil.concat(tlObjectCollector, "add", propertyName).toLowerCase();
                unregisterMethodName1 = StringBuilderUtil.concat(tlObjectCollector, "unregister", propertyName).toLowerCase();
                unregisterMethodName2 = StringBuilderUtil.concat(tlObjectCollector, "remove", propertyName).toLowerCase();
            }
            var methods = ReflectUtil.getMethods(extendableInterface);
            Method addMethod = null, removeMethod = null;
            for (var method : methods) {
                var paramInfos = method.getParameterTypes();
                if (paramInfos.length != expectedParamCount) {
                    continue;
                }
                var methodName = method.getName().toLowerCase();
                if (propertyName != null && !methodName.equals(registerMethodName1) && !methodName.equals(registerMethodName2) && !methodName.equals(unregisterMethodName1) &&
                        !methodName.equals(unregisterMethodName2)) {
                    continue;
                }
                if (methodName.startsWith("register") || methodName.startsWith("add")) {
                    boolean match = true;
                    for (int a = paramInfos.length; a-- > 1; ) {
                        var paramInfo = paramInfos[a];
                        var argumentType = argumentTypes[a - 1];
                        if (argumentType != null && !unboxType(paramInfo).isAssignableFrom(unboxType(argumentType))) {
                            match = false;
                            break;
                        }
                    }
                    if (match) {
                        addMethod = method;
                    }
                } else if (methodName.startsWith("unregister") || methodName.startsWith("remove")) {
                    var match = true;
                    for (int a = paramInfos.length; a-- > 1; ) {
                        var paramInfo = paramInfos[a];
                        var argumentType = argumentTypes[a - 1];
                        if (argumentType != null && !unboxType(paramInfo).isAssignableFrom(unboxType(argumentType))) {
                            match = false;
                            break;
                        }
                    }
                    if (match) {
                        removeMethod = method;
                    }
                }
            }

            if (addMethod == null || removeMethod == null) {
                throw new ExtendableException(
                        "No extendable methods pair like 'add/remove' or 'register/unregister' found on interface " + extendableInterface.getName() + " to add extension signature with exactly " +
                                expectedParamCount + " argument(s)");
            }
            addRemoveMethods = new Method[] { addMethod, removeMethod };

            put(keyItem, addRemoveMethods);
            return addRemoveMethods;
        } finally {
            writeLock.unlock();
        }
    }

    protected Class<?> unboxType(Class<?> type) {
        Class<?> unwrappedType = WrapperTypeSet.getUnwrappedType(type);
        if (unwrappedType != null) {
            return unwrappedType;
        }
        return type;
    }

    @Override
    public Method[] getAddRemoveMethods(Class<?> extendableInterface) {
        Lock writeLock = getWriteLock();
        writeLock.lock();
        try {
            Method[] methods = ReflectUtil.getMethods(extendableInterface);
            Method addMethod = null, removeMethod = null;
            Class<?>[] foundParamInfos = null;
            for (Method method : methods) {
                Class<?>[] paramInfos = method.getParameterTypes();
                String methodName = method.getName().toLowerCase();
                if (methodName.startsWith("register") || methodName.startsWith("add")) {
                    if (addMethod != null) {
                        throw new ExtendableException(
                                "No unique extendable methods pair like 'add/remove' or 'register/unregister' found on interface '" + extendableInterface.getName() + "' to add extension signature");
                    }
                    if (foundParamInfos == null) {
                        foundParamInfos = paramInfos;
                    } else {
                        compareParamInfos(paramInfos, foundParamInfos, extendableInterface);
                    }
                    addMethod = method;
                } else if (methodName.startsWith("unregister") || methodName.startsWith("remove")) {
                    if (removeMethod != null) {
                        throw new ExtendableException(
                                "No unique extendable methods pair like 'add/remove' or 'register/unregister' found on interface '" + extendableInterface.getName() + "' to add extension signature");
                    }
                    if (foundParamInfos == null) {
                        foundParamInfos = paramInfos;
                    } else {
                        compareParamInfos(paramInfos, foundParamInfos, extendableInterface);
                    }
                    removeMethod = method;
                }
            }

            if (addMethod == null || removeMethod == null) {
                throw new ExtendableException(
                        "No unique extendable methods pair like 'add/remove' or 'register/unregister' found on interface '" + extendableInterface.getName() + "' to add extension signature");
            }
            KeyItem keyItem = new KeyItem(extendableInterface, null, addMethod.getParameterTypes().length);
            Method[] addRemoveMethods = new Method[] { addMethod, removeMethod };

            put(keyItem, addRemoveMethods);

            return addRemoveMethods;
        } finally {
            writeLock.unlock();
        }
    }

    protected void compareParamInfos(Class<?>[] paramInfos, Class<?>[] foundParamInfos, Class<?> extendableInterface) {
        for (int a = paramInfos.length; a-- > 1; ) {
            Class<?> paramInfo = paramInfos[a];
            Class<?> foundParamInfo = foundParamInfos[a];
            if (paramInfo != foundParamInfo) {
                throw new ExtendableException(
                        "No unique extendable methods pair like 'add/remove' or 'register/unregister' found on interface '" + extendableInterface.getName() + "' to add extension signature");
            }
        }
    }

    public static class KeyItem {
        protected final Class<?> extendableInterface;

        protected final String propertyName;

        protected final int argumentCount;

        public KeyItem(Class<?> extendableInterface, String propertyName, int argumentCount) {
            this.extendableInterface = extendableInterface;
            this.propertyName = propertyName;
            this.argumentCount = argumentCount;
        }

        @Override
        public int hashCode() {
            int hashCode = extendableInterface.hashCode() ^ argumentCount;
            if (propertyName != null) {
                hashCode ^= propertyName.hashCode();
            }
            return hashCode;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (!(obj instanceof KeyItem)) {
                return false;
            }
            KeyItem other = (KeyItem) obj;
            return extendableInterface.equals(other.extendableInterface) && argumentCount == other.argumentCount && Objects.equals(propertyName, other.propertyName);
        }
    }
}
