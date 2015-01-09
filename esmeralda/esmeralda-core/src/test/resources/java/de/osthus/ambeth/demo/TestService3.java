package de.osthus.ambeth.demo;

public class TestService3<V extends AbstractObject>
{
	// @SuppressWarnings("unchecked")
	// public <T> T genericMethod(T arg)
	// {
	// try
	// {
	// return (T) arg.getClass().newInstance();
	// }
	// catch (InstantiationException | IllegalAccessException e)
	// {
	// throw RuntimeExceptionUtil.mask(e);
	// }
	// }

	public <T extends V> V simpleGenericArgumentOfClassBound(T arg1, V arg2, AbstractObject arg3)
	{
		// intended blank
		return arg1;
	}
}
