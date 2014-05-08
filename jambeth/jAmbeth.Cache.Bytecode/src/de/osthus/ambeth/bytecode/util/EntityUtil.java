package de.osthus.ambeth.bytecode.util;

import de.osthus.ambeth.bytecode.EmbeddedEnhancementHint;
import de.osthus.ambeth.bytecode.EntityEnhancementHint;
import de.osthus.ambeth.bytecode.IEnhancementHint;
import de.osthus.ambeth.bytecode.behavior.BytecodeBehaviorState;

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
