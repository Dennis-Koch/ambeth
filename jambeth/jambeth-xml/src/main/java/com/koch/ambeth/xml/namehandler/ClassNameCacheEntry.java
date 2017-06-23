package com.koch.ambeth.xml.namehandler;

import com.koch.ambeth.xml.SpecifiedMember;

public class ClassNameCacheEntry {
	public final String writtenClassName;

	public final String xmlNamespace;

	public final SpecifiedMember[] membersToWrite;

	public final String classMembers;

	public ClassNameCacheEntry(String writtenClassName, String xmlNamespace, String classMembers,
			SpecifiedMember[] membersToWrite) {
		this.writtenClassName = writtenClassName;
		this.xmlNamespace = xmlNamespace;
		this.classMembers = classMembers;
		this.membersToWrite = membersToWrite;
	}
}
