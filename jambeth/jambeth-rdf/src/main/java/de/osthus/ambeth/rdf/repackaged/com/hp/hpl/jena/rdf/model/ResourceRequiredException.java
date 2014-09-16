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

package de.osthus.ambeth.rdf.repackaged.com.hp.hpl.jena.rdf.model;

import de.osthus.ambeth.rdf.repackaged.com.hp.hpl.jena.graph.*;
import de.osthus.ambeth.rdf.repackaged.com.hp.hpl.jena.shared.*;

/**
 * Exception to throw when an RDFNode required to be a Resource isn't, or when a Node supposed to be a resource isn't.
 */
public class ResourceRequiredException extends JenaException
{
	public ResourceRequiredException(RDFNode n)
	{
		this(n.asNode());
	}

	public ResourceRequiredException(Node n)
	{
		super(n.toString(PrefixMapping.Extended, true));
	}
}