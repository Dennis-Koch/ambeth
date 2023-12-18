package com.koch.ambeth.ioc.link;

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

import com.koch.ambeth.ioc.accessor.IAccessorTypeProvider;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.bytecode.DelegateEnhancementHint;
import com.koch.ambeth.ioc.bytecode.IBytecodeEnhancer;
import com.koch.ambeth.ioc.bytecode.IDelegateConstructor;
import com.koch.ambeth.ioc.extendable.IExtendableRegistry;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.util.ParamChecker;
import com.koch.ambeth.util.ParamHolder;
import com.koch.ambeth.util.ReflectUtil;
import com.koch.ambeth.util.collections.IMap;
import com.koch.ambeth.util.collections.LinkedHashMap;
import com.koch.ambeth.util.exception.MaskingRuntimeException;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;
import com.koch.ambeth.util.proxy.DelegateInterceptor;
import com.koch.ambeth.util.proxy.Factory;
import com.koch.ambeth.util.proxy.ICascadedInterceptor;
import com.koch.ambeth.util.proxy.IProxyFactory;
import lombok.Setter;

import java.lang.reflect.Method;

public class LinkContainer extends AbstractLinkContainer {
    /**
     * Generates a map of methods where the key is a method on the given <code>parameterType</code>
     * and the value is a corresponding method on the given <code>listenerType</code> by considering
     * the given <code>listenerMethodName</code>.
     *
     * @param listenerType
     * @param listenerMethodName
     * @param parameterType
     * @return
     */
    public static IMap<Method, Method> buildDelegateMethodMap(Class<?> listenerType, String listenerMethodName, Class<?> parameterType) {
        var methodsOnExpectedListenerType = ReflectUtil.getDeclaredMethods(parameterType);
        var mappedMethods = new LinkedHashMap<Method, Method>();
        for (var methodOnExpectedListenerType : methodsOnExpectedListenerType) {
            var parameterAnnotations = methodOnExpectedListenerType.getParameterAnnotations();
            var types = methodOnExpectedListenerType.getParameterTypes();

            Method method = null;
            while (true) {
                method = ReflectUtil.getDeclaredMethod(true, listenerType, methodOnExpectedListenerType.getReturnType(), listenerMethodName, types);
                if (method == null && types.length > 0) {
                    var firstType = types[0];
                    types[0] = null;
                    method = ReflectUtil.getDeclaredMethod(true, listenerType, methodOnExpectedListenerType.getReturnType(), listenerMethodName, types);
                    types[0] = firstType;
                }
                if (method != null) {
                    break;
                }
                if (types.length > 1) {
                    var annotationsOfLastType = parameterAnnotations[types.length - 1];
                    LinkOptional linkOptional = null;
                    for (var annotationOfLastType : annotationsOfLastType) {
                        if (annotationOfLastType instanceof LinkOptional) {
                            linkOptional = (LinkOptional) annotationOfLastType;
                            break;
                        }
                    }
                    if (linkOptional != null) {
                        // drop last expected argument and look again
                        var newTypes = new Class<?>[types.length - 1];
                        System.arraycopy(types, 0, newTypes, 0, newTypes.length);
                        types = newTypes;
                        continue;
                    }
                }
                throw new IllegalArgumentException("Could not map given method '" + listenerMethodName + "' of listener " + listenerType + " to signature: " + methodOnExpectedListenerType);
            }
            mappedMethods.put(methodOnExpectedListenerType, method);
        }
        return mappedMethods;
    }

    @Autowired(optional = true)
    protected IAccessorTypeProvider accessorTypeProvider;

    @Autowired(optional = true)
    protected IBytecodeEnhancer bytecodeEnhancer;

    @Setter
    protected IExtendableRegistry extendableRegistry;

    @Setter
    protected IProxyFactory proxyFactory;

    protected Method addMethod;

    protected Method removeMethod;

    @LogInstance
    private ILogger log;

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();

        ParamChecker.assertNotNull(extendableRegistry, "ExtendableRegistry");
        ParamChecker.assertNotNull(proxyFactory, "ProxyFactory");
    }

    @Override
    protected Object resolveRegistryIntern(Object registry) {
        registry = super.resolveRegistryIntern(registry);
        var linkArgumentsPH = new ParamHolder<Object[]>();
        Method[] methods;
        if (registryPropertyName != null) {
            methods = extendableRegistry.getAddRemoveMethods(registry.getClass(), registryPropertyName, arguments, linkArgumentsPH);
        } else {
            methods = extendableRegistry.getAddRemoveMethods(registryBeanAutowiredType, arguments, linkArgumentsPH);
        }
        arguments = linkArgumentsPH.getValue();
        addMethod = methods[0];
        removeMethod = methods[1];
        return registry;
    }

    @Override
    protected Object resolveListenerIntern(Object listener) {
        listener = super.resolveListenerIntern(listener);
        if (listenerMethodName == null) {
            return listener;
        }
        var parameterType = addMethod.getParameterTypes()[0];
        if (listener instanceof Factory) {
            var callback = ((Factory) listener).getInterceptor();
            if (callback instanceof ICascadedInterceptor cascadedInterceptor) {
                Object target = cascadedInterceptor;
                while (target instanceof ICascadedInterceptor) {
                    var targetOfTarget = ((ICascadedInterceptor) target).getTarget();
                    if (targetOfTarget != null) {
                        target = targetOfTarget;
                    } else {
                        target = null;
                        break;
                    }
                }
                if (target != null) {
                    listener = target;
                }
            }
        }
        var listenerType = listener.getClass();

        Object delegateInstance = null;
        if (bytecodeEnhancer != null && accessorTypeProvider != null) {
            try {
                var delegateType = bytecodeEnhancer.getEnhancedType(Object.class, new DelegateEnhancementHint(listenerType, listenerMethodName, parameterType));
                var constructor = accessorTypeProvider.getConstructorType(IDelegateConstructor.class, delegateType);
                delegateInstance = constructor.createInstance(listener);
            } catch (MaskingRuntimeException e) {
                if (!(e.getCause() instanceof NoClassDefFoundError)) {
                    throw e;
                }
                // This can happen in OSGi environments where the bytecode classLoader is not able e.g. to
                // resolve some imported class declaration on the bytecode generated class
            }
        }
        if (delegateInstance == null) {
            var mappedMethods = buildDelegateMethodMap(listenerType, listenerMethodName, parameterType);
            var interceptor = new DelegateInterceptor(listener, mappedMethods);
            delegateInstance = proxyFactory.createProxy(parameterType, listenerType.getInterfaces(), interceptor);
        }
        return delegateInstance;
    }

    protected void evaluateRegistryMethods(Object registry) {
    }

    @Override
    protected ILogger getLog() {
        return log;
    }

    @Override
    protected void handleLink(Object registry, Object listener) {
        evaluateRegistryMethods(registry);
        arguments[0] = listener;
        try {
            addMethod.invoke(registry, arguments);
        } catch (Exception e) {
            throw RuntimeExceptionUtil.mask(e);
        }
    }

    @Override
    protected void handleUnlink(Object registry, Object listener) {
        if (arguments.length == 0) {
            return;
        }
        arguments[0] = listener;
        try {
            removeMethod.invoke(registry, arguments);
        } catch (Exception e) {
            throw RuntimeExceptionUtil.mask(e);
        }
    }
}
