package com.koch.ambeth.util.config;

import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

import com.koch.ambeth.util.collections.ISet;

public interface IProperties {
	IProperties getParent();

	Object get(String key);

	Object get(String key, IProperties initiallyCalledProps);

	/**
	 * Resolve all known properties within a given value string. This will also be done recursively
	 * for properties which contain other properties.<br>
	 * Unknown variables will stay in the original string without further modification (to later put a
	 * string and resolve it)<br>
	 * Examples: {@link PropertyResolverTest}<br>
	 * "exampleString with var ${exampleVar} more content ${${recursivelyResolvedVar}} etc..."
	 *
	 * @param value
	 * @return
	 */
	String resolvePropertyParts(String value);

	String getString(String key);

	String getString(String key, String defaultValue);

	Iterator<Entry<String, Object>> iterator();

	ISet<String> collectAllPropertyKeys();

	void collectAllPropertyKeys(Set<String> allPropertiesSet);
}
