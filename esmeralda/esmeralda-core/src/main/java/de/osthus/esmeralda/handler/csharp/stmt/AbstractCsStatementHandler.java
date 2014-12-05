package de.osthus.esmeralda.handler.csharp.stmt;

import com.sun.source.tree.StatementTree;

import de.osthus.esmeralda.handler.AbstractStatementHandler;
import de.osthus.esmeralda.handler.IStatementHandlerExtension;
import de.osthus.esmeralda.handler.csharp.ICsHelper;
import de.osthus.esmeralda.misc.Lang;

public abstract class AbstractCsStatementHandler<T extends StatementTree> extends AbstractStatementHandler<T> implements IStatementHandlerExtension<T>
{
	public AbstractCsStatementHandler()
	{
		language = Lang.C_SHARP;
	}

	public void setLanguageHelper(ICsHelper languageHelper)
	{
		this.languageHelper = languageHelper;
	}
}