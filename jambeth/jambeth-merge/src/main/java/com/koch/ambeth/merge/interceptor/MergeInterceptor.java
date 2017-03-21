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

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.config.Property;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
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
import com.koch.ambeth.service.model.ISecurityScope;
import com.koch.ambeth.service.model.IServiceDescription;
import com.koch.ambeth.service.proxy.IMethodLevelBehavior;
import com.koch.ambeth.service.proxy.ServiceClient;
import com.koch.ambeth.util.IConversionHelper;
import com.koch.ambeth.util.annotation.Remove;
import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.proxy.AbstractInterceptor;

import net.sf.cglib.proxy.MethodProxy;

public class MergeInterceptor extends AbstractInterceptor
{
	// Intentionally no SensitiveThreadLocal
	protected static final ThreadLocal<Boolean> processServiceActive = new ThreadLocal<Boolean>();

	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

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
	protected Annotation getMethodLevelBehavior(Method method)
	{
		return behavior.getBehaviourOfMethod(method);
	}

	@Override
	protected Object interceptMergeIntern(Method method, Object[] arguments, Annotation annotation, Boolean isAsyncBegin) throws Throwable
	{
		if (arguments == null || arguments.length != 1 && arguments.length != 3)
		{
			throw new Exception("Arguments currently must be only 1 or 3: " + method.toString());
		}

		Object argumentToMerge = arguments[0];
		Object argumentToDelete = getArgumentToDelete(arguments, method.getParameterTypes());
		ProceedWithMergeHook proceedHook = getProceedHook(arguments);
		MergeFinishedCallback finishedCallback = getFinishedCallback(arguments);
		mergeProcess.process(argumentToMerge, argumentToDelete, proceedHook, finishedCallback);
		if (!void.class.equals(method.getReturnType()))
		{
			return argumentToMerge;
		}
		return null;
	}

	@Override
	protected Object interceptDeleteIntern(Method method, Object[] arguments, Annotation annotation, Boolean isAsyncBegin) throws Throwable
	{
		if (arguments == null || arguments.length != 1 && arguments.length != 3)
		{
			throw new Exception("Arguments currently must be only 1 or 3: " + method.toString());
		}
		ProceedWithMergeHook proceedHook = getProceedHook(arguments);
		MergeFinishedCallback finishedCallback = getFinishedCallback(arguments);
		Remove remove = (Remove) annotation;
		if (remove != null)
		{
			String idName = remove.idName();
			Class<?> entityType = remove.entityType();
			if (idName != null && idName.length() > 0)
			{
				if (void.class.equals(entityType))
				{
					throw new IllegalStateException("Annotation invalid: " + remove + " on method " + method.toString());
				}
				deleteById(method, entityType, idName, arguments[0], proceedHook, finishedCallback);
				return null;
			}
		}
		Object argumentToDelete = arguments[0];
		mergeProcess.process(null, argumentToDelete, proceedHook, finishedCallback);
		if (!void.class.equals(method.getReturnType()))
		{
			return argumentToDelete;
		}
		return null;
	}

	@Override
	protected Object interceptApplication(Object obj, Method method, Object[] args, MethodProxy proxy, Annotation annotation, Boolean isAsyncBegin)
			throws Throwable
	{
		Boolean oldProcessServiceActive = processServiceActive.get();
		if (Boolean.TRUE.equals(oldProcessServiceActive) || processService == null || !method.getDeclaringClass().isAnnotationPresent(ServiceClient.class))
		{
			return super.interceptApplication(obj, method, args, proxy, annotation, isAsyncBegin);
		}
		ISecurityScope[] securityScopes = securityScopeProvider.getSecurityScopes();
		IServiceDescription serviceDescription = SyncToAsyncUtil.createServiceDescription(serviceName, method, args, securityScopes);
		processServiceActive.set(Boolean.TRUE);
		try
		{
			return processService.invokeService(serviceDescription);
		}
		finally
		{
			if (oldProcessServiceActive == null)
			{
				processServiceActive.remove();
			}
			else
			{
				processServiceActive.set(oldProcessServiceActive);
			}
		}
	}

