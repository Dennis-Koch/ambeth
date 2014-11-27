package de.osthus.esmeralda.handler.csharp.stmt;

import com.sun.source.tree.BlockTree;

import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;

public interface ICsBlockHandler
{

	void writeBlockContentWithoutIntendation(BlockTree blockTree);

}