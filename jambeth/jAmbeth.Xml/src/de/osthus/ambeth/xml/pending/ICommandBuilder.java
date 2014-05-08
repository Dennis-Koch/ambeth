package de.osthus.ambeth.xml.pending;

public interface ICommandBuilder
{
	IObjectCommand build(ICommandTypeRegistry commandTypeRegistry, IObjectFuture objectFuture, Object parent, Object... optionals);
}
