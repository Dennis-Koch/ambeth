package de.osthus.ambeth.extscanner;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javassist.CtClass;
import de.osthus.ambeth.collections.ArrayList;
import de.osthus.classbrowser.java.TypeDescription;

public class ExtendableEntry implements IMultiPlatformFeature, Comparable<ExtendableEntry>
{
	public static final Pattern pattern = Pattern.compile("(?:.*[/\\.])?([^/\\.]+)");

	public boolean hasArguments;

	public TypeDescription javaSrc;

	public TypeDescription javascriptSrc;

	public TypeDescription csharpSrc;

	public String javaFile;

	public String csharpFile;

	public final String fqExtensionName;

	public final String simpleExtensionName;

	@Override
	public int compareTo(ExtendableEntry o)
	{
		return simpleExtensionName.compareTo(o.simpleExtensionName);
	}

	@Override
	public boolean inJavascript()
	{
		return javascriptSrc != null;
	}

	@Override
	public boolean inJava()
	{
		return javaSrc != null;
	}

	@Override
	public boolean inCSharp()
	{
		return csharpSrc != null;
	}

	public final String simpleName;

	public final String fqName;

	public final ArrayList<CtClass> usedBy = new ArrayList<CtClass>();

	public ExtendableEntry(String fqName, String fqExtensionName)
	{
		this.fqName = fqName;
		this.fqExtensionName = fqExtensionName;
		Matcher matcher = pattern.matcher(fqName);
		if (!matcher.matches())
		{
			throw new IllegalArgumentException(fqName);
		}
		simpleName = matcher.group(1);
		matcher = pattern.matcher(fqExtensionName);
		if (!matcher.matches())
		{
			throw new IllegalArgumentException(fqExtensionName);
		}
		simpleExtensionName = matcher.group(1);
	}
}
