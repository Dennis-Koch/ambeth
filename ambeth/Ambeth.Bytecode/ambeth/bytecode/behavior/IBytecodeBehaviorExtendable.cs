namespace De.Osthus.Ambeth.Bytecode.Behavior
{
    public interface IBytecodeBehaviorExtendable
    {
        void RegisterBytecodeBehavior(IBytecodeBehavior bytecodeBehavior);

        void UnregisterBytecodeBehavior(IBytecodeBehavior bytecodeBehavior);
    }
}