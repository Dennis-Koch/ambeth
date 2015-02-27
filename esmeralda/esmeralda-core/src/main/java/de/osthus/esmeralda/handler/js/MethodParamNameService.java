package de.osthus.esmeralda.handler.js;

import java.util.Arrays;

import de.osthus.ambeth.collections.HashMap;
import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;

public class MethodParamNameService implements IMethodParamNameService, IInitializingBean
{
	private static class MethodParamKey
	{
		public static final String ANY = "*"; // Use only if no overloads exist

		private final String fqClassName;

		private final String methodName;

		private final String[] fqParamClassNames;

		public MethodParamKey(String fqClassName, String methodName, String... fqParamClassNames)
		{
			this.fqClassName = fqClassName;
			this.methodName = methodName;
			this.fqParamClassNames = Arrays.copyOf(fqParamClassNames, fqParamClassNames.length);
		}

		public MethodParamKey(Class<?> fqClass, String methodName, Class<?>... fqParamClasses)
		{
			this(fqClass.getName(), methodName, toFqNames(fqParamClasses));
		}

		private static String[] toFqNames(Class<?>[] fqParamClasses)
		{
			int length = fqParamClasses.length;
			String[] fqParamClassNames = new String[length];
			for (int i = 0; i < length; i++)
			{
				Class<?> fqParamClass = fqParamClasses[i];
				if (fqParamClass != null)
				{
					fqParamClassNames[i] = fqParamClass.getName();
				}
				else
				{
					fqParamClassNames[i] = ANY;
				}
			}
			return fqParamClassNames;
		}

		@Override
		public int hashCode()
		{
			int hashCode = fqClassName.hashCode();
			hashCode += 23 * methodName.hashCode();
			hashCode *= fqParamClassNames.length;
			return hashCode;
		}

		@Override
		public boolean equals(Object obj)
		{
			if (!(obj instanceof MethodParamKey))
			{
				return false;
			}

			MethodParamKey other = (ConstructorParamKey) obj;

			String[] otherFqParamClassNames = other.fqParamClassNames;
			if (!fqClassName.equals(other.fqClassName) || !methodName.equals(other.methodName) || fqParamClassNames.length != otherFqParamClassNames.length)
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

	private static class ConstructorParamKey extends MethodParamKey
	{
		public ConstructorParamKey(String fqClassName, String... fqParamClassNames)
		{
			super(fqClassName, CONSTRUCTOR_NAME, fqParamClassNames);
		}

		public ConstructorParamKey(Class<?> fqClass, Class<?>... fqParamClasses)
		{
			super(fqClass, CONSTRUCTOR_NAME, fqParamClasses);
		}
	}

	private static final String CONSTRUCTOR_NAME = "<init>";

	@LogInstance
	private ILogger log;

	protected static final HashMap<MethodParamKey, String[]> methodParamTypesToConstructorParamNames = new HashMap<>();

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		init();
	}

	@Override
	public String[] getConstructorParamNames(String fqClassName, String... fqParamClassNames)
	{
		ConstructorParamKey key = new ConstructorParamKey(fqClassName, fqParamClassNames);
		String[] paramNames = get(key);
		return paramNames;
	}

	@Override
	public String[] getMethodParamNames(String fqClassName, String methodName, String... fqParamClassNames)
	{
		if (!CONSTRUCTOR_NAME.equals(methodName))
		{
			throw new UnsupportedOperationException("Currently only constructors are supported");
		}

		MethodParamKey key = new MethodParamKey(fqClassName, methodName, fqParamClassNames);
		String[] paramNames = get(key);
		return paramNames;
	}

	protected String[] get(MethodParamKey key)
	{
		String[] paramNames = methodParamTypesToConstructorParamNames.get(key);
		return paramNames;
	}

