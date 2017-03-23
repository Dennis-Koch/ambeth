package com.koch.ambeth.stream;

import com.koch.ambeth.stream.float64.IDoubleInputSource;

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


/**
 * Marker interface not intended for direct use. Sub-interfaces of this are used to provide
 * streaming capabilities of dedicated primitive (non-relational) properties of an entity. The
 * available strongly-typed sources are:<br>
 * <ul>
 * <li>{@link com.koch.ambeth.stream.binary.IBinaryInputSource}</li>
 * <li>{@link com.koch.ambeth.stream.bool.IBooleanInputSource}</li>
 * <li>{@link com.koch.ambeth.stream.chars.ICharacterInputSource}</li>
 * <li>{@link com.koch.ambeth.stream.date.IDateInputSource}</li>
 * <li>{@link com.koch.ambeth.stream.float32.IFloatInputSource}</li>
 * <li>{@link com.koch.ambeth.stream.float64.IDoubleInputSource}</li>
 * <li>{@link com.koch.ambeth.stream.int32.IIntInputSource}</li>
 * <li>{@link com.koch.ambeth.stream.int64.ILongInputSource}</li>
 * <li>{@link com.koch.ambeth.stream.strings.IStringInputSource}</li>
 * </ul>
 *
 * Those strongly-typed sources for primitives and strings are provided to comply more easily to
 * some regulatory constraints: Sometimes it is expected or desired that application code does not
 * make implicit type-conversions "anywhere" in the code but ensures that the used API already
 * provided the business-correct encoded value for a streamed data source.<br>
 * <br>
 *
 * Please note that the source itself is expected to be a rather light-weight object - so
 * intentionally NOT requiring or locking any resources outside of the plain-java heap (e.g. locks,
 * file handles, sessions, connections or large data structures whatsoever): The source is meant to
 * work as a factory for the real (potentially heavy-weight) inputstream. So e.g. a
 * {@link IDoubleInputSource#deriveDoubleInputStream()} would then do the hard-work - on explicit
 * source usage}.<br>
 * <br>
 * If a streaming source is used in a remote deployment scenario in most cases the backing
 * implementation to provide the streamed data is working with the chunk-pattern. Means: The default
 * stream implementations delegate their internal calls for data chunks to a named
 * {@link IChunkProvider}. The chunk provider could then be a stub for a call for dedicated chunks
 * to a remote service.<br>
 * <br>
 * If you implement a custom {@link ICacheRetriever} or {@link IPrimitiveRetriever} you can assign
 * just an instance of {@link IInputSourceTemplate} to prepare the corresponding property for the
 * full streaming capability. For convenience there is already an enum value
 * ({@link InputSourceTemplate#INSTANCE}) ready to use as the primitive property value. As soon as
 * the cache needs to hydrate the entity in any 1st level cache the corresponding dedicated
 * converter is called to instantiate the type-safe data source (e.g. an
 * {@link IDoubleInputSource}). The usage of the enum can look like this:<br>
 * <br>
 * <code>
 * Object[] getPrimitives(List&lt;IObjRelation&gt; objPropertyKeys) {<br>
 * IObjRelation objPropertyKey = objPropertyKeys.get(0);<br>
 * return new Object[] { InputSourceTemplate.INSTANCE };<br>
 * }</code>
 */
public interface IInputSource {
	IInputStream deriveInputStream();
}
