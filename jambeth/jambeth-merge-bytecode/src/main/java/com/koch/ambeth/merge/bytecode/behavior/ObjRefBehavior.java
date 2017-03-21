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
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.merge.bytecode.visitor.ObjRefVisitor;
import com.koch.ambeth.merge.metadata.ObjRefEnhancementHint;
import com.koch.ambeth.service.merge.IEntityMetaDataProvider;
import com.koch.ambeth.service.merge.model.IEntityMetaData;
import com.koch.ambeth.service.merge.model.IObjRef;

public class ObjRefBehavior extends AbstractBehavior
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected IEntityMetaDataProvider entityMetaDataProvider;

	@Override
	public Class<?>[] getEnhancements()
	{
		return new Class<?>[] { IObjRef.class };
	}

	@Override
	public ClassVisitor extend(ClassVisitor visitor, IBytecodeBehaviorState state, List<IBytecodeBehavior> remainingPendingBehaviors,
			List<IBytecodeBehavior> cascadePendingBehaviors)
	{
		ObjRefEnhancementHint memberHint = state.getContext(ObjRefEnhancementHint.class);
		if (memberHint == null)
		{
			return visitor;
		}
		IEntityMetaData metaData = entityMetaDataProvider.getMetaData(memberHint.getEntityType());
		visitor = new ObjRefVisitor(visitor, metaData, memberHint.getIdIndex());
		return visitor;
	}
}
