package de.osthus.ambeth.bytecode.core;

import java.io.File;
import java.io.FileInputStream;

import de.osthus.ambeth.bytecode.IBytecodeClassLoader;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;

public class BytecodeStoreItem
{
	protected final File[] files;

	protected final String[] enhancedTypeNames;

	public BytecodeStoreItem(File[] files, String[] enhancedTypeNames)
	{
		this.files = files;
		this.enhancedTypeNames = enhancedTypeNames;
	}

	public Class<?> readEnhancedType(IBytecodeClassLoader bytecodeClassLoader)
	{
		try
		{
			Class<?> lastEnhancedType = null;
			for (int a = 0, size = files.length; a < size; a++)
			{
				File file = files[a];
				byte[] content = new byte[(int) file.length()];
				FileInputStream fis = new FileInputStream(file);
				try
				{
					if (fis.read(content) != file.length())
					{
						throw new IllegalStateException();
					}
					lastEnhancedType = bytecodeClassLoader.loadClass(enhancedTypeNames[a], content);
				}
				finally
				{
					fis.close();
				}
			}
			return lastEnhancedType;
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}
}
