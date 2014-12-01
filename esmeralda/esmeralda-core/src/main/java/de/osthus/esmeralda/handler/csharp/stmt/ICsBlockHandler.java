package de.osthus.esmeralda.handler.csharp.stmt;

import com.sun.source.tree.BlockTree;

public interface ICsBlockHandler
{
	void writeBlockContentWithoutIntendation(BlockTree blockTree);
}