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

package de.osthus.ambeth.rdf.repackaged.com.hp.hpl.jena.sparql.vocabulary;

import de.osthus.ambeth.rdf.repackaged.com.hp.hpl.jena.rdf.model.Model;
import de.osthus.ambeth.rdf.repackaged.com.hp.hpl.jena.rdf.model.ModelFactory;
import de.osthus.ambeth.rdf.repackaged.com.hp.hpl.jena.rdf.model.Property;
import de.osthus.ambeth.rdf.repackaged.com.hp.hpl.jena.rdf.model.Resource;

/**
 * Vocabulary definitions from result-set.n3
 * 
 * @author Auto-generated by schemagen on 10 Jun 2006 18:47
 */
public class ResultSetGraphVocab
{
	/**
	 * <p>
	 * The RDF model that holds the vocabulary terms
	 * </p>
	 */
	private static Model m_model = ModelFactory.createDefaultModel();

	/**
	 * <p>
	 * The namespace of the vocabulary as a string
	 * </p>
	 */
	public static final String NS = "http://www.w3.org/2001/sw/DataAccess/tests/result-set#";

	/**
	 * <p>
	 * The namespace of the vocabulary as a string
	 * </p>
	 * 
	 * @see #NS
	 */
	public static String getURI()
	{
		return NS;
	}

	/**
	 * <p>
	 * The namespace of the vocabulary as a resource
	 * </p>
	 */
	public static final Resource NAMESPACE = m_model.createResource(NS);

	/**
	 * <p>
	 * Boolean result
	 * </p>
	 */
	public static final Property p_boolean = m_model.createProperty("http://www.w3.org/2001/sw/DataAccess/tests/result-set#boolean");

	/**
	 * <p>
	 * Variable name
	 * </p>
	 */
	public static final Property value = m_model.createProperty("http://www.w3.org/2001/sw/DataAccess/tests/result-set#value");

	/**
	 * <p>
	 * Variable name
	 * </p>
	 */
	public static final Property variable = m_model.createProperty("http://www.w3.org/2001/sw/DataAccess/tests/result-set#variable");

	/**
	 * <p>
	 * Index for ordered result sets
	 * </p>
	 */
	public static final Property index = m_model.createProperty("http://www.w3.org/2001/sw/DataAccess/tests/result-set#index");

	/**
	 * <p>
	 * Multi-occurrence property associating a result solution (row) resource to a single (variable, value) binding
	 * </p>
	 */
	public static final Property binding = m_model.createProperty("http://www.w3.org/2001/sw/DataAccess/tests/result-set#binding");

	/**
	 * <p>
	 * MultivaluedName of a variable used in the result set
	 * </p>
	 */
	public static final Property resultVariable = m_model.createProperty("http://www.w3.org/2001/sw/DataAccess/tests/result-set#resultVariable");

	/**
	 * <p>
	 * Number of rows in the result table
	 * </p>
	 */
	public static final Property size = m_model.createProperty("http://www.w3.org/2001/sw/DataAccess/tests/result-set#size");

	public static final Property solution = m_model.createProperty("http://www.w3.org/2001/sw/DataAccess/tests/result-set#solution");

	/**
	 * <p>
	 * Class of things that represent a single (variable, value) pairing
	 * </p>
	 */
	public static final Resource ResultBinding = m_model.createResource("http://www.w3.org/2001/sw/DataAccess/tests/result-set#ResultBinding");

	/**
	 * <p>
	 * Class of things that represent a row in the result table - one solution to the query
	 * </p>
	 */
	public static final Resource ResultSolution = m_model.createResource("http://www.w3.org/2001/sw/DataAccess/tests/result-set#ResultSolution");

	/**
	 * <p>
	 * Class of things that represent the result set
	 * </p>
	 */
	public static final Resource ResultSet = m_model.createResource("http://www.w3.org/2001/sw/DataAccess/tests/result-set#ResultSet");

}
