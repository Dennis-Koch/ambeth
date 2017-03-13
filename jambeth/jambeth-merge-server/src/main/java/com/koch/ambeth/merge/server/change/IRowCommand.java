package com.koch.ambeth.merge.server.change;


public interface IRowCommand
{
	void addCommand(IChangeCommand command);

	IChangeCommand getCommand();
}