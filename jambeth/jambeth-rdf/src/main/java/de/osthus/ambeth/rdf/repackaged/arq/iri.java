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

package de.osthus.ambeth.rdf.repackaged.arq;

import java.util.Iterator;

import de.osthus.ambeth.rdf.repackaged.org.apache.jena.iri.IRI;
import de.osthus.ambeth.rdf.repackaged.org.apache.jena.iri.IRIFactory;
import de.osthus.ambeth.rdf.repackaged.org.apache.jena.iri.Violation;
import de.osthus.ambeth.rdf.repackaged.org.apache.jena.riot.system.IRIResolver;

public class iri
{

	public static void main(String[] args)
	{
		IRIFactory iriFactory = IRIFactory.iriImplementation();
		iriFactory = IRIResolver.iriFactory;

		boolean first = true;
		for (String iriStr : args)
		{
			if (!first)
				System.out.println();
			first = false;

			IRI iri = iriFactory.create(iriStr);
			System.out.println(iriStr + " ==> " + iri);
			if (iri.isRelative())
				System.out.println("Relative: " + iri.isRelative());

			Iterator<Violation> vIter = iri.violations(true);
			for (; vIter.hasNext();)
			{
				System.out.println(vIter.next().getShortMessage());
			}
		}
	}

}