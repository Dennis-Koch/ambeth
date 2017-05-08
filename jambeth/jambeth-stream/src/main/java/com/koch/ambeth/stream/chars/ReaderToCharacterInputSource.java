package com.koch.ambeth.stream.chars;

/*-
 * #%L
 * jambeth-stream
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

import java.io.Reader;

import com.koch.ambeth.stream.IInputStream;

public class ReaderToCharacterInputSource implements ICharacterInputSource {
	protected final Reader reader;

	public ReaderToCharacterInputSource(Reader reader) {
		this.reader = reader;
	}

	@Override
	public IInputStream deriveInputStream() {
		return new ReaderToCharacterInputStream(reader);
	}

	@Override
	public ICharacterInputStream deriveCharacterInputStream() {
		return new ReaderToCharacterInputStream(reader);
	}
}
