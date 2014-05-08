package de.osthus.ambeth.xml.postprocess;

import de.osthus.ambeth.collections.IMap;
import de.osthus.ambeth.collections.ISet;
import de.osthus.ambeth.xml.IWriter;

public interface IPostProcessWriter extends IWriter
{
	ISet<Object> getSubstitutedEntities();

	IMap<Object, Integer> getMutableToIdMap();

	IMap<Object, Integer> getImmutableToIdMap();
}
