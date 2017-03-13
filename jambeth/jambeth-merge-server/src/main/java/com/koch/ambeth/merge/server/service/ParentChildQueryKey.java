package com.koch.ambeth.merge.server.service;

import java.util.Arrays;

import com.koch.ambeth.util.IPrintable;

public class ParentChildQueryKey implements IPrintable
{
	protected final Class<?> selectedEntityType;

	protected final String selectingMemberName;

	protected final String[] childMemberNames;

	public ParentChildQueryKey(Class<?> selectedEntityType, String selectingMemberName, String[] childMemberNames)
	{
		this.selectedEntityType = selectedEntityType;
		this.selectingMemberName = selectingMemberName;
		this.childMemberNames = childMemberNames;
	}

	@Override
	public int hashCode()
	{
		return selectedEntityType.hashCode() ^ selectingMemberName.hashCode() ^ Arrays.hashCode(childMemberNames);
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj == this)
		{
			return true;
		}
		if (!(obj instanceof ParentChildQueryKey))
		{
			return false;
		}
		ParentChildQueryKey other = (ParentChildQueryKey) obj;
		return selectedEntityType.equals(other.selectedEntityType) && selectingMemberName.equals(other.selectingMemberName)
				&& Arrays.equals(childMemberNames, other.childMemberNames);
	}

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		toString(sb);
		return sb.toString();
	}

	@Override
	public void toString(StringBuilder sb)
	{
		sb.append(getClass().getSimpleName()).append(": SelectedEntityType=").append(selectedEntityType.getName()).append("\r\tSelectingMemberName=")
				.append(selectingMemberName);
		sb.append("\r\tChildMemberNames=").append(Arrays.toString(childMemberNames));
	}

}
