package com.koch.ambeth.service.metadata;

/*-
 * #%L
 * jambeth-service
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

import java.util.regex.Pattern;

public final class EmbeddedMember {
	private static final Pattern pattern = Pattern.compile(Pattern.quote("."));

	public static String[] split(String memberName) {
		return pattern.split(memberName);
	}

	public static String buildMemberPathString(Member[] memberPath) {
		StringBuilder sb = new StringBuilder();
		for (int a = 0, size = memberPath.length; a < size; a++) {
			Member member = memberPath[a];
			if (a > 0) {
				sb.append('.');
			}
			sb.append(member.getName());
		}
		return sb.toString();
	}

	public static String[] buildMemberPathToken(Member[] memberPath) {
		String[] token = new String[memberPath.length];
		for (int a = memberPath.length; a-- > 0;) {
			Member member = memberPath[a];
			token[a] = member.getName();
		}
		return token;
	}

	private EmbeddedMember() {
		// intended blank
	}
}
