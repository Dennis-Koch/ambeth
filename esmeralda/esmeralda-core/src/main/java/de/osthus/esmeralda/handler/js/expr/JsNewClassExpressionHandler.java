package de.osthus.esmeralda.handler.js.expr;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sun.source.tree.StatementTree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.util.TreePath;
import com.sun.tools.javac.code.Symbol.MethodSymbol;
import com.sun.tools.javac.code.Symbol.VarSymbol;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.code.Type.MethodType;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCBlock;
import com.sun.tools.javac.tree.JCTree.JCClassDecl;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCIdent;
import com.sun.tools.javac.tree.JCTree.JCLiteral;
import com.sun.tools.javac.tree.JCTree.JCMethodDecl;
import com.sun.tools.javac.tree.JCTree.JCNewClass;
import com.sun.tools.javac.tree.JCTree.JCTypeParameter;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;

import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.HashMap;
import de.osthus.ambeth.collections.HashSet;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.threading.IResultingBackgroundWorkerDelegate;
import de.osthus.esmeralda.IConversionContext;
import de.osthus.esmeralda.ILanguageHelper;
import de.osthus.esmeralda.handler.AbstractExpressionHandler;
import de.osthus.esmeralda.handler.IStatementHandlerExtension;
import de.osthus.esmeralda.handler.IVariable;
import de.osthus.esmeralda.handler.js.IJsHelper;
import de.osthus.esmeralda.misc.IWriter;
import de.osthus.esmeralda.misc.Lang;
import de.osthus.esmeralda.snippet.SnippetTrigger;
import demo.codeanalyzer.common.model.JavaClassInfo;

public class JsNewClassExpressionHandler extends AbstractExpressionHandler<JCNewClass>
{
	public static class ConstructorParamKey
	{
		public static final String ANY = "*"; // Only use, if no overloads exist

		private final String fqClassName;

		private final String[] fqParamClassNames;

		public ConstructorParamKey(String fqClassName, String... fqParamClassNames)
		{
			this.fqClassName = fqClassName;
			this.fqParamClassNames = Arrays.copyOf(fqParamClassNames, fqParamClassNames.length);
		}

		@Override
		public int hashCode()
		{
			int hashCode = fqClassName.hashCode();
			hashCode *= fqParamClassNames.length;
			return hashCode;
		}

		@Override
		public boolean equals(Object obj)
		{
			if (!(obj instanceof ConstructorParamKey))
			{
				return false;
			}

			ConstructorParamKey other = (ConstructorParamKey) obj;

			String[] otherFqParamClassNames = other.fqParamClassNames;
			if (!fqClassName.equals(other.fqClassName) || fqParamClassNames.length != otherFqParamClassNames.length)
			{
				return false;
			}

			for (int i = 0; i < fqParamClassNames.length; i++)
			{
				String thisName = fqParamClassNames[i];
				String otherName = otherFqParamClassNames[i];
				if (ANY.equals(thisName) || ANY.equals(otherName))
				{
					continue;
				}
				if (!thisName.equals(otherName))
				{
					return false;
				}
			}

			return true;
		}
	}

	public static final Pattern anonymousPattern = Pattern.compile("<anonymous (.+)>([^<>]*)");

	protected static final HashMap<ConstructorParamKey, String[]> constructorParamTypesToConstructorParamNames = new HashMap<>();

