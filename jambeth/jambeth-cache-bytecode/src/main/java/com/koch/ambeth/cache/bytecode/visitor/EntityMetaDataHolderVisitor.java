package com.koch.ambeth.cache.bytecode.visitor;

/*-
 * #%L
 * jambeth-cache-bytecode
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
import com.koch.ambeth.merge.proxy.IEntityMetaDataHolder;
import com.koch.ambeth.service.merge.model.IEntityMetaData;

public class EntityMetaDataHolderVisitor extends ClassGenerator
{
	public static final MethodInstance m_template_getEntityMetaData = new MethodInstance(null, IEntityMetaDataHolder.class, IEntityMetaData.class,
			"get__EntityMetaData");

	public static MethodInstance getImplementedGetEntityMetaData(ClassGenerator cv, IEntityMetaData metaData)
	{
		MethodInstance method = MethodInstance.findByTemplate(m_template_getEntityMetaData, true);
		if (method != null)
		{
			// already implemented
			return method;
		}
		FieldInstance f_entityMetaData = cv.implementStaticAssignedField("sf__entityMetaData", metaData);

		return cv.implementGetter(m_template_getEntityMetaData, f_entityMetaData);
	}

	protected IEntityMetaData metaData;

	public EntityMetaDataHolderVisitor(ClassVisitor cv, IEntityMetaData metaData)
	{
		super(cv);
		this.metaData = metaData;
	}

	@Override
	public void visitEnd()
	{
		implementGetEntityMetaData();
		super.visitEnd();
	}

	protected void implementGetEntityMetaData()
	{
		getImplementedGetEntityMetaData(this, metaData);
	}
}
