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

import java.util.Calendar;

import com.koch.ambeth.util.annotation.Interning;

public interface IPassword {
	public static final String Algorithm = "Algorithm";

	public static final String ChangeAfter = "ChangeAfter";

	public static final String IterationCount = "IterationCount";

	public static final String HistoryUser = "HistoryUser";

	public static final String KeySize = "KeySize";

	public static final String Salt = "Salt";

	public static final String User = "User";

	public static final String Value = "Value";

	IUser getUser();

	IUser getHistoryUser();

	char[] getValue();

	void setValue(char[] value);

	Calendar getChangeAfter();

	void setChangeAfter(Calendar changeAfter);

	@Interning
	String getAlgorithm();

	void setAlgorithm(String algorithm);

	int getIterationCount();

	void setIterationCount(int iterationCount);

	int getKeySize();

	void setKeySize(int keySize);

	char[] getSalt();

	void setSalt(char[] salt);

	Integer getSaltLength();

	void setSaltLength(Integer saltLength);

	IPBEConfiguration getSaltPBEConfiguration();
}
