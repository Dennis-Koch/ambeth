using De.Osthus.Ambeth.Ioc.Exceptions;
using System;
using System.Reflection;

namespace De.Osthus.Ambeth.Garbageproxy
{
    public abstract class GCProxy : IDisposable
    {
        public static readonly MethodInfo disposeMethod;

        static GCProxy()
        {
            disposeMethod = typeof(IDisposable).GetMethod("Dispose");
        }

        protected Object target;

        protected IDisposable disposable;

        public GCProxy(IDisposable target)
            : this(target, target)
        {
            // intended blank
        }

        public GCProxy(Object target, IDisposable disposable)
        {
            this.target = target;
            this.disposable = disposable;
        }

        ~GCProxy()
        {
            Dispose();
        }

        public void Dispose()
        {
            IDisposable disposable = this.disposable;
            if (disposable != null)
            {
                disposable.Dispose();
                this.disposable = null;
            }
            target = null;
        }

        protected Object ResolveTarget()
        {
            Object target = this.target;
            if (target != null)
            {
                return target;
            }
            throw new BeanAlreadyDisposedException("This handle has already been disposed. This seems like a memory leak in your application if you refer to illegal handles");
        }

        public override String ToString()
        {
            Object target = this.target;
            if (target != null)
            {
                return target.ToString();
            }
            return base.ToString();
        }
    }
}