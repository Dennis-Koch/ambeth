package de.osthus.ambeth.persistence.jdbc.stream;

import de.osthus.ambeth.stream.binary.IBinaryInputSource;
import de.osthus.ambeth.stream.chars.ICharacterInputSource;

public interface EntityWithLob
{
	int getId();

	int getVersion();

	IBinaryInputSource getBlob();

	void setBlob(IBinaryInputSource blob);

	ICharacterInputSource getClob();

	void setClob(ICharacterInputSource cblob);
}
