package de.osthus.esmeralda.handler.csharp;

import java.io.IOException;
import java.io.Writer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.osthus.ambeth.collections.HashMap;
import de.osthus.ambeth.collections.IMap;
import de.osthus.ambeth.collections.ISet;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.objectcollector.IThreadLocalObjectCollector;
import de.osthus.ambeth.threading.IBackgroundWorkerDelegate;
import de.osthus.ambeth.util.StringConversionHelper;
import de.osthus.esmeralda.ConversionContext;

public class CsharpHelper implements ICsharpHelper
{
	protected static final Pattern genericTypePattern = Pattern.compile("(.+)<(.+)>");

	protected static final Pattern commaSplitPattern = Pattern.compile(",");

	protected static final HashMap<String, String> javaTypeToCsharpMap = new HashMap<String, String>();

	static
	{
		javaTypeToCsharpMap.put("boolean", "bool");
		javaTypeToCsharpMap.put("char", "char");
		javaTypeToCsharpMap.put("byte", "sbyte");
		javaTypeToCsharpMap.put("short", "short");
		javaTypeToCsharpMap.put("int", "int");
		javaTypeToCsharpMap.put("long", "long");
		javaTypeToCsharpMap.put("float", "float");
		javaTypeToCsharpMap.put("double", "double");
		javaTypeToCsharpMap.put("java.lang.Boolean", "bool?");
		javaTypeToCsharpMap.put("java.lang.Character", "char?");
		javaTypeToCsharpMap.put("java.lang.Byte", "sbyte?");
		javaTypeToCsharpMap.put("java.lang.Short", "short?");
		javaTypeToCsharpMap.put("java.lang.Integer", "int?");
		javaTypeToCsharpMap.put("java.lang.Long", "long?");
		javaTypeToCsharpMap.put("java.lang.Float", "float?");
		javaTypeToCsharpMap.put("java.lang.Double", "double?");
		javaTypeToCsharpMap.put("java.lang.String", "System.String");
		javaTypeToCsharpMap.put("java.lang.Class<?>", "System.Type");
		javaTypeToCsharpMap.put("java.util.Map.Entry", "De.Osthus.Ambeth.Collections.Entry");
	}

	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected IThreadLocalObjectCollector objectCollector;

	@Override
	public Writer newLineIntend(ConversionContext context, Writer writer) throws IOException
	{
		writer.append('\n');
		int indentationLevel = context.getIndentationLevel();
		for (int a = indentationLevel; a-- > 0;)
		{
			writer.append('\t');
		}
		return writer;
	}

	@Override
	public void scopeIntend(ConversionContext context, Writer writer, IBackgroundWorkerDelegate run) throws Throwable
	{
		newLineIntend(context, writer).append('{');
		context.incremetIndentationLevel();
		try
		{
			run.invoke();
		}
		finally
		{
			context.decremetIndentationLevel();
		}
		newLineIntend(context, writer).append('}');
	}

	@Override
	public String camelCaseName(String typeName)
	{
		String[] packageSplit = typeName.split(Pattern.quote("."));
		StringBuilder sb = new StringBuilder();
		for (int a = 0, size = packageSplit.length; a < size; a++)
		{
			if (a > 0)
			{
				sb.append('.');
			}
			sb.append(StringConversionHelper.upperCaseFirst(objectCollector, packageSplit[a]));
		}
		return sb.toString();
	}

	@Override
	public Writer writeType(String typeName, ConversionContext context, Writer writer) throws Throwable
	{
		typeName = typeName.trim();
		String mappedTypeName = javaTypeToCsharpMap.get(typeName);
		if (mappedTypeName == null)
		{
			Matcher genericTypeMatcher = genericTypePattern.matcher(typeName);
			if (genericTypeMatcher.matches())
			{
				String plainType = genericTypeMatcher.group(1);

				writeType(plainType, context, writer).append('<');

				String typeArguments = genericTypeMatcher.group(2);
				String[] typeArgumentsSplit = commaSplitPattern.split(typeArguments);
				boolean firstArgument = true;
				for (String typeArgumentSplit : typeArgumentsSplit)
				{
					if (firstArgument)
					{
						firstArgument = false;
					}
					else
					{
						writer.append(',');
					}
					writeType(typeArgumentSplit, context, writer);
				}
				writer.append('>');
				return writer;
			}
			mappedTypeName = camelCaseName(typeName);
		}
		ISet<String> usedTypes = context.getUsedTypes();
		if (usedTypes != null)
		{
			usedTypes.add(mappedTypeName);
		}
		else
		{
			IMap<String, String> imports = context.getImports();
			if (imports != null)
			{
				String nameFromImplicitImport = imports.get(mappedTypeName);
				if (nameFromImplicitImport != null)
				{
					mappedTypeName = nameFromImplicitImport;
				}
			}
		}
		writer.append(mappedTypeName);
		return writer;
	}
}
