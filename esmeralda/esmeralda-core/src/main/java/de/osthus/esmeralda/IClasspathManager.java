package de.osthus.esmeralda;

import java.nio.file.Path;
import java.util.Arrays;

import de.osthus.ambeth.collections.HashSet;

public interface IClasspathManager
{
	// TODO Will be correctly implemented later
	public static HashSet<String> EXISTING_METHODS_CS = new HashSet<>(Arrays.asList("System.Console.write", "System.Console.writeln"));

	boolean isInClasspath(String fullClassName);

	Path getFullPath(String fullClassName);

	HashSet<String> getClasspathMethods();
}
