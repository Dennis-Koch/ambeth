package com.koch.ambeth.xml.postprocess;

import com.koch.ambeth.util.collections.IMap;
import com.koch.ambeth.util.collections.ISet;
import com.koch.ambeth.xml.IWriter;

public interface IPostProcessWriter extends IWriter
{
	ISet<Object> getSubstitutedEntities();

	IMap<Object, Integer> getMutableToIdMap();

	IMap<Object, Integer> getImmutableToIdMap();
}
