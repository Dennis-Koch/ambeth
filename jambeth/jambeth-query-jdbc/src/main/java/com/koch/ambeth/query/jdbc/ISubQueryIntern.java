package com.koch.ambeth.query.jdbc;

import com.koch.ambeth.query.jdbc.sql.ITableAliasProvider;

public interface ISubQueryIntern
{
	void reAlias(ITableAliasProvider tableAliasProvider);
}
