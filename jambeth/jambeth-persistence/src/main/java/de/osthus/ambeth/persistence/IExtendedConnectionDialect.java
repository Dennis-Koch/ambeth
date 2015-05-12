package de.osthus.ambeth.persistence;

import java.sql.Blob;
import java.sql.Clob;

import de.osthus.ambeth.stream.binary.IBinaryInputSource;
import de.osthus.ambeth.stream.chars.ICharacterInputSource;

public interface IExtendedConnectionDialect
{
	IBinaryInputSource createBinaryInputSource(Blob blob);

	ICharacterInputSource createCharacterInputSource(Clob clob);
}