	static
	{
		String fqNameString = String.class.getName();
		String fqNameThrowable = java.lang.Throwable.class.getName();
		String fqNameClassCastException = java.lang.ClassCastException.class.getName();
		String fqNameIllegalArgumentException = java.lang.IllegalArgumentException.class.getName();
		String fqNameRuntimeException = java.lang.RuntimeException.class.getName();
		String fqNameSInt = int.class.getName();
		String fqNameSLong = long.class.getName();
		String fqNameSFloat = float.class.getName();
		String fqNameSBoolean = boolean.class.getName();
		String fqNameInteger = java.lang.Integer.class.getName();
		String fqNameFloat = java.lang.Float.class.getName();
		String fqNameByteArray = "byte[]"; // getName() returns "[B"
		String fqNameCharArray = "char[]"; // getName() returns "[C"
		String fqNameCollection = Collection.class.getName();
		String fqNameCharset = java.nio.charset.Charset.class.getName();
		String fqNameStringBuilder = java.lang.StringBuilder.class.getName();
		String fqNameReader = java.io.Reader.class.getName();
		String fqNameWriter = java.io.Writer.class.getName();
		String fqNameInputStream = java.io.InputStream.class.getName();
		String fqNameOutputStream = java.io.OutputStream.class.getName();
		String fqNameInputStreamReader = java.io.InputStreamReader.class.getName();
		String fqNameOutputStreamWriter = java.io.OutputStreamWriter.class.getName();
		String fqNameBufferedInputStream = java.io.BufferedInputStream.class.getName();
		String fqNameBufferedOutputStream = java.io.BufferedOutputStream.class.getName();
		String fqNameFileInputStream = java.io.FileInputStream.class.getName();
		String fqNameFileOutputStream = java.io.FileOutputStream.class.getName();
		String fqNameByteArrayInputStream = java.io.ByteArrayInputStream.class.getName();
		String fqNameBufferedReader = java.io.BufferedReader.class.getName();
		String fqNameBufferedWriter = java.io.BufferedWriter.class.getName();
		String fqNameFile = java.io.File.class.getName();
		String fqNameDate = java.util.Date.class.getName();
		String fqNameSqlDate = java.sql.Date.class.getName();
		String fqNameSqlTimestamp = java.sql.Timestamp.class.getName();
		String fqNameBigDecimal = java.math.BigDecimal.class.getName();
		String fqNameBigInteger = java.math.BigInteger.class.getName();
		String fqNameIProperties = de.osthus.ambeth.config.IProperties.class.getName();
		String fqNameProperties = de.osthus.ambeth.config.Properties.class.getName();
		String fqNameArrayList = de.osthus.ambeth.collections.ArrayList.class.getName();
		String fqNameSetLinkedEntry = de.osthus.ambeth.collections.SetLinkedEntry.class.getName();
		String fqNameListElem = de.osthus.ambeth.collections.ListElem.class.getName();
		String fqNameCleanupInvalidKeysSet = de.osthus.ambeth.collections.CleanupInvalidKeysSet.class.getName();
		String fqNameIInvalidKeyChecker = de.osthus.ambeth.collections.IInvalidKeyChecker.class.getName();
		String fqNameClassWriter = de.osthus.ambeth.repackaged.org.objectweb.asm.ClassWriter.class.getName();
		String fqNameCountDownLatch = java.util.concurrent.CountDownLatch.class.getName();
		String fqNameSoftReference = java.lang.ref.SoftReference.class.getName();
		String fqNameWeakReference = java.lang.ref.WeakReference.class.getName();

		String pgNameCollections = "de.osthus.ambeth.collections.";

		// Single String constructors
		registerConstructor(new ConstructorParamKey(fqNameFile, fqNameString), "pathname");
		registerConstructor(new ConstructorParamKey(fqNameFileInputStream, fqNameString), "name");
		registerConstructor(new ConstructorParamKey(fqNameFileOutputStream, fqNameString), "name");
		registerConstructor(new ConstructorParamKey(fqNameStringBuilder, fqNameString), "str");
		registerConstructor(new ConstructorParamKey(fqNameBigDecimal, fqNameString), "val");
		registerConstructor(new ConstructorParamKey(fqNameBigDecimal, fqNameBigInteger), "val");
		registerConstructor(new ConstructorParamKey(fqNameBigInteger, fqNameString), "val");
		registerConstructor(new ConstructorParamKey("java.text.SimpleDateFormat", fqNameString), "pattern");

		// Constructors on java.lang.String
		registerConstructor(new ConstructorParamKey(fqNameString, fqNameByteArray, fqNameCharset), "bytes", "charset");
		registerConstructor(new ConstructorParamKey(fqNameString, fqNameByteArray, fqNameString), "bytes", "charsetName");
		registerConstructor(new ConstructorParamKey(fqNameString, fqNameByteArray, fqNameSInt, fqNameSInt, fqNameCharset), "bytes", "offset", "length",
				"charset");
		registerConstructor(new ConstructorParamKey(fqNameString, fqNameByteArray, fqNameSInt, fqNameSInt, fqNameString), "bytes", "offset", "length",
				"charsetName");
		registerConstructor(new ConstructorParamKey(fqNameString, fqNameCharArray), "value");

		// Exception constructors
		registerConstructor(new ConstructorParamKey("java.io.IOException", fqNameString), "message");
		registerConstructor(new ConstructorParamKey("java.io.IOException", fqNameThrowable), "cause");
		registerConstructor(new ConstructorParamKey(fqNameClassCastException, fqNameString), "s");
		registerConstructor(new ConstructorParamKey(fqNameIllegalArgumentException, fqNameString), "s");
		registerConstructor(new ConstructorParamKey(fqNameIllegalArgumentException, fqNameString, fqNameThrowable), "message", "cause");
		registerConstructor(new ConstructorParamKey("java.lang.IllegalStateException", fqNameString), "s");
		registerConstructor(new ConstructorParamKey("java.lang.IllegalStateException", fqNameString, fqNameThrowable), "message", "cause");
		registerConstructor(new ConstructorParamKey("java.lang.NullPointerException", fqNameString), "s");
		registerConstructor(new ConstructorParamKey(fqNameRuntimeException, fqNameString), "message");
		registerConstructor(new ConstructorParamKey(fqNameRuntimeException, fqNameThrowable), "cause");
		registerConstructor(new ConstructorParamKey(fqNameRuntimeException, fqNameString, fqNameThrowable), "message", "cause");
		registerConstructor(new ConstructorParamKey("java.lang.UnsupportedOperationException", fqNameString), "message");

		// Java collections constructors
		registerConstructor(new ConstructorParamKey("java.util.ArrayList", fqNameSInt), "initialCapacity");
		registerConstructor(new ConstructorParamKey("java.util.ArrayList", fqNameInteger), "initialCapacity");
		registerHashConstructors("java.util.HashSet", fqNameSInt, fqNameInteger, fqNameSFloat, fqNameFloat);
		registerHashConstructors("java.util.HashMap", fqNameSInt, fqNameInteger, fqNameSFloat, fqNameFloat);

		// Ambeth collections constructors
		registerConstructor(new ConstructorParamKey(fqNameArrayList, fqNameSInt), "initialCapacity");
		registerConstructor(new ConstructorParamKey(fqNameArrayList, fqNameInteger), "initialCapacity");
		registerConstructor(new ConstructorParamKey(fqNameArrayList, fqNameCollection), "coll");
		registerConstructor(new ConstructorParamKey(fqNameArrayList, Object[].class.getName()), "array");
		registerConstructor(new ConstructorParamKey(fqNameSetLinkedEntry, fqNameSInt, "K", fqNameSetLinkedEntry), "hash", "key", "nextEntry");
		registerConstructor(new ConstructorParamKey(fqNameListElem, ConstructorParamKey.ANY), "value");
		registerConstructor(new ConstructorParamKey(fqNameCleanupInvalidKeysSet, fqNameIInvalidKeyChecker, fqNameSFloat), "invalidKeyChecker", "loadFactor");
		registerConstructor(new ConstructorParamKey(fqNameCleanupInvalidKeysSet, fqNameIInvalidKeyChecker, fqNameFloat), "invalidKeyChecker", "loadFactor");

		registerHashConstructors(pgNameCollections + "HashSet", fqNameSInt, fqNameInteger, fqNameSFloat, fqNameFloat);
		registerHashConstructors(pgNameCollections + "IdentityHashSet", fqNameSInt, fqNameInteger, fqNameSFloat, fqNameFloat);
		registerHashConstructors(pgNameCollections + "LinkedHashSet", fqNameSInt, fqNameInteger, fqNameSFloat, fqNameFloat);
		registerHashConstructors(pgNameCollections + "IdentityLinkedSet", fqNameSInt, fqNameInteger, fqNameSFloat, fqNameFloat);
		registerHashConstructors(pgNameCollections + "HashMap", fqNameSInt, fqNameInteger, fqNameSFloat, fqNameFloat);
		registerHashConstructors(pgNameCollections + "IdentityHashMap", fqNameSInt, fqNameInteger, fqNameSFloat, fqNameFloat);
		registerTupleHashMaps(pgNameCollections, fqNameSInt, fqNameInteger, fqNameSFloat, fqNameFloat);

		// Other constructors
		registerConstructor(new ConstructorParamKey(fqNameDate, fqNameSLong), "date");
		registerConstructor(new ConstructorParamKey(fqNameSqlDate, fqNameSLong), "date");
		registerConstructor(new ConstructorParamKey(fqNameSqlTimestamp, fqNameSLong), "time");
		registerConstructor(new ConstructorParamKey(fqNameFile, fqNameString, fqNameString), "parent", "child");
		registerConstructor(new ConstructorParamKey(fqNameInputStreamReader, fqNameInputStream, fqNameCharset), "in", "cs");
		registerConstructor(new ConstructorParamKey(fqNameOutputStreamWriter, fqNameOutputStream, fqNameCharset), "out", "cs");
		registerConstructor(new ConstructorParamKey(fqNameBufferedInputStream, fqNameInputStream), "in");
		registerConstructor(new ConstructorParamKey(fqNameBufferedOutputStream, fqNameOutputStream), "out");
		registerConstructor(new ConstructorParamKey(fqNameFileInputStream, fqNameReader), "in");
		registerConstructor(new ConstructorParamKey(fqNameFileInputStream, fqNameFile), "file");
		registerConstructor(new ConstructorParamKey(fqNameFileOutputStream, fqNameFile, fqNameSBoolean), "file", "append");
		registerConstructor(new ConstructorParamKey(fqNameByteArrayInputStream, fqNameByteArray), "buf");
		registerConstructor(new ConstructorParamKey(fqNameBufferedReader, fqNameReader), "in");
		registerConstructor(new ConstructorParamKey(fqNameBufferedWriter, fqNameWriter), "out");
		registerConstructor(new ConstructorParamKey(fqNameBufferedWriter, fqNameOutputStreamWriter), "out");
		registerConstructor(new ConstructorParamKey(fqNameCountDownLatch, fqNameSInt), "count");
		registerConstructor(new ConstructorParamKey(fqNameSoftReference, ConstructorParamKey.ANY), "referent");
		registerConstructor(new ConstructorParamKey(fqNameSoftReference, ConstructorParamKey.ANY, java.lang.ref.ReferenceQueue.class.getName()), "referent",
				"q");
		registerConstructor(new ConstructorParamKey(fqNameWeakReference, ConstructorParamKey.ANY), "referent");
		registerConstructor(new ConstructorParamKey(fqNameWeakReference, ConstructorParamKey.ANY, java.lang.ref.ReferenceQueue.class.getName()), "referent",
				"q");

		// Other Ambeth constructors
		registerConstructor(new ConstructorParamKey(fqNameProperties, fqNameIProperties), "parent");
		registerConstructor(new ConstructorParamKey(fqNameClassWriter, fqNameSInt), "flags");
		registerConstructor(new ConstructorParamKey("de.osthus.ambeth.appendable.AppendableStringBuilder", fqNameStringBuilder), "sb");
	}

