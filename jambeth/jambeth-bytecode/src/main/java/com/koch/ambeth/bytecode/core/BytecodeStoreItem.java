package com.koch.ambeth.bytecode.core;

/*-
 * #%L
 * jambeth-bytecode
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

import java.io.File;
import java.io.FileInputStream;

import com.koch.ambeth.bytecode.IBytecodeClassLoader;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;

public class BytecodeStoreItem {
	protected final File[] files;

	protected final String[] enhancedTypeNames;

	public BytecodeStoreItem(File[] files, String[] enhancedTypeNames) {
		this.files = files;
		this.enhancedTypeNames = enhancedTypeNames;
	}

	public Class<?> readEnhancedType(IBytecodeClassLoader bytecodeClassLoader,
			ClassLoader classLoader) {
		try {
			Class<?> lastEnhancedType = null;
			for (int a = 0, size = files.length; a < size; a++) {
				File file = files[a];
				byte[] content = new byte[(int) file.length()];
				FileInputStream fis = new FileInputStream(file);
				try {
					if (fis.read(content) != file.length()) {
						throw new IllegalStateException();
					}
					lastEnhancedType =
							bytecodeClassLoader.loadClass(enhancedTypeNames[a], content, classLoader);
				}
				finally {
					fis.close();
				}
			}
			return lastEnhancedType;
		}
		catch (Exception e) {
			throw RuntimeExceptionUtil.mask(e);
		}
	}
}
