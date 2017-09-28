package com.koch.ambeth.service.rest;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;

import javax.net.ssl.SSLContext;

import org.apache.http.ssl.SSLContexts;

import com.koch.ambeth.ioc.IInitializingBean;
import com.koch.ambeth.ioc.config.Property;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.service.ISSLContextFactory;
import com.koch.ambeth.service.rest.config.RESTConfigurationConstants;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;

public class SSLContextFactory implements ISSLContextFactory, IInitializingBean {
	@LogInstance
	private ILogger log;

	@Property(name = RESTConfigurationConstants.SslKeyStorePassword, defaultValue = "developer")
	protected char[] storePassword;

	@Property(name = RESTConfigurationConstants.SslKeyStoreFile,
			defaultValue = "configuration/publicKey.store")
	protected Path keyStoreFile;

	@Override
	public void afterPropertiesSet() throws Throwable {
		keyStoreFile = keyStoreFile.toAbsolutePath().normalize();
		if (!Files.exists(keyStoreFile) || Files.isDirectory(keyStoreFile)) {
			if (log.isDebugEnabled()) {
				log.debug("Keystore file '" + keyStoreFile
						+ "' not found. Please configure property '" + "ssl.keystore.file" + "' correctly");
			}
			keyStoreFile = null;
		}
	}

	@Override
	public SSLContext createSSLContext() {
		try {
			KeyStore store = readStore();
			if (store == null) {
				return null;
			}
			return SSLContexts.custom().loadKeyMaterial(store, null).build();
		}
		catch (Exception e) {
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	protected KeyStore readStore() {
		try (InputStream keyStoreStream = Files.newInputStream(keyStoreFile)) {
			KeyStore keyStore = KeyStore.getInstance("JKS"); // or "PKCS12"
			keyStore.load(keyStoreStream, storePassword);
			return keyStore;
		}
		catch (Exception e) {
			try (InputStream keyStoreStream = Files.newInputStream(keyStoreFile)) {
				KeyStore keyStore = KeyStore.getInstance("PKCS12");
				keyStore.load(keyStoreStream, storePassword);
				return keyStore;
			}
			catch (Exception e2) {
				throw RuntimeExceptionUtil.mask(e);
			}
		}
	}
}
