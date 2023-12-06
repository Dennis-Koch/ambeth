package com.koch.ambeth.service.remote;

/*-
 * #%L
 * jambeth-service-rest
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

import com.koch.ambeth.ioc.IDisposableBean;
import com.koch.ambeth.ioc.IInitializingBean;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.config.Property;
import com.koch.ambeth.ioc.typeinfo.TypeInfoItemUtil;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.service.transfer.AmbethServiceException;
import com.koch.ambeth.util.Arrays;
import com.koch.ambeth.util.IClassCache;
import com.koch.ambeth.util.IConversionHelper;
import com.koch.ambeth.util.ListUtil;
import com.koch.ambeth.util.ParamChecker;
import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.collections.IdentityLinkedMap;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;
import com.koch.ambeth.util.proxy.AbstractSimpleInterceptor;
import com.koch.ambeth.util.proxy.MethodProxy;
import com.koch.ambeth.util.state.StateRollback;
import com.koch.ambeth.util.threading.IGuiThreadHelper;

import java.io.BufferedReader;
import java.io.StringReader;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class InMemoryClientServiceInterceptor extends AbstractSimpleInterceptor implements IRemoteInterceptor, IInitializingBean, IDisposableBean {

    public static final String PROP_REMOTE_SERVICE_CONTEXT_SUPPLIER_TYPE = "RemoteServiceContextSupplier";

    @Autowired
    protected IClassCache classCache;

    @Autowired
    protected IInMemoryClientServiceResolver clientServiceResolver;

    @Autowired
    protected IConversionHelper conversionHelper;

    @Autowired
    protected IGuiThreadHelper guiThreadHelper;

    @Property
    protected String serviceName;

    protected volatile boolean disposed;

    @LogInstance
    private ILogger log;

    private Object remoteSourceIdentifier;

    @Override
    public void afterPropertiesSet() {
        ParamChecker.assertNotNull(serviceName, IRemoteTargetProvider.SERVICE_NAME_PROP);
    }

    @Override
    public void destroy() throws Throwable {
        disposed = true;
    }

    @Override
    public Object getRemoteSourceIdentifier() {
        if (remoteSourceIdentifier == null) {
            throw new IllegalStateException("Must be valid at this point");
        }
        return remoteSourceIdentifier;
    }

    @Override
    public String getServiceName() {
        return serviceName;
    }

    @Override
    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    @Override
    protected Object interceptIntern(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
        if (disposed) {
            throw new IllegalStateException("Bean already disposed");
        }
        var remoteServiceContext = clientServiceResolver.resolveRemoteServiceContext(null, serviceName, method, args);
        if (remoteSourceIdentifier == null) {
            remoteSourceIdentifier = remoteServiceContext.getRemoteSourceIdentifier();
        }
        if (toStringMethod.equals(method)) {
            return "Stub for '" + serviceName + "' in-memory to service endpoint '" + remoteServiceContext.toString() + "/" + serviceName + "'";
        }
        if (guiThreadHelper != null && guiThreadHelper.isInGuiThread()) {
            throw new Exception("It is not allowed to call this interceptor from GUI thread");
        }
        boolean enrichException = true;
        URL url = null;
        try {

            Object result;
            var rollback = StateRollback.chain(chain -> {
                chain.append(() -> remoteServiceContext.cleanupThreadLocal());
            });
            try {
                var remoteServiceBean = remoteServiceContext.getServiceByName(serviceName);
                result = method.invoke(remoteServiceBean, args);
            } finally {
                rollback.rollback();
            }

            if (result instanceof AmbethServiceException) {
                Exception exception = parseServiceException((AmbethServiceException) result);
                RuntimeExceptionUtil.fillInClientStackTraceIfPossible(exception);
                enrichException = false;
                throw exception;
            }
            return convertToExpectedType(method.getReturnType(), method.getGenericReturnType(), result);
        } catch (Throwable e) {
            if (enrichException && url != null) {
                throw RuntimeExceptionUtil.mask(e, "Error occurred while calling url '" + url + "'");
            }
            throw e;
        }
    }

    @SuppressWarnings("unchecked")
    protected Exception parseServiceException(AmbethServiceException serviceException) {
        var serviceCause = serviceException.getCause();
        Exception cause = null;
        if (serviceCause != null) {
            cause = parseServiceException(serviceCause);
        }
        try {
            var exceptionType = (Class<? extends Exception>) classCache.loadClass(serviceException.getExceptionType());
            Exception ex;
            if (cause == null) {
                var constructor = exceptionType.getConstructor(String.class);
                ex = constructor.newInstance(serviceException.getMessage());
            } else {
                var constructor = exceptionType.getConstructor(String.class, Throwable.class);
                ex = constructor.newInstance(serviceException.getMessage(), cause);
            }

            var stes = new ArrayList<StackTraceElement>();
            Pattern stePattern = Pattern.compile("\\s*(.+)\\.([^\\.]+)\\(([^\\:\\)]+)(?:\\:(\\d+))?\\)");
            BufferedReader reader = new BufferedReader(new StringReader(serviceException.getStackTrace()));
            String line;
            while ((line = reader.readLine()) != null) {
                Matcher matcher = stePattern.matcher(line);
                if (!matcher.matches()) {
                    return new IllegalStateException(serviceException.getMessage() + "\n" + serviceException.getStackTrace(), cause);
                }
                String declaringClass = matcher.group(1);
                String methodName = matcher.group(2);
                String file = matcher.group(3);
                String lineNumber = matcher.group(4);
                if ("Native Method".equals(file)) {
                    file = null;
                    lineNumber = "-2";
                }
                stes.add(new StackTraceElement(declaringClass, methodName, file, lineNumber != null ? Integer.valueOf(lineNumber) : -1));
            }
            ex.setStackTrace(stes.toArray(StackTraceElement.class));
            return ex;
        } catch (Exception ignored) {
            return new IllegalStateException(serviceException.getExceptionType() + ":" + serviceException.getMessage() + "\n" + serviceException.getStackTrace(), cause);
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    protected Object convertToExpectedType(Class<?> expectedType, Type genericType, Object result) {
        if (void.class.equals(expectedType) || result == null) {
            return null;
        }
        if (Collection.class.isAssignableFrom(expectedType)) {
            var targetCollection = ListUtil.createCollectionOfType(expectedType);

            var alreadyConvertedMap = new IdentityLinkedMap<>();

            var itemType = TypeInfoItemUtil.getElementTypeUsingReflection(expectedType, genericType);

            if (result instanceof Iterable) {
                for (var item : (Iterable<?>) result) {
                    var convertedItem = alreadyConvertedMap.get(item);
                    if (convertedItem == null && !alreadyConvertedMap.containsKey(item)) {
                        convertedItem = conversionHelper.convertValueToType(itemType, item);
                        alreadyConvertedMap.put(item, convertedItem);
                    }
                    targetCollection.add(convertedItem);
                }
            } else {
                var convertedItem = conversionHelper.convertValueToType(itemType, result);
                targetCollection.add(convertedItem);
            }
            return targetCollection;
        } else if (expectedType.isArray()) {
            var list = new ArrayList<>();
            if (result instanceof Iterable) {
                for (Object item : (Iterable<?>) result) {
                    list.add(item);
                }
            } else {
                list.add(result);
            }

            var alreadyConvertedMap = new IdentityLinkedMap<>();

            var itemType = TypeInfoItemUtil.getElementTypeUsingReflection(expectedType, genericType);

            var array = Array.newInstance(expectedType.getComponentType(), list.size());
            var preparedArraySet = Arrays.prepareSet(array);
            for (int a = 0, size = list.size(); a < size; a++) {
                var item = list.get(a);
                var convertedItem = alreadyConvertedMap.get(item);
                if (convertedItem == null && !alreadyConvertedMap.containsKey(item)) {
                    convertedItem = conversionHelper.convertValueToType(itemType, item);
                    alreadyConvertedMap.put(item, convertedItem);
                }
                preparedArraySet.set(a, convertedItem);
            }
            return array;
        }
        return conversionHelper.convertValueToType(expectedType, result);
    }
}
