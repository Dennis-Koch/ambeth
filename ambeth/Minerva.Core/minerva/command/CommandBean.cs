using System;
using System.ComponentModel;
using System.Windows.Input;
using System.Threading;
#if SILVERLIGHT
using Telerik.Windows.Controls;
#endif
using De.Osthus.Ambeth.Ioc;
using De.Osthus.Ambeth.Util;

namespace De.Osthus.Minerva.Command
{
    public abstract class CommandBean<T> : ICommand, IInitializingBean, IStartingBean, IDisposableBean
    {
        #region Events

        public virtual event EventHandler CanExecuteChanged
        {
            add
            {
                CommandManager.RequerySuggested += value;
            }
            remove
            {
                CommandManager.RequerySuggested -= value;
            }
        }

        #endregion
        
        #region Constructors

        protected bool readyToWork = false;

        public virtual void AfterPropertiesSet()
        {
            readyToWork = true;
        }

        public virtual void AfterStarted()
        {
            RaiseCanExecuteChanged();
        }

        public virtual void Destroy()
        {
            readyToWork = false;
        }

        #endregion

        #region ICommand Members

        protected abstract void ExecuteIntern(T obj);

        protected virtual bool CanExecuteIntern(T obj)
        {
            return true;
        }

        public virtual void RaiseCanExecuteChanged()
        {
            CommandManager.InvalidateRequerySuggested();
        }

        public virtual void Execute(Object parameter)
        {
            ExecuteIntern((T)parameter);
        }

        public virtual bool CanExecute(Object parameter)
        {
            if (!readyToWork)
            {
                return false;
            }
            return CanExecuteIntern((T)parameter);
        }

        #endregion
    }
}
