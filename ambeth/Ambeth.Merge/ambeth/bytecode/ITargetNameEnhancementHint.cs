using System;

namespace De.Osthus.Ambeth.Bytecode
{
    public interface ITargetNameEnhancementHint : IEnhancementHint
    {
        String GetTargetName(Type typeToEnhance);
    }
}
