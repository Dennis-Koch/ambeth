package com.koch.ambeth.server.helloworld.security;

/*-
 * #%L
 * jambeth-server-helloworld
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

import com.koch.ambeth.security.model.IPBEConfiguration;
import com.koch.ambeth.security.model.ISignAndVerify;
import com.koch.ambeth.security.model.ISignature;
import com.koch.ambeth.security.model.IUser;

public class PojoSignature implements ISignature, IPBEConfiguration, ISignAndVerify {
	protected IUser user;

	protected char[] privateKey;

	protected char[] publicKey;

	private String signatureAlgorithm;

	private String keyFactoryAlgorithm;

	private String encryptionAlgorithm;

	private String encryptionKeySpec;

	private char[] encryptionKeyIV;

	private char[] paddedKeySalt;

	private int paddedKeySaltSize;

	private String paddedKeyAlgorithm;

	private int paddedKeySize;

	private int paddedKeyIterations;

	@Override
	public IUser getUser() {
		return user;
	}

	public void setUser(IUser user) {
		this.user = user;
	}

	@Override
	public char[] getPrivateKey() {
		return privateKey;
	}

	@Override
	public void setPrivateKey(char[] privateKey) {
		this.privateKey = privateKey;
	}

	@Override
	public char[] getPublicKey() {
		return publicKey;
	}

	@Override
	public void setPublicKey(char[] publicKey) {
		this.publicKey = publicKey;
	}

	@Override
	public IPBEConfiguration getPBEConfiguration() {
		return this;
	}

	@Override
	public ISignAndVerify getSignAndVerify() {
		return this;
	}

	@Override
	public String getSignatureAlgorithm() {
		return signatureAlgorithm;
	}

	@Override
	public void setSignatureAlgorithm(String signatureAlgorithm) {
		this.signatureAlgorithm = signatureAlgorithm;
	}

	@Override
	public String getKeyFactoryAlgorithm() {
		return keyFactoryAlgorithm;
	}

	@Override
	public void setKeyFactoryAlgorithm(String keyFactoryAlgorithm) {
		this.keyFactoryAlgorithm = keyFactoryAlgorithm;
	}

	@Override
	public String getEncryptionAlgorithm() {
		return encryptionAlgorithm;
	}

	@Override
	public void setEncryptionAlgorithm(String encryptionAlgorithm) {
		this.encryptionAlgorithm = encryptionAlgorithm;
	}

	@Override
	public String getEncryptionKeySpec() {
		return encryptionKeySpec;
	}

	@Override
	public void setEncryptionKeySpec(String encryptionKeySpec) {
		this.encryptionKeySpec = encryptionKeySpec;
	}

	@Override
	public char[] getEncryptionKeyIV() {
		return encryptionKeyIV;
	}

	@Override
	public void setEncryptionKeyIV(char[] encryptionKeyIV) {
		this.encryptionKeyIV = encryptionKeyIV;
	}

	@Override
	public String getPaddedKeyAlgorithm() {
		return paddedKeyAlgorithm;
	}

	@Override
	public void setPaddedKeyAlgorithm(String paddedKeyAlgorithm) {
		this.paddedKeyAlgorithm = paddedKeyAlgorithm;
	}

	@Override
	public int getPaddedKeySize() {
		return paddedKeySize;
	}

	@Override
	public void setPaddedKeySize(int paddedKeySize) {
		this.paddedKeySize = paddedKeySize;
	}

	@Override
	public int getPaddedKeyIterations() {
		return paddedKeyIterations;
	}

	@Override
	public void setPaddedKeyIterations(int paddedKeyIterations) {
		this.paddedKeyIterations = paddedKeyIterations;
	}

	@Override
	public int getPaddedKeySaltSize() {
		return paddedKeySaltSize;
	}

	@Override
	public void setPaddedKeySaltSize(int paddedKeySaltSize) {
		this.paddedKeySaltSize = paddedKeySaltSize;
	}

	@Override
	public char[] getPaddedKeySalt() {
		return paddedKeySalt;
	}

	@Override
	public void setPaddedKeySalt(char[] paddedKeySalt) {
		this.paddedKeySalt = paddedKeySalt;
	}
}
