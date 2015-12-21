package de.osthus.ambeth.changecontroller;

/**
 * Classes that uses this as base class can be registered as validations that are called for changes on persisted data by
 * 
 * <code>
 *    IBeanConfiguration beanConf = contextFactory.registerAnonymousBean(--this class--.class);
	  contextFactory.link(beanConf).to(IChangeControllerExtendable.class).with(T.class);
 * </code>
 * 
 * Each validation rule should provide a unique id. Validation rules are evaluated according to the alphabetical order of their ids.
 * 
 * Validation rules are called after business rules ({@link AbstractBusinessRule}). This is simply done by sorting the results of the {@link #toString()}
 * methods alphabetically and validation rules return "Validation" whereas business rule start with "Business rule".
 * 
 * The class provides three distinct methods to override to start a validation in certain circumstances.
 * 
 * <ul>
 * <li> {@link #onChange(Object, Object, de.osthus.ambeth.changecontroller.CacheView)} whenever an object is created, updated or deleted</li>
 * <li> {@link #onCreate(Object, de.osthus.ambeth.changecontroller.CacheView)} whenever an object is created</li>
 * <li> {@link #onDelete(Object, de.osthus.ambeth.changecontroller.CacheView)} whenever an object is deleted</li>
 * <li> {@link #onUpdate(Object, Object, de.osthus.ambeth.changecontroller.CacheView)} whenever an object is updated</li>
 * </ul>
 * 
 * If a validation fails, a {@link ValidationException} should be thrown. This is a recommendation which is not enforced.
 * 
 * @param <T>
 *            The data type of the beans that should be watched.
 */
public abstract class AbstractValidation<T> extends AbstractRule<T>
{
	private String validationId;

	public abstract String getValidationId();

	@Override
	public String toString()
	{
		return getSortingKey();
	}

	@Override
	public String getSortingKey()
	{
		if (validationId == null)
		{
			validationId = "Validation " + getValidationId();
		}
		return validationId;
	}
}
