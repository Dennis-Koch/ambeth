package com.koch.ambeth.shell.core;

/*-
 * #%L
 * jambeth-shell
 * %%
 * Copyright (C) 2017 Koch Softwaredevelopment
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 * #L%
 */

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import com.koch.ambeth.ioc.IInitializingBean;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.config.Property;
import com.koch.ambeth.repackaged.com.esotericsoftware.reflectasm.MethodAccess;
import com.koch.ambeth.shell.AmbethShell;
import com.koch.ambeth.shell.core.CommandExtension.Parameter;
import com.koch.ambeth.shell.core.CommandExtension.Usage;
import com.koch.ambeth.shell.core.annotation.CommandArg;
import com.koch.ambeth.shell.util.Utils;
import com.koch.ambeth.util.IClassLoaderProvider;
import com.koch.ambeth.util.IConversionHelper;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;

/**
 * {@inheritDoc}
 */
public class CommandBindingImpl implements CommandBinding, IInitializingBean {
	@Autowired
	protected IClassLoaderProvider classLoaderProvider;

	@Autowired
	protected IConversionHelper conversionHelper;

	@Autowired
	protected CommandExtensionExtendable commandExtensions;

	@Autowired
	protected AmbethShell shell;

	@Property
	protected String name;

	@Property
	protected String description;

	@Property
	protected Object commandBean;

	@Property
	protected CommandArg[] args;

	@Property
	protected Method method;

	@Property
	protected MethodAccess methodAccess;

	protected int methodIndex;

	@Override
	public void afterPropertiesSet() throws Throwable {
		methodIndex = methodAccess.getIndex(method.getName(), method.getParameterTypes());
	}

