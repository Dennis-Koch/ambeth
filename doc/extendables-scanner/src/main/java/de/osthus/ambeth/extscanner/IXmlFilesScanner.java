package de.osthus.ambeth.extscanner;

import java.util.SortedMap;

import de.osthus.classbrowser.java.TypeDescription;

public interface IXmlFilesScanner
{
	SortedMap<String, TypeDescription> getCsharpTypes();

	SortedMap<String, TypeDescription> getJavaTypes();
}