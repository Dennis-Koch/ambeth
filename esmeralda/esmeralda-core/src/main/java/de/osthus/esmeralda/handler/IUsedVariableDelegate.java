package de.osthus.esmeralda.handler;

import de.osthus.esmeralda.IConversionContext;
import de.osthus.esmeralda.ILanguageHelper;
import de.osthus.esmeralda.misc.IWriter;

public interface IUsedVariableDelegate
{
	void invoke(IVariable usedVariable, boolean firstVariable, IConversionContext context, ILanguageHelper languageHelper, IWriter writer);
}
