package com.koch.ambeth.persistence.jdbc.lob;

import com.koch.ambeth.service.metadata.Member;
import com.koch.ambeth.stream.IInputStream;
import com.koch.ambeth.stream.binary.IBinaryInputStream;
import com.koch.ambeth.stream.chars.ICharacterInputStream;

public interface ILobInputSourceController
{
	IInputStream deriveInputStream(Object parentEntity, Member member);

	IBinaryInputStream deriveBinaryInputStream(Object parentEntity, Member member);

	ICharacterInputStream deriveCharacterInputStream(Object parentEntity, Member member);
}