package de.osthus.ambeth.copy;

public abstract class AbstractCustomConstructorOCE implements IObjectCopierExtension
{
	@Override
	public final Object deepClone(Object original, IObjectCopierState objectCopierState)
	{
		Object clone = createCloneInstance(original, objectCopierState);
		objectCopierState.addClone(original, clone);
		objectCopierState.deepCloneProperties(original, clone);
		return clone;
	}

	protected abstract Object createCloneInstance(Object original, IObjectCopierState objectCopierState);
}
