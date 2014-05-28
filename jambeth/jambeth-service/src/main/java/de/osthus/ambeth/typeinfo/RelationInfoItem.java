package de.osthus.ambeth.typeinfo;

import de.osthus.ambeth.annotation.Cascade;
import de.osthus.ambeth.annotation.CascadeLoadMode;
import de.osthus.ambeth.config.IProperties;
import de.osthus.ambeth.config.ServiceConfigurationConstants;

public abstract class RelationInfoItem extends TypeInfoItem implements IRelationInfoItem
{
	protected CascadeLoadMode cascadeLoadMode = CascadeLoadMode.DEFAULT;

	protected boolean manyTo;

	protected boolean toMany;

	public void configure(IProperties properties)
	{
		toMany = !getElementType().equals(getRealType());

		Cascade cascadeAnnotation = getAnnotation(Cascade.class);
		if (cascadeAnnotation != null)
		{
			cascadeLoadMode = cascadeAnnotation.load();
		}
		if (cascadeLoadMode == null || CascadeLoadMode.DEFAULT.equals(cascadeLoadMode))
		{
			cascadeLoadMode = CascadeLoadMode.valueOf(properties.getString(toMany ? ServiceConfigurationConstants.ToManyDefaultCascadeLoadMode
					: ServiceConfigurationConstants.ToOneDefaultCascadeLoadMode, CascadeLoadMode.DEFAULT.toString()));
		}
		if (cascadeLoadMode == null || CascadeLoadMode.DEFAULT.equals(cascadeLoadMode))
		{
			cascadeLoadMode = toMany ? CascadeLoadMode.LAZY : CascadeLoadMode.EAGER_VERSION;
		}
	}

	@Override
	public CascadeLoadMode getCascadeLoadMode()
	{
		return cascadeLoadMode;
	}

	public void setManyTo(boolean manyTo)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isManyTo()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isToMany()
	{
		return toMany;
	}
}