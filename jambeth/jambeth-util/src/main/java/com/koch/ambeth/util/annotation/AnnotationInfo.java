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

public class AnnotationInfo<A extends Annotation> implements IAnnotationInfo<A> {
	protected final A annotation;

	protected final AnnotatedElement annotatedElement;

	public AnnotationInfo(A annotation, AnnotatedElement annotatedElement) {
		this.annotation = annotation;
		this.annotatedElement = annotatedElement;
	}

	@Override
	public A getAnnotation() {
		return annotation;
	}

	@Override
	public AnnotatedElement getAnnotatedElement() {
		return annotatedElement;
	}

	@Override
	public String toString() {
		return getAnnotatedElement() + ": " + getAnnotation();
	}
}
