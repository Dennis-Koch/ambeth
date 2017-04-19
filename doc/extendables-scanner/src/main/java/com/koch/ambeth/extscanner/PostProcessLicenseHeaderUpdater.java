package com.koch.ambeth.extscanner;

import com.koch.ambeth.ioc.IStartingBean;
import com.koch.ambeth.util.collections.HashMap;
import com.koch.ambeth.util.collections.IMap;
import com.koch.classbrowser.java.TypeDescription;

public class PostProcessLicenseHeaderUpdater extends AbstractLatexScanner implements IStartingBean {
	@Override
	protected void buildModel(IMap<String, TypeDescription> javaTypes,
			IMap<String, TypeDescription> csharpTypes) throws Throwable {

		searchForFiles(sourcePath, new HashMap<>(), (file, relativeFilePath) -> {
			String lower = relativeFilePath.toLowerCase();
			if (lower.endsWith(".java") || lower.endsWith(".cs")) {
				filterFileContent(file, relativeFilePath);
			}
		});
	}

	@Override
	protected void handleModel() throws Throwable {
		// intended blank
	}
}
