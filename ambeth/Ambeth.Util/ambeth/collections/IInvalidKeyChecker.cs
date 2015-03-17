namespace De.Osthus.Ambeth.Collections
{
    public interface IInvalidKeyChecker<K>
    {
        bool IsKeyValid(K key);
    }
}