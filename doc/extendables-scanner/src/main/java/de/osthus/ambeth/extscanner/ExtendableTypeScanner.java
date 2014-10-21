package de.osthus.ambeth.extscanner;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.ILinkedMap;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.config.Property;
import de.osthus.ambeth.ioc.IStartingBean;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;

public class ExtendableTypeScanner extends AbstractLatexScanner implements IStartingBean
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Property(name = "target-tex-file")
	protected String allExtendablesTexFilePath;

	@Property(name = "target-extendable-tex-dir")
	protected String targetExtendableTexDirPath;

	@Override
	public void afterStarted() throws Throwable
	{
		File allExtendablesTexFile = new File(allExtendablesTexFilePath).getCanonicalFile();
		allExtendablesTexFile.getParentFile().mkdirs();

		File targetExtendableTexDir = new File(targetExtendableTexDirPath).getCanonicalFile();
		targetExtendableTexDir.mkdirs();

		log.info("TargetTexFile: " + allExtendablesTexFile);
		log.info("ExtendableTexDir: " + targetExtendableTexDir);

		String targetExtendableTexDirCP = targetExtendableTexDir.getPath();
		String targetTexFileCP = allExtendablesTexFile.getParent();
		if (!targetExtendableTexDirCP.startsWith(targetTexFileCP))
		{
			throw new IllegalStateException("Path '" + targetExtendableTexDirCP + "' must reside within '" + targetTexFileCP + "'");
		}
		String pathToExtendableTexFile = targetExtendableTexDirCP.substring(targetTexFileCP.length() + 1);

		ClassPool pool = ClassPool.getDefault();
		IList<String> targetClassNames = scanForClasses(pool);

		Collections.sort(targetClassNames);

		ArrayList<CtClass> clazzes = new ArrayList<CtClass>(targetClassNames.size());

		for (int a = 0, size = targetClassNames.size(); a < size; a++)
		{
			String className = targetClassNames.get(a);
			if (!className.endsWith("Extendable"))
			{
				continue;
			}
			CtClass cc = pool.get(className);
			if (!cc.isInterface())
			{
				continue;
			}
			clazzes.add(cc);
		}
		Collections.sort(clazzes, new Comparator<CtClass>()
		{
			@Override
			public int compare(CtClass o1, CtClass o2)
			{
				return o1.getSimpleName().compareTo(o2.getSimpleName());
			}
		});

		ILinkedMap<CtClass, File> hasPendantInCSharp = resolvePendantInCSharp(clazzes);

		log.info("Found " + clazzes.size() + " files");

		FileWriter fw = new FileWriter(allExtendablesTexFile);
		try
		{
			fw.append("%---------------------------------------------------------------\n");
			fw.append("% This file is FULLY generated. Please do not edit anything here\n");
			fw.append("% Any changes have to be done to the java class " + ExtendableTypeScanner.class.getName() + "\n");
			fw.append("%---------------------------------------------------------------\n");
			fw.append("\\chapter{Ambeth Extension Points}\n");
			fw.append("\\begin{longtable}{| l | l | c | c |} \\hline \\textbf{Extension Point} & \\textbf{Extension} & \\textbf{Java} & \\textbf{C\\#} \\\n");
			fw.append("\t\\endhead\n");
			fw.append("\t\\hline\n");

			ArrayList<String> includes = new ArrayList<String>();

			for (int a = 0, size = clazzes.size(); a < size; a++)
			{
				CtClass cc = clazzes.get(a);
				log.info("Handling " + cc.getName());
				String texName = cc.getSimpleName();

				String labelName = "extendable:" + texName;
				if (!writeTableRow(cc, labelName, fw, hasPendantInCSharp))
				{
					continue;
				}
				fw.append(" \\\\\n");
				fw.append("\t\\hline\n");

				String expectedExtendableTexFileName = texName + ".tex";

				includes.add(pathToExtendableTexFile + "/" + texName);

				File expectedExtendableTexFile = new File(targetExtendableTexDir, expectedExtendableTexFileName);

				if (!expectedExtendableTexFile.exists())
				{
					writeEmptyExtendableTexFile(cc, labelName, expectedExtendableTexFile);
				}
			}
			fw.append("\\end{longtable}\n");
			for (int a = 0, size = includes.size(); a < size; a++)
			{
				// now write all chapter includes which are referenced from the table
				fw.append("\\input{").append(includes.get(a)).append("}\n");
			}
			fw.append("%--END OF GENERATION----------------------------------------------------------\n");
		}
		finally
		{
			fw.close();
		}
	}

	protected void writeEmptyExtendableTexFile(CtClass extendableType, String labelName, File targetFile) throws Exception
	{
		FileWriter fw = new FileWriter(targetFile);
		try
		{
			// \section{CacheRetrieverExtendable}
			// \label{feature:CacheRetrieverExtendable}

			// String visibleName = extendableName;
			// if (visibleName.startsWith("I"))
			// {
			// visibleName = visibleName.substring(1);
			// }
			// if (visibleName.endsWith("Extendable"))
			// {
			// visibleName = visibleName.substring(0, visibleName.length() - "Extendable".length());
			// }
			fw.append("\\section{").append(extendableType.getSimpleName()).append("}\n");
			fw.append("\\label{").append(labelName).append("}\n");
			writeJavadoc(extendableType, fw);
			fw.append("\n");
			fw.append("\\TODO\n");
		}
		finally
		{
			fw.close();
		}
	}

	protected boolean writeTableRow(CtClass extendableType, String labelName, FileWriter fw, Map<CtClass, File> hasPendantInCSharp) throws Exception
	{
		CtClass extensionType = null;
		CtMethod[] methods = extendableType.getMethods();
		for (int a = methods.length; a-- > 0;)
		{
			String methodName = methods[a].getName();
			if ((methodName.toLowerCase().startsWith("register") || methodName.toLowerCase().startsWith("add")) && methods[a].getParameterTypes().length >= 1)
			{
				extensionType = methods[a].getParameterTypes()[0];
				break;
			}
		}
		if (extensionType == null)
		{
			log.debug("No extension type found on extendable '" + extendableType.getName() + "'");
			return false;
		}
		fw.append("\t\t");
		fw.append("\\nameref{").append(labelName).append('}');
		// writeJavadoc(extendableType, fw);
		fw.append(" &\n\t\t");
		writeJavadoc(extensionType, fw);
		fw.append(" &\n\t\t");
		fw.append("X &\n\t\t");
		if (hasPendantInCSharp.get(extendableType) != null)
		{
			fw.append("X ");
		}
		else
		{
			fw.append("  ");
		}
		// fw.append("\n\t\t");
		// fw.append("\\shortprettyref{").append(labelName).append('}');
		return true;
	}

	protected void writeJavadoc(CtClass type, FileWriter fw) throws IOException
	{
		fw.append("\\javadoc{").append(type.getName()).append("}{").append(type.getSimpleName()).append('}');
	}
}
