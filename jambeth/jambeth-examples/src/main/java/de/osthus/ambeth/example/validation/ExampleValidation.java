package de.osthus.ambeth.example.validation;

import de.osthus.ambeth.changecontroller.AbstractValidation;
import de.osthus.ambeth.changecontroller.CacheView;
import de.osthus.ambeth.changecontroller.ValidationException;
import de.osthus.ambeth.example.bytecode.ExampleEntity;

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
	protected void validateInvariant(ExampleEntity newEntity, ExampleEntity oldEntity, CacheView views)
	{
		String name = newEntity.getName();
		if (name != null && name.length() > NAME_MAX_LENGTH)
		{
			throw new ValidationException("Name too long (max " + NAME_MAX_LENGTH + " characters)", newEntity);
		}
	}
}
