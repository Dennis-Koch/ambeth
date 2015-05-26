package de.osthus.ambeth.dot;

import java.io.Closeable;
import java.io.IOException;
import java.io.Writer;

import de.osthus.ambeth.collections.IdentityHashMap;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;

public class DotWriter implements Closeable, IDotWriter
{
	protected Writer writer;

	protected boolean attributeState;

	protected final IdentityHashMap<Object, String> handleToNodeIdMap = new IdentityHashMap<Object, String>();

	public DotWriter(Writer writer)
	{
		this.writer = writer;
		write("digraph G {");
	}

	protected String getNodeName(Object handle)
	{
		String nodeId = handleToNodeIdMap.get(handle);
		if (nodeId != null)
		{
			return nodeId;
		}
		nodeId = Integer.valueOf(handleToNodeIdMap.size() + 1).toString();
		handleToNodeIdMap.put(handle, nodeId);
		return nodeId;
	}

	protected void write(char oneChar)
	{
		try
		{
			writer.write(oneChar);
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	protected void write(String str)
	{
		try
		{
			writer.write(str);
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	public void attribute(String key, String value)
	{
		if (!attributeState)
		{
			write(" [");
			attributeState = true;
		}
		write(' ');
		write(key);
		write("=\"");
		write(value);
		write('\"');
	}

	@Override
	public IDotNode openNode(Object node)
	{
		return openNode(getNodeName(node));
	}

	@Override
	public IDotNode openNode(String nodeName)
	{
		write("\n\t");
		write(nodeName);
		return new IDotNode()
		{
			@Override
			public IDotWriter endNode()
			{
				return DotWriter.this.endNode();
			}

			@Override
			public IDotNode attribute(String key, String value)
			{
				DotWriter.this.attribute(key, value);
				return this;
			}
		};
	}

	public IDotWriter endNode()
	{
		if (attributeState)
		{
			write(']');
			attributeState = false;
		}
		write(';');
		return this;
	}

	@Override
	public IDotEdge openEdge(Object fromNode, Object toNode)
	{
		return openEdge(getNodeName(fromNode), getNodeName(toNode));
	}

	@Override
	public IDotEdge openEdge(String fromNode, String toNode)
	{
		write("\n\t");
		write(fromNode);
		write(" -> ");
		write(toNode);
		return new IDotEdge()
		{
			@Override
			public IDotWriter endEdge()
			{
				return DotWriter.this.endEdge();
			}

			@Override
			public IDotEdge attribute(String key, String value)
			{
				DotWriter.this.attribute(key, value);
				return this;
			}
		};
	}

	public IDotWriter endEdge()
	{
		if (attributeState)
		{
			write(']');
			attributeState = false;
		}
		write(';');
		return this;
	}

	@Override
	public void close() throws IOException
	{
		write("\n}");
		writer.close();
	}

}
