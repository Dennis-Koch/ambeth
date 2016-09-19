package de.osthus.ambeth.xml;

import de.osthus.ambeth.xml.pending.ICommandTypeExtendable;
import de.osthus.ambeth.xml.pending.ICommandTypeRegistry;
import de.osthus.ambeth.xml.pending.IObjectCommand;

public interface IReader
{
	boolean isEmptyElement();

	String getAttributeValue(String attributeName);

	Object readObject();

	Object readObject(Class<?> returnType);

	String getElementName();

	String getElementValue();

	boolean nextTag();

	boolean nextToken();

	boolean isStartTag();

	void moveOverElementEnd();

	Object getObjectById(int id);

	Object getObjectById(int id, boolean checkExistence);

	void putObjectWithId(Object obj, int id);

	void putMembersOfType(Class<?> type, SpecifiedMember[] members);

	SpecifiedMember[] getMembersOfType(Class<?> type);

	void addObjectCommand(IObjectCommand pendingSetter);

	// IReader contains the registry because the reader in fact is the deserialization state.
	ICommandTypeRegistry getCommandTypeRegistry();

	ICommandTypeExtendable getCommandTypeExtendable();
}
