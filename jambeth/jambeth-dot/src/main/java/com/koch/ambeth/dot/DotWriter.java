package com.koch.ambeth.dot;

/*-
 * #%L
 * jambeth-dot
 * %%
 * Copyright (C) 2017 Koch Softwaredevelopment
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 * #L%
 */

import java.io.Closeable;
import java.io.IOException;
import java.io.Writer;

import com.koch.ambeth.util.collections.IdentityHashMap;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;

public class DotWriter implements Closeable, IDotWriter {
	protected Writer writer;

	protected boolean attributeState;

	protected final IdentityHashMap<Object, String> handleToNodeIdMap = new IdentityHashMap<>();

	public DotWriter(Writer writer) {
		this.writer = writer;
		write("digraph G {");
	}

	public void attribute(String key, String value) {
		if (!attributeState) {
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
	public void close() throws IOException {
		write("\n}");
		writer.close();
	}

	public IDotWriter endEdge() {
		if (attributeState) {
			write(']');
			attributeState = false;
		}
		write(';');
		return this;
	}

	public IDotWriter endNode() {
		if (attributeState) {
			write(']');
			attributeState = false;
		}
		write(';');
		return this;
	}

	protected String getNodeName(Object handle) {
		String nodeId = handleToNodeIdMap.get(handle);
		if (nodeId != null) {
			return nodeId;
		}
		nodeId = Integer.valueOf(handleToNodeIdMap.size() + 1).toString();
		handleToNodeIdMap.put(handle, nodeId);
		return nodeId;
	}

	@Override
	public boolean hasNode(Object handle) {
		return handleToNodeIdMap.containsKey(handle);
	}

	@Override
	public IDotEdge openEdge(Object fromNode, Object toNode) {
		return openEdge(getNodeName(fromNode), getNodeName(toNode));
	}

	@Override
	public IDotEdge openEdge(String fromNode, String toNode) {
		write("\n\t");
		write(fromNode);
		write(" -> ");
		write(toNode);
		return new IDotEdge() {
			@Override
			public IDotEdge attribute(String key, String value) {
				DotWriter.this.attribute(key, value);
				return this;
			}

			@Override
			public IDotWriter endEdge() {
				return DotWriter.this.endEdge();
			}
		};
	}

	@Override
	public IDotNode openNode(Object node) {
		return openNode(getNodeName(node));
	}

	@Override
	public IDotNode openNode(String nodeName) {
		write("\n\t");
		write(nodeName);
		return new IDotNode() {
			@Override
			public IDotNode attribute(String key, String value) {
				DotWriter.this.attribute(key, value);
				return this;
			}

			@Override
			public IDotWriter endNode() {
				return DotWriter.this.endNode();
			}
		};
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + ": " + writer.toString();
	}

	protected void write(char oneChar) {
		try {
			writer.append(oneChar);
		} catch (Exception e) {
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	protected void write(String str) {
		try {
			writer.append(str);
		} catch (Exception e) {
			throw RuntimeExceptionUtil.mask(e);
		}
	}
}
