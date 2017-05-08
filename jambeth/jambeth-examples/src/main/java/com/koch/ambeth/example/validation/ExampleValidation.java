package com.koch.ambeth.example.validation;

/*-
 * #%L
 * jambeth-examples
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

import com.koch.ambeth.example.bytecode.ExampleEntity;
import com.koch.ambeth.merge.changecontroller.AbstractValidation;
import com.koch.ambeth.merge.changecontroller.ICacheView;
import com.koch.ambeth.merge.changecontroller.ValidationException;

public class ExampleValidation extends AbstractValidation<ExampleEntity> {
	private static final int NAME_MAX_LENGTH = 40;

	@Override
	public String getValidationId() {
		return "1.0 - Example entity validation: name must be maximally 40 characters long";
	}

	/**
	 * Validate invariant is on every change called (creation and update) but not on deletion
	 */
	@Override
	protected void validateInvariant(ExampleEntity newEntity, ExampleEntity oldEntity,
			ICacheView views) {
		String name = newEntity.getName();
		if (name != null && name.length() > NAME_MAX_LENGTH) {
			throw new ValidationException("Name too long (max " + NAME_MAX_LENGTH + " characters)",
					newEntity);
		}
	}
}
