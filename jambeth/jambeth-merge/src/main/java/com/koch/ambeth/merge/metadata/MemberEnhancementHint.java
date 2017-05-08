package com.koch.ambeth.merge.metadata;

/*-
 * #%L
 * jambeth-merge
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

import java.io.Serializable;
import java.util.regex.Pattern;

import org.objectweb.asm.Type;

import com.koch.ambeth.ioc.bytecode.IEnhancementHint;
import com.koch.ambeth.ioc.bytecode.ITargetNameEnhancementHint;
import com.koch.ambeth.service.metadata.Member;

public class MemberEnhancementHint
		implements IEnhancementHint, ITargetNameEnhancementHint, Serializable {
	private static final long serialVersionUID = -4297854443506118537L;

	protected final Class<?> declaringType;

	protected final String memberName;

	public MemberEnhancementHint(Class<?> declaringType, String memberName) {
		this.declaringType = declaringType;
		this.memberName = memberName;
	}

	public Class<?> getDeclaringType() {
		return declaringType;
	}

	public String getMemberName() {
		return memberName;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (!getClass().equals(obj.getClass())) {
			return false;
		}
		MemberEnhancementHint other = (MemberEnhancementHint) obj;
		return getDeclaringType().equals(other.getDeclaringType())
				&& getMemberName().equals(other.getMemberName());
	}

	@Override
	public int hashCode() {
		return getClass().hashCode() ^ getDeclaringType().hashCode() ^ getMemberName().hashCode();
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends IEnhancementHint> T unwrap(Class<T> includedHintType) {
		if (MemberEnhancementHint.class.equals(includedHintType)) {
			return (T) this;
		}
		return null;
	}

	@Override
	public String getTargetName(Class<?> typeToEnhance) {
		return Type.getInternalName(declaringType) + "$" + Member.class.getSimpleName() + "$"
				+ memberName.replaceAll(Pattern.quote("."), "_");
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + ": Path=" + declaringType.getSimpleName() + "."
				+ memberName;
	}
}
