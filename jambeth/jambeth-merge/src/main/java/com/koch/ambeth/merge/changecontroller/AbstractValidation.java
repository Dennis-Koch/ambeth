package com.koch.ambeth.merge.changecontroller;

/*-
 * #%L
 * jambeth-merge
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

/**
 * Classes that uses this as base class can be registered as validations that are called for changes
 * on persisted data by
 *
 * <code>
 *    IBeanConfiguration beanConf = contextFactory.registerAnonymousBean(--this class--.class);
	  contextFactory.link(beanConf).to(IChangeControllerExtendable.class).with(T.class);
 * </code>
 *
 * Each validation rule should provide a unique id. Validation rules are evaluated according to the
 * alphabetical order of their ids.
 *
 * Validation rules are called after business rules ({@link AbstractBusinessRule}). This is simply
 * done by sorting the results of the {@link #toString()} methods alphabetically and validation
 * rules return "Validation" whereas business rule start with "Business rule".
 *
 * The class provides three distinct methods to override to start a validation in certain
 * circumstances.
 *
 * <ul>
 * <li>{@link #onChange(Object, Object, com.koch.ambeth.merge.changecontroller.CacheView)} whenever
 * an object is created, updated or deleted</li>
 * <li>{@link #onCreate(Object, com.koch.ambeth.merge.changecontroller.CacheView)} whenever an
 * object is created</li>
 * <li>{@link #onDelete(Object, com.koch.ambeth.merge.changecontroller.CacheView)} whenever an
 * object is deleted</li>
 * <li>{@link #onUpdate(Object, Object, com.koch.ambeth.merge.changecontroller.CacheView)} whenever
 * an object is updated</li>
 * </ul>
 *
 * If a validation fails, a {@link ValidationException} should be thrown. This is a recommendation
 * which is not enforced.
 *
 * @param <T> The data type of the beans that should be watched.
 */
public abstract class AbstractValidation<T> extends AbstractRule<T> {
	private String validationId;

	public abstract String getValidationId();

	@Override
	public String toString() {
		return getSortingKey();
	}

	@Override
	public String getSortingKey() {
		if (validationId == null) {
			validationId = "Validation " + getValidationId();
		}
		return validationId;
	}
}
