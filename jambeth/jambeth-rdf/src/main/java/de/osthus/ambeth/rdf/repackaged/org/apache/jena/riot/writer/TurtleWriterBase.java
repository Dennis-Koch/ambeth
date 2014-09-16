/**
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

package de.osthus.ambeth.rdf.repackaged.org.apache.jena.riot.writer;

import java.io.OutputStream;
import java.io.Writer;

import de.osthus.ambeth.rdf.repackaged.org.apache.jena.atlas.io.IndentedWriter;
import de.osthus.ambeth.rdf.repackaged.org.apache.jena.riot.Lang;
import de.osthus.ambeth.rdf.repackaged.org.apache.jena.riot.system.IRIResolver;
import de.osthus.ambeth.rdf.repackaged.org.apache.jena.riot.system.PrefixMap;
import de.osthus.ambeth.rdf.repackaged.org.apache.jena.riot.system.RiotLib;

import de.osthus.ambeth.rdf.repackaged.com.hp.hpl.jena.graph.Graph;
import de.osthus.ambeth.rdf.repackaged.com.hp.hpl.jena.sparql.util.Context;

public abstract class TurtleWriterBase extends WriterGraphRIOTBase
{
	@Override
	public Lang getLang()
	{
		return Lang.TURTLE;
	}

	@Override
	public void write(Writer out, Graph graph, PrefixMap prefixMap, String baseURI, Context context)
	{
		IndentedWriter iOut = RiotLib.create(out);
		output$(iOut, graph, prefixMap, baseURI);
	}

	@Override
	public void write(OutputStream out, Graph graph, PrefixMap prefixMap, String baseURI, Context context)
	{
		IndentedWriter iOut = new IndentedWriter(out);
		output$(iOut, graph, prefixMap, baseURI);
	}

	private void output$(IndentedWriter iOut, Graph graph, PrefixMap prefixMap, String baseURI)
	{
		if (baseURI != null)
			baseURI = IRIResolver.resolveString(baseURI);
		output(iOut, graph, prefixMap, baseURI);
		iOut.flush();
	}

	protected abstract void output(IndentedWriter iOut, Graph graph, PrefixMap prefixMap, String baseURI);
}