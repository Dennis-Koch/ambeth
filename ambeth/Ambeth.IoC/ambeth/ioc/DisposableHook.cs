using System;

namespace De.Osthus.Ambeth.Ioc
{
    public class DisposableHook : IDisposableBean
    {
        private readonly IDisposable hook;

        public DisposableHook(IDisposable hook)
        {
            this.hook = hook;
        }

        public void Destroy()
        {
            hook.Dispose();
        }
    }
}
