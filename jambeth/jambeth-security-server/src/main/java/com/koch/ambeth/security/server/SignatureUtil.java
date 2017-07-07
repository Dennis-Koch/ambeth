package com.koch.ambeth.security.server;

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

import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.config.Property;
import com.koch.ambeth.security.model.IPBEConfiguration;
import com.koch.ambeth.security.model.ISignAndVerify;
import com.koch.ambeth.security.model.ISignature;
import com.koch.ambeth.security.server.config.SecurityServerConfigurationConstants;
import com.koch.ambeth.util.ParamChecker;
import com.koch.ambeth.util.codec.Base64;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;

public class SignatureUtil implements ISignatureUtil {
	@Autowired
	protected IPBEncryptor pbEncryptor;

	@Autowired
	protected ISecureRandom secureRandom;

	@Property(name = SecurityServerConfigurationConstants.SignatureAlgorithmName, defaultValue = "SHA1withECDSA")
	protected String algorithm;

	@Property(name = SecurityServerConfigurationConstants.SignatureKeyAlgorithmName, defaultValue = "EC")
	protected String keyFactoryAlgorithm;

	@Property(name = SecurityServerConfigurationConstants.SignatureKeySize, defaultValue = "384")
	protected int keySize;

	protected KeyPairGenerator keyGen;

	@Override
	public void generateNewSignature(ISignature newEmptySignature, char[] clearTextPassword) {
		ParamChecker.assertParamNotNull(clearTextPassword, "clearTextPassword");

		try {
			if (keyGen == null) {
				synchronized (this) {
					if (keyGen == null) {
						keyGen = KeyPairGenerator.getInstance(keyFactoryAlgorithm);
						keyGen.initialize(keySize, secureRandom.getSecureRandomHandle());
					}
				}
			}
			newEmptySignature.getSignAndVerify().setSignatureAlgorithm(algorithm);
			// important that the keyFactoryAlgorithm matches the keyGenerator algorithm here
			newEmptySignature.getSignAndVerify().setKeyFactoryAlgorithm(keyFactoryAlgorithm);

			KeyPair pair;
			synchronized (keyGen) {
				// cannot be 100 percent sure if the keyGen is thread-safe, so we synchronize it here
				pair = keyGen.generateKeyPair();
			}

			byte[] unencryptedPrivateKey = pair.getPrivate().getEncoded();
			byte[] encryptedPrivateKey = pbEncryptor.encrypt(newEmptySignature.getPBEConfiguration(),
					true, clearTextPassword, unencryptedPrivateKey);

			newEmptySignature
					.setPublicKey(Base64.encodeBytes(pair.getPublic().getEncoded()).toCharArray());
			newEmptySignature.setPrivateKey(Base64.encodeBytes(encryptedPrivateKey).toCharArray());
		}
		catch (Exception e) {
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	@Override
	public void reencryptSignature(ISignature signature, char[] oldClearTextPassword,
			char[] newClearTextPassword) {
		try {
			byte[] encryptedPrivateKey = Base64.decode(signature.getPrivateKey());
			IPBEConfiguration pbec = signature.getPBEConfiguration();
			byte[] decryptedPrivateKey = pbEncryptor.decrypt(pbec, oldClearTextPassword,
					encryptedPrivateKey);
			pbec.setPaddedKeyAlgorithm(null);
			pbec.setPaddedKeyIterations(0);
			pbec.setPaddedKeySize(0);
			pbec.setPaddedKeySaltSize(0);
			pbec.setPaddedKeySalt(null);
			pbec.setEncryptionAlgorithm(null);
			pbec.setEncryptionKeySpec(null);
			pbec.setEncryptionKeyIV(null);
			encryptedPrivateKey = pbEncryptor.encrypt(pbec, true, newClearTextPassword,
					decryptedPrivateKey);
			signature.setPrivateKey(Base64.encodeBytes(encryptedPrivateKey).toCharArray());
		}
		catch (Exception e) {
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	@Override
	public Signature createSignatureHandle(ISignAndVerify signAndVerify, byte[] privateKey) {
		try {
			// use the private key to create the signature handle
			PKCS8EncodedKeySpec decryptedPrivateKeySpec = new PKCS8EncodedKeySpec(privateKey);
			KeyFactory keyFactory = KeyFactory.getInstance(signAndVerify.getKeyFactoryAlgorithm());
			PrivateKey privateKeyHandle = keyFactory.generatePrivate(decryptedPrivateKeySpec);
			Signature jSignature = java.security.Signature
					.getInstance(signAndVerify.getSignatureAlgorithm());
			jSignature.initSign(privateKeyHandle, secureRandom.getSecureRandomHandle());
			return jSignature;
		}
		catch (Exception e) {
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	@Override
	public Signature createVerifyHandle(ISignAndVerify signAndVerify, byte[] publicKey) {
		try {
			// use the public key to create the signature handle
			X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKey);
			KeyFactory keyFactory = KeyFactory.getInstance(signAndVerify.getKeyFactoryAlgorithm());
			PublicKey publicKeyHandle = keyFactory.generatePublic(keySpec);
			Signature jSignature = java.security.Signature
					.getInstance(signAndVerify.getSignatureAlgorithm());
			jSignature.initVerify(publicKeyHandle);
			return jSignature;
		}
		catch (Exception e) {
			throw RuntimeExceptionUtil.mask(e);
		}
	}
}
