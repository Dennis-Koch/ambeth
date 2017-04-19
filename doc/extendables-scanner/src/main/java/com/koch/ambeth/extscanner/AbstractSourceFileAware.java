package com.koch.ambeth.extscanner;

import java.io.File;
import java.util.regex.Pattern;

import com.koch.classbrowser.java.TypeDescription;

public abstract class AbstractSourceFileAware
		implements ISourceFileAware, IMultiPlatformFeature, ITexFileAware {
	public static final Pattern pattern = Pattern.compile("(?:.*[/\\.])?([^/\\.]+)");

	public TypeDescription javaSrc;

	public TypeDescription javascriptSrc;

	public TypeDescription csharpSrc;

	public String javaFile;

	public String csharpFile;

	public String javascriptFile;

	private File texFile;

	@Override
	public TypeDescription getCsharpSrc() {
		return csharpSrc;
	}

	@Override
	public TypeDescription getJavaSrc() {
		return javaSrc;
	}

	@Override
	public TypeDescription getJavascriptSrc() {
		return javascriptSrc;
	}

	@Override
	public void setJavaFile(File absoluteJavaFile, String relativeJavaFile) {
		javaFile = relativeJavaFile;
	}

	@Override
	public void setCsharpFile(File absoluteCsharpFile, String relativeCsharpFile) {
		csharpFile = relativeCsharpFile;
	}

	@Override
	public void setJavascriptFile(File absoluteJavascriptFile, String relativeJavascriptFile) {
		javascriptFile = relativeJavascriptFile;
	}

	@Override
	public boolean inJavascript() {
		return javascriptSrc != null;
	}

	@Override
	public boolean inJava() {
		return javaSrc != null;
	}

	@Override
	public boolean inCSharp() {
		return csharpSrc != null;
	}

	@Override
	public File getTexFile() {
		return texFile;
	}

	public void setTexFile(File texFile) {
		this.texFile = texFile;
	}
}
