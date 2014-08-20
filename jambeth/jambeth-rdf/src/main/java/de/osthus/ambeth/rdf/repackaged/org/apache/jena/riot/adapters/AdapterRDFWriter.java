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

package de.osthus.ambeth.rdf.repackaged.org.apache.jena.riot.adapters;

import java.io.OutputStream;
import java.io.Writer;

import de.osthus.ambeth.rdf.repackaged.org.apache.jena.riot.Lang;
import de.osthus.ambeth.rdf.repackaged.org.apache.jena.riot.system.PrefixMap;
import de.osthus.ambeth.rdf.repackaged.org.apache.jena.riot.writer.WriterGraphRIOTBase;

import de.osthus.ambeth.rdf.repackaged.com.hp.hpl.jena.graph.Graph;
import de.osthus.ambeth.rdf.repackaged.com.hp.hpl.jena.rdf.model.ModelFactory;
import de.osthus.ambeth.rdf.repackaged.com.hp.hpl.jena.rdf.model.RDFWriter;
import de.osthus.ambeth.rdf.repackaged.com.hp.hpl.jena.sparql.util.Context;

/** Wrapper for using old-style Jena RDFWriters in RIOT. */

public abstract class AdapterRDFWriter extends WriterGraphRIOTBase
{
	protected abstract RDFWriter create();

	@Override
	public abstract Lang getLang();

	@Override
	public void write(Writer out, Graph graph, PrefixMap prefixMap, String baseURI, Context context)
	{
		RDFWriter w = create();
		w.write(ModelFactory.createModelForGraph(graph), out, baseURI);
	}

	@Override
	public void write(OutputStream out, Graph graph, PrefixMap prefixMap, String baseURI, Context context)
	{
		RDFWriter w = create();
		w.write(ModelFactory.createModelForGraph(graph), out, baseURI);
	}
}
