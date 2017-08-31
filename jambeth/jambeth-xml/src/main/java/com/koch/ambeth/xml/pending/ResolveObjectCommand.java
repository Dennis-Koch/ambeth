package com.koch.ambeth.xml.pending;

import com.koch.ambeth.util.ParamChecker;
import com.koch.ambeth.xml.IReader;

public class ResolveObjectCommand implements IObjectCommand {
	protected IObjectFuture objectFuture;

	public ResolveObjectCommand(IObjectFuture objectFuture) {
		ParamChecker.assertParamNotNull(objectFuture, "ObjectFuture");
		this.objectFuture = objectFuture;
	}

	@Override
	public IObjectFuture getObjectFuture() {
		return objectFuture;
	}

	@Override
	public void execute(IReader reader) {
		// NoOp
	}
}
