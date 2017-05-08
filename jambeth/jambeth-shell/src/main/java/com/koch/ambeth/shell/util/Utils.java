package com.koch.ambeth.shell.util;

/*-
 * #%L
 * jambeth-shell
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

import java.io.IOException;
import java.io.InputStream;

public class Utils {
	public static String stringPadEnd(String string, int minLength, char padChar) {
		if (string.length() >= minLength) {
			return string;
		}
		StringBuilder sb = new StringBuilder(minLength);
		sb.append(string);
		for (int i = string.length(); i < minLength; i++) {
			sb.append(padChar);
		}
		return sb.toString();
	}

	public static String readInputStream(InputStream is) throws IOException {
		StringBuilder sb = new StringBuilder();
		int bufferSize = 1024;
		byte[] buffer = new byte[bufferSize];
		int amountRead = -1;
		while ((amountRead = is.read(buffer)) != -1) {
			sb.append(new String(buffer, 0, amountRead));
		}
		return sb.toString();
	}
}