	protected static void registerHashConstructors(String fqClassName, String fqNameSInt, String fqNameInteger, String fqNameSFloat, String fqNameFloat)
	{
		registerConstructor(new ConstructorParamKey(fqClassName, fqNameSInt), "initialCapacity");
		registerConstructor(new ConstructorParamKey(fqClassName, fqNameInteger), "initialCapacity");
		registerConstructor(new ConstructorParamKey(fqClassName, fqNameSFloat), "loadFactor");
		registerConstructor(new ConstructorParamKey(fqClassName, fqNameFloat), "loadFactor");
		registerConstructor(new ConstructorParamKey(fqClassName, fqNameSInt, fqNameSFloat), "initialCapacity", "loadFactor");
		registerConstructor(new ConstructorParamKey(fqClassName, fqNameInteger, fqNameSFloat), "initialCapacity", "loadFactor");
		registerConstructor(new ConstructorParamKey(fqClassName, fqNameSInt, fqNameFloat), "initialCapacity", "loadFactor");
		registerConstructor(new ConstructorParamKey(fqClassName, fqNameInteger, fqNameFloat), "initialCapacity", "loadFactor");
	}

	protected static void registerTupleHashMaps(String pgNameCollections, String fqNameSInt, String fqNameInteger, String fqNameSFloat, String fqNameFloat)
	{
		String[] constNames = { "value", "hash", "nextEntry" };
		for (int i = 2; i <= 4; i++)
		{
			String fqNameMap = pgNameCollections + "Tuple" + i + "KeyHashMap";
			String fqNameEntry = pgNameCollections + "Tuple" + i + "KeyEntry";

			registerHashConstructors(fqNameMap, fqNameSInt, fqNameInteger, fqNameSFloat, fqNameFloat);

			String[] types = new String[i + 3];
			String[] names = new String[i + 3];
			int j = 0;
			for (; j < i; j++)
			{
				types[j] = "Key" + (j + 1);
				names[j] = "key" + (j + 1);
			}
			System.arraycopy(constNames, 0, names, j, 3);
			types[j++] = "V";
			types[j++] = fqNameSInt;
			types[j++] = fqNameEntry;
			ConstructorParamKey constructorParamKey = new ConstructorParamKey(fqNameEntry, types);
			constructorParamTypesToConstructorParamNames.put(constructorParamKey, names);
		}
	}

