package de.osthus.ambeth.changecontroller;

/**
 * Classes that uses this as base class can be registered as business rules that are called for changes on persisted data by
 * 
 * <code>
 *    IBeanConfiguration beanConf = contextFactory.registerAnonymousBean(--this class--.class);
	  contextFactory.link(beanConf).to(IChangeControllerExtendable.class).with(T.class);
 * </code>
 * 
 * Each validation rule should provide a unique id. Business rules are evaluated according to the alphabetical order of their ids.
 * 
 * Business rules are called before validation rules ({@link AbstractValidation}). This is simply done by sorting the results of the {@link #toString()} methods
 * alphabetically and validation rules return "Validation" whereas business rule start with "Business rule".
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
 * @param <T>
 *            The data type of the beans that should be watched.
 */
public abstract class AbstractBusinessRule<T> extends AbstractRule<T>
{
	private String ruleId;

	public abstract String getBusinessRuleId();

	@Override
	public String toString()
	{
		return getSortingKey();
	}

	@Override
	public String getSortingKey()
	{
		if (ruleId == null)
		{
			ruleId = "Business rule " + getBusinessRuleId();
		}
		return ruleId;
	}
}
