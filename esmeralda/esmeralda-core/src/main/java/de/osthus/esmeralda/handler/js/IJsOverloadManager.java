package de.osthus.esmeralda.handler.js;

import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.HashMap;
import de.osthus.esmeralda.handler.ITransformedMethod;
import demo.codeanalyzer.common.model.Method;

public interface IJsOverloadManager
{
	public static final String STATIC = "jsOverloadManager_static";

	public static final String NON_STATIC = "jsOverloadManager_non-static";

	void registerOverloads(String fqClassName, HashMap<String, ArrayList<Method>> overloadedMethods);

	boolean hasOverloads(Method method);

	boolean hasOverloads(ITransformedMethod transformedMethod);

	HashMap<String, ArrayList<Method>> getOverloadedMethods(String fqClassName);
}
