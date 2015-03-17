package de.osthus.ambeth.merge;

import de.osthus.ambeth.merge.model.ICUDResult;

public interface ICUDResultComparer
{
	boolean equalsCUDResult(ICUDResult left, ICUDResult right);

	ICUDResult diffCUDResult(ICUDResult left, ICUDResult right);
}