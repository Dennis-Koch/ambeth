package de.osthus.esmeralda.handler;

import com.sun.source.tree.StatementTree;

public interface IStatementHandlerRegistry
{
	<T extends StatementTree> IStatementHandlerExtension<T> get(String key);
}
