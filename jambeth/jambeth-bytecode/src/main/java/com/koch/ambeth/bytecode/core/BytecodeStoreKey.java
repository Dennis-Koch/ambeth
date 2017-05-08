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

import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.util.Arrays;

import com.koch.ambeth.ioc.bytecode.IEnhancementHint;
import com.koch.ambeth.util.audit.util.NullOutputStream;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;

public class BytecodeStoreKey implements Serializable {
	private static final long serialVersionUID = 4538722613389001123L;

	protected final Class<?> hintType;

	protected final byte[] sha1;

	public BytecodeStoreKey(Class<?> hintType, byte[] sha1) {
		this.hintType = hintType;
		this.sha1 = sha1;
	}

	public BytecodeStoreKey(Class<?> baseType, IEnhancementHint hint) {
		hintType = hint.getClass();
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-1");

			ObjectOutputStream oos =
					new ObjectOutputStream(new DigestOutputStream(new NullOutputStream(), digest));

			oos.writeObject(baseType);
			oos.writeObject(hint);
			oos.close();
			sha1 = digest.digest();
		}
		catch (Throwable e) {
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	public Class<?> getHintType() {
		return hintType;
	}

	public byte[] getSha1() {
		return sha1;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (!(obj instanceof BytecodeStoreKey)) {
			return false;
		}
		BytecodeStoreKey other = (BytecodeStoreKey) obj;
		return hintType.equals(other.hintType) && Arrays.equals(sha1, other.sha1);
	}

	@Override
	public int hashCode() {
		return getClass().hashCode() ^ hintType.hashCode() ^ Arrays.hashCode(sha1);
	}

	@Override
	public String toString() {
		return hintType + " SHA1:" + Arrays.toString(sha1);
	}
}
