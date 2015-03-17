using System;

namespace De.Osthus.Ambeth.Util
{
    public class WrappingNamedRunnable : INamedRunnable
    {
        protected readonly Runnable runnable;

        protected readonly String name;

        public WrappingNamedRunnable(Runnable runnable, String name)
        {
            this.runnable = runnable;
            this.name = name;
        }

        public void Run()
        {
            runnable.Run();
        }

        public String Name
        {
            get
            {
                return name;
            }
        }
    }
}