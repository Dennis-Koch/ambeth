package de.osthus.ambeth.dot;

import java.io.Closeable;

public interface IDotWriter extends Closeable
{
	IDotNode openNode(Object node);

	IDotNode openNode(String nodeName);

	IDotEdge openEdge(Object fromNode, Object toNode);

	IDotEdge openEdge(String fromNode, String toNode);
}