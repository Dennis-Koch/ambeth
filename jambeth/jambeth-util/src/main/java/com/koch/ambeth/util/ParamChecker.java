package com.koch.ambeth.util;

/*-
 * #%L
 * jambeth-util
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

public class ParamChecker {
	private static boolean unitTestMode = false;

	protected ParamChecker() {
	}

	private static void throwException(String prefix, String name) {
		if (name != null && name.length() > 0) {
			throw new IllegalArgumentException(
					prefix + Character.toUpperCase(name.charAt(0)) + name.substring(1));
		}
		throw new IllegalArgumentException(prefix + name);
	}

	public static void assertNull(Object value, String name) {
		if (value != null) {
			if (!unitTestMode) {
				throwException("Property must not be valid here: ", name);
			}
		}
	}

	/**
	 * &Uuml;berpr&uuml;fung ob der entsprechende Value w&auml;hrend der Bean-Initialisierung gesetzt
	 * wurde. F&uuml;r die Ausgabe der Fehlermeldung wird hier der &uuml;bergebene Parameter
	 * <code>name</code> ausgegeben.<br>
	 *
	 * @param value Das zu &uuml;berpr&uuml;fende Objekt.
	 * @param name Der in der Fehlermeldung auszugebende Name.
	 */
	public static void assertNotNull(Object value, String name) {
		if (value == null) {
			if (!unitTestMode) {
				throwException("Property must be valid: ", name);
			}
		}
	}

	public static void assertNotNull(double value, String name) {
		if (value == 0.0) {
			if (!unitTestMode) {
				throwException("Property must be valid: ", name);
			}
		}
	}

	public static void assertParamNotNull(Object value, String name) {
		if (value == null) {
			throwException("Property must be valid: ", name);
		}
	}

	public static void assertParamNotNullOrEmpty(String value, String name) {
		if (value == null || value.isEmpty()) {
			throwException("Property must be valid: ", name);
		}
	}

	public static void assertTrue(boolean expression, String name) {
		if (!expression) {
			if (!unitTestMode) {
				throwException("Property must be valid: ", name);
			}
		}
	}

	public static void assertFalse(boolean expression, String name) {
		if (expression) {
			if (!unitTestMode) {
				throwException("Property must be valid: ", name);
			}
		}
	}

	public static void assertParamOfType(Object value, String name, Class<?> type) {
		ParamChecker.assertNotNull(value, name);
		if (!type.isAssignableFrom(value.getClass())) {
			if (!unitTestMode) {
				throwException("Parameter must be an instance of " + type.toString() + " but is "
						+ value.getClass().toString() + ": ", name);
			}
		}
	}

	public static void setUnitTestMode(boolean unitTestMode) {
		ParamChecker.unitTestMode = unitTestMode;
	}
}
