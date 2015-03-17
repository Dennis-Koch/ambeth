package de.osthus.ambeth.persistence.jdbc.lob;

import de.osthus.ambeth.metadata.Member;
import de.osthus.ambeth.stream.IInputStream;
import de.osthus.ambeth.stream.binary.IBinaryInputStream;
import de.osthus.ambeth.stream.chars.ICharacterInputStream;

public interface ILobInputSourceController
{
	IInputStream deriveInputStream(Object parentEntity, Member member);

	IBinaryInputStream deriveBinaryInputStream(Object parentEntity, Member member);

	ICharacterInputStream deriveCharacterInputStream(Object parentEntity, Member member);
}