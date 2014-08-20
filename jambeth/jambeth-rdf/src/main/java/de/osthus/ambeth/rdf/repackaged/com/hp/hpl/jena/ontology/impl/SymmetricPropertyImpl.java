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

// Package
///////////////
package de.osthus.ambeth.rdf.repackaged.com.hp.hpl.jena.ontology.impl;

// Imports
///////////////
import de.osthus.ambeth.rdf.repackaged.com.hp.hpl.jena.enhanced.*;
import de.osthus.ambeth.rdf.repackaged.com.hp.hpl.jena.graph.*;
import de.osthus.ambeth.rdf.repackaged.com.hp.hpl.jena.ontology.*;

/**
 * <p>
 * Implementation of the symmetric property abstraction
 * </p>
 */
public class SymmetricPropertyImpl extends ObjectPropertyImpl implements SymmetricProperty
{
	// Constants
	// ////////////////////////////////

	// Static variables
	// ////////////////////////////////

	/**
	 * A factory for generating SymmetricProperty facets from nodes in enhanced graphs. Note: should not be invoked directly by user code: use
	 * {@link de.osthus.ambeth.rdf.repackaged.com.hp.hpl.jena.rdf.model.RDFNode#as as()} instead.
	 */
	@SuppressWarnings("hiding")
	public static Implementation factory = new Implementation()
	{
		@Override
		public EnhNode wrap(Node n, EnhGraph eg)
		{
			if (canWrap(n, eg))
			{
				return new SymmetricPropertyImpl(n, eg);
			}
			else
			{
				throw new ConversionException("Cannot convert node " + n + " to SymmetricProperty");
			}
		}

		@Override
		public boolean canWrap(Node node, EnhGraph eg)
		{
			// node will support being an SymmetricProperty facet if it has
			// rdf:type owl:SymmetricProperty or equivalent
			Profile profile = (eg instanceof OntModel) ? ((OntModel) eg).getProfile() : null;
			return (profile != null) && profile.isSupported(node, eg, SymmetricProperty.class);
		}
	};

	// Instance variables
	// ////////////////////////////////

	// Constructors
	// ////////////////////////////////

	/**
	 * <p>
	 * Construct a symmetric property node represented by the given node in the given graph.
	 * </p>
	 * 
	 * @param n
	 *            The node that represents the resource
	 * @param g
	 *            The enh graph that contains n
	 */
	public SymmetricPropertyImpl(Node n, EnhGraph g)
	{
		super(n, g);
	}

	// External signature methods
	// ////////////////////////////////

	// Internal implementation methods
	// ////////////////////////////////

	// ==============================================================================
	// Inner class definitions
	// ==============================================================================

}
