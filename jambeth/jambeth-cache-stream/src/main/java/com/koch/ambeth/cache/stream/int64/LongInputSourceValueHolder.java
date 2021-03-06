package com.koch.ambeth.cache.stream.int64;

/*-
 * #%L
 * jambeth-cache-stream
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

import com.koch.ambeth.cache.stream.AbstractInputSourceValueHolder;
import com.koch.ambeth.stream.IInputStream;
import com.koch.ambeth.stream.int64.BinaryToLongInputStream;
import com.koch.ambeth.stream.int64.ILongInputSource;
import com.koch.ambeth.stream.int64.ILongInputStream;

public class LongInputSourceValueHolder extends AbstractInputSourceValueHolder
		implements ILongInputSource {
	@Override
	public IInputStream deriveInputStream() {
		return deriveLongInputStream();
	}

	@Override
	public ILongInputStream deriveLongInputStream() {
		return new BinaryToLongInputStream(createBinaryInputStream());
	}
}
