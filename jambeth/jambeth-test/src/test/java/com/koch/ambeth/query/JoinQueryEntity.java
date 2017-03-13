package com.koch.ambeth.query;

import com.koch.ambeth.model.AbstractEntity;

public class JoinQueryEntity extends AbstractEntity
{
	protected JoinQueryEntity parent;

	protected int joinValue1;

	protected int joinValue2;

	protected JoinQueryEntity()
	{
		// Intended blank
	}

	public JoinQueryEntity getParent()
	{
		return parent;
	}

	public void setParent(JoinQueryEntity parent)
	{
		this.parent = parent;
	}

	public int getJoinValue1()
	{
		return joinValue1;
	}

	public void setJoinValue1(int joinValue1)
	{
		this.joinValue1 = joinValue1;
	}

	public int getJoinValue2()
	{
		return joinValue2;
	}

	public void setJoinValue2(int joinValue2)
	{
		this.joinValue2 = joinValue2;
	}
}
