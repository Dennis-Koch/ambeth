package com.koch.ambeth.stream;

import com.koch.ambeth.service.cache.model.ILoadContainer;
import com.koch.ambeth.util.IConversionHelper;

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

import com.koch.ambeth.util.IImmutableType;

/**
 * Just an enum to ensure singleton pattern easily. It is marked as immutable type which would be
 * necessary if the class would not be an enum any time in the future. The only reason why it exists
 * is to mark a primitive value of a {@link ILoadContainer} for {@link IInputSource} functionality.
 * The <code>IInputSource</code> itself will be created by the {@link IConversionHelper} for each
 * applied streamable property of an entity instance
 */
public enum InputSourceTemplate implements IInputSourceTemplate, IImmutableType {
	INSTANCE;
}