	protected static void registerConstructor(ConstructorParamKey key, String... paramNames)
	{
		constructorParamTypesToConstructorParamNames.put(key, paramNames);
	}

	public static final String getFqNameFromAnonymousName(String fqName)
	{
		Matcher anonymousMatcher = JsNewClassExpressionHandler.anonymousPattern.matcher(fqName);
		if (!anonymousMatcher.matches())
		{
			return fqName;
		}
		return anonymousMatcher.group(1) + anonymousMatcher.group(2);
	}

	public static final String findFqAnonymousName(TreePath path)
	{
		TreePath currPath = path;
		String reverseSuffix = "";
		String anonymousFqName;
		while (true)
		{
			JCClassDecl leaf = (JCClassDecl) currPath.getLeaf();
			anonymousFqName = buildGenericTypeName(leaf);
			if (anonymousFqName.indexOf('.') != -1)
			{
				return anonymousFqName + reverseSuffix;
			}
			else if (reverseSuffix.length() > 0)
			{
				reverseSuffix = "." + anonymousFqName + reverseSuffix;
			}
			else
			{
				reverseSuffix = "." + anonymousFqName;
			}
			currPath = currPath.getParentPath();
		}
	}

	public static String buildGenericTypeName(JCClassDecl classdecl)
	{
		String fqName = classdecl.sym.toString();

		StringBuilder simpleNameBuilder = new StringBuilder(fqName);
		boolean first = true;
		for (JCTypeParameter tp : classdecl.typarams)
		{
			if (first)
			{
				simpleNameBuilder.append('<');
				first = false;
			}
			else
			{
				simpleNameBuilder.append(',');
			}
			simpleNameBuilder.append(tp.type.toString());
		}
		if (!first)
		{
			simpleNameBuilder.append('>');
		}
		return simpleNameBuilder.toString();
	}

	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Override
	protected void handleExpressionIntern(JCNewClass newClass)
	{
		IConversionContext context = this.context.getCurrent();
		ILanguageHelper languageHelper = context.getLanguageHelper();
		IWriter writer = context.getWriter();

		// the type can be null in the case of the internal constructor of enums
		String owner = newClass.type != null ? newClass.type.toString() : null;
		if (owner == null || "<any>".equals(owner))
		{
			owner = newClass.clazz.toString();
		}
		JCClassDecl def = newClass.def;
		if (def == null)
		{
			writer.append("Ext.create(\"");
			languageHelper.writeType(owner);
			writer.append('"');
			List<JCExpression> arguments = newClass.args;
			if (!arguments.isEmpty())
			{
				Iterator<String> paramNames = extractParamNames(newClass);
				writer.append(", { ");
				boolean firstParameter = true;
				for (JCExpression argument : arguments)
				{
					firstParameter = languageHelper.writeStringIfFalse(", ", firstParameter);
					String paramName = paramNames.next();
					writer.append('"').append(paramName).append("\" : ");
					languageHelper.writeExpressionTree(argument);
				}
				writer.append(" }");
			}
			writer.append(')');
			String typeOnStack = context.getClassInfo().getFqName();
			if (newClass.type != null || newClass.clazz instanceof JCIdent)
			{
				typeOnStack = owner;
			}
			context.setTypeOnStack(typeOnStack);
			return;
		}
		// this is an anonymous class instantiation
		writeAnonymousInstantiation(owner, def);
	}

