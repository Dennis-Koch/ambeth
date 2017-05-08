package com.koch.classbrowser.compare;

import com.koch.classbrowser.compare.CompareApplication;
import com.koch.classbrowser.java.OutputUtil;

public class CompareApplicationLocal
{

	public static void main(String[] args)
	{
		System.setProperty(CompareApplication.ARG_KEY_JAVAFILE, "C:/dev/osthus-extensions/data/" + OutputUtil.EXPORT_FILE_NAME);
		System.setProperty(CompareApplication.ARG_KEY_CSHARPFILE, "C:/dev/osthus-extensions/data/export_csharp.xml");
		System.setProperty(CompareApplication.ARG_KEY_RESULTTYPE, CompareApplication.EXPORT_TYPE_CHECKSTYLE);
		System.setProperty(CompareApplication.ARG_KEY_TARGETPATH, "C:/dev/osthus-extensions/results");

		CompareApplication.main(new String[0]);
	}

}
