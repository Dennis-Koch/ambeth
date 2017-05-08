package com.koch.ambeth.service.metadata;

/*-
 * #%L
 * jambeth-service
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

import java.lang.annotation.Annotation;

public class IntermediateEmbeddedPrimitiveMember extends IntermediatePrimitiveMember
		implements IEmbeddedMember {
	protected final Member[] memberPath;

	protected final PrimitiveMember childMember;

	protected final String[] memberPathToken;

	protected final String memberPathString;

	public IntermediateEmbeddedPrimitiveMember(Class<?> entityType, Class<?> realType,
			Class<?> elementType, String propertyName, Member[] memberPath, PrimitiveMember childMember) {
		super(entityType, entityType, realType, elementType, propertyName, null);
		this.memberPath = memberPath;
		this.childMember = childMember;
		memberPathToken = EmbeddedMember.buildMemberPathToken(memberPath);
		memberPathString = EmbeddedMember.buildMemberPathString(memberPath);
	}

	@Override
	public boolean isToMany() {
		return childMember.isToMany();
	}

	@Override
	public <V extends Annotation> V getAnnotation(Class<V> annotationType) {
		return childMember.getAnnotation(annotationType);
	}

	@Override
	public boolean isTechnicalMember() {
		return childMember.isTechnicalMember();
	}

	@Override
	public boolean isTransient() {
		return childMember.isTransient();
	}

	@Override
	public void setTechnicalMember(boolean technicalMember) {
		((IPrimitiveMemberWrite) childMember).setTechnicalMember(technicalMember);
	}

	@Override
	public void setTransient(boolean isTransient) {
		((IPrimitiveMemberWrite) childMember).setTransient(isTransient);
	}

	@Override
	public void setDefinedBy(PrimitiveMember definedBy) {
		((IPrimitiveMemberWrite) childMember).setDefinedBy(definedBy);
	}

	@Override
	public Member[] getMemberPath() {
		return memberPath;
	}

	@Override
	public String getMemberPathString() {
		return memberPathString;
	}

	@Override
	public String[] getMemberPathToken() {
		return memberPathToken;
	}

	@Override
	public Member getChildMember() {
		return childMember;
	}
}
