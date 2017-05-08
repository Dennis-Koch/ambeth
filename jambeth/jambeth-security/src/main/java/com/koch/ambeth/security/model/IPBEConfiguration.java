package com.koch.ambeth.security.model;

/*-
 * #%L
 * jambeth-security
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

public interface IPBEConfiguration {
	String getEncryptionAlgorithm();

	void setEncryptionAlgorithm(String encryptionAlgorithm);

	String getEncryptionKeySpec();

	void setEncryptionKeySpec(String encryptionKeySpec);

	char[] getEncryptionKeyIV();

	void setEncryptionKeyIV(char[] encryptionKeyIV);

	String getPaddedKeyAlgorithm();

	void setPaddedKeyAlgorithm(String paddedKeyAlgorithm);

	int getPaddedKeySize();

	void setPaddedKeySize(int paddedKeySize);

	int getPaddedKeyIterations();

	void setPaddedKeyIterations(int paddedKeyIterations);

	int getPaddedKeySaltSize();

	void setPaddedKeySaltSize(int paddedKeySaltSize);

	char[] getPaddedKeySalt();

	void setPaddedKeySalt(char[] paddedKeySalt);
}
