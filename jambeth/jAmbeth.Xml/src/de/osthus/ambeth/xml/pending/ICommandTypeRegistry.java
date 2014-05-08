package de.osthus.ambeth.xml.pending;

public interface ICommandTypeRegistry
{
	<T extends IObjectCommand> Class<? extends T> getOverridingCommandType(Class<? extends T> commandType);
}
