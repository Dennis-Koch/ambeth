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

package de.osthus.ambeth.rdf.repackaged.com.hp.hpl.jena.sparql.core.assembler;

import de.osthus.ambeth.rdf.repackaged.com.hp.hpl.jena.assembler.Assembler;
import de.osthus.ambeth.rdf.repackaged.com.hp.hpl.jena.assembler.Mode;
import de.osthus.ambeth.rdf.repackaged.com.hp.hpl.jena.query.Dataset;
import de.osthus.ambeth.rdf.repackaged.com.hp.hpl.jena.rdf.model.Resource;
import de.osthus.ambeth.rdf.repackaged.com.hp.hpl.jena.update.GraphStore;
import de.osthus.ambeth.rdf.repackaged.com.hp.hpl.jena.update.GraphStoreFactory;

public class GraphStoreAssembler extends DatasetAssembler
{
	public static Resource getType()
	{
		return DatasetAssemblerVocab.tGraphStore;
	}

	@Override
	public Object open(Assembler a, Resource root, Mode mode)
	{
		// Same vocabulary as datasets.
		// Have dispatched on type by now so can just call the dataset code to build a Dataset.

		GraphStore gs = null;
		Object ds = super.open(a, root, mode);
		if (ds instanceof Dataset)
			gs = GraphStoreFactory.create((Dataset) ds);
		else
			throw new DatasetAssemblerException(root, "Not a graph store");

		return gs;
	}
}
