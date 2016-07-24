package de.osthus.ambeth.query.squery.service;

import de.osthus.ambeth.proxy.MergeContext;
import de.osthus.ambeth.proxy.Service;

@Service(value = IPersonService.class)
@MergeContext
public abstract class PersonService implements IPersonService
{
	public static final String CONCRETE_METHOD_ERROR = "this method will not be intercepted";

	@Override
	public String someConcreteMethod(String anyValue)
	{
		return CONCRETE_METHOD_ERROR;
	}
}
