package de.osthus.esmeralda.handler.js.stmt;

import com.sun.source.tree.StatementTree;

import de.osthus.esmeralda.handler.AbstractStatementHandler;
import de.osthus.esmeralda.handler.IStatementHandlerExtension;
import de.osthus.esmeralda.misc.Lang;

public abstract class AbstractJsStatementHandler<T extends StatementTree> extends AbstractStatementHandler<T> implements IStatementHandlerExtension<T>
{
	public AbstractJsStatementHandler()
	{
		super(Lang.JS);
	}
}