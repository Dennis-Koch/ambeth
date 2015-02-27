package de.osthus.esmeralda.handler.js;

import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.HashMap;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.esmeralda.handler.ITransformedMethod;
import de.osthus.esmeralda.handler.js.transformer.DefaultMethodTransformer;
import demo.codeanalyzer.common.model.Method;

public class JsOverloadManager implements IJsOverloadManager
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	protected HashMap<String, HashMap<String, ArrayList<Method>>> overloadedMethods = new HashMap<>();

	@Override
	public void registerOverloads(String fqClass, HashMap<String, ArrayList<Method>> overloadedMethods)
	{
		this.overloadedMethods.put(fqClass, overloadedMethods);
	}

	@Override
	public boolean hasOverloads(Method method)
	{
		String fqClassName = method.getOwningClass().getFqName();
		String methodName = !method.isConstructor() ? method.getName() : DefaultMethodTransformer.THIS;
		boolean hasOverloads = hasOverloads(fqClassName, methodName);
		return hasOverloads;
	}

	@Override
	public boolean hasOverloads(ITransformedMethod transformedMethod)
	{
		String fqClassName = transformedMethod.getOwner();
		String methodName = transformedMethod.getName();
		boolean hasOverloads = hasOverloads(fqClassName, methodName);
		return hasOverloads;
	}

	protected boolean hasOverloads(String fqClassName, String methodName)
	{
		HashMap<String, ArrayList<Method>> classesOverloadedMethods = overloadedMethods.get(fqClassName);
		if (classesOverloadedMethods == null)
		{
			return false;
		}

		ArrayList<Method> overloads = classesOverloadedMethods.get(methodName);
		return overloads != null;
	}

	@Override
	public HashMap<String, ArrayList<Method>> getOverloadedMethods(String fqClassName)
	{
		HashMap<String, ArrayList<Method>> classesOverloadedMethods = overloadedMethods.get(fqClassName);
		return classesOverloadedMethods;
	}
}
