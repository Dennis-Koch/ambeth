package com.koch.ambeth.xml;

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

import com.koch.ambeth.util.typeinfo.ITypeInfoItem;

public class SpecifiedMember {
	private ITypeInfoItem member;

	private ITypeInfoItem specifiedMember;

	public SpecifiedMember(ITypeInfoItem member, ITypeInfoItem specifiedMember) {
		super();
		this.member = member;
		this.specifiedMember = specifiedMember;
	}

	public ITypeInfoItem getMember() {
		return member;
	}

	public ITypeInfoItem getSpecifiedMember() {
		return specifiedMember;
	}

	@Override
	public String toString() {
		return getMember().toString();
	}
}
