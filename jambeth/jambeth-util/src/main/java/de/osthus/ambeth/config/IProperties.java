package de.osthus.ambeth.config;

import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

import de.osthus.ambeth.collections.ISet;

public interface IProperties
{
	IProperties getParent();

	Object get(String key);

	String resolvePropertyParts(String value);

	String getString(String key);

	String getString(String key, String defaultValue);

	Iterator<Entry<String, Object>> iterator();

	ISet<String> collectAllPropertyKeys();

	void collectAllPropertyKeys(Set<String> allPropertiesSet);
}