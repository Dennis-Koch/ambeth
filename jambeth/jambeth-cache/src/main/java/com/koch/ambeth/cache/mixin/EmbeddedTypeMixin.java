package com.koch.ambeth.cache.mixin;

import com.koch.ambeth.util.model.IEmbeddedType;

public class EmbeddedTypeMixin {
	public final Object getRoot(IEmbeddedType embeddedObject) {
		Object parent = embeddedObject.getParent();
		while (parent instanceof IEmbeddedType) {
			parent = ((IEmbeddedType) parent).getParent();
		}
		return parent;
	}
}
