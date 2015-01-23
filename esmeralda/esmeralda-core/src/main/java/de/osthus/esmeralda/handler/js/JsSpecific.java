package de.osthus.esmeralda.handler.js;

import de.osthus.ambeth.collections.HashSet;
import de.osthus.esmeralda.ILanguageSpecific;

public class JsSpecific implements ILanguageSpecific
{
	private final HashSet<String> methodScopeVars = new HashSet<>();

	public HashSet<String> getMethodScopeVars()
	{
		return methodScopeVars;
	}
}
