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

import com.koch.ambeth.service.merge.model.IEntityMetaData;

public class ObjRefStoreVisitor extends ObjRefVisitor {
	public ObjRefStoreVisitor(ClassVisitor cv, IEntityMetaData metaData, int idIndex) {
		super(cv, metaData, idIndex);
	}

	@Override
	public void visitEnd() {
		super.visitEnd();
	}
}