	protected Iterator<String> extractParamNames(JCNewClass newClass)
	{
		IConversionContext context = this.context.getCurrent();
		final IJsHelper languageHelper = (IJsHelper) context.getLanguageHelper();

		MethodSymbol constructor = (MethodSymbol) newClass.constructor;
		ArrayList<String> paramNames = new ArrayList<>();
		if (constructor != null && constructor.params != null)
		{
			for (VarSymbol param : constructor.params)
			{
				paramNames.add(param.name.toString());
			}
		}
		else
		{
			if (constructor != null && constructor.type != null)
			{
				String fqClassName = newClass.type.toString();
				fqClassName = languageHelper.removeGenerics(fqClassName);

				com.sun.tools.javac.util.List<Type> argtypes = ((MethodType) constructor.type).argtypes;
				int size = argtypes.size();
				String[] fqParamClassNames = new String[size];
				for (int i = 0; i < size; i++)
				{
					Type argType = argtypes.get(i);
					fqParamClassNames[i] = languageHelper.removeGenerics(argType.toString());
				}

				ConstructorParamKey key = new ConstructorParamKey(fqClassName, fqParamClassNames);
				String[] paramNamesArray = constructorParamTypesToConstructorParamNames.get(key);
				if (paramNamesArray != null)
				{
					paramNames.addAll(paramNamesArray);
				}
				else
				{
					String newClassString = newClass.toString();
					throw new SnippetTrigger("No names for called constructors parameters available").setContext(newClassString);
				}
			}
			else
			{
				String className = newClass.clazz.toString();
				JavaClassInfo classInfo = context.resolveClassInfo(className, true);
				if (classInfo == null)
				{
					String newClassString = newClass.toString();
					throw new SnippetTrigger("No names or types for called constructors parameters available").setContext(newClassString);
				}
				String fqClassName = classInfo.getFqName();
				fqClassName = languageHelper.removeGenerics(fqClassName);

				com.sun.tools.javac.util.List<JCExpression> args = newClass.args;
				int size = args.size();
				String[] fqParamClassNames = new String[size];
				for (int i = 0; i < size; i++)
				{
					final JCExpression param = args.get(i);
					if (param instanceof JCLiteral)
					{
						Object value = ((JCLiteral) param).value;
						fqParamClassNames[i] = languageHelper.removeGenerics(value.getClass().getName());
					}
					else
					{
						String typeOnStack = astHelper.writeToStash(new IResultingBackgroundWorkerDelegate<String>()
						{
							@Override
							public String invoke() throws Throwable
							{
								IConversionContext context = JsNewClassExpressionHandler.this.context;
								languageHelper.writeExpressionTree(param);
								String resultType = context.getTypeOnStack();
								return resultType;
							}
						});
						JavaClassInfo onStackClassInfo = context.resolveClassInfo(typeOnStack, true);
						if (classInfo != null)
						{
							typeOnStack = onStackClassInfo.getFqName();
						}
						fqParamClassNames[i] = languageHelper.removeGenerics(typeOnStack);
					}
					if (fqParamClassNames[i] == null)
					{
						String newClassString = newClass.toString();
						throw new SnippetTrigger("No names or types for called constructors parameters available").setContext(newClassString);
					}
				}

				ConstructorParamKey key = new ConstructorParamKey(fqClassName, fqParamClassNames);
				String[] paramNamesArray = constructorParamTypesToConstructorParamNames.get(key);
				if (paramNamesArray != null)
				{
					paramNames.addAll(paramNamesArray);
				}
				else
				{
					String newClassString = newClass.toString();
					throw new SnippetTrigger("No names or types for called constructors parameters available").setContext(newClassString);
				}
			}
		}
		return paramNames.iterator();
	}

