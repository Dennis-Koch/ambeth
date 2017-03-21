package com.koch.ambeth.ioc.annotation;

/*-
 * #%L
 * jambeth-ioc
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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD})
public @interface Autowired {
	/**
	 * The bean name to lookup for to resolve the injection. This parameter is optional. If specified
	 * the type of the annotated field or setter is not considered for the injection.
	 *
	 * @return The bean name to lookup
	 */
	String value() default "";

	/**
	 * The bean name of the context where the to-be-injected bean has to be looked up for. This
	 * parameter is optional. It can be used together with or without {@link #value()} to lookup a
	 * named bean or autowired bean from a "foreign" context
	 *
	 * @return The bean name of the foreign context to lookup
	 */
	String fromContext() default "";

	/**
	 * Defines whether the injection operation should continue if the resolution of the bean name or
	 * autowired type fails for whatever reason. This parameter is optional
	 *
	 * @return true, if the injection operation should continue even on resolution exceptions
	 */
	boolean optional() default false;
}
