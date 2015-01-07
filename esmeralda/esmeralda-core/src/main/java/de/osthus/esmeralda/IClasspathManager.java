package de.osthus.esmeralda;

import java.util.Arrays;

import de.osthus.ambeth.collections.HashSet;

// TODO Will be correctly implemented later
public interface IClasspathManager
{
	public static HashSet<String> EXISTING_METHODS_CS = new HashSet<>(Arrays.asList("System.Console.write", "System.Console.writeln"));

	public static HashSet<String> EXISTING_METHODS_JS = new HashSet<>(Arrays.asList("console.log"));
}
