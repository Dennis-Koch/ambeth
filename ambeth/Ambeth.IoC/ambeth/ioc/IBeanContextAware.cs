namespace De.Osthus.Ambeth.Ioc
{
    public interface IBeanContextAware
    {
        IServiceContext BeanContext { set; }
    }
}
