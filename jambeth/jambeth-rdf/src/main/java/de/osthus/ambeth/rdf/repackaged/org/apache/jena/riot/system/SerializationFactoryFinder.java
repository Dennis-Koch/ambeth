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

package de.osthus.ambeth.rdf.repackaged.org.apache.jena.riot.system;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;

import de.osthus.ambeth.rdf.repackaged.org.apache.jena.atlas.data.SerializationFactory;
import de.osthus.ambeth.rdf.repackaged.org.apache.jena.atlas.lib.Sink;
import de.osthus.ambeth.rdf.repackaged.org.apache.jena.riot.lang.LabelToNode;
import de.osthus.ambeth.rdf.repackaged.org.apache.jena.riot.lang.LangNQuads;
import de.osthus.ambeth.rdf.repackaged.org.apache.jena.riot.lang.LangNTriples;
import de.osthus.ambeth.rdf.repackaged.org.apache.jena.riot.out.NodeToLabel;
import de.osthus.ambeth.rdf.repackaged.org.apache.jena.riot.out.SinkQuadOutput;
import de.osthus.ambeth.rdf.repackaged.org.apache.jena.riot.out.SinkTripleOutput;
import de.osthus.ambeth.rdf.repackaged.org.apache.jena.riot.system.IRIResolver;
import de.osthus.ambeth.rdf.repackaged.org.apache.jena.riot.system.ParserProfileBase;
import de.osthus.ambeth.rdf.repackaged.org.apache.jena.riot.system.Prologue;
import de.osthus.ambeth.rdf.repackaged.org.apache.jena.riot.tokens.Tokenizer;
import de.osthus.ambeth.rdf.repackaged.org.apache.jena.riot.tokens.TokenizerFactory;

import de.osthus.ambeth.rdf.repackaged.com.hp.hpl.jena.graph.Triple;
import de.osthus.ambeth.rdf.repackaged.com.hp.hpl.jena.sparql.core.Quad;
import de.osthus.ambeth.rdf.repackaged.com.hp.hpl.jena.sparql.engine.binding.Binding;
import de.osthus.ambeth.rdf.repackaged.com.hp.hpl.jena.sparql.engine.binding.BindingInputStream;
import de.osthus.ambeth.rdf.repackaged.com.hp.hpl.jena.sparql.engine.binding.BindingOutputStream;

public class SerializationFactoryFinder
{
	public static SerializationFactory<Binding> bindingSerializationFactory()
	{
		return new SerializationFactory<Binding>()
		{
			@Override
			public Sink<Binding> createSerializer(OutputStream out)
			{
				return new BindingOutputStream(out);
			}

			@Override
			public Iterator<Binding> createDeserializer(InputStream in)
			{
				return new BindingInputStream(in);
			}

			@Override
			public long getEstimatedMemorySize(Binding item)
			{
				// TODO traverse the binding, and add up the variable + node sizes + object overhead
				return 0;
			}
		};
	}

	public static SerializationFactory<Triple> tripleSerializationFactory()
	{
		return new SerializationFactory<Triple>()
		{
			@Override
			public Sink<Triple> createSerializer(OutputStream out)
			{
				return new SinkTripleOutput(out, null, NodeToLabel.createBNodeByLabelEncoded());
			}

			@Override
			public Iterator<Triple> createDeserializer(InputStream in)
			{
				Tokenizer tokenizer = TokenizerFactory.makeTokenizerASCII(in);
				ParserProfileBase profile = new ParserProfileBase(new Prologue(null, IRIResolver.createNoResolve()), null, LabelToNode.createUseLabelEncoded());
				LangNTriples parser = new LangNTriples(tokenizer, profile, null);
				return parser;
			}

			@Override
			public long getEstimatedMemorySize(Triple item)
			{
				// TODO
				return 0;
			}
		};
	}

	public static SerializationFactory<Quad> quadSerializationFactory()
	{
		return new SerializationFactory<Quad>()
		{
			@Override
			public Sink<Quad> createSerializer(OutputStream out)
			{
				return new SinkQuadOutput(out, null, NodeToLabel.createBNodeByLabelEncoded());
			}

			@Override
			public Iterator<Quad> createDeserializer(InputStream in)
			{
				Tokenizer tokenizer = TokenizerFactory.makeTokenizerASCII(in);
				ParserProfileBase profile = new ParserProfileBase(new Prologue(null, IRIResolver.createNoResolve()), null, LabelToNode.createUseLabelEncoded());
				LangNQuads parser = new LangNQuads(tokenizer, profile, null);
				return parser;
			}

			@Override
			public long getEstimatedMemorySize(Quad item)
			{
				// TODO
				return 0;
			}
		};
	}
}