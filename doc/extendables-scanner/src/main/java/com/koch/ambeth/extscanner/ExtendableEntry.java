package com.koch.ambeth.extscanner;

import java.util.regex.Matcher;

import com.koch.ambeth.util.collections.ArrayList;

import javassist.CtClass;

public class ExtendableEntry extends AbstractSourceFileAware
		implements Comparable<ExtendableEntry> {
	public boolean hasArguments;

	public final String fqExtensionName;

	public final String simpleExtensionName;

	public final String labelName;

	public final String simpleName;

	public final String fqName;

	public final ArrayList<CtClass> usedBy = new ArrayList<>();

	public ExtendableEntry(String fqName, String simpleName, String labelName,
			String fqExtensionName) {
		this.fqName = fqName;
		this.labelName = labelName;
		this.fqExtensionName = fqExtensionName;
		this.simpleName = simpleName;
		Matcher matcher = pattern.matcher(fqExtensionName);
		if (!matcher.matches()) {
			throw new IllegalArgumentException(fqExtensionName);
		}
		simpleExtensionName = matcher.group(1);
	}

	@Override
	public int compareTo(ExtendableEntry o) {
		return simpleExtensionName.compareTo(o.simpleExtensionName);
	}
}
