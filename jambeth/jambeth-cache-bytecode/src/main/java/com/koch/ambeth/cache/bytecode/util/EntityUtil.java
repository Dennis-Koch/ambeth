package com.koch.ambeth.cache.bytecode.util;

import com.koch.ambeth.bytecode.behavior.BytecodeBehaviorState;
import com.koch.ambeth.ioc.bytecode.IEnhancementHint;
import com.koch.ambeth.merge.bytecode.EmbeddedEnhancementHint;
import com.koch.ambeth.merge.bytecode.EntityEnhancementHint;

public class EntityUtil
{
	public static Class<?> getEntityType(IEnhancementHint hint)
	{
		EntityEnhancementHint entityEnhancementHint = hint.unwrap(EntityEnhancementHint.class);
		if (entityEnhancementHint != null)
		{
			return BytecodeBehaviorState.getState().getOriginalType();
		}
		EmbeddedEnhancementHint embeddedEnhancementHint = hint.unwrap(EmbeddedEnhancementHint.class);
		if (embeddedEnhancementHint != null)
		{
			return embeddedEnhancementHint.getRootEntityType();
		}
		return null;
	}
}
