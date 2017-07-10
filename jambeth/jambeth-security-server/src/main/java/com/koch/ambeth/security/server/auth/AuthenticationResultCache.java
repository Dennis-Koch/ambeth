package com.koch.ambeth.security.server.auth;

/*-
 * #%L
 * jambeth-security-server
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

import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.koch.ambeth.ioc.IInitializingBean;
import com.koch.ambeth.security.IAuthentication;
import com.koch.ambeth.security.IAuthenticationResult;
import com.koch.ambeth.service.cache.ClearAllCachesEvent;
import com.koch.ambeth.util.collections.IMapEntry;
import com.koch.ambeth.util.collections.WeakHashMap;

public class AuthenticationResultCache implements IInitializingBean, IAuthenticationResultCache {
	public static class CacheValue {
		public final IAuthenticationResult authentiationResult;

		public final long createdTimestamp = System.currentTimeMillis();

		public CacheValue(IAuthenticationResult authentiationResult) {
			this.authentiationResult = authentiationResult;
		}
	}

	public static final String DELEGATE_HANDLE_CLEAR_ALL_CACHES_EVENT = "handleClearAllCachesEvent";

	protected final Charset charset = Charset.forName("UTF-8");

	protected MessageDigest digest;

	protected final Lock writeLock = new ReentrantLock();

	protected long cacheInterval = 60000;

	protected final WeakHashMap<byte[], CacheValue> cachedAuthenticationResultMap = new WeakHashMap<byte[], CacheValue>() {

		@Override
		protected boolean equalKeys(byte[] key, IMapEntry<byte[], CacheValue> entry) {
			return Arrays.equals(key, entry.getKey());
		}

		@Override
		protected int extractHash(byte[] key) {
			return Arrays.hashCode(key);
		}
	};

	@Override
	public void afterPropertiesSet() throws Throwable {
		digest = MessageDigest.getInstance("SHA-256");
	}

	public void handleClearAllCachesEvent(ClearAllCachesEvent evnt) {
		Lock writeLock = this.writeLock;
		writeLock.lock();
		try {
			cachedAuthenticationResultMap.clear();
		}
		finally {
			writeLock.unlock();
		}
	}

	protected byte[] buildAuthenticationHash(IAuthentication authentication) {
		digest.reset();
		digest.update(authentication.getUserName().getBytes(charset));
		return digest.digest(new String(authentication.getPassword()).getBytes(charset));
	}

	@Override
	public IAuthenticationResult resolveAuthenticationResult(IAuthentication authentication) {
		Lock writeLock = this.writeLock;
		writeLock.lock();
		try {
			byte[] hash = buildAuthenticationHash(authentication);
			CacheValue cacheValue = cachedAuthenticationResultMap.get(hash);
			if (cacheValue == null) {
				return null;
			}
			if (cacheValue.createdTimestamp + cacheInterval < System.currentTimeMillis()) {
				cachedAuthenticationResultMap.remove(hash);
				return null;
			}
			return cacheValue.authentiationResult;
		}
		finally {
			writeLock.unlock();
		}
	}

	@Override
	public void cacheAuthenticationResult(IAuthentication authentication,
			IAuthenticationResult authenticationResult) {
		writeLock.lock();
		try {
			byte[] hash = buildAuthenticationHash(authentication);
			cachedAuthenticationResultMap.put(hash, new CacheValue(authenticationResult));
		}
		finally {
			writeLock.unlock();
		}

	}
}
