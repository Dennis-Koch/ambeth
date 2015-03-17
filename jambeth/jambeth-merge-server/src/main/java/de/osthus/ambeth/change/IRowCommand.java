package de.osthus.ambeth.change;


public interface IRowCommand
{
	void addCommand(IChangeCommand command);

	IChangeCommand getCommand();
}