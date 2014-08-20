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

package de.osthus.ambeth.rdf.repackaged.com.hp.hpl.jena.sparql.pfunction.library;

import java.util.Arrays;
import java.util.Iterator;

import de.osthus.ambeth.rdf.repackaged.org.apache.jena.atlas.iterator.Iter;
import de.osthus.ambeth.rdf.repackaged.org.apache.jena.atlas.iterator.Transform;
import de.osthus.ambeth.rdf.repackaged.org.apache.jena.atlas.lib.StrUtils;

import de.osthus.ambeth.rdf.repackaged.com.hp.hpl.jena.graph.Node;
import de.osthus.ambeth.rdf.repackaged.com.hp.hpl.jena.graph.NodeFactory;
import de.osthus.ambeth.rdf.repackaged.com.hp.hpl.jena.sparql.core.Var;
import de.osthus.ambeth.rdf.repackaged.com.hp.hpl.jena.sparql.engine.ExecutionContext;
import de.osthus.ambeth.rdf.repackaged.com.hp.hpl.jena.sparql.engine.QueryIterator;
import de.osthus.ambeth.rdf.repackaged.com.hp.hpl.jena.sparql.engine.binding.Binding;
import de.osthus.ambeth.rdf.repackaged.com.hp.hpl.jena.sparql.engine.binding.BindingFactory;
import de.osthus.ambeth.rdf.repackaged.com.hp.hpl.jena.sparql.engine.iterator.QueryIterPlainWrapper;
import de.osthus.ambeth.rdf.repackaged.com.hp.hpl.jena.sparql.expr.ExprEvalException;
import de.osthus.ambeth.rdf.repackaged.com.hp.hpl.jena.sparql.pfunction.PFuncSimpleAndList;
import de.osthus.ambeth.rdf.repackaged.com.hp.hpl.jena.sparql.pfunction.PropFuncArg;

/**
 * Property function that requires the subject to be unbound, and the object to contain a list of two items, the first of which is a string to be split, and the
 * second is a regular expression denoting the split point. The subject variable is bound for each result of the split, and each result has the whitespace
 * trimmed from it.
 */
public class strSplit extends PFuncSimpleAndList
{
	@Override
	public QueryIterator execEvaluated(final Binding binding, final Node subject, final Node predicate, final PropFuncArg object, final ExecutionContext execCxt)
	{
		if (!Var.isVar(subject))
			throw new ExprEvalException("Subject is not a variable (" + subject + ")");

		if (object.getArgListSize() != 2)
			throw new ExprEvalException("Object list must contain exactly two arguments, the string to split and a regular expression");

		String s = object.getArg(0).getLiteralLexicalForm();
		String regex = object.getArg(1).getLiteralLexicalForm();

		final Var subjectVar = Var.alloc(subject);

		// StrUtils will also trim whitespace
		String[] tokens = StrUtils.split(s, regex);
		Iterator<Binding> it = Iter.map(Arrays.asList(tokens).iterator(), new Transform<String, Binding>()
		{
			@Override
			public Binding convert(String item)
			{
				return BindingFactory.binding(binding, subjectVar, NodeFactory.createLiteral(item));
			}
		});
		return new QueryIterPlainWrapper(it, execCxt);
	}

}
