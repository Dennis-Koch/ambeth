package com.koch.ambeth.util.annotation;

import java.lang.annotation.ElementType;

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

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks the annotated element with the hint for the entity cache to intern the corresponding values
 * when the entities are initalized or refreshed. It should be used for values of entities with low
 * variance amongst the data set. E.g. 5 million project entities within a data repository might
 * have a property containing the name (String) of the user owning each project. But there may be
 * only 100 different users owning at least one project. In such cases it is recommended to annotate
 * the corresponding entitiy field or entity property method so that the comparatively low amount of
 * different user strings are shared amongst all 5 million project entities. As an effect this can
 * greatly reduce the overall memory footprint and GC pressure when dealing with large amounts of
 * data.<br>
 * <br>
 * In general it can be said that if you are quite sure that a specific value of a to-be-annotated
 * entity property is used at least on 3 distinct entity instances - those entity instances do not
 * necessarily be of the same entity type or from the same property name - then it can have a
 * beneficial impact to intern this property. So with the example above with the 5 million projects
 * it makes sense to annotate the corresponding username property for up to around 1.5 million
 * different usernames.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.FIELD})
public @interface Interning {
	boolean value() default true;
}
