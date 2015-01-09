package de.osthus.esmeralda.handler.js;

import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.HashMap;
import de.osthus.esmeralda.handler.IMethodHandler;
import demo.codeanalyzer.common.model.Method;

public interface IJsMethodHandler extends IMethodHandler
{
	// FIXME temp. workaround
	void handle(HashMap<String, ArrayList<Method>> overloadMethods);
}
