package com.koch.ambeth.example.validation;

import com.koch.ambeth.example.bytecode.ExampleEntity;
import com.koch.ambeth.merge.changecontroller.AbstractValidation;
import com.koch.ambeth.merge.changecontroller.ICacheView;
import com.koch.ambeth.merge.changecontroller.ValidationException;

public class ExampleValidation extends AbstractValidation<ExampleEntity>
{
	private static final int NAME_MAX_LENGTH = 40;

	@Override
	public String getValidationId()
	{
		return "1.0 - Example entity validation: name must be maximally 40 characters long";
	}

	/**
	 * Validate invariant is on every change called (creation and update) but not on deletion
	 */
	@Override
	protected void validateInvariant(ExampleEntity newEntity, ExampleEntity oldEntity, ICacheView views)
	{
		String name = newEntity.getName();
		if (name != null && name.length() > NAME_MAX_LENGTH)
		{
			throw new ValidationException("Name too long (max " + NAME_MAX_LENGTH + " characters)", newEntity);
		}
	}
}
