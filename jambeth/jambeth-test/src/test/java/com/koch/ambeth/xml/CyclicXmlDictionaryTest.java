package com.koch.ambeth.xml;

/*-
 * #%L
 * jambeth-test
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Before;
import org.junit.Test;

import com.koch.ambeth.util.ReflectUtil;

public class CyclicXmlDictionaryTest {
	private static final String pathToCSharp =
			"../../ambeth/Ambeth.Xml/ambeth/xml/CyclicXmlDictionary.cs";

	CyclicXmlDictionary fixture;

	@Before
	public void setUp() {
		fixture = new CyclicXmlDictionary();
	}

	@Test
	public void compareDictionaries() throws Exception {
		File cSharpCode = new File(pathToCSharp);
		System.out.println(cSharpCode.getAbsolutePath());
		assertTrue("Cannot find C# code. Test skiped.", cSharpCode.canRead());

		List<Method> getters = getFixtureGetters();
		Map<String, String> cSharpValues = readCSharpValues(cSharpCode);

		checkCompleteness(getters, cSharpValues);

		for (Method getter : getters) {
			String fullName = getter.getName();
			String name = fullName.substring(3, fullName.length());
			String javaValue = (String) getter.invoke(fixture);
			String cSharpValue = cSharpValues.get(name);
			assertEquals(javaValue, cSharpValue);
		}
	}

	protected List<Method> getFixtureGetters() {
		Class<?> fixtureType = fixture.getClass();

		List<Method> getters = new ArrayList<Method>();
		Method[] allMethods = ReflectUtil.getMethods(fixtureType);
		for (Method method : allMethods) {
			if (method.getReturnType().equals(String.class) && method.getParameterTypes().length == 0
					&& method.getName().startsWith("get")) {
				getters.add(method);
			}
		}

		return getters;
	}

	protected Map<String, String> readCSharpValues(File cSharpFile) throws IOException {
		StringBuilder sb = new StringBuilder((int) cSharpFile.length());
		BufferedReader in = new BufferedReader(new FileReader(cSharpFile));
		try {
			String line;
			while ((line = in.readLine()) != null) {
				sb.append(line);
			}
		}
		finally {
			in.close();
		}
		String cSharpCode = sb.toString();
		cSharpCode = cSharpCode.replaceAll("\\s+", " ");

		Pattern valuePattern = Pattern.compile("public String (.+?) \\{ get \\{ return \"(.+?)\"; } }");
		Matcher matcher = valuePattern.matcher(cSharpCode);

		Map<String, String> values = new HashMap<String, String>();
		while (matcher.find()) {
			String name = matcher.group(1);
			String value = matcher.group(2);
			values.put(name, value);
		}

		return values;
	}

	protected void checkCompleteness(List<Method> getters, Map<String, String> cSharpValues) {
		Set<String> javaNames = new HashSet<String>(getters.size());
		Set<String> cSharpNames = new HashSet<String>(cSharpValues.size());

		for (Method getter : getters) {
			String fullName = getter.getName();
			String name = fullName.substring(3, fullName.length());
			javaNames.add(name);
		}
		cSharpNames.addAll(cSharpValues.keySet());

		Iterator<String> iter = javaNames.iterator();
		while (iter.hasNext()) {
			String name = iter.next();
			if (cSharpNames.remove(name)) {
				iter.remove();
			}
		}

		if (javaNames.isEmpty() && cSharpNames.isEmpty()) {
			return;
		}

		StringBuilder sb = new StringBuilder();
		if (!cSharpNames.isEmpty()) {
			sb.append("Java is missing tag name(s) for :");
			iter = cSharpNames.iterator();
			while (iter.hasNext()) {
				String name = iter.next();
				sb.append(" " + name);
			}
		}
		if (!javaNames.isEmpty()) {
			if (sb.length() > 0) {
				sb.append(System.getProperty("line.separator"));
			}
			sb.append("C# is missing tag name(s) for :");
			iter = javaNames.iterator();
			while (iter.hasNext()) {
				String name = iter.next();
				sb.append(" " + name);
			}
		}

		fail(sb.toString());
	}
}
