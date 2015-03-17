package de.osthus.ambeth.query;

import de.osthus.ambeth.query.sql.ITableAliasProvider;

public interface ISubQueryIntern
{
	void reAlias(ITableAliasProvider tableAliasProvider);
}
