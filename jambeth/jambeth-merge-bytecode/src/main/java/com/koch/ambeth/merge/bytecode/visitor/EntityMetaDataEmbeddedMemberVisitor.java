package com.koch.ambeth.merge.bytecode.visitor;

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

import org.objectweb.asm.ClassVisitor;

import com.koch.ambeth.bytecode.ClassGenerator;
import com.koch.ambeth.bytecode.FieldInstance;
import com.koch.ambeth.bytecode.MethodInstance;
import com.koch.ambeth.bytecode.visitor.InterfaceAdder;
import com.koch.ambeth.service.metadata.EmbeddedMember;
import com.koch.ambeth.service.metadata.IEmbeddedMember;
import com.koch.ambeth.service.metadata.Member;

public class EntityMetaDataEmbeddedMemberVisitor extends ClassGenerator
{
	protected static final MethodInstance template_m_getMemberPath = new MethodInstance(null, IEmbeddedMember.class, Member[].class, "getMemberPath");

	protected static final MethodInstance template_m_getMemberPathString = new MethodInstance(null, IEmbeddedMember.class, String.class, "getMemberPathString");

	protected static final MethodInstance template_m_getMemberPathToken = new MethodInstance(null, IEmbeddedMember.class, String[].class, "getMemberPathToken");

	protected static final MethodInstance template_m_getChildMember = new MethodInstance(null, IEmbeddedMember.class, Member.class, "getChildMember");

	protected final Class<?> entityType;

	protected final String memberName;

	protected final Member[] members;

	public EntityMetaDataEmbeddedMemberVisitor(ClassVisitor cv, Class<?> entityType, String memberName, Member[] members)
	{
		super(new InterfaceAdder(cv, IEmbeddedMember.class));
		this.entityType = entityType;
		this.memberName = memberName;
		this.members = members;
	}

	@Override
	public void visitEnd()
	{
		String[] memberNameSplit = EmbeddedMember.split(memberName);
		Member[] memberPath = new Member[members.length - 1];
		System.arraycopy(members, 0, memberPath, 0, memberPath.length);
		implementGetMemberPath(memberPath);
		implementGetMemberPathString(EmbeddedMember.buildMemberPathString(members));
		implementGetMemberPathToken(memberNameSplit);
		implementGetChildMember(members[members.length - 1]);
		super.visitEnd();
	}

	protected void implementGetMemberPath(Member[] memberPath)
	{
		FieldInstance f_memberPath = implementStaticAssignedField("sf__memberPath", memberPath);
		implementGetter(template_m_getMemberPath, f_memberPath);
	}

	protected void implementGetMemberPathString(String memberPathString)
	{
		FieldInstance f_memberPathString = implementStaticAssignedField("sf__memberPathString", memberPathString);
		implementGetter(template_m_getMemberPathString, f_memberPathString);
	}

	protected void implementGetMemberPathToken(String[] memberPathSplit)
	{
		FieldInstance f_memberPathToken = implementStaticAssignedField("sf__memberPathToken", memberPathSplit);
		implementGetter(template_m_getMemberPathToken, f_memberPathToken);
	}

	protected void implementGetChildMember(Member childMember)
	{
		FieldInstance f_childMember = implementStaticAssignedField("sf__childMember", childMember);
		implementGetter(template_m_getChildMember, f_childMember);
	}
}
