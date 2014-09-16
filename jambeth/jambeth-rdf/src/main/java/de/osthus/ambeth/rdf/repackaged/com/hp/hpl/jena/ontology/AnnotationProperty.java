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
package de.osthus.ambeth.rdf.repackaged.com.hp.hpl.jena.ontology;

// Imports
///////////////
import de.osthus.ambeth.rdf.repackaged.com.hp.hpl.jena.rdf.model.*;

/**
 * <p>
 * Interface that represents the category of annotation properties in an ontology language. Annotation properties are distinguished in some languages (such as
 * OWL) - in order to maintain theoretical properties of the language, which depend on clean separation of syntactic categories. Annotation properties may not
 * be used in property expressions. There is no guarantee that a given language will have any annotation properties.
 * </p>
 */
public interface AnnotationProperty extends OntProperty, Property
{
	// Constants
	// ////////////////////////////////

	// External signature methods
	// ////////////////////////////////

}