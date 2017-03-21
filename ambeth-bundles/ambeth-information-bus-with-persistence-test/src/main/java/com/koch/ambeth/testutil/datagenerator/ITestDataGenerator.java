package com.koch.ambeth.testutil.datagenerator;

/*-
 * #%L
 * jambeth-information-bus-with-persistence-test
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

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

public interface ITestDataGenerator
{

	/**
	 * Generates an entity and fills all simple fields with a default value as defined by the extended parts
	 * 
	 * @param type
	 *            the type a test instance should be created of
	 * @param toIgnore
	 *            Fields which should not be set. Technical attributes are always ignored.
	 * @return an instance with all fields set
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 * @throws IllegalArgumentException
	 */
	<T> T generateTestClass(Class<T> type, String... toIgnore) throws InstantiationException, IllegalAccessException, IllegalArgumentException,
			InvocationTargetException;

	/**
	 * Generates an entity and fills all simple fields with a default value as defined by the extended parts
	 * 
	 * @param type
	 *            the type a test instance should be created of
	 * @param patternToAdd
	 *            the pattern which describes, what should be added to the methodname if it is the value to be set. Especially important for having BUIDs
	 *            singleton
	 * @param toIgnore
	 *            Fields which should not be set. Technical attributes are always ignored.
	 * @return an instance with all fields set
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 * @throws IllegalArgumentException
	 */
	<T> T generateTestClass(Class<T> type, Map<Object, Object> arguments, String... toIgnore) throws InstantiationException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException;

	/**
	 * Tests an entity for default values as defined by the extended parts
	 * 
	 * @param type
	 *            the instance which properties are tested
	 * @param toIgnore
	 *            property names which should not be tested (e.g. because of changed fields). Technical attributes are always ignored.
	 * @return an instance with all fields set
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 * @throws IllegalArgumentException
	 */
	void checkTestInstance(Object instance, String... toIgnore) throws InstantiationException, IllegalAccessException, IllegalArgumentException,
			InvocationTargetException;

	/**
	 * Tests an entity for default values as defined by the extended parts
	 * 
	 * @param type
	 *            the instance which properties are tested
	 * @param arguments
	 *            Map describing which arguments should be use
	 * @param toIgnore
	 *            property names which should not be tested (e.g. because of changed fields). Technical attributes are always ignored.
	 * @return an instance with all fields set
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 * @throws IllegalArgumentException
	 */
	void checkTestInstance(Object instance, Map<Object, Object> arguments, String... toIgnore) throws InstantiationException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException;

	/**
	 * Compare each property of the given two objects
	 * 
	 * @param expected
	 *            object with expected property values
	 * @param actual
	 *            object to check properties against expected
	 * @param recursive
	 *            descend recursively to referenced complexed Objects
	 * @param toIgnore
	 *            List properties to ignore
	 * @throws InvocationTargetException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 */
	void compareTestInstance(Object expected, Object actual, boolean recursive, String... toIgnore) throws IllegalArgumentException, IllegalAccessException,
			InvocationTargetException;
}
