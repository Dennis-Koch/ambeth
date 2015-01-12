package de.osthus.esmeralda;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Iterator;

import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;

public class ToDoWriter implements IToDoWriter
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected IConversionContext context;

	@Override
	public void clearToDoFolder()
	{
		try
		{
			Path path = createToDoPath();
			DirectoryStream<Path> directoryStream = Files.newDirectoryStream(path);
			Iterator<Path> iter = directoryStream.iterator();
			while (iter.hasNext())
			{
				Path next = iter.next();
				if (Files.isRegularFile(next))
				{
					Files.delete(next);
				}
			}
		}
		catch (IOException e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	@Override
	public void write(String topic, String todo)
	{
		try
		{
			Path todoPath = createToDoPath();
			Path path = todoPath.resolve(topic + ".txt");
			try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND))
			{
				writer.write(todo);
				writer.newLine();
			}
		}
		catch (IOException e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	protected Path createToDoPath() throws IOException
	{
		IConversionContext context = this.context.getCurrent();

		Path srcPath = context.getTargetPath().toPath();
		Path targetPath = srcPath.getParent();
		String languagePathName = context.getLanguagePath();
		Path relativeTodoPath = Paths.get("todos", languagePathName);
		Path todoPath = targetPath.resolve(relativeTodoPath);
		Files.createDirectories(todoPath);
		return todoPath;
	}
}
