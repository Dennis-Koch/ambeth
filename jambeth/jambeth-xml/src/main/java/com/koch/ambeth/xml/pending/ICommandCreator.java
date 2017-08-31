package com.koch.ambeth.xml.pending;

public interface ICommandCreator {
	IObjectCommand createCommand(ICommandTypeRegistry commandTypeRegistry, IObjectFuture objectFuture,
			Object parent, Object[] optionals);
}
