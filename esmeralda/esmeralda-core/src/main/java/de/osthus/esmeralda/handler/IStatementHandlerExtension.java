package de.osthus.esmeralda.handler;

import com.sun.source.tree.Tree;

public interface IStatementHandlerExtension<T extends Tree>
{
	void handle(T tree);

	void handle(T tree, boolean standalone);
}
