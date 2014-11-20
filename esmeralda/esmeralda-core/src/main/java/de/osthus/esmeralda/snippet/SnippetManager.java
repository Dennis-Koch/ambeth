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

import org.apache.commons.codec.digest.DigestUtils;

import com.sun.source.tree.MethodTree;

import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.util.ParamChecker;
import de.osthus.esmeralda.IConversionContext;
import de.osthus.esmeralda.ILanguageHelper;
import de.osthus.esmeralda.IWriter;
import demo.codeanalyzer.common.model.JavaClassInfo;
import demo.codeanalyzer.common.model.Method;

public class SnippetManager implements ISnippetManager, IInitializingBean
{
	private static final String TAB_EQUIVALENT = "    ";

	private static final Pattern PATTERN_START_TABS = Pattern.compile("^(" + TAB_EQUIVALENT + ")+");

	private static final String TEXT_COMMENTED_OUT = "// ";

	private static final String TEXT_EXPL_HEADER = "This code cannot be converted automatically. Please provide a snippet to use in the converted file.";

	private static final String TEXT_EXPL_EMPTY_LINE = "The next line should be empty and will not be included in the final snippet.";

	private static final String TEXT_SNIPPET_START = "Snippet Start: From file ";

	private static final String TEXT_SNIPPET_END = "Snippet End";

	private static final String TEXT_UNTRANSLATABLE_CODE_START = "Untranslatable Code Start";

	private static final String TEXT_UNTRANSLATABLE_CODE_END = "Untranslatable Code End";

	private static final String NL = System.getProperty("line.separator");

	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	protected MethodTree methodTree;

	protected ILanguageHelper languageHelper;

	@Autowired
	protected IConversionContext context;

	protected JavaClassInfo classInfo;

	protected Method method;

	protected Path snippetBasePath;

	protected Path snippetPath;

	protected String[] fileNameParts = new String[2];

	protected ArrayList<String> usedSnippetFiles = new ArrayList<>();

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		ParamChecker.assertNotNull(methodTree, "methodAstNode");
		ParamChecker.assertNotNull(languageHelper, "languageHelper");

		IConversionContext context = this.context.getCurrent();
		classInfo = context.getClassInfo();
		method = context.getMethod();

