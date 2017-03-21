package com.koch.ambeth.merge.bytecode.behavior;

/*-
 * #%L
 * jambeth-merge-bytecode
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

import java.util.List;

import org.objectweb.asm.ClassVisitor;

import com.koch.ambeth.bytecode.behavior.AbstractBehavior;
import com.koch.ambeth.bytecode.behavior.IBytecodeBehavior;
import com.koch.ambeth.bytecode.behavior.IBytecodeBehaviorState;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.bytecode.IBytecodeEnhancer;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.merge.bytecode.EmbeddedEnhancementHint;
import com.koch.ambeth.merge.bytecode.visitor.EntityMetaDataEmbeddedMemberVisitor;
import com.koch.ambeth.merge.bytecode.visitor.EntityMetaDataMemberVisitor;
import com.koch.ambeth.merge.bytecode.visitor.EntityMetaDataPrimitiveMemberVisitor;
import com.koch.ambeth.merge.bytecode.visitor.EntityMetaDataRelationMemberVisitor;
import com.koch.ambeth.merge.metadata.IMemberTypeProvider;
import com.koch.ambeth.merge.metadata.MemberEnhancementHint;
import com.koch.ambeth.merge.metadata.RelationMemberEnhancementHint;
import com.koch.ambeth.service.merge.IEntityMetaDataProvider;
import com.koch.ambeth.service.metadata.EmbeddedMember;
import com.koch.ambeth.service.metadata.IEmbeddedMember;
import com.koch.ambeth.service.metadata.IPrimitiveMemberWrite;
import com.koch.ambeth.service.metadata.IRelationMemberWrite;
import com.koch.ambeth.service.metadata.Member;
import com.koch.ambeth.util.typeinfo.IPropertyInfo;
import com.koch.ambeth.util.typeinfo.IPropertyInfoProvider;

public class EntityMetaDataMemberBehavior extends AbstractBehavior {
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected IBytecodeEnhancer bytecodeEnhancer;

	@Autowired
	protected IEntityMetaDataProvider entityMetaDataProvider;

	@Autowired
	protected IMemberTypeProvider memberTypeProvider;

	@Autowired
	protected IPropertyInfoProvider propertyInfoProvider;

	@Override
	public Class<?>[] getEnhancements() {
		return new Class<?>[] {IRelationMemberWrite.class, IPrimitiveMemberWrite.class,
				IEmbeddedMember.class};
	}

	@Override
	public ClassVisitor extend(ClassVisitor visitor, IBytecodeBehaviorState state,
			List<IBytecodeBehavior> remainingPendingBehaviors,
			List<IBytecodeBehavior> cascadePendingBehaviors) {
		MemberEnhancementHint memberHint = state.getContext(MemberEnhancementHint.class);
		if (memberHint == null) {
			return visitor;
		}
		RelationMemberEnhancementHint relationMemberHint =
				state.getContext(RelationMemberEnhancementHint.class);

		String[] memberNameSplit = EmbeddedMember.split(memberHint.getMemberName());
		if (memberNameSplit.length == 1) {
			IPropertyInfo[] propertyPath = new IPropertyInfo[1];
			propertyPath[0] = propertyInfoProvider.getProperty(memberHint.getDeclaringType(),
					memberHint.getMemberName());
			if (propertyPath[0] == null) {
				throw new IllegalArgumentException("Member not found: " + memberHint.getDeclaringType()
						+ "." + memberHint.getMemberName());
			}
			visitor = new EntityMetaDataMemberVisitor(visitor, memberHint.getDeclaringType(),
					memberHint.getDeclaringType(), memberHint.getMemberName(), entityMetaDataProvider,
					propertyPath);
			if (relationMemberHint != null) {
				visitor = new EntityMetaDataRelationMemberVisitor(visitor, memberHint.getDeclaringType(),
						memberHint.getMemberName(), propertyPath);
			}
			else {
				visitor = new EntityMetaDataPrimitiveMemberVisitor(visitor, memberHint.getDeclaringType(),
						memberHint.getMemberName(), propertyPath);
			}
			return visitor;
		}
		Member[] members = new Member[memberNameSplit.length];
		Class<?> currType = memberHint.getDeclaringType();
		IPropertyInfo[] propertyPath = new IPropertyInfo[members.length];
		StringBuilder sb = new StringBuilder();
		for (int a = 0, size = memberNameSplit.length; a < size; a++) {
			propertyPath[a] = propertyInfoProvider.getProperty(currType, memberNameSplit[a]);
			if (propertyPath[a] == null) {
				throw new IllegalArgumentException(
						"Member not found: " + currType + "." + memberNameSplit[a]);
			}
			if (a + 1 < memberNameSplit.length) {
				members[a] = memberTypeProvider.getMember(currType, memberNameSplit[a]);
			}
			else if (relationMemberHint != null) {
				members[a] = memberTypeProvider.getRelationMember(currType, memberNameSplit[a]);
			}
			else {
				members[a] = memberTypeProvider.getPrimitiveMember(currType, memberNameSplit[a]);
			}
			if (a > 0) {
				sb.append('.');
			}
			sb.append(members[a].getName());
			if (a + 1 < memberNameSplit.length) {
				currType = bytecodeEnhancer.getEnhancedType(members[a].getRealType(),
						new EmbeddedEnhancementHint(memberHint.getDeclaringType(), currType, sb.toString()));
			}
			else {
				currType = members[a].getRealType();
			}
		}
		visitor = new EntityMetaDataMemberVisitor(visitor, memberHint.getDeclaringType(),
				memberHint.getDeclaringType(), memberHint.getMemberName(), entityMetaDataProvider,
				propertyPath);
		if (relationMemberHint != null) {
			visitor = new EntityMetaDataRelationMemberVisitor(visitor, memberHint.getDeclaringType(),
					memberHint.getMemberName(), propertyPath);
		}
		else {
			visitor = new EntityMetaDataPrimitiveMemberVisitor(visitor, memberHint.getDeclaringType(),
					memberHint.getMemberName(), propertyPath);
		}
		visitor = new EntityMetaDataEmbeddedMemberVisitor(visitor, memberHint.getDeclaringType(),
				memberHint.getMemberName(), members);
		return visitor;
	}
}
