package com.koch.ambeth.persistence.jdbc.lob;

import com.koch.ambeth.merge.copy.IObjectCopierExtension;
import com.koch.ambeth.merge.copy.IObjectCopierState;

public class BlobInputSourceObjectCopier implements IObjectCopierExtension {
	@Override
	public Object deepClone(Object original, IObjectCopierState objectCopierState) {
		return new BlobInputSource(((BlobInputSource) original).lobInputSourceController);
	}
}
