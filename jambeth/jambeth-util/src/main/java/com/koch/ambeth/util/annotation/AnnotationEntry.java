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

public class AnnotationEntry<T extends Annotation> {
	protected final T annotation;

	protected final AnnotatedElement declaringAnnotatedElement;

	public AnnotationEntry(T annotation, AnnotatedElement declaringAnnotatedElement) {
		this.annotation = annotation;
		this.declaringAnnotatedElement = declaringAnnotatedElement;
	}

	public T getAnnotation() {
		return annotation;
	}

	public AnnotatedElement getDeclaringAnnotatedElement() {
		return declaringAnnotatedElement;
	}

	public Class<?> getDeclaringType() {
		return (Class<?>) declaringAnnotatedElement;
	}
}