		createSnippetPath();
		createFileNameParts();
	}

	protected void createSnippetPath()
	{
		ParamChecker.assertNotNull(classInfo, "classInfo");
		ParamChecker.assertNotNull(method, "method");

		IConversionContext context = this.context.getCurrent();
		File snippetPathBase = context.getSnippetPath();
		Path packagePath = languageHelper.createRelativeTargetPath();
		snippetPath = Paths.get(snippetPathBase.getAbsolutePath(), packagePath.toString());
	}

	// TODO think about adding the parameters to the name.
	// TODOInclude a "dryRun" flag in the ConversionContext
	protected void createFileNameParts()
	{
		String methodName = method.getName();
		String targetFileName = languageHelper.createTargetFileName(classInfo);
		int lastDot = targetFileName.lastIndexOf(".");
		fileNameParts[0] = targetFileName.substring(0, lastDot) + "." + methodName;
		fileNameParts[1] = targetFileName.substring(lastDot + 1);
	}

	// FIXME This Code is run multiple times since there are some "analysis" runs in the CsharpClassNodeHandler
	// FIXME False positives due to overloaded methods (they do not use the files of the other overloaded methods)
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

	// TODO think: May be change the parameter to a string list of statements
	@Override
	public void writeSnippet(String untranslatableCode)
	{
		untranslatableCode = unindentCode(untranslatableCode);
		String md5Hash = DigestUtils.md5Hex(untranslatableCode);

		Path snippetFilePath = createSnippetFilePath(md5Hash);
		String relativeSnippetFileName = createRelativeSnippetFilePath(snippetFilePath).toString();

		usedSnippetFiles.add(snippetFilePath.getFileName().toString());

		boolean fileExists = Files.exists(snippetFilePath);
		if (fileExists)
		{
			List<String> snippet = readSnippet(snippetFilePath);
			if (snippet != null)
			{
				writeSnippetIntern(snippet, relativeSnippetFileName);
			}
			else if (log.isWarnEnabled())
			{
				writeCodeTodo(untranslatableCode, relativeSnippetFileName);
				log.warn("Existing snippet file '" + relativeSnippetFileName + "' is needed, but was not edited yet.");
			}

			return;
		}

		createSnippetFile(snippetFilePath, untranslatableCode);
		writeCodeTodo(untranslatableCode, relativeSnippetFileName);
		if (log.isInfoEnabled())
		{
			log.info("A new snippet file was created at '" + relativeSnippetFileName + "'");
		}

		// TODO later: find equal snippets
		// TODO If implemented use the code and inform in log
		// TODO If not implemented just inform in log
	}

	protected String unindentCode(String untranslatableCode)
	{
		if (!untranslatableCode.startsWith(TAB_EQUIVALENT))
		{
			return untranslatableCode;
		}

		Matcher matcher = PATTERN_START_TABS.matcher(untranslatableCode);
		matcher.find();
		String tabs = matcher.group(1);
		untranslatableCode = untranslatableCode.substring(tabs.length());

		String unindentedCodde = untranslatableCode.replaceAll("([\n\r]+)" + tabs, "$1");

		return unindentedCodde;
	}

	protected Path createSnippetFilePath(String md5Hash)
	{
		StringBuilder sb = new StringBuilder();
		sb.append(fileNameParts[0]).append(".").append(md5Hash).append(".").append(fileNameParts[1]);
		String fileName = sb.toString();
		Path snippetFilePath = snippetPath.resolve(fileName);
		return snippetFilePath;
	}

	protected Path createRelativeSnippetFilePath(Path snippetFilePath)
	{
		IConversionContext context = this.context.getCurrent();

		File snippetPathBase = context.getSnippetPath();
		String languagePath = context.getLanguagePath();
		String absoluteSnippetPath = snippetPathBase.getAbsolutePath() + (languagePath != null ? File.separator + languagePath : "");
		Path relativeSnippetFilePath = Paths.get(absoluteSnippetPath).relativize(snippetFilePath);
		return relativeSnippetFilePath;
	}

	protected ArrayList<String> findAllSnippetFiles()
	{
		ArrayList<String> allSnippetFiles = new ArrayList<>();
		File snippetDir = snippetPath.toFile();
		if (!Files.exists(snippetPath))
		{
			return allSnippetFiles;
		}

		String nameForRegex = fileNameParts[0].replace(".", "\\.");
		String snippetNameRegex = nameForRegex + "\\.[0-9a-f]{32}\\." + fileNameParts[1];
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

	protected List<String> readSnippet(Path snippetFilePath)
	{
		try
		{
			List<String> lines = Files.readAllLines(snippetFilePath, StandardCharsets.UTF_8);
			List<String> withoutPreface = null;
			for (int i = 0, size = lines.size(); i < size; i++)
			{
				String line = lines.get(i);
				if (!line.startsWith("//"))
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

			return withoutPreface;
		}
		catch (IOException e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	protected void writeSnippetIntern(List<String> snippet, String relativeSnippetFileName)
	{
		IConversionContext context = this.context.getCurrent();
		IWriter writer = context.getWriter();

		languageHelper.newLineIntend();
		writer.append(TEXT_COMMENTED_OUT).append(TEXT_SNIPPET_START).append(relativeSnippetFileName);
		for (int i = 0, size = snippet.size(); i < size; i++)
		{
			String line = snippet.get(i);
			languageHelper.newLineIntend();
			writer.append(line);
		}
		languageHelper.newLineIntend();
		writer.append(TEXT_COMMENTED_OUT).append(TEXT_SNIPPET_END);
	}

	protected void createSnippetFile(Path snippetFile, String untranslatableCode)
	{
		StringBuilder sb = new StringBuilder();
		sb.append(TEXT_COMMENTED_OUT).append(TEXT_EXPL_HEADER).append(NL);
		sb.append(TEXT_COMMENTED_OUT).append(NL);

		String[] lines = untranslatableCode.split("[\\n\\r]+");
		for (String line : lines)
		{
			sb.append(TEXT_COMMENTED_OUT).append(line).append(NL);
		}

		sb.append(TEXT_COMMENTED_OUT).append(NL).append(TEXT_COMMENTED_OUT).append(TEXT_EXPL_EMPTY_LINE);

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

	protected void writeCodeTodo(String untranslatableCode, String relativeSnippetFileName)
	{
		IConversionContext context = this.context.getCurrent();
		IWriter writer = context.getWriter();

		languageHelper.newLineIntend();
		writer.append(TEXT_COMMENTED_OUT).append("TODO implement snippet file '" + relativeSnippetFileName + "'");
		languageHelper.newLineIntend();
		writer.append(TEXT_COMMENTED_OUT).append(TEXT_UNTRANSLATABLE_CODE_START);

		String[] codeLines = untranslatableCode.split("[\\n\\r]+");
		for (String line : codeLines)
		{
			languageHelper.newLineIntend();
			writer.append(TEXT_COMMENTED_OUT).append(line);
		}

		languageHelper.newLineIntend();
		writer.append(TEXT_COMMENTED_OUT).append(TEXT_UNTRANSLATABLE_CODE_END);
	}
}
