package com.koch.ambeth.util.stream;

/*-
 * #%L
 * jambeth-util
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

import java.io.InputStream;

public class InputStreamWithLength implements IInputStreamWithLength
{
	private final InputStream inputStream;

	private final int overallLength;

	public InputStreamWithLength(InputStream inputStream, int overallLength)
	{
		super();
		this.inputStream = inputStream;
		this.overallLength = overallLength;
	}

	@Override
	public InputStream getInputStream()
	{
		return inputStream;
	}

	@Override
	public int getOverallLength()
	{
		return overallLength;
	}

	@Override
	public String toString()
	{
		return super.toString() + ", Length: " + overallLength;
	}
}
