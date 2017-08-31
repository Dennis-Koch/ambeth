package com.koch.ambeth.xml.pending;

/*-
 * #%L
 * jambeth-xml
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

import com.koch.ambeth.ioc.IInitializingBean;
import com.koch.ambeth.service.metadata.Member;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;
import com.koch.ambeth.xml.IReader;

public class ObjectSetterCommand extends AbstractObjectCommand
		implements IObjectCommand, IInitializingBean {
	protected final Member member;

	public ObjectSetterCommand(IObjectFuture objectFuture, Object parent, Member member) {
		super(objectFuture, parent);
		this.member = member;
		try {
			afterPropertiesSet();
		}
		catch (Throwable e) {
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	@Override
	public void execute(IReader reader) {
		Object value = objectFuture.getValue();
		member.setValue(parent, value);
	}
}
