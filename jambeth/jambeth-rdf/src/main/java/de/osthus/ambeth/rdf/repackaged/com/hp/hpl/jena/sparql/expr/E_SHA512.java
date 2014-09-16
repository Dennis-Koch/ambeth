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

package de.osthus.ambeth.rdf.repackaged.com.hp.hpl.jena.sparql.expr;

import java.util.Locale;

import de.osthus.ambeth.rdf.repackaged.com.hp.hpl.jena.sparql.sse.Tags;

public class E_SHA512 extends ExprDigest
{
	private static final String symbol = Tags.tagSHA512.toUpperCase(Locale.ROOT);

	public E_SHA512(Expr expr)
	{
		super(expr, symbol, "SHA-512");
	}

	@Override
	public Expr copy(Expr expr)
	{
		return new E_SHA512(expr);
	}
}
