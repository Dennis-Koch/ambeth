using De.Osthus.Ambeth.Bytecode.Behavior;
using De.Osthus.Ambeth.Typeinfo;
using De.Osthus.Ambeth.Util;
using System;
using System.Reflection;

namespace De.Osthus.Ambeth.Bytecode.Util
{
    public class EntityUtil
    {        
        public static Type GetEntityType(IEnhancementHint hint)
        {            
            EntityEnhancementHint entityEnhancementHint = hint.Unwrap<EntityEnhancementHint>();
            if (entityEnhancementHint != null)
            {
                return BytecodeBehaviorState.State.OriginalType;
            }
            EmbeddedEnhancementHint embeddedEnhancementHint = hint.Unwrap<EmbeddedEnhancementHint>();
            if (embeddedEnhancementHint != null)
            {
                return embeddedEnhancementHint.RootEntityType;
            }
            return null;
        }
    }
}