	protected void writeAnonymousInstantiation(String owner, JCClassDecl def)
	{
		IConversionContext context = this.context.getCurrent();
		IJsHelper languageHelper = (IJsHelper) context.getLanguageHelper();
		IWriter writer = context.getWriter();
		HashSet<String> methodScopeVars = languageHelper.getLanguageSpecific().getMethodScopeVars();

		owner = JsNewClassExpressionHandler.getFqNameFromAnonymousName(def.sym.toString());
		JavaClassInfo newClassInfo = context.resolveClassInfo(owner);

		writer.append("Ext.create(\"");
		languageHelper.writeType(owner);
		writer.append("\", { ");

		HashSet<String> alreadyHandled = new HashSet<>();
		boolean firstParameter = true;
		for (IVariable usedVariable : newClassInfo.getAllUsedVariables())
		{
			String name = usedVariable.getName();
			if (!alreadyHandled.add(name))
			{
				// The IVariable instances have no equals(). So there are duplicates.
				continue;
			}

			firstParameter = languageHelper.writeStringIfFalse(", ", firstParameter);
			writer.append('"').append(name).append("\" : ");
			if (!methodScopeVars.contains(name))
			{
				writer.append("this.");
			}
			writer.append(name);
		}
		writer.append(" })");
		context.setTypeOnStack(owner);
	}

