package de.osthus.classbrowser.compare;

import de.osthus.classbrowser.java.OutputUtil;

public class CompareApplicationLocal {

	public static void main(String[] args) {
		System.setProperty(CompareApplication.ARG_KEY_JAVAFILE, "test/" + OutputUtil.EXPORT_FILE_NAME);
		System.setProperty(CompareApplication.ARG_KEY_CSHARPFILE, "test/export_csharp.xml");
		System.setProperty(CompareApplication.ARG_KEY_RESULTTYPE, CompareApplication.EXPORT_TYPE_CHECKSTYLE);
		System.setProperty(CompareApplication.ARG_KEY_TARGETPATH, "C:\\dev\\ClassComparison\\result");
				
		CompareApplication.main(new String[0]);
	}

}
