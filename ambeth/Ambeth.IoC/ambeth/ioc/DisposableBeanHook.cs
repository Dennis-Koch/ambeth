namespace De.Osthus.Ambeth.Ioc
{
    public class DisposableBeanHook : IDisposableBean
    {
        private readonly IDisposableBean hook;

        public DisposableBeanHook(IDisposableBean hook)
        {
            this.hook = hook;
        }

        public void Destroy()
        {
            hook.Destroy();
        }
    }
}
