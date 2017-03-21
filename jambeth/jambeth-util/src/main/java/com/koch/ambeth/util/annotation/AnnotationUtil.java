package com.koch.ambeth.util.annotation;

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

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;

public final class AnnotationUtil {
	private AnnotationUtil() {
		// Intended blank
	}

	public static final <A extends Annotation> A getFirstAnnotation(
			AnnotationCache<A> annotationCache, AnnotatedElement... sourceElements) {
		for (AnnotatedElement annotatedElement : sourceElements) {
			A annotation = annotationCache.getAnnotation(annotatedElement);
			if (annotation != null) {
				return annotation;
			}
		}
		return null;
	}

	/**
	 * Gets the first annotation from a type. Searches recursive through super classes and interfaces
	 *
	 * @param annotationType the annotation to search for
	 * @param annotatedType the type to investigate
	 * @param inherit if inherited annotations should be searched
	 * @return Annotation or null if Annotation was not found
	 */
	public static final <A extends Annotation> A getAnnotation(Class<A> annotationType,
			Class<?> annotatedType, boolean inherit) {
		if (annotatedType == null) {
			return null;
		}
		A annotation = annotatedType.getAnnotation(annotationType);
		if (annotation == null && inherit) {
			for (Class<?> interfaceType : annotatedType.getInterfaces()) {
				annotation = getAnnotation(annotationType, interfaceType, inherit);
				if (annotation != null) {
					return annotation;
				}
			}
			return getAnnotation(annotationType, annotatedType.getSuperclass(), inherit);
		}
		return annotation;
	}

	/**
	 * Gets the first annotation from a method. Searches recursive through super classes and
	 * interfaces
	 *
	 * @param annotationType the annotation to search for
	 * @param annotatedType the type to investigate
	 * @param methodName the method to investigate
	 * @param parameterTypes the parameter types of the investigated method
	 * @return Annotation or null if Annotation was not found
	 */
	public static final <A extends Annotation> A getAnnotation(Class<A> annotationType,
			Class<?> annotatedType, String methodName, Class<?>... parameterTypes) {
		if (annotatedType == null) {
			return null;
		}
		Method method;
		try {
			method = annotatedType.getMethod(methodName, parameterTypes);
		}
		catch (Throwable e) {
			// method not defined in this type
			return null;
		}

		A annotation = method.getAnnotation(annotationType);
		if (annotation == null) {
			for (Class<?> interfaceType : annotatedType.getInterfaces()) {
				try {
					annotation = getAnnotation(annotationType, interfaceType, methodName, parameterTypes);
					if (annotation != null) {
						return annotation;
					}
				}
				catch (Throwable e) {
					// method not defined in this interface
					continue;
				}
			}
			return getAnnotation(annotationType, annotatedType.getSuperclass(), methodName,
					parameterTypes);
		}
		return annotation;
	}
}
