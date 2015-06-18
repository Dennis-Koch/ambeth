package de.osthus.ambeth.mapping;

import java.util.Iterator;

import de.osthus.ambeth.accessor.AbstractAccessor;
import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.merge.model.IEntityMetaData;
import de.osthus.ambeth.metadata.Member;

public class PropertyExpansion extends AbstractAccessor
{
	protected ArrayList<Member> memberPath;

	private ArrayList<IEntityMetaData> metaDataPath;

	public PropertyExpansion(ArrayList<Member> memberPath, ArrayList<IEntityMetaData> metaDataPath)
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
		if (obj == null || memberPath == null || memberPath.size() == 0)
		{
			throw new NullPointerException("target object was null or the memberPath was invaldi");
		}
		Object targetObj = obj;
		Object peekValue;
		Iterator<Member> iterator = memberPath.iterator();
		Member targetMember = iterator.next();

		// travel down the path to the last element of the path
		while (iterator.hasNext())
		{
			peekValue = targetMember.getValue(targetObj);
			if (peekValue == null)
			{
				// get meta data for next target to create
				IEntityMetaData entityMetaData = metaDataPath.get(memberPath.indexOf(targetMember));
				// last element, or now meta data available
				if (entityMetaData == null)
				{
					throw new IllegalStateException(
							"Must never happen, because there is a next member, and that means that the current targetMember must have meta data.");
				}
				peekValue = entityMetaData.newInstance();
				targetMember.setValue(targetObj, peekValue);

			}
			targetObj = peekValue;
			// save the last member
			targetMember = iterator.next();
		}

		// if we are here, then obj is the last element
		targetMember.setValue(targetObj, value);
	}
}
