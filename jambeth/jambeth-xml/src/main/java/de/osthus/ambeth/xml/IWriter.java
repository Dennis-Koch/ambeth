package de.osthus.ambeth.xml;


public interface IWriter
{
	boolean isInAttributeState();

	void writeEscapedXml(CharSequence unescapedString);

	void writeAttribute(CharSequence attributeName, Object attributeValue);

	void writeAttribute(CharSequence attributeName, CharSequence attributeValue);

	void writeEndElement();

	void writeCloseElement(CharSequence elementName);

	void write(CharSequence s);

	void writeOpenElement(CharSequence elementName);

	void writeStartElement(CharSequence elementName);

	void writeStartElementEnd();

	void writeObject(Object obj);

	void writeEmptyElement(CharSequence elementName);

	void write(char s);

	int getIdOfObject(Object obj);

	int acquireIdForObject(Object obj);

	void putMembersOfType(Class<?> type, SpecifiedMember[] members);

	SpecifiedMember[] getMembersOfType(Class<?> type);

	void addSubstitutedEntity(Object entity);
}
