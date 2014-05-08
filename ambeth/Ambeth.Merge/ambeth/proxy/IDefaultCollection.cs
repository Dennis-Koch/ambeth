namespace De.Osthus.Ambeth.Proxy
{
    /**
     * Marker interface for collections which do not force an eager load of relations by themselves
     * 
     * @param <T>
     */
    public interface IDefaultCollection
    {
        bool HasDefaultState { get; }
    }
}
