package com.koch.ambeth.merge;

import com.koch.ambeth.merge.model.ICUDResult;

public interface ICUDResultComparer
{
	boolean equalsCUDResult(ICUDResult left, ICUDResult right);

	ICUDResult diffCUDResult(ICUDResult left, ICUDResult right);
}