package com.koch.ambeth.stream.ioc;

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

import com.koch.ambeth.ioc.IInitializingModule;
import com.koch.ambeth.ioc.annotation.FrameworkModule;
import com.koch.ambeth.ioc.config.IBeanConfiguration;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.stream.GenericInputSourceConverter;
import com.koch.ambeth.stream.IInputSource;
import com.koch.ambeth.stream.IInputStream;
import com.koch.ambeth.stream.binary.IBinaryInputStream;
import com.koch.ambeth.stream.bool.BinaryBooleanConverter;
import com.koch.ambeth.stream.bool.IBooleanInputSource;
import com.koch.ambeth.stream.bool.IBooleanInputStream;
import com.koch.ambeth.stream.float32.BinaryFloatConverter;
import com.koch.ambeth.stream.float32.IFloatInputSource;
import com.koch.ambeth.stream.float32.IFloatInputStream;
import com.koch.ambeth.stream.float64.BinaryDoubleConverter;
import com.koch.ambeth.stream.float64.IDoubleInputSource;
import com.koch.ambeth.stream.float64.IDoubleInputStream;
import com.koch.ambeth.stream.int32.BinaryIntConverter;
import com.koch.ambeth.stream.int32.IIntInputSource;
import com.koch.ambeth.stream.int32.IIntInputStream;
import com.koch.ambeth.stream.int64.BinaryLongConverter;
import com.koch.ambeth.stream.int64.ILongInputSource;
import com.koch.ambeth.stream.int64.ILongInputStream;
import com.koch.ambeth.stream.strings.BinaryStringConverter;
import com.koch.ambeth.stream.strings.IStringInputSource;
import com.koch.ambeth.stream.strings.IStringInputStream;
import com.koch.ambeth.util.IDedicatedConverter;
import com.koch.ambeth.util.IDedicatedConverterExtendable;
import com.koch.ambeth.util.typeinfo.INoEntityTypeExtendable;

@FrameworkModule
public class StreamModule implements IInitializingModule
{
	@LogInstance
	private ILogger log;

	@Override
	public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
	{
		beanContextFactory.link(IInputSource.class).to(INoEntityTypeExtendable.class);

		IBeanConfiguration genericConverterBC = beanContextFactory.registerBean(GenericInputSourceConverter.class);
		beanContextFactory.link(genericConverterBC).to(IDedicatedConverterExtendable.class).with(IInputSource.class, IInputStream.class);

		registerConverter(beanContextFactory, BinaryBooleanConverter.class, boolean[].class, IBooleanInputStream.class, IBooleanInputSource.class);
		registerConverter(beanContextFactory, BinaryDoubleConverter.class, double[].class, IDoubleInputStream.class, IDoubleInputSource.class);
		registerConverter(beanContextFactory, BinaryFloatConverter.class, float[].class, IFloatInputStream.class, IFloatInputSource.class);
		registerConverter(beanContextFactory, BinaryIntConverter.class, int[].class, IIntInputStream.class, IIntInputSource.class);
		registerConverter(beanContextFactory, BinaryLongConverter.class, long[].class, ILongInputStream.class, ILongInputSource.class);
		registerConverter(beanContextFactory, BinaryStringConverter.class, String[].class, IStringInputStream.class, IStringInputSource.class);
	}

	public static IBeanConfiguration registerConverter(IBeanContextFactory beanContextFactory, Class<? extends IDedicatedConverter> type, Class<?> fromType,
			Class<? extends IInputStream> inputStreamType, Class<? extends IInputSource> inputSourceType)
	{
		IBeanConfiguration converterBC = beanContextFactory.registerBean(type);
		beanContextFactory.link(converterBC).to(IDedicatedConverterExtendable.class).with(fromType, IBinaryInputStream.class);
		beanContextFactory.link(converterBC).to(IDedicatedConverterExtendable.class).with(fromType, inputStreamType);
		beanContextFactory.link(converterBC).to(IDedicatedConverterExtendable.class).with(inputSourceType, inputStreamType);
		beanContextFactory.link(converterBC).to(IDedicatedConverterExtendable.class).with(inputSourceType, IBinaryInputStream.class);
		return converterBC;
	}

}