	protected void writeDelegate(String owner, JCClassDecl def)
	{
		IConversionContext context = this.context.getCurrent();
		ILanguageHelper languageHelper = context.getLanguageHelper();
		IWriter writer = context.getWriter();

		owner = getFqNameFromAnonymousName(owner);

		if (def.defs.size() != 2)
		{
			// 1 method is always the constructor, the other method the delegate method
			throw new IllegalStateException("Anonymous class must define exactly one method to be able to be converted to a C# delegate");
		}
		JCMethodDecl delegateMethod = null;
		for (JCTree method : def.defs)
		{
			if (!"<init>".equals(((JCMethodDecl) method).getName().toString()))
			{
				delegateMethod = (JCMethodDecl) method;
				break;
			}
		}
		writer.append("new ");
		languageHelper.writeType(owner);
		writer.append("(delegate(");
		boolean firstParameter = true;

		for (JCVariableDecl parameter : delegateMethod.getParameters())
		{
			firstParameter = languageHelper.writeStringIfFalse(", ", firstParameter);
			IStatementHandlerExtension<StatementTree> stmtHandler = statementHandlerRegistry.getExtension(Lang.JS + parameter.getKind());
			stmtHandler.handle(parameter, false);
		}
		writer.append(')');

		IStatementHandlerExtension<JCBlock> blockHandler = statementHandlerRegistry.getExtension(Lang.JS + Kind.BLOCK);
		blockHandler.handle(delegateMethod.getBody());
		writer.append(')');

		context.setTypeOnStack(owner);
	}
}
