package com.koch.ambeth.datachange.model;

import java.util.Objects;

import com.koch.ambeth.util.IImmutableType;
import com.koch.ambeth.util.IPrintable;
import com.koch.ambeth.util.StringBuilderUtil;

public class PostDataChange implements IPostDataChange, IImmutableType, IPrintable {
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
		return StringBuilderUtil.printPrintable(this);
	}

	@Override
	public void toString(StringBuilder sb) {
		StringBuilderUtil.appendPrintable(sb, getDataChange());
	}
}
