package de.osthus.ambeth.xml.pending;

public interface ICommandTypeExtendable
{
	void registerOverridingCommandType(Class<? extends IObjectCommand> overridingCommandType, Class<? extends IObjectCommand> commandType);

	void unregisterOverridingCommandType(Class<? extends IObjectCommand> overridingCommandType, Class<? extends IObjectCommand> commandType);
}
