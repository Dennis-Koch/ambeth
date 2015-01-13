package de.osthus.esmeralda.handler.js;

import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.HashMap;
import de.osthus.esmeralda.ILanguageSpecific;
import demo.codeanalyzer.common.model.Method;

public class JsSpecific implements ILanguageSpecific
{
	protected HashMap<String, ArrayList<Method>> overloadedMethods;

	public HashMap<String, ArrayList<Method>> getOverloadedMethods()
	{
		return overloadedMethods;
	}

	public void setOverloadedMethods(HashMap<String, ArrayList<Method>> overloadedMethods)
	{
		this.overloadedMethods = overloadedMethods;
	}
}
