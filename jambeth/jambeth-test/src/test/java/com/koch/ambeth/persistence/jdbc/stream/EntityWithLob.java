package com.koch.ambeth.persistence.jdbc.stream;

import com.koch.ambeth.stream.binary.IBinaryInputSource;
import com.koch.ambeth.stream.chars.ICharacterInputSource;

public interface EntityWithLob
{
	int getId();

	int getVersion();

	IBinaryInputSource getBlob();

	void setBlob(IBinaryInputSource blob);

	ICharacterInputSource getClob();

	void setClob(ICharacterInputSource cblob);
}