	@Override
	public Object execute(List<String> arguments) {
		Class<?>[] parameterTypes = methodAccess.getParameterTypes()[methodIndex];
		Type[] genericParameterTypes = method.getGenericParameterTypes();
		Object[] translatedArgs = new Object[parameterTypes.length];

		Set<ParsedArgument> parsedArgs = new HashSet<>();
		for (int n = 0; n < arguments.size(); n++) {
			parsedArgs.add(new ParsedArgument(arguments.get(n), n, shell.getContext()));
		}
		for (int index = 0, size = args.length; index < size; index++) {
			CommandArg ca = args[index];
			Class<?> paramType = parameterTypes[index];
			Type genericParamType = genericParameterTypes[index];
			for (ParsedArgument pa : parsedArgs) {
				if (pa.matchedBy(ca)) {
					Class<?> targetType = paramType;
					if (Entry.class.equals(paramType)) {
						if (genericParamType instanceof ParameterizedType) {
							Type type = ((ParameterizedType) genericParamType).getActualTypeArguments()[1];
							if (type instanceof ParameterizedType) {
								type = ((ParameterizedType) type).getRawType();
							}
							// intentionally no else-if here
							if (type instanceof Class) {
								targetType = (Class<?>) type;
							}
						}
						Object targetValue = conversionHelper.convertValueToType(targetType, pa.value);
						translatedArgs[index] = new SimpleEntry<>(pa.name, targetValue);
					}
					else {
						Object targetValue = conversionHelper.convertValueToType(targetType, pa.value);
						translatedArgs[index] = targetValue;
					}
				}
			}
			if (translatedArgs[index] == null && !ca.defaultValue().isEmpty()) {
				translatedArgs[index] = ca.defaultValue();
			}
			if (translatedArgs[index] == null && !ca.optional()) {
				shell.println(printUsage());
				throw new RuntimeException("Mandatory argument is missing!");
			}
		}

		try {
			return methodAccess.invoke(commandBean, methodIndex, translatedArgs);
		}
		catch (Exception e) {
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	@Override
	public String printHelp() {
		StringWriter strWriter = new StringWriter();
		PrintWriter pw = new PrintWriter(strWriter);
		try {
			if (!description.isEmpty()) {
				pw.println(description);
			}

			pw.print(printUsage());

			return strWriter.toString();
		}
		finally {
			pw.close();
			try {
				strWriter.close();
			}
			catch (IOException e) {
				throw RuntimeExceptionUtil.mask(e);
			}
		}

	}

	/**
	 * http://courses.cms.caltech.edu/cs11/material/general/usage.html
	 */
	@Override
	public String printUsage() {
		StringWriter strWriter = new StringWriter();
		PrintWriter pw = new PrintWriter(strWriter);
		try {
			String indent = "  ";

			pw.println("Usage:");
			pw.print(indent + name + " ");
			int maxArgStringLength = 0;
			for (CommandArg arg : args) {
				String argName = arg.name().isEmpty() ? "<" + arg.alt() + ">" : arg.name();
				// String argName = arg.name();

				// argName += arg.defaultValue();

				argName = !argName.isEmpty() && arg.optional() ? "[" + argName + "]" : argName;

				pw.print(argName + " ");
				maxArgStringLength =
						Math.max(maxArgStringLength, Math.max(argName.length(), arg.alt().length() + 2));
			}
			pw.println();
			if (args.length > 0) {
				pw.println("options:");
				for (CommandArg arg : args) {
					String argName = arg.name().isEmpty() ? "<" + arg.alt() + ">" : arg.name();
					argName = !argName.isEmpty() && arg.optional() ? "[" + argName + "]" : argName;
					pw.print(indent + Utils.stringPadEnd(argName, maxArgStringLength + 4, ' '));
					String description = arg.description();
					if (!arg.descriptionFile().isEmpty()) {
						// TODO implement read descriptions from files
						try {
							description = Utils.readInputStream(
									classLoaderProvider.getClassLoader().getResourceAsStream(arg.descriptionFile()));
						}
						catch (IOException e) {
							pw.println("Warning: Failure to load description file: " + arg.descriptionFile());
						}
					}
					pw.print(description);
					if (!arg.defaultValue().isEmpty()) {
						pw.print(" [DEFAULT: " + arg.defaultValue() + "]");
					}
					pw.println();
				}
			}
			// print addition info from extensions
			pw.print(printExtensionUsages(commandExtensions.getExtensionsOfCommand(name)));

			return strWriter.toString();
		}
		finally {
			pw.close();
			try {
				strWriter.close();
			}
			catch (IOException e) {
				throw RuntimeExceptionUtil.mask(e);
			}
		}

	}

	/**
	 * print usage of command extensions
	 *
	 * @param extensions
	 */
	private String printExtensionUsages(List<CommandExtension> extensions) {
		if (extensions == null || extensions.isEmpty()) {
			return "";
		}
		StringWriter strWriter = new StringWriter();
		PrintWriter pw = new PrintWriter(strWriter);
		try {
			pw.println();

			List<Usage> usages = new ArrayList<>();
			int maxParamLength = 0;
			for (CommandExtension extension : extensions) {
				Usage usage = extension.getUsage();
				if (usage == null) {
					continue;
				}
				usages.add(usage);

				if (usage.getParameters() != null) {
					for (Parameter p : usage.getParameters()) {
						maxParamLength = Math.max(p.getName().length(), maxParamLength);
					}
				}

			}
			maxParamLength += 3;

			for (Usage usage : usages) {

				pw.println("Extension:   " + usage.getName());
				pw.println("Description: " + usage.getDescription());
				if (usage.getParameters() != null && usage.getParameters().size() > 0) {
					pw.println("Parameters:");
					for (Parameter p : usage.getParameters()) {
						pw.print("  " + Utils.stringPadEnd("[" + p.getName() + "]", maxParamLength, ' '));
						pw.print(p.getDescription());
						if (p.getDefaultValue() != null) {
							pw.print(" [Default: " + p.getDefaultValue() + "]");
						}
						pw.println();
					}
				}
				pw.println();
			}
			return strWriter.toString();
		}
		finally {
			pw.close();
			try {
				strWriter.close();
			}
			catch (IOException e) {
				throw RuntimeExceptionUtil.mask(e);
			}
		}
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getDescription() {
		return description;
	}

}
