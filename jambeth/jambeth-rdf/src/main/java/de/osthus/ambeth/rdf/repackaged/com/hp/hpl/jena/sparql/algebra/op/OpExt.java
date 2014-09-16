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

package de.osthus.ambeth.rdf.repackaged.com.hp.hpl.jena.sparql.algebra.op;

import de.osthus.ambeth.rdf.repackaged.org.apache.jena.atlas.io.IndentedWriter;

import de.osthus.ambeth.rdf.repackaged.com.hp.hpl.jena.sparql.ARQNotImplemented;
import de.osthus.ambeth.rdf.repackaged.com.hp.hpl.jena.sparql.algebra.Op;
import de.osthus.ambeth.rdf.repackaged.com.hp.hpl.jena.sparql.algebra.OpVisitor;
import de.osthus.ambeth.rdf.repackaged.com.hp.hpl.jena.sparql.algebra.Transform;
import de.osthus.ambeth.rdf.repackaged.com.hp.hpl.jena.sparql.engine.ExecutionContext;
import de.osthus.ambeth.rdf.repackaged.com.hp.hpl.jena.sparql.engine.QueryIterator;
import de.osthus.ambeth.rdf.repackaged.com.hp.hpl.jena.sparql.serializer.SerializationContext;
import de.osthus.ambeth.rdf.repackaged.com.hp.hpl.jena.sparql.sse.Tags;
import de.osthus.ambeth.rdf.repackaged.com.hp.hpl.jena.sparql.sse.writers.WriterLib;

/**
 * Marker for extension points Execution will be per-engine specific
 */
public abstract class OpExt extends OpBase
{
	protected final String tag;

	public OpExt(String name)
	{
		tag = name;
	}

	/**
	 * Return an op that will used by query processing algorithms such as optimization. This method returns a non-extension Op expression that is the equivalent
	 * SPARQL expression. For example, this is the Op replaced by this extension node.
	 */
	public abstract Op effectiveOp();

	/**
	 * Evaluate the op, given a stream of bindings as input Throw UnsupportedOperationException if this OpExt is not executeable.
	 */
	public abstract QueryIterator eval(QueryIterator input, ExecutionContext execCxt);

	@Override
	public final String getName()
	{
		return tag;
	}

	@Override
	public final void visit(OpVisitor opVisitor)
	{
		opVisitor.visit(this);
	}

	@Override
	public void output(IndentedWriter out, SerializationContext sCxt)
	{
		int line = out.getRow();

		if (false)
		{
			// Write in (ext NAME ...) form.
			WriterLib.start(out, Tags.tagExt, WriterLib.NoNL);
			out.print(getName());
			out.print(" ");
			outputArgs(out, sCxt);
			WriterLib.finish(out, Tags.tagExt);
		}
		else
		{
			// Write in (NAME ...) form.
			WriterLib.start(out, tag, WriterLib.NoNL);
			outputArgs(out, sCxt);
			WriterLib.finish(out, tag);
		}

		if (line != out.getRow())
			out.ensureStartOfLine();
	}

	public Op apply(Transform transform)
	{
		throw new ARQNotImplemented("OpExt.appy(Transform)");
	}

	// /** Return the sub tag - must match the builder */
	// public abstract String getSubTag() ;

	/** Output the arguments in legal SSE format. Multiple items, whitespace separated */
	public abstract void outputArgs(IndentedWriter out, SerializationContext sCxt);
}