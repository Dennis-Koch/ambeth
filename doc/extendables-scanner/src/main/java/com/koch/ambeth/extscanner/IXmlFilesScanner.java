package com.koch.ambeth.extscanner;

import java.util.SortedMap;

import com.koch.classbrowser.java.TypeDescription;

public interface IXmlFilesScanner {
	SortedMap<String, TypeDescription> getCsharpTypes();

	SortedMap<String, TypeDescription> getJavaTypes();
}
