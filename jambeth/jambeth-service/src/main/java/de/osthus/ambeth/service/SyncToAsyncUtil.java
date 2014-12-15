package de.osthus.ambeth.service;

import java.lang.reflect.Method;
import java.util.Map;

import de.osthus.ambeth.collections.HashMap;
import de.osthus.ambeth.model.ISecurityScope;
import de.osthus.ambeth.transfer.MethodDescription;
import de.osthus.ambeth.transfer.ServiceDescription;
import de.osthus.ambeth.util.Lock;
import de.osthus.ambeth.util.ReadWriteLock;

public final class SyncToAsyncUtil
{

	protected static final Map<Method, Method[]> syncToAsyncDict = new HashMap<Method, Method[]>();

	protected static final Map<Method, Method> asyncToSyncDict = new HashMap<Method, Method>();

	protected static final Lock readLock, writeLock;

	static
	{
		ReadWriteLock rwLock = new ReadWriteLock();
		readLock = rwLock.getReadLock();
		writeLock = rwLock.getWriteLock();
	}

	// public static Method getSyncMethod(Method asyncBeginMethod, Class<?>
	// syncInterface) {
	// readLock.lock();
	// try {
	// Method syncMethod = asyncToSyncDict.get(asyncBeginMethod);
	// if (syncMethod != null) {
	// return syncMethod;
	// }
	// } finally {
	// readLock.unlock();
	// }
	// Class<?>[] parameters = asyncBeginMethod.getParameterTypes();
	// String asyncMethodName = asyncBeginMethod.getName();
	// String syncMethodName;
	// Class<?>[] paramTypes;
	// if (asyncMethodName.startsWith("begin")) {
	// syncMethodName = asyncMethodName.substring(5);
	// // Trim last 2 arguments "IAsyncCallback" and "Objectstate"
	// paramTypes = new Class<?>[parameters.length - 2];
	// System.arraycopy(parameters, 0, paramTypes, 0, paramTypes.length);
	// } else {
	// throw new IllegalArgumentException("AsyncMethod '" + asyncBeginMethod
	// + "' does not seem to be a Begin-method");
	// }
	// Method result;
	// try {
	// result = syncInterface.getMethod(syncMethodName, paramTypes);
	// } catch (Exception e) {
	// throw RuntimeExceptionUtil.mask(e);
	// }
	// if (result == null) {
	// throw new IllegalArgumentException("No method with name '" +
	// syncMethodName + "'" + paramTypes + " found");
	// }
	// writeLock.lock();
	// try {
	// // Some other thread might be faster here so we have to check this
	// if (!asyncToSyncDict.containsKey(asyncBeginMethod)) {
	// asyncToSyncDict.put(asyncBeginMethod, result);
	// }
	// } finally {
	// writeLock.unlock();
	// }
	// return result;
	// }
	//
	// public static Method[] GetAsyncMethods(Method syncMethod, Class<?>
	// asyncInterface) {
	// readLock.lock();
	// try {
	// Method[] asyncMethods = syncToAsyncDict.get(syncMethod);
	// if (asyncMethods != null) {
	// return asyncMethods;
	// }
	// } finally {
	// readLock.unlock();
	// }
	// Class<?>[] parameters = syncMethod.getParameterTypes();
	//
	// Class<?>[] beginTypes = new Class<?>[parameters.length + 2];
	// System.arraycopy(parameters, 0, beginTypes, 0, parameters.length);
	// beginTypes[beginTypes.length - 2] = AsyncCallback.class;
	// beginTypes[beginTypes.length - 1] = Object.class;
	//
	// String methodName = syncMethod.getName();
	// String beginName = "Begin" + methodName;
	// String endName = "End" + methodName;
	// Method beginMethod = asyncInterface.getMethod(beginName, beginTypes);
	//
	// if (beginMethod == null) {
	// throw new IllegalArgumentException("No method with name '" + beginName +
	// "'" + parameters + " found");
	// }
	// Method endMethod = asyncInterface.getMethod(endName, new Class<?>[] {
	// IAsyncResult.class });
	// if (endMethod == null) {
	// throw new IllegalArgumentException("No method with name '" + endName +
	// "'" + IAsyncResult.class + " found");
	// }
	// Method[] result = new Method[] { beginMethod, endMethod };
	// writeLock.lock();
	// try {
	// // Some other thread might be faster here so we have to check this
	// if (!syncToAsyncDict.containsKey(syncMethod)) {
	// syncToAsyncDict.put(syncMethod, result);
	// }
	// if (!asyncToSyncDict.containsKey(beginMethod)) {
	// asyncToSyncDict.put(beginMethod, syncMethod);
	// }
	// } finally {
	// writeLock.unlock();
	// }
	// return result;
	// }
	//
	// public static Object[] BuildSyncArguments(Object[] asyncArguments,
	// IParamHolder<AsyncCallback> asyncCallback) {
	// asyncCallback = (AsyncCallback) asyncArguments[asyncArguments.length -
	// 2];
	// Object[] syncArguments = new Object[asyncArguments.length - 2];
	// System.arraycopy(asyncArguments, 0, syncArguments, 0,
	// syncArguments.length);
	// return syncArguments;
	// }

	public static ServiceDescription createServiceDescription(String serviceName, Method syncMethod, Object[] syncArguments, ISecurityScope... securityScopes)
	{
		ServiceDescription serviceDescription = new ServiceDescription();
		serviceDescription.setServiceName(serviceName);
		serviceDescription.setMethodName(syncMethod.getName());
		serviceDescription.setParamTypes(syncMethod.getParameterTypes());
		serviceDescription.setArguments(syncArguments);
		ISecurityScope[] securityScopesWeb = new ISecurityScope[securityScopes.length];
		System.arraycopy(securityScopes, 0, securityScopesWeb, 0, securityScopes.length);
		serviceDescription.setSecurityScopes(securityScopesWeb);
		return serviceDescription;
	}

	public static MethodDescription createMethodDescription(Method syncMethod)
	{
		MethodDescription methodDescription = new MethodDescription();
		methodDescription.setMethodName(syncMethod.getName());
		methodDescription.setParamTypes(syncMethod.getParameterTypes());
		methodDescription.setServiceType(syncMethod.getDeclaringClass());
		return methodDescription;
	}

	private SyncToAsyncUtil()
	{
		// Intended blank
	}
}
