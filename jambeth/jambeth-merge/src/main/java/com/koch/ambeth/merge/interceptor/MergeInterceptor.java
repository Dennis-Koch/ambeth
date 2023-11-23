package com.koch.ambeth.merge.interceptor;

/*-
 * #%L
 * jambeth-merge
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

import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.config.Property;
import com.koch.ambeth.merge.IMergeProcess;
import com.koch.ambeth.merge.MergeFinishedCallback;
import com.koch.ambeth.merge.ProceedWithMergeHook;
import com.koch.ambeth.merge.security.ISecurityScopeProvider;
import com.koch.ambeth.merge.transfer.ObjRef;
import com.koch.ambeth.service.IProcessService;
import com.koch.ambeth.service.SyncToAsyncUtil;
import com.koch.ambeth.service.merge.IEntityMetaDataProvider;
import com.koch.ambeth.service.merge.model.IEntityMetaData;
import com.koch.ambeth.service.merge.model.IObjRef;
import com.koch.ambeth.service.metadata.Member;
import com.koch.ambeth.service.proxy.IMethodLevelBehavior;
import com.koch.ambeth.service.proxy.ServiceClient;
import com.koch.ambeth.util.IConversionHelper;
import com.koch.ambeth.util.annotation.Process;
import com.koch.ambeth.util.annotation.Remove;
import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.proxy.AbstractInterceptor;
import com.koch.ambeth.util.proxy.MethodProxy;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;

public class MergeInterceptor extends AbstractInterceptor {
    protected static final ThreadLocal<Boolean> processServiceActive = new ThreadLocal<>();
    // Intentionally no SensitiveThreadLocal
    public static String BEHAVIOR_PROP = "Behavior";
    public static String PROCESS_SERVICE_PROP = "ProcessService";
    public static String SERVICE_NAME_PROP = "ServiceName";
    @Autowired
    protected IMethodLevelBehavior<Annotation> behavior;

    @Autowired
    protected IConversionHelper conversionHelper;

    @Autowired
    protected IEntityMetaDataProvider entityMetaDataProvider;

    @Autowired
    protected IMergeProcess mergeProcess;

    @Autowired(optional = true)
    protected IProcessService processService;

    @Autowired
    protected ISecurityScopeProvider securityScopeProvider;

    protected boolean isCloneCacheControllerActive;

    @Property
    protected String serviceName;

    @Override
    protected Annotation getMethodLevelBehavior(Method method) {
        if (behavior == null) {
            // this may be the case if during proxy instiation on the to-be-booted IoC container an
            // exception occurs
            // e.g. a DuplicateAutowireableException
            return null;
        }
        return behavior.getBehaviourOfMethod(method);
    }

    @Override
    protected Object interceptMergeIntern(Method method, Object[] arguments, Annotation annotation, Boolean isAsyncBegin) throws Throwable {
        if (arguments == null || arguments.length != 1 && arguments.length != 3) {
            throw new Exception("Arguments currently must be only 1 or 3: " + method.toString());
        }

        var argumentToMerge = arguments[0];
        var argumentToDelete = getArgumentToDelete(arguments, method.getParameterTypes());
        var proceedHook = getProceedHook(arguments);
        var finishedCallback = getFinishedCallback(arguments);
        mergeProcess.begin().merge(argumentToMerge).delete(argumentToDelete).onLocalDiff(proceedHook).onSuccess(finishedCallback).finish();
        if (!void.class.equals(method.getReturnType())) {
            return argumentToMerge;
        }
        return null;
    }

    @Override
    protected Object interceptDeleteIntern(Method method, Object[] arguments, Annotation annotation, Boolean isAsyncBegin) throws Throwable {
        if (arguments == null || arguments.length != 1 && arguments.length != 3) {
            throw new Exception("Arguments currently must be only 1 or 3: " + method.toString());
        }
        var proceedHook = getProceedHook(arguments);
        var finishedCallback = getFinishedCallback(arguments);
        var remove = (Remove) annotation;
        if (remove != null) {
            var idName = remove.idName();
            var entityType = remove.entityType();
            if (idName != null && idName.length() > 0) {
                if (void.class.equals(entityType)) {
                    throw new IllegalStateException("Annotation invalid: " + remove + " on method " + method.toString());
                }
                deleteById(method, entityType, idName, arguments[0], proceedHook, finishedCallback);
                return null;
            }
        }
        var argumentToDelete = arguments[0];
        mergeProcess.begin().delete(argumentToDelete).onLocalDiff(proceedHook).onSuccess(finishedCallback).finish();
        if (!void.class.equals(method.getReturnType())) {
            return argumentToDelete;
        }
        return null;
    }

    @Override
    protected Object interceptApplication(Object obj, Method method, Object[] args, MethodProxy proxy, Annotation annotation, Boolean isAsyncBegin) throws Throwable {
        var oldProcessServiceActive = processServiceActive.get();
        if (Boolean.TRUE.equals(oldProcessServiceActive) || processService == null || (!method.getDeclaringClass().isAnnotationPresent(ServiceClient.class) && !(annotation instanceof Process))) {
            return super.interceptApplication(obj, method, args, proxy, annotation, isAsyncBegin);
        }
        var securityScopes = securityScopeProvider.getSecurityScopes();
        var serviceDescription = SyncToAsyncUtil.createServiceDescription(serviceName, method, args, securityScopes);
        processServiceActive.set(Boolean.TRUE);
        try {
            return processService.invokeService(serviceDescription);
        } finally {
            if (oldProcessServiceActive == null) {
                processServiceActive.remove();
            } else {
                processServiceActive.set(oldProcessServiceActive);
            }
        }
    }

    protected void deleteById(Method method, Class<?> entityType, String idName, Object ids, ProceedWithMergeHook proceedHook, MergeFinishedCallback finishedCallback) {
        var metaData = getSpecifiedMetaData(method, Remove.class, entityType);
        var idMember = getSpecifiedMember(method, Remove.class, metaData, idName);
        var idIndex = metaData.getIdIndexByMemberName(idName);

        var idType = idMember.getRealType();
        var objRefs = new ArrayList<IObjRef>();
        buildObjRefs(entityType, idIndex, idType, ids, objRefs);
        mergeProcess.begin().delete(objRefs).onLocalDiff(proceedHook).onSuccess(finishedCallback).finish();
    }

    protected void buildObjRefs(Class<?> entityType, byte idIndex, Class<?> idType, Object ids, List<IObjRef> objRefs) {
        if (ids == null) {
            return;
        }
        if (ids instanceof List) {
            var list = (List<?>) ids;
            for (int a = 0, size = list.size(); a < size; a++) {
                var id = list.get(a);
                buildObjRefs(entityType, idIndex, idType, id, objRefs);
            }
            return;
        } else if (ids instanceof Collection) {
            var iter = ((Collection<?>) ids).iterator();
            while (iter.hasNext()) {
                var id = iter.next();
                buildObjRefs(entityType, idIndex, idType, id, objRefs);
            }
            return;
        } else if (ids.getClass().isArray()) {
            var size = Array.getLength(ids);
            for (int a = 0; a < size; a++) {
                var id = Array.get(ids, a);
                buildObjRefs(entityType, idIndex, idType, id, objRefs);
            }
            return;
        }
        var convertedId = conversionHelper.convertValueToType(idType, ids);
        var objRef = new ObjRef(entityType, idIndex, convertedId, null);
        objRefs.add(objRef);
    }

    protected IEntityMetaData getSpecifiedMetaData(Method method, Class<? extends Annotation> annotation, Class<?> entityType) {
        var metaData = entityMetaDataProvider.getMetaData(entityType);
        if (metaData == null) {
            throw new IllegalArgumentException(
                    "Please specify a valid returnType for the " + annotation.getSimpleName() + " annotation on method " + method.toString() + ". The current value " + entityType.getName() + " is " + "not a valid entity");
        }
        return metaData;
    }

    protected Member getSpecifiedMember(Method method, Class<? extends Annotation> annotation, IEntityMetaData metaData, String memberName) {
        if (memberName == null || memberName.isEmpty()) {
            return metaData.getIdMember();
        }
        var member = metaData.getMemberByName(memberName);
        if (member == null) {
            throw new IllegalArgumentException(
                    "No member " + metaData.getEntityType().getName() + "." + memberName + " found. Please check your " + annotation.getSimpleName() + " annotation on method " + method.toString());
        }
        return member;
    }

    protected Object getArgumentToDelete(Object[] args, Class<?>[] parameters) {
        if (parameters == null || parameters.length < 2) {
            return null;
        }
        var parameterToLook = parameters[parameters.length - 1];
        if (ProceedWithMergeHook.class.isAssignableFrom(parameterToLook)) {
            if (parameters.length == 3) {
                return args[1];
            }
        } else if (parameters.length == 2) {
            return args[1];
        }
        return null;
    }

    protected ProceedWithMergeHook getProceedHook(Object[] args) {
        if (args == null || args.length == 0) {
            return null;
        }
        if (args[args.length - 1] instanceof ProceedWithMergeHook) {
            return (ProceedWithMergeHook) args[args.length - 1];
        }
        return null;
    }

    protected MergeFinishedCallback getFinishedCallback(Object[] args) {
        if (args == null) {
            return null;
        }
        for (int a = args.length; a-- > 0; ) {
            var arg = args[a];
            if (arg instanceof MergeFinishedCallback) {
                return (MergeFinishedCallback) arg;
            }
        }
        return null;
    }
}
