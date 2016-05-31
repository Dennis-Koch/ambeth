package de.osthus.ambeth.mapping;

import java.util.Arrays;

import de.osthus.ambeth.accessor.AbstractAccessor;
import de.osthus.ambeth.merge.model.IEntityMetaData;
import de.osthus.ambeth.metadata.Member;
import de.osthus.ambeth.model.IDataObject;
import de.osthus.ambeth.util.EqualsUtil;

public class PropertyExpansion extends AbstractAccessor
{
	private final Member[] memberPath;

	private final IEntityMetaData[] metaDataPath;

	public PropertyExpansion(Member[] memberPath, IEntityMetaData[] metaDataPath)
	{
		this.memberPath = memberPath;
		this.metaDataPath = metaDataPath;
	}

	@Override
	public boolean canRead()
	{
		return true;
	}

	@Override
	public boolean canWrite()
	{
		return false;
	}

	@Override
	public Object getValue(Object obj, boolean allowNullEquivalentValue)
	{
		// TODO: What is a NullEqivalentValue?
		return getValue(obj);
	}

	@Override
	public Object getValue(Object obj)
	{
		if (obj == null)
		{
			return null;
		}

		for (Member member : memberPath)
		{
			obj = member.getValue(obj);
			if (obj == null)
			{
				return null;
			}
		}
		return obj;
	}

	@Override
	public void setValue(Object obj, Object value)
	{
		// if target object is null it is an error
		if (obj == null || memberPath == null || memberPath.length == 0)
		{
			throw new NullPointerException("target object was null or the memberPath was invaldi");
		}
		Object targetObj = obj;

		// travel down the path to the last element of the path
		for (int a = 0, size = memberPath.length - 1; a < size; a++)
		{
			Member member = memberPath[a];
			Object entity = member.getValue(targetObj);
			if (entity == null)
			{
				// get meta data for next target to create
				IEntityMetaData entityMetaData = metaDataPath[a];
				// last element, or now meta data available
				if (entityMetaData == null)
				{
					throw new IllegalStateException(
							"Must never happen, because there is a next member, and that means that the current targetMember must have meta data: '"
									+ Arrays.toString(memberPath) + "'");
				}
				entity = entityMetaData.newInstance();
				member.setValue(targetObj, entity);
			}
			targetObj = entity;
		}
		Member lastMember = memberPath[memberPath.length - 1];
		// if we are here, then obj is the last element
		if (!EqualsUtil.equals(lastMember.getValue(targetObj), value))
		{
			lastMember.setValue(targetObj, value);
			IDataObject dObj = (IDataObject) targetObj;
			// FIXME: this hack tells the merge process that "we did something here"
			dObj.setToBeUpdated(true);
		}
	}
}
