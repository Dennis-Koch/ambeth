package com.koch.ambeth.persistence;

/*-
 * #%L
 * jambeth-persistence
 * %%
 * Copyright (C) 2017 Koch Softwaredevelopment
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 * #L%
 */

import com.koch.ambeth.merge.transfer.ObjRef;
import com.koch.ambeth.persistence.api.ILinkCursor;
import com.koch.ambeth.persistence.api.ILinkCursorItem;
import com.koch.ambeth.util.IDisposable;

public class EmptyLinkCursor implements IDisposable, ILinkCursor {
	public static final ILinkCursor instance = new EmptyLinkCursor();

	@Override
	public void dispose() {
		// Intended blank
	}

	@Override
	public boolean moveNext() {
		return false;
	}

	@Override
	public byte getFromIdIndex() {
		return ObjRef.UNDEFINED_KEY_INDEX;
	}

	@Override
	public byte getToIdIndex() {
		return ObjRef.UNDEFINED_KEY_INDEX;
	}

	@Override
	public ILinkCursorItem getCurrent() {
		return null;
	}

}
