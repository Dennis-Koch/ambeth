package de.osthus.ambeth.extscanner;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javassist.CtClass;
import de.osthus.ambeth.collections.ArrayList;

public class ExtendableEntry
{
	public static final Pattern pattern = Pattern.compile("(?:.*[/\\.])?([^/\\.]+)");

	public CtClass javaType;

	public String fqExtensionName;

	public String simpleExtensionName;

	public boolean inJava;

	public boolean inCSharp;

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
