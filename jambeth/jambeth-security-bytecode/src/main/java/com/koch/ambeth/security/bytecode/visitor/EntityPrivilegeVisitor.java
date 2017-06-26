package com.koch.ambeth.security.bytecode.visitor;

/*-
 * #%L
 * jambeth-security-bytecode
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

import java.lang.reflect.Constructor;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import com.koch.ambeth.bytecode.ClassGenerator;
import com.koch.ambeth.bytecode.ConstructorInstance;
import com.koch.ambeth.bytecode.FieldInstance;
import com.koch.ambeth.bytecode.MethodGenerator;
import com.koch.ambeth.bytecode.MethodInstance;
import com.koch.ambeth.bytecode.Script;
import com.koch.ambeth.bytecode.ScriptWithIndex;
import com.koch.ambeth.bytecode.behavior.BytecodeBehaviorState;
import com.koch.ambeth.bytecode.behavior.IBytecodeBehaviorState;
import com.koch.ambeth.security.privilege.model.IPrivilege;
import com.koch.ambeth.security.privilege.model.IPropertyPrivilege;
import com.koch.ambeth.service.merge.model.IEntityMetaData;
import com.koch.ambeth.service.metadata.Member;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;

public class EntityPrivilegeVisitor extends ClassGenerator {
	protected final IEntityMetaData metaData;
	protected final boolean create;
	protected final boolean read;
	protected final boolean update;
	protected final boolean delete;
	protected final boolean execute;

	public EntityPrivilegeVisitor(ClassVisitor cv, IEntityMetaData metaData, boolean create,
			boolean read, boolean update, boolean delete, boolean execute) {
		super(cv);
		this.metaData = metaData;
		this.create = create;
		this.read = read;
		this.update = update;
		this.delete = delete;
		this.execute = execute;
	}

	@Override
	public void visitEnd() {
		// AbstractPrivilege.class;
		MethodInstance template_m_getPrimitivePropertyPrivilege = new MethodInstance(null,
				IPrivilege.class, IPropertyPrivilege.class, "getPrimitivePropertyPrivilege", int.class);
		MethodInstance template_m_setPrimitivePropertyPrivilege =
				new MethodInstance(null, Opcodes.ACC_PROTECTED, void.class, "setPrimitivePropertyPrivilege",
						null, int.class, IPropertyPrivilege.class);

		MethodInstance template_m_getRelationPropertyPrivilege = new MethodInstance(null,
				IPrivilege.class, IPropertyPrivilege.class, "getRelationPropertyPrivilege", int.class);
		MethodInstance template_m_setRelationPropertyPrivilege =
				new MethodInstance(null, Opcodes.ACC_PROTECTED, void.class, "setRelationPropertyPrivilege",
						null, int.class, IPropertyPrivilege.class);

		implementGetSetPropertyPrivilege(metaData.getPrimitiveMembers(),
				template_m_getPrimitivePropertyPrivilege, template_m_setPrimitivePropertyPrivilege);
		implementGetSetPropertyPrivilege(metaData.getRelationMembers(),
				template_m_getRelationPropertyPrivilege, template_m_setRelationPropertyPrivilege);

		{
			MethodGenerator mg =
					visitMethod(new MethodInstance(null, IPrivilege.class, boolean.class, "isCreateAllowed"));
			mg.push(create);
			mg.returnValue();
			mg.endMethod();
		}
		{
			MethodGenerator mg =
					visitMethod(new MethodInstance(null, IPrivilege.class, boolean.class, "isReadAllowed"));
			mg.push(read);
			mg.returnValue();
			mg.endMethod();
		}
		{
			MethodGenerator mg =
					visitMethod(new MethodInstance(null, IPrivilege.class, boolean.class, "isUpdateAllowed"));
			mg.push(update);
			mg.returnValue();
			mg.endMethod();
		}
		{
			MethodGenerator mg =
					visitMethod(new MethodInstance(null, IPrivilege.class, boolean.class, "isDeleteAllowed"));
			mg.push(delete);
			mg.returnValue();
			mg.endMethod();
		}
		{
			MethodGenerator mg = visitMethod(
					new MethodInstance(null, IPrivilege.class, boolean.class, "isExecuteAllowed"));
			mg.push(execute);
			mg.returnValue();
			mg.endMethod();
		}
		{
			MethodGenerator mg = visitMethod(new MethodInstance(null, IPrivilege.class,
					IPropertyPrivilege.class, "getDefaultPropertyPrivilegeIfValid"));
			mg.pushNull();
			mg.returnValue();
			mg.endMethod();
		}
		implementConstructor(template_m_setPrimitivePropertyPrivilege,
				template_m_setRelationPropertyPrivilege);
		super.visitEnd();
	}

	protected void implementConstructor(MethodInstance template_m_setPrimitivePropertyPrivilege,
			MethodInstance template_m_setRelationPropertyPrivilege) {
		IBytecodeBehaviorState state = BytecodeBehaviorState.getState();
		Constructor<?> constructor;
		try {
			constructor =
					state.getCurrentType().getDeclaredConstructor(boolean.class, boolean.class, boolean.class,
							boolean.class, boolean.class, IPropertyPrivilege[].class, IPropertyPrivilege[].class);
		}
		catch (Exception e) {
			throw RuntimeExceptionUtil.mask(e);
		}
		ConstructorInstance c_method = new ConstructorInstance(constructor);

		MethodGenerator mg = visitMethod(c_method);
		mg.loadThis();
		mg.loadArgs();
		mg.invokeConstructor(c_method);

		Type propertyPrivilegeType = Type.getType(IPropertyPrivilege.class);
		for (int primitiveIndex = 0, size =
				metaData.getPrimitiveMembers().length; primitiveIndex < size; primitiveIndex++) {
			mg.loadThis();
			mg.push(primitiveIndex);
			mg.loadArg(5);
			mg.push(primitiveIndex);
			mg.arrayLoad(propertyPrivilegeType);
			mg.invokeVirtual(template_m_setPrimitivePropertyPrivilege);
		}
		for (int relationIndex = 0, size =
				metaData.getRelationMembers().length; relationIndex < size; relationIndex++) {
			mg.loadThis();
			mg.push(relationIndex);
			mg.loadArg(6);
			mg.push(relationIndex);
			mg.arrayLoad(propertyPrivilegeType);
			mg.invokeVirtual(template_m_setRelationPropertyPrivilege);
		}

		mg.returnValue();
		mg.endMethod();
	}

	protected void implementGetSetPropertyPrivilege(Member[] members,
			MethodInstance template_getPropertyPrivilege, MethodInstance template_setPropertyPrivilege) {
		FieldInstance[] fields = new FieldInstance[members.length];

		for (int index = 0, size = members.length; index < size; index++) {
			Member member = members[index];
			FieldInstance field = implementField(new FieldInstance(Opcodes.ACC_PRIVATE,
					getFieldName(member), null, IPropertyPrivilege.class));
			fields[index] = field;
		}
		implementGetPropertyPrivilege(fields, template_getPropertyPrivilege);
		implementSetPropertyPrivilege(fields, template_setPropertyPrivilege);
	}

	protected void implementGetPropertyPrivilege(final FieldInstance[] fields,
			MethodInstance template_getPropertyPrivilege) {
		implementSwitchByIndex(template_getPropertyPrivilege, "Given memberIndex not known",
				fields.length, new ScriptWithIndex() {
					@Override
					public void execute(MethodGenerator mg, int fieldIndex) {
						mg.getThisField(fields[fieldIndex]);
						mg.returnValue();
					}
				});
	}

	protected void implementSetPropertyPrivilege(final FieldInstance[] fields,
			MethodInstance template_setPropertyPrivilege) {
		implementSwitchByIndex(template_setPropertyPrivilege, "Given memberIndex not known",
				fields.length, new ScriptWithIndex() {
					@Override
					public void execute(MethodGenerator mg, int fieldIndex) {
						mg.putThisField(fields[fieldIndex], new Script() {
							@Override
							public void execute(MethodGenerator mg) {
								mg.loadArg(1);
							}
						});
						mg.returnValue();
					}
				});
	}

	public static String getFieldName(Member member) {
		return member.getName().replaceAll("\\.", "_");
	}
}
