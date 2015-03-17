package de.osthus.esmeralda.handler;

import com.sun.source.tree.Tree;

public interface IStatementHandlerRegistry
{
	<T extends Tree> IStatementHandlerExtension<T> getExtension(String key);
}
