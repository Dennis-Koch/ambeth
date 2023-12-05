package com.koch.ambeth.persistence.proxy;

import com.koch.ambeth.filter.IPagingResponse;
import com.koch.ambeth.ioc.IServiceContext;
import com.koch.ambeth.ioc.config.IBeanConfiguration;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.query.interceptor.QueryInterceptor;
import com.koch.ambeth.query.interceptor.QueryInterceptorCommand;
import com.koch.ambeth.query.squery.QueryUtils;
import com.koch.ambeth.service.config.ServiceConfigurationConstants;
import com.koch.ambeth.service.proxy.AbstractCascadePostProcessor;
import com.koch.ambeth.util.annotation.AnnotationCache;
import com.koch.ambeth.util.annotation.Find;
import com.koch.ambeth.util.annotation.NoProxy;
import com.koch.ambeth.util.annotation.SmartQuery;
import com.koch.ambeth.util.collections.WeakValueHashMap;
import com.koch.ambeth.util.config.IProperties;
import com.koch.ambeth.util.proxy.ICascadedInterceptor;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

public class QueryPostProcessor extends AbstractCascadePostProcessor {
    protected static final AnnotationCache<Find> FIND_CACHE = new AnnotationCache<>(Find.class) {
        @Override
        protected boolean annotationEquals(Find left, Find right) {
            return left.equals(right);
        }
    };
    protected static final AnnotationCache<NoProxy> NO_PROXY_CACHE = new AnnotationCache<>(NoProxy.class) {
        @Override
        protected boolean annotationEquals(NoProxy left, NoProxy right) {
            return left.equals(right);
        }
    };
    private static final Pattern PATTERN_QUERY_METHOD = Pattern.compile("(retrieve|read|find|get).*");
    protected final AnnotationCache<SmartQuery> SMART_QUERY_CACHE = new AnnotationCache<>(SmartQuery.class) {
        @Override
        protected boolean annotationEquals(SmartQuery left, SmartQuery right) {
            return Objects.equals(left.entityType(), right.entityType());
        }
    };

    protected final Map<Class<?>, Map<Method, QueryInterceptorCommand>> beanTypeToCommandsMap = new WeakValueHashMap<>();

    @LogInstance
    private ILogger log;

    @Override
    protected ICascadedInterceptor handleServiceIntern(IBeanContextFactory beanContextFactory, IServiceContext beanContext, IBeanConfiguration beanConfiguration, Class<?> type,
            Set<Class<?>> requestedTypes) {
        var props = beanContext.getService(IProperties.class);
        var networkClientMode = Boolean.parseBoolean(props.getString(ServiceConfigurationConstants.NetworkClientMode, "false"));
        if (networkClientMode) {
            // for the smart query behavior we don't need logic on the RPC stub side
            return null;
        }
        var methodToCommandMap = resolveMethodToComandMap(type);
        if (methodToCommandMap.isEmpty()) {
            return null;
        }
        var interceptor = new QueryInterceptor();
        if (beanContext.isRunning()) {
            var interceptorBC = beanContext.registerWithLifecycle(interceptor).propertyValue(QueryInterceptor.P_METHOD_TO_COMMAND_MAP, methodToCommandMap);
            return interceptorBC.finish();
        }
        beanContextFactory.registerWithLifecycle(interceptor).propertyValue(QueryInterceptor.P_METHOD_TO_COMMAND_MAP, methodToCommandMap);
        return interceptor;
    }

    protected Map<Method, QueryInterceptorCommand> resolveMethodToComandMap(Class<?> type) {
        synchronized (beanTypeToCommandsMap) {
            var methodToCommandMap = beanTypeToCommandsMap.get(type);
            if (methodToCommandMap != null) {
                return methodToCommandMap;
            }
        }
        Map<Method, QueryInterceptorCommand> methodToCommandMap = new HashMap<>();
        for (var method : type.getMethods()) {
            interceptIntern(method, methodToCommandMap);
        }
        if (methodToCommandMap.isEmpty()) {
            methodToCommandMap = Map.of();
        }
        synchronized (beanTypeToCommandsMap) {
            var existingMethodToCommandMap = beanTypeToCommandsMap.get(type);
            if (existingMethodToCommandMap != null) {
                // concurrent thread was faster
                return existingMethodToCommandMap;
            }
            beanTypeToCommandsMap.put(type, methodToCommandMap);
            return methodToCommandMap;
        }
    }

    protected void interceptIntern(Method currMethod, Map<Method, QueryInterceptorCommand> methodToCommandMap) {
        if (NO_PROXY_CACHE.getAnnotation(currMethod) != null) {
            return;
        }
        var methodName = currMethod.getName().toLowerCase();
        Boolean isAsyncBegin = null;
        if (methodName.startsWith("begin")) {
            isAsyncBegin = Boolean.TRUE;
            methodName = methodName.substring(5);
        } else if (methodName.startsWith("end")) {
            isAsyncBegin = Boolean.FALSE;
            methodName = methodName.substring(3);
        }
        intercept(currMethod, methodToCommandMap, methodName, isAsyncBegin);
    }

    protected void intercept(Method currMethod, Map<Method, QueryInterceptorCommand> methodToCommandMap, String lowerCaseMethodName, Boolean isAsyncBegin) {
        var smartQuery = SMART_QUERY_CACHE.getAnnotation(currMethod);
        if (smartQuery != null) {
            if (Modifier.isAbstract(currMethod.getModifiers()) && QueryUtils.canBuildQuery(currMethod.getName())) {
                methodToCommandMap.put(currMethod, (interceptor, obj, method, args, proxy) -> interceptor.interceptSmartQuery(obj, method, args, proxy, isAsyncBegin, smartQuery));
                return;
            }
            throw new IllegalArgumentException(
                    "Method '" + currMethod.toGenericString() + "' does not suite " + SmartQuery.class.getSimpleName() + " behavior. Please adhere to the naming convention");
        }
        var find = FIND_CACHE.getAnnotation(currMethod);
        if (find != null || PATTERN_QUERY_METHOD.matcher(currMethod.getName()).matches()) {
            if (currMethod.getParameterCount() == 3 && IPagingResponse.class.isAssignableFrom(currMethod.getReturnType())) {
                methodToCommandMap.put(currMethod, (interceptor, obj, method, args, proxy) -> interceptor.interceptFind(obj, method, args, proxy, isAsyncBegin, find));
            }
        }
    }
}
