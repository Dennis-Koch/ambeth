package de.osthus.esmeralda.handler.js;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.osthus.ambeth.collections.HashMap;
import de.osthus.ambeth.collections.HashSet;
import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;

public class JsClasspathManager implements IJsClasspathManager, IInitializingBean
{
	// FIXME temp.
	private static final Path EXTERNAL_PATH = Paths
			.get("C:\\dev\\legacy\\osthus-extensions\\osthus-ambeth_jsAmbeth\\jsambeth\\jsambeth-esmeraldaextend\\src\\main\\js");

	private static final Pattern JS_METHOD_NAME = Pattern.compile("\\s*\"?(\\w+)\"?\\: function ?\\(.*\\) \\{");

	private final FileVisitor<Path> jsFileVisitor = new FileVisitor<Path>()
	{
		/**
		 * {@inheritDoc}
		 */
		@Override
		public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException
		{
			return FileVisitResult.CONTINUE;
		}

		@Override
		public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException
		{
			if (Files.isDirectory(file))
			{
				return FileVisitResult.CONTINUE;
			}

			Path relativePath = EXTERNAL_PATH.relativize(file);
			String relativeName = relativePath.toString();
			relativeName = relativeName.substring(0, relativeName.length() - 3);
			String fullClassName = relativeName.replaceAll("\\" + File.separator, ".");
			classNameToPath.put(fullClassName, file);

			List<String> allLines = Files.readAllLines(file, StandardCharsets.UTF_8);
			for (String line : allLines)
			{
				Matcher matcher = JS_METHOD_NAME.matcher(line);
				if (matcher.find())
				{
					String methodName = matcher.group(1);
					String fullMethodName = fullClassName + '.' + methodName;
					methodsInPath.add(fullMethodName);
				}
			}

			return FileVisitResult.CONTINUE;
		}

		@Override
		public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException
		{
			return FileVisitResult.TERMINATE;
		}

		@Override
		public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException
		{
			return FileVisitResult.CONTINUE;
		}
	};

	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	private final HashMap<String, Path> classNameToPath = new HashMap<>();

	private final HashSet<String> methodsInPath = new HashSet<>();

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		methodsInPath.add("console.log");

		Files.walkFileTree(EXTERNAL_PATH, jsFileVisitor);
	}

	@Override
	public boolean isInClasspath(String fullClassName)
	{
		return classNameToPath.containsKey(fullClassName);
	}

	@Override
	public Path getFullPath(String fullClassName)
	{
		Path fullPath = classNameToPath.get(fullClassName);
		return fullPath;
	}

	@Override
	public HashSet<String> getClasspathMethods()
	{
		return methodsInPath;
	}
}
