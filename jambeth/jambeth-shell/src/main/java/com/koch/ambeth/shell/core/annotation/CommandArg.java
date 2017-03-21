package com.koch.ambeth.shell.core.annotation;

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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 
 * @author daniel.mueller
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface CommandArg
{
	/**
	 * the name of the argument, will be used to match the argument name in cli.
	 * 
	 * @return
	 */
	String name() default "";

	/**
	 * used for usage generation when the name of the arg is empty
	 * 
	 * @return
	 */
	String alt() default "";

	/**
	 * not used yet
	 * 
	 * @return
	 */
	String shortName() default "";

	/**
	 * used to generate help text
	 * 
	 * @return
	 */
	String description() default "";

	/**
	 * not used yet
	 * 
	 * @return
	 */
	String descriptionFile() default "";

	/**
	 * is argument optional or mandatory
	 * 
	 * @return
	 */
	boolean optional() default false;

	/**
	 * the default value
	 * 
	 * @return
	 */
	String defaultValue() default "";
}
