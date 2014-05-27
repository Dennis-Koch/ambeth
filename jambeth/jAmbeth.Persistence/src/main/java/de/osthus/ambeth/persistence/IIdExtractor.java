package de.osthus.ambeth.persistence;

import java.util.List;

public interface IIdExtractor
{

	List<Object> extractIds(Object idProviderHandle);

}
