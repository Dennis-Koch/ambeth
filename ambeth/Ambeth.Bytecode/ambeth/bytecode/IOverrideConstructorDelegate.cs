using De.Osthus.Ambeth.Bytecode.Visitor;

namespace De.Osthus.Ambeth.Bytecode
{
    public delegate void IOverrideConstructorDelegate(IClassVisitor cv, ConstructorInstance superConstructor);
}
