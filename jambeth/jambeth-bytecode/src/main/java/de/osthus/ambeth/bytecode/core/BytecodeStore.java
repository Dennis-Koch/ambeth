package de.osthus.ambeth.bytecode.core;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.ObjectOutputStream;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.osthus.ambeth.audit.util.NullOutputStream;
import de.osthus.ambeth.bytecode.IBytecodeClassLoader;
import de.osthus.ambeth.bytecode.IBytecodeEnhancer;
import de.osthus.ambeth.bytecode.IEnhancementHint;
import de.osthus.ambeth.bytecode.behavior.IBytecodeBehavior;
import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.HashMap;
import de.osthus.ambeth.config.Properties;
import de.osthus.ambeth.config.Property;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.util.HexUtil;

/**
 * The problem with this feature is that initialized static fields on the serialized enhanced types can not easily be restored... This may be further evaluated
 * at a later point. Maybe the whole feature can not be implemented efficiently...
 */
@Deprecated
public class BytecodeStore implements IBytecodeStore
{
	private static final Pattern pattern = Pattern.compile("(.+)\\.[^\\.]+");

	@Autowired
	protected IBytecodeClassLoader bytecodeClassLoader;

	@Property(name = "user.home")
	protected String userHome;

	protected String buildHashOfBehaviors(IBytecodeEnhancer bytecodeEnhancer, IBytecodeBehavior[] behaviors)
	{
		try
		{
			MessageDigest digest = MessageDigest.getInstance("SHA-1");

			ObjectOutputStream oos = new ObjectOutputStream(new DigestOutputStream(new NullOutputStream(), digest));

			oos.writeInt(behaviors.length);
			oos.writeObject(bytecodeEnhancer.getClass());
			for (int a = behaviors.length; a-- > 0;)
			{
				oos.writeObject(behaviors[a].getClass());
			}
			oos.close();
			return HexUtil.toHexLowerLetters(digest.digest());
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	protected File getSessionDir(IBytecodeEnhancer bytecodeEnhancer, IBytecodeBehavior[] behaviors)
	{
		String hash = buildHashOfBehaviors(bytecodeEnhancer, behaviors);
		File baseDir = new File(userHome, "jambeth-bytecode");
		return new File(baseDir, hash);
	}

	@Override
	public HashMap<BytecodeStoreKey, BytecodeStoreItem> loadEnhancedTypes(IBytecodeEnhancer bytecodeEnhancer, IBytecodeBehavior[] behaviors)
	{
		File sessionDir = getSessionDir(bytecodeEnhancer, behaviors);

		HashMap<BytecodeStoreKey, BytecodeStoreItem> contentMap = new HashMap<BytecodeStoreKey, BytecodeStoreItem>();
		if (!sessionDir.exists())
		{
			return contentMap;
		}
		File[] hintDirs = sessionDir.listFiles(new FileFilter()
		{
			@Override
			public boolean accept(File pathname)
			{
				return pathname.isDirectory();
			}
		});
		if (hintDirs != null)
		{
			for (int a = hintDirs.length; a-- > 0;)
			{
				scanForHints(hintDirs[a], contentMap);
			}
		}
		return contentMap;
	}

	@Override
	public void storeEnhancedType(BytecodeEnhancer bytecodeEnhancer, IBytecodeBehavior[] behaviors, Class<?> baseType, IEnhancementHint hint,
			Class<?> enhancedType, List<Class<?>> enhancedTypesPipeline)
	{
		try
		{
			File sessionDir = getSessionDir(bytecodeEnhancer, behaviors);

			BytecodeStoreKey key = new BytecodeStoreKey(baseType, hint);

			File hintDir = new File(sessionDir, key.getHintType().getName());

			String name = HexUtil.toHexLowerLetters(key.getSha1());

			File hintInstanceDir = new File(hintDir, name);

			hintInstanceDir.mkdirs();

			File nameFile = new File(hintDir, name + ".properties");
			Properties props = new Properties();
			for (Class<?> enhancedTypeInPipeline : enhancedTypesPipeline)
			{
				File enhancedTypeFile = new File(hintDir, enhancedTypeInPipeline.getName() + ".class");

				props.putString(enhancedTypeInPipeline.getName(), null);

				FileOutputStream fis = new FileOutputStream(enhancedTypeFile);
				try
				{
					byte[] content = bytecodeClassLoader.readTypeAsBinary(enhancedTypeInPipeline);
					fis.write(content);
				}
				finally
				{
					fis.close();
				}
			}
			FileWriter fw = new FileWriter(nameFile, false);
			try
			{
				boolean first = true;
				for (Entry<String, Object> entry : props)
				{
					Object value = entry.getValue();
					if (first)
					{
						first = false;
					}
					else
					{
						fw.append("\r\n");
					}
					fw.append(entry.getKey()).append('=');
					if (value != null)
					{
						fw.append(value.toString());
					}
				}
			}
			finally
			{
				fw.close();
			}
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	protected void scanForHints(File hintDir, Map<BytecodeStoreKey, BytecodeStoreItem> contentMap)
	{
		Class<?> hintType;
		try
		{
			hintType = Thread.currentThread().getContextClassLoader().loadClass(hintDir.getName());
		}
		catch (Throwable e)
		{
			return;
		}
		File[] propertyFiles = hintDir.listFiles(new FileFilter()
		{

			@Override
			public boolean accept(File pathname)
			{
				return pathname.isFile() && pathname.getName().endsWith(".properties");
			}
		});
		if (propertyFiles != null)
		{
			for (int a = propertyFiles.length; a-- > 0;)
			{
				scanForKeys(hintDir, propertyFiles[a], hintType, contentMap);
			}
		}
	}

	protected void scanForKeys(File hintInstanceDir, File propertyFile, Class<?> hintType, Map<BytecodeStoreKey, BytecodeStoreItem> contentMap)
	{
		try
		{
			Properties props = new Properties();

			FileInputStream fis = new FileInputStream(propertyFile);
			try
			{
				props.load(new BufferedInputStream(fis));
			}
			finally
			{
				fis.close();
			}
			Matcher matcher = pattern.matcher(propertyFile.getName());
			if (!matcher.matches())
			{
				throw new IllegalStateException();
			}
			byte[] sha1 = HexUtil.toBytes(matcher.group(1).toString());
			BytecodeStoreKey key = new BytecodeStoreKey(hintType, sha1);
			ArrayList<String> enhancedTypeNames = new ArrayList<String>();
			ArrayList<File> contentFiles = new ArrayList<File>();
			for (Entry<String, Object> entry : props)
			{
				String enhancedTypeName = entry.getKey();
				File contentFile = new File(hintInstanceDir, enhancedTypeName + ".class");
				if (!contentFile.exists())
				{
					// the whole persisted enhancement pipeline seems to be corrupt
					return;
				}
				enhancedTypeNames.add(enhancedTypeName);
				contentFiles.add(contentFile);
			}
			contentMap.put(key, new BytecodeStoreItem(contentFiles.toArray(File.class), enhancedTypeNames.toArray(String.class)));
		}
		catch (Throwable e)
		{
			return;
		}
	}
}
