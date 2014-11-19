package de.osthus.esmeralda.snippet;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.util.ParamChecker;
import de.osthus.esmeralda.ConversionContext;
import de.osthus.esmeralda.ILanguageHelper;
import demo.codeanalyzer.common.model.JavaClassInfo;
import demo.codeanalyzer.common.model.Method;

public class SnippetManager implements ISnippetManager, IInitializingBean
{
	private static final String TEXT_COMMENTED_OUT = "// ";

	private static final String TEXT_EXPL_HEADER = "This code could not be converted automatically. Please provide a snippet to use in the converted file.";

	private static final String TEXT_EXPL_EMPTY_LINE = "The next line should be empty and will not be included in the final snippet.";

	private static final String NL = System.getProperty("line.separator");

	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	protected ILanguageHelper languageHelper;

	protected Object methodAstNode;

	protected ConversionContext context;

	protected JavaClassInfo classInfo;

	protected Method method;

	protected Path snippetPath;

	protected ArrayList<String> usedSnippetFiles = new ArrayList<>();

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		ParamChecker.assertNotNull(methodAstNode, "methodAstNode");
		ParamChecker.assertNotNull(context, "context");

		classInfo = context.getClassInfo();
		method = context.getMethod();

		createSnippetPath();
	}

	protected void createSnippetPath()
	{
		ParamChecker.assertNotNull(classInfo, "classInfo");
		ParamChecker.assertNotNull(method, "method");

		File snippetPathBase = context.getSnippetPath();
		String languagePath = context.getLanguagePath();
		if (languagePath != null)
		{
			snippetPathBase = new File(snippetPathBase, languagePath);
		}

		Path packagePath = languageHelper.createRelativeTargetPath();
		String className = classInfo.getName();
		Path classPath = packagePath.resolve(className);
		snippetPath = Paths.get(snippetPathBase.getAbsolutePath(), classPath.toString());
	}

	public void setConversionContext(ConversionContext context)
	{
		this.context = context;
	}

	public void setMethodAstNode(Object methodAstNode)
	{
		this.methodAstNode = methodAstNode;
	}

	@Override
	public void finished()
	{
		List<String> allSnippetFiles = findAllSnippetFiles();
		allSnippetFiles.removeAll(usedSnippetFiles);

		if (!allSnippetFiles.isEmpty() && log.isInfoEnabled())
		{
			StringBuilder sb = new StringBuilder("The following snippet file(s) was/were not used:");
			for (String snippetFile : allSnippetFiles)
			{
				sb.append(NL).append("\t").append(snippetPath).append(File.separatorChar).append(snippetFile);
			}
			log.info(sb);
		}
	}

	@Override
	public String getSnippet(Object astNode, ConversionContext context)
	{
		// TODO find snippet file and return snippet code
		// TODO if snippet file does not exist, create file and report this

		// TODO find equal snippets
		// TODO If implemented use the code and inform in log
		// If not implemented just inform in log
		return null;
	}

	protected ArrayList<String> findAllSnippetFiles()
	{
		ArrayList<String> allSnippetFiles = new ArrayList<>();
		File snippetDir = snippetPath.toFile();
		if (!snippetDir.exists())
		{
			return allSnippetFiles;
		}

		String targetFileName = languageHelper.createTargetFileName(classInfo);
		int lastDot = targetFileName.lastIndexOf(".");
		String name = targetFileName.substring(0, lastDot);
		String postfix = targetFileName.substring(lastDot + 1);

		String nameForRegex = name.replace(".", "\\.");
		String snippetNameRegex = nameForRegex + "\\.[0-9a-f]{32}\\." + postfix;
		final Pattern snippetFilePattern = Pattern.compile(snippetNameRegex);

		FilenameFilter filter = new FilenameFilter()
		{

			@Override
			public boolean accept(File dir, String name)
			{
				Matcher matcher = snippetFilePattern.matcher(name);
				return matcher.matches();
			}
		};
		String[] snippetFiles = snippetDir.list(filter);
		allSnippetFiles.addAll(Arrays.asList(snippetFiles));

		return allSnippetFiles;
	}

	protected String readSnippet(Path snippetFile, ConversionContext context)
	{
		try
		{
			List<String> lines = Files.readAllLines(snippetFile, StandardCharsets.UTF_8);
			List<String> withoutPreface = null;
			for (int i = 0, size = lines.size(); i < size; i++)
			{
				String line = lines.get(i);
				if (!line.startsWith("// "))
				{
					withoutPreface = lines.subList(i, size);
					break;
				}
			}

			if (withoutPreface == null)
			{
				// Snippet file was not yet edited
				return null;
			}

			if (withoutPreface.get(0).isEmpty())
			{
				// Skipping empty separator line
				withoutPreface = withoutPreface.subList(1, withoutPreface.size());
			}

			// TODO glue lines indeted

			return null;
		}
		catch (IOException e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	protected void createSnippetFile(Path snippetFile, String untranslatableCode)
	{
		StringBuilder sb = new StringBuilder();
		sb.append(TEXT_COMMENTED_OUT).append(TEXT_EXPL_HEADER).append(NL);

		String[] lines = untranslatableCode.split("[\\n\\r]+");
		for (String line : lines)
		{
			sb.append(TEXT_COMMENTED_OUT).append(line).append(NL);
		}

		sb.append(NL).append(TEXT_COMMENTED_OUT).append(TEXT_EXPL_EMPTY_LINE);

		byte[] bytes = sb.toString().getBytes();
		try
		{
			Files.createDirectories(snippetFile.getParent());
			Files.write(snippetFile, bytes);
		}
		catch (IOException e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}
}