	protected void deleteById(Method method, Class<?> entityType, String idName, Object ids, ProceedWithMergeHook proceedHook,
			MergeFinishedCallback finishedCallback)
	{
		IEntityMetaData metaData = getSpecifiedMetaData(method, Remove.class, entityType);
		Member idMember = getSpecifiedMember(method, Remove.class, metaData, idName);
		byte idIndex = metaData.getIdIndexByMemberName(idName);

		Class<?> idType = idMember.getRealType();
		ArrayList<IObjRef> objRefs = new ArrayList<IObjRef>();
		buildObjRefs(entityType, idIndex, idType, ids, objRefs);
		mergeProcess.process(null, objRefs, proceedHook, finishedCallback);
	}

	protected void buildObjRefs(Class<?> entityType, byte idIndex, Class<?> idType, Object ids, List<IObjRef> objRefs)
	{
		if (ids == null)
		{
			return;
		}
		if (ids instanceof List)
		{
			List<?> list = (List<?>) ids;
			for (int a = 0, size = list.size(); a < size; a++)
			{
				Object id = list.get(a);
				buildObjRefs(entityType, idIndex, idType, id, objRefs);
			}
			return;
		}
		else if (ids instanceof Collection)
		{
			Iterator<?> iter = ((Collection<?>) ids).iterator();
			while (iter.hasNext())
			{
				Object id = iter.next();
				buildObjRefs(entityType, idIndex, idType, id, objRefs);
			}
			return;
		}
		else if (ids.getClass().isArray())
		{
			int size = Array.getLength(ids);
			for (int a = 0; a < size; a++)
			{
				Object id = Array.get(ids, a);
				buildObjRefs(entityType, idIndex, idType, id, objRefs);
			}
			return;
		}
		Object convertedId = conversionHelper.convertValueToType(idType, ids);
		ObjRef objRef = new ObjRef(entityType, idIndex, convertedId, null);
		objRefs.add(objRef);
	}

	protected IEntityMetaData getSpecifiedMetaData(Method method, Class<? extends Annotation> annotation, Class<?> entityType)
	{
		IEntityMetaData metaData = entityMetaDataProvider.getMetaData(entityType);
		if (metaData == null)
		{
			throw new IllegalArgumentException("Please specify a valid returnType for the " + annotation.getSimpleName() + " annotation on method "
					+ method.toString() + ". The current value " + entityType.getName() + " is not a valid entity");
		}
		return metaData;
	}

	protected Member getSpecifiedMember(Method method, Class<? extends Annotation> annotation, IEntityMetaData metaData, String memberName)
	{
		if (memberName == null || memberName.isEmpty())
		{
			return metaData.getIdMember();
		}
		Member member = metaData.getMemberByName(memberName);
		if (member == null)
		{
			throw new IllegalArgumentException("No member " + metaData.getEntityType().getName() + "." + memberName + " found. Please check your "
					+ annotation.getSimpleName() + " annotation on method " + method.toString());
		}
		return member;
	}

	// /// <summary>
	// /// Filter method parameters that should not be serialized
	// /// </summary>
	// /// <param name="methodDescription">The method description to filter</param>
	// protected virtual void FilterParameters(MethodDescription methodDescription)
	// {
	// List<Type> paramTypes = new List<Type>();
	// foreach(Type type in methodDescription.ParamTypes) {
	// if (!type.IsAssignableFrom(typeof(ProceedWithMergeHook))) {
	// paramTypes.Add(type);
	// }
	// }
	// methodDescription.ParamTypes = paramTypes.ToArray();
	// }

	protected Object getArgumentToDelete(Object[] args, Class<?>[] parameters)
	{
		if (parameters == null || parameters.length < 2)
		{
			return null;
		}
		Class<?> parameterToLook = parameters[parameters.length - 1];
		if (ProceedWithMergeHook.class.isAssignableFrom(parameterToLook))
		{
			if (parameters.length == 3)
			{
				return args[1];
			}
		}
		else if (parameters.length == 2)
		{
			return args[1];
		}
		return null;
	}

	protected ProceedWithMergeHook getProceedHook(Object[] args)
	{
		if (args == null || args.length == 0)
		{
			return null;
		}
		if (args[args.length - 1] instanceof ProceedWithMergeHook)
		{
			return (ProceedWithMergeHook) args[args.length - 1];
		}
		return null;
	}

	protected MergeFinishedCallback getFinishedCallback(Object[] args)
	{
		if (args == null)
		{
			return null;
		}
		for (int a = args.length; a-- > 0;)
		{
			Object arg = args[a];
			if (arg instanceof MergeFinishedCallback)
			{
				return (MergeFinishedCallback) arg;
			}
		}
		return null;
	}
}
