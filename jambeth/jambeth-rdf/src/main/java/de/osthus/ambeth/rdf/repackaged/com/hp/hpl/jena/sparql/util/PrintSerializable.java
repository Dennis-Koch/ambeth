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

package de.osthus.ambeth.rdf.repackaged.com.hp.hpl.jena.sparql.util;

import de.osthus.ambeth.rdf.repackaged.org.apache.jena.atlas.io.IndentedWriter;
import de.osthus.ambeth.rdf.repackaged.org.apache.jena.atlas.io.Printable;

import de.osthus.ambeth.rdf.repackaged.com.hp.hpl.jena.shared.PrefixMapping;
import de.osthus.ambeth.rdf.repackaged.com.hp.hpl.jena.sparql.serializer.SerializationContext;

public interface PrintSerializable extends Printable
{
	// Eventually replace by output support in RIOT
	public void output(IndentedWriter out, SerializationContext sCxt);

	public String toString(PrefixMapping pmap);
}
