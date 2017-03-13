package com.koch.ambeth.merge.server.change;

public class RowCommand implements IRowCommand {
	protected IChangeCommand toBeExecuted;

	@Override
	public void addCommand(IChangeCommand command) {
		if (toBeExecuted != null) {
			toBeExecuted = toBeExecuted.addCommand(command);
		}
		else {
			toBeExecuted = command;
		}
	}

	@Override
	public IChangeCommand getCommand() {
		return toBeExecuted;
	}
}
