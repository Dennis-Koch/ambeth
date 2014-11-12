package de.osthus.ambeth.extscanner;

import java.io.File;

import de.osthus.classbrowser.java.TypeDescription;

public interface ISourceFileAware
{
	TypeDescription getJavaSrc();

	TypeDescription getCsharpSrc();

	TypeDescription getJavascriptSrc();

	void setJavaFile(File absoluteJavaFile, String relativeJavaFile);

	void setCsharpFile(File absoluteCsharpFile, String relativeCsharpFile);

	void setJavascriptFile(File absoluteJavascriptFile, String relativeJavascriptFile);
}
