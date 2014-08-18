/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.osthus.ambeth.rdf.repackaged.org.apache.jena.riot.out;

import java.io.OutputStream;

import de.osthus.ambeth.rdf.repackaged.org.apache.jena.atlas.io.AWriter;
import de.osthus.ambeth.rdf.repackaged.org.apache.jena.atlas.io.IO;
import de.osthus.ambeth.rdf.repackaged.org.apache.jena.atlas.lib.Sink;
import de.osthus.ambeth.rdf.repackaged.org.apache.jena.riot.RDFDataMgr;
import de.osthus.ambeth.rdf.repackaged.org.apache.jena.riot.system.Prologue;
import de.osthus.ambeth.rdf.repackaged.org.apache.jena.riot.writer.WriterStreamRDFTuples;

import de.osthus.ambeth.rdf.repackaged.com.hp.hpl.jena.graph.Node;
import de.osthus.ambeth.rdf.repackaged.com.hp.hpl.jena.sparql.core.Quad;

/**
 * A class that print quads, N-Quads style *
 * 
 * @see WriterStreamRDFTuples
 * @see RDFDataMgr#writeTriples
 */
public class SinkQuadOutput implements Sink<Quad>
{
	private Prologue prologue = null;
	private AWriter out;
	private NodeToLabel labelPolicy = null;
	private NodeFormatter nodeFmt = new NodeFormatterNT();

	public SinkQuadOutput(OutputStream outs, Prologue prologue, NodeToLabel labels)
	{
		out = IO.wrapUTF8(outs);
		setPrologue(prologue);
		setLabelPolicy(labels);
	}

	// Need to do this later sometimes to sort out the plumbing.
	public void setPrologue(Prologue prologue)
	{
		this.prologue = prologue;
	}

	public void setLabelPolicy(NodeToLabel labels)
	{
		this.labelPolicy = labels;
	}

	@Override
	public void send(Quad quad)
	{
		Node s = quad.getSubject();
		Node p = quad.getPredicate();
		Node o = quad.getObject();
		Node g = quad.getGraph();

		nodeFmt.format(out, s);
		out.print(" ");
		nodeFmt.format(out, p);
		out.print(" ");
		nodeFmt.format(out, o);

		if (outputGraphSlot(g))
		{
			out.print(" ");
			nodeFmt.format(out, g);
		}

		out.print(" .\n");
	}

	private static boolean outputGraphSlot(Node g)
	{
		return (g != null && g != Quad.tripleInQuad && !Quad.isDefaultGraph(g));
	}

	@Override
	public void flush()
	{
		IO.flush(out);
	}

	@Override
	public void close()
	{
		IO.flush(out);
		// Don't close the underlying OutputStream that was passed in.
	}
}
