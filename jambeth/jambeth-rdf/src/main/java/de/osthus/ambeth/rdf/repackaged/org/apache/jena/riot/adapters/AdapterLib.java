/**
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

package de.osthus.ambeth.rdf.repackaged.org.apache.jena.riot.adapters;

import java.util.Iterator;

import de.osthus.ambeth.rdf.repackaged.com.hp.hpl.jena.sparql.util.Utils;
import de.osthus.ambeth.rdf.repackaged.org.apache.jena.atlas.web.TypedInputStream;
import de.osthus.ambeth.rdf.repackaged.org.apache.jena.riot.RiotException;
import de.osthus.ambeth.rdf.repackaged.org.apache.jena.riot.system.stream.LocationMapper;
import de.osthus.ambeth.rdf.repackaged.org.apache.jena.riot.system.stream.Locator;
import de.osthus.ambeth.rdf.repackaged.org.apache.jena.riot.system.stream.LocatorClassLoader;
import de.osthus.ambeth.rdf.repackaged.org.apache.jena.riot.system.stream.LocatorFile;
import de.osthus.ambeth.rdf.repackaged.org.apache.jena.riot.system.stream.LocatorHTTP;
import de.osthus.ambeth.rdf.repackaged.org.apache.jena.riot.system.stream.LocatorZip;

class AdapterLib
{
	public static de.osthus.ambeth.rdf.repackaged.com.hp.hpl.jena.util.TypedStream convert(TypedInputStream in)
	{
		return new de.osthus.ambeth.rdf.repackaged.com.hp.hpl.jena.util.TypedStream(in, in.getContentType(), in.getCharset());
	}

	public static LocationMapper copyConvert(de.osthus.ambeth.rdf.repackaged.com.hp.hpl.jena.util.LocationMapper locMap)
	{
		LocationMapper lmap2 = new LocationMapper();
		if (locMap == null)
		{
			return null;
		}

		Iterator<String> sIter1 = locMap.listAltEntries();
		for (; sIter1.hasNext();)
		{
			String k = sIter1.next();
			lmap2.addAltEntry(k, locMap.getAltEntry(k));
		}

		Iterator<String> sIter2 = locMap.listAltPrefixes();

		for (; sIter2.hasNext();)
		{
			String k = sIter2.next();
			lmap2.addAltEntry(k, locMap.getAltPrefix(k));
		}
		return lmap2;
	}

	public static Locator convert(de.osthus.ambeth.rdf.repackaged.com.hp.hpl.jena.util.Locator oldloc)
	{
		if (oldloc instanceof de.osthus.ambeth.rdf.repackaged.com.hp.hpl.jena.util.LocatorFile)
		{
			de.osthus.ambeth.rdf.repackaged.com.hp.hpl.jena.util.LocatorFile lFile = (de.osthus.ambeth.rdf.repackaged.com.hp.hpl.jena.util.LocatorFile) oldloc;
			return new LocatorFile(lFile.getDir());
		}
		if (oldloc instanceof de.osthus.ambeth.rdf.repackaged.com.hp.hpl.jena.util.LocatorClassLoader)
		{
			de.osthus.ambeth.rdf.repackaged.com.hp.hpl.jena.util.LocatorClassLoader classLoc = (de.osthus.ambeth.rdf.repackaged.com.hp.hpl.jena.util.LocatorClassLoader) oldloc;
			return new LocatorClassLoader(classLoc.getClassLoader());
		}
		if (oldloc instanceof de.osthus.ambeth.rdf.repackaged.com.hp.hpl.jena.util.LocatorURL)
		{
			return new LocatorHTTP();
		}
		if (oldloc instanceof de.osthus.ambeth.rdf.repackaged.com.hp.hpl.jena.util.LocatorZip)
		{
			de.osthus.ambeth.rdf.repackaged.com.hp.hpl.jena.util.LocatorZip zipLoc = (de.osthus.ambeth.rdf.repackaged.com.hp.hpl.jena.util.LocatorZip) oldloc;
			return new LocatorZip(zipLoc.getZipFileName());
		}

		throw new RiotException("Unrecognized Locator: " + Utils.className(oldloc));
	}
}
