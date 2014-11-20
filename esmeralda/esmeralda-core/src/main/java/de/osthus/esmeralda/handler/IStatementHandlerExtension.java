package de.osthus.esmeralda.handler;

import com.sun.source.tree.StatementTree;

public interface IStatementHandlerExtension<T extends StatementTree>
{
	void handle(T tree);
}