	private void init()
	{
		String fqNameString = String.class.getName();
		String fqNameSInt = int.class.getName();
		String fqNameSFloat = float.class.getName();
		String fqNameInteger = Integer.class.getName();
		String fqNameFloat = Float.class.getName();
		String fqNameByteArray = byte.class.getName() + "[]"; // getName() returns "[B"
		String fqNameCharArray = char.class.getName() + "[]"; // getName() returns "[C"
		String fqNameObjectArray = Object.class.getName() + "[]"; // getName() returns "[Ljava.lang.Object;"
		String fqNameCollection = java.util.Collection.class.getName();
		String fqNameCharset = java.nio.charset.Charset.class.getName();
		String fqNameReader = java.io.Reader.class.getName();
		String fqNameWriter = java.io.Writer.class.getName();
		String fqNameInputStream = java.io.InputStream.class.getName();
		String fqNameOutputStream = java.io.OutputStream.class.getName();
		String fqNameInputStreamReader = java.io.InputStreamReader.class.getName();
		String fqNameOutputStreamWriter = java.io.OutputStreamWriter.class.getName();
		String fqNameBufferedInputStream = java.io.BufferedInputStream.class.getName();
		String fqNameBufferedOutputStream = java.io.BufferedOutputStream.class.getName();
		String fqNameFileInputStream = java.io.FileInputStream.class.getName();
		String fqNameByteArrayInputStream = java.io.ByteArrayInputStream.class.getName();
		String fqNameBufferedReader = java.io.BufferedReader.class.getName();
		String fqNameBufferedWriter = java.io.BufferedWriter.class.getName();
		String fqNameArrayList = de.osthus.ambeth.collections.ArrayList.class.getName();
		String fqNameSetLinkedEntry = de.osthus.ambeth.collections.SetLinkedEntry.class.getName();
		String fqNameListElem = de.osthus.ambeth.collections.ListElem.class.getName();
		String fqNameCleanupInvalidKeysSet = de.osthus.ambeth.collections.CleanupInvalidKeysSet.class.getName();
		String fqNameIInvalidKeyChecker = de.osthus.ambeth.collections.IInvalidKeyChecker.class.getName();

		String pgNameCollections = "de.osthus.ambeth.collections.";

		// Single String constructors
		registerConstructor(new ConstructorParamKey(java.io.File.class, String.class), "pathname");
		registerConstructor(new ConstructorParamKey(java.io.FileInputStream.class, String.class), "name");
		registerConstructor(new ConstructorParamKey(java.io.FileOutputStream.class, String.class), "name");
		registerConstructor(new ConstructorParamKey(StringBuilder.class, String.class), "str");
		registerConstructor(new ConstructorParamKey(java.math.BigDecimal.class, String.class), "val");
		registerConstructor(new ConstructorParamKey(java.math.BigDecimal.class, java.math.BigInteger.class), "val");
		registerConstructor(new ConstructorParamKey(java.math.BigInteger.class, String.class), "val");
		registerConstructor(new ConstructorParamKey(java.text.SimpleDateFormat.class, String.class), "pattern");

		// Constructors on String
		registerConstructor(new ConstructorParamKey(fqNameString, fqNameByteArray, fqNameCharset), "bytes", "charset");
		registerConstructor(new ConstructorParamKey(fqNameString, fqNameByteArray, fqNameString), "bytes", "charsetName");
		registerConstructor(new ConstructorParamKey(fqNameString, fqNameByteArray, fqNameSInt, fqNameSInt, fqNameCharset), "bytes", "offset", "length",
				"charset");
		registerConstructor(new ConstructorParamKey(fqNameString, fqNameByteArray, fqNameSInt, fqNameSInt, fqNameString), "bytes", "offset", "length",
				"charsetName");
		registerConstructor(new ConstructorParamKey(fqNameString, fqNameCharArray), "value");

		// Exception constructors
		registerConstructor(new ConstructorParamKey(java.io.IOException.class, String.class), "message");
		registerConstructor(new ConstructorParamKey(java.io.IOException.class, Throwable.class), "cause");
		registerConstructor(new ConstructorParamKey(ClassCastException.class, String.class), "s");
		registerConstructor(new ConstructorParamKey(IllegalArgumentException.class, String.class), "s");
		registerConstructor(new ConstructorParamKey(IllegalArgumentException.class, String.class, Throwable.class), "message", "cause");
		registerConstructor(new ConstructorParamKey(IllegalStateException.class, String.class), "s");
		registerConstructor(new ConstructorParamKey(IllegalStateException.class, String.class, Throwable.class), "message", "cause");
		registerConstructor(new ConstructorParamKey(NullPointerException.class, String.class), "s");
		registerConstructor(new ConstructorParamKey(RuntimeException.class, String.class), "message");
		registerConstructor(new ConstructorParamKey(RuntimeException.class, Throwable.class), "cause");
		registerConstructor(new ConstructorParamKey(RuntimeException.class, String.class, Throwable.class), "message", "cause");
		registerConstructor(new ConstructorParamKey(UnsupportedOperationException.class, String.class), "message");

		// Java collections constructors
		registerConstructor(new ConstructorParamKey(java.util.ArrayList.class, int.class), "initialCapacity");
		registerConstructor(new ConstructorParamKey(java.util.ArrayList.class, Integer.class), "initialCapacity");
		registerHashConstructors("java.util.HashSet", fqNameSInt, fqNameInteger, fqNameSFloat, fqNameFloat);
		registerHashConstructors("java.util.HashMap", fqNameSInt, fqNameInteger, fqNameSFloat, fqNameFloat);

		// Ambeth collections constructors
		registerConstructor(new ConstructorParamKey(fqNameArrayList, fqNameSInt), "initialCapacity");
		registerConstructor(new ConstructorParamKey(fqNameArrayList, fqNameInteger), "initialCapacity");
		registerConstructor(new ConstructorParamKey(fqNameArrayList, fqNameCollection), "coll");
		registerConstructor(new ConstructorParamKey(fqNameArrayList, fqNameObjectArray), "array");
		registerConstructor(new ConstructorParamKey(fqNameSetLinkedEntry, fqNameSInt, "K", fqNameSetLinkedEntry), "hash", "key", "nextEntry");
		registerConstructor(new ConstructorParamKey(fqNameListElem, MethodParamKey.ANY), "value");
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
		registerConstructor(new ConstructorParamKey(java.util.Date.class, long.class), "date");
		registerConstructor(new ConstructorParamKey(java.sql.Date.class, long.class), "date");
		registerConstructor(new ConstructorParamKey(java.sql.Timestamp.class, long.class), "time");
		registerConstructor(new ConstructorParamKey(java.io.File.class, String.class, String.class), "parent", "child");
		registerConstructor(new ConstructorParamKey(fqNameInputStreamReader, fqNameInputStream, fqNameCharset), "in", "cs");
		registerConstructor(new ConstructorParamKey(fqNameOutputStreamWriter, fqNameOutputStream, fqNameCharset), "out", "cs");
		registerConstructor(new ConstructorParamKey(fqNameBufferedInputStream, fqNameInputStream), "in");
		registerConstructor(new ConstructorParamKey(fqNameBufferedOutputStream, fqNameOutputStream), "out");
		registerConstructor(new ConstructorParamKey(fqNameFileInputStream, fqNameReader), "in");
		registerConstructor(new ConstructorParamKey(java.io.FileInputStream.class, java.io.File.class), "file");
		registerConstructor(new ConstructorParamKey(java.io.FileOutputStream.class, java.io.File.class, boolean.class), "file", "append");
		registerConstructor(new ConstructorParamKey(fqNameByteArrayInputStream, fqNameByteArray), "buf");
		registerConstructor(new ConstructorParamKey(fqNameBufferedReader, fqNameReader), "in");
		registerConstructor(new ConstructorParamKey(fqNameBufferedWriter, fqNameWriter), "out");
		registerConstructor(new ConstructorParamKey(fqNameBufferedWriter, fqNameOutputStreamWriter), "out");
		registerConstructor(new ConstructorParamKey(java.util.concurrent.CountDownLatch.class, int.class), "count");
		registerConstructor(new ConstructorParamKey(java.lang.ref.SoftReference.class, (Class<?>) null), "referent");
		registerConstructor(new ConstructorParamKey(java.lang.ref.SoftReference.class, null, java.lang.ref.ReferenceQueue.class), "referent", "q");
		registerConstructor(new ConstructorParamKey(java.lang.ref.WeakReference.class, (Class<?>) null), "referent");
		registerConstructor(new ConstructorParamKey(java.lang.ref.WeakReference.class, null, java.lang.ref.ReferenceQueue.class), "referent", "q");
		registerConstructor(new ConstructorParamKey(javax.management.ObjectName.class, String.class), "name");

		// Other Ambeth constructors
		registerConstructor(new ConstructorParamKey(de.osthus.ambeth.config.Properties.class, de.osthus.ambeth.config.IProperties.class), "parent");
		registerConstructor(new ConstructorParamKey(de.osthus.ambeth.repackaged.org.objectweb.asm.ClassWriter.class, int.class), "flags");
		registerConstructor(new ConstructorParamKey(de.osthus.ambeth.appendable.AppendableStringBuilder.class, StringBuilder.class), "sb");
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
			methodParamTypesToConstructorParamNames.put(constructorParamKey, names);
		}
	}

	protected static void registerConstructor(ConstructorParamKey key, String... paramNames)
	{
		methodParamTypesToConstructorParamNames.put(key, paramNames);
	}
}
