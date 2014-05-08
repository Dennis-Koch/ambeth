using System;

namespace De.Osthus.Ambeth.Bytecode
{
    public interface IBytecodePrinter
    {
        String ToPrintableBytecode(Type type);
    }
}
