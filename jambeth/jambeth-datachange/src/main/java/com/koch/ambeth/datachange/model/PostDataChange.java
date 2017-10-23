package com.koch.ambeth.datachange.model;

import java.util.Objects;

import com.koch.ambeth.util.IImmutableType;

public class PostDataChange implements IPostDataChange, IImmutableType {
	private final IDataChange dataChange;

	public PostDataChange(IDataChange dataChange) {
		Objects.requireNonNull(dataChange, "dataChange");
		this.dataChange = dataChange;
	}

	@Override
	public IDataChange getDataChange() {
		return dataChange;
	}

	@Override
	public String toString() {
		return getDataChange().toString();
	}
}
