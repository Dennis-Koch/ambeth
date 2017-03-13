package com.koch.ambeth.persistence;

import java.sql.Blob;
import java.sql.Clob;

import com.koch.ambeth.stream.binary.IBinaryInputSource;
import com.koch.ambeth.stream.chars.ICharacterInputSource;

public interface IExtendedConnectionDialect
{
	IBinaryInputSource createBinaryInputSource(Blob blob);

	ICharacterInputSource createCharacterInputSource(Clob clob);
}
