package com.koch.ambeth.persistence;

import java.util.List;

public interface IIdExtractor
{

	List<Object> extractIds(Object idProviderHandle);

}
