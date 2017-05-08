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

import java.util.Iterator;

public class BasicEnumerator<E> implements Iterator<E> {

	public E getCurrent() {
		throw new UnsupportedOperationException("Not implemented");
	}

	public boolean moveNext() {
		throw new UnsupportedOperationException("Not implemented");
	}

	public void reset() {
		throw new UnsupportedOperationException("Not implemented");
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException("Not implemented");
	}

	@Override
	public boolean hasNext() {
		throw new UnsupportedOperationException("Not implemented");
	}

	@Override
	public E next() {
		throw new UnsupportedOperationException("Not implemented");
	}

}
