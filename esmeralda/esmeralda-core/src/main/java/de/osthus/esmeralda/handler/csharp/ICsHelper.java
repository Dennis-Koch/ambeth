package de.osthus.esmeralda.handler.csharp;

import de.osthus.esmeralda.ILanguageHelper;

public interface ICsHelper extends ILanguageHelper
{
	@Override
	CsSpecific getLanguageSpecific();
}