package com.koch.ambeth.extscanner.model;

import java.util.regex.Matcher;

import com.koch.ambeth.util.collections.ArrayList;
import com.koch.classbrowser.java.TypeDescription;

import javassist.CtClass;

public class ExtendableEntry extends AbstractSourceFileAware
		implements Comparable<ExtendableEntry> {
	public boolean hasArguments;

	public final String fqExtensionName;

	public final String simpleExtensionName;

	public final String labelName;

	public final String simpleName;

	public final ArrayList<CtClass> usedBy = new ArrayList<>();

	public final TypeDescription type;

	public ExtendableEntry(TypeDescription type, String simpleName, String labelName,
			String fqExtensionName) {
		this.type = type;
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
		return simpleName.compareTo(o.simpleName);
	}
}
