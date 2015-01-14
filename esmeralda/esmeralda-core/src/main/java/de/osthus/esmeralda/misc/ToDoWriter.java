package de.osthus.esmeralda.misc;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Iterator;

import de.osthus.ambeth.config.Property;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.esmeralda.IConversionContext;

public class ToDoWriter implements IToDoWriter
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected IConversionContext context;

	@Property(name = "todo-path", mandatory = false)
	protected File todoPath;

	@Override
	public void clearToDoFolder(String languagePathName)
	{
		if (todoPath == null)
		{
			return;
		}

		Path path = createToDoPath(languagePathName);
		if (!Files.exists(path))
		{
			return;
		}

		try
		{
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
		if (todoPath == null)
		{
			return;
		}

		IConversionContext context = this.context.getCurrent();
		String languagePathName = context.getLanguagePath();
		write(topic, todo, languagePathName, context.isDryRun());
	}

	@Override
	public void write(String topic, String todo, String languagePathName, boolean dryRun)
	{
		if (todoPath == null || dryRun)
		{
			return;
		}

		try
		{
			Path todoPath = createToDoPath(languagePathName);
			Files.createDirectories(todoPath);

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

	protected Path createToDoPath(String languagePathName)
	{
		Path todoPath = this.todoPath.toPath().resolve(languagePathName);
		return todoPath;
	}
}
