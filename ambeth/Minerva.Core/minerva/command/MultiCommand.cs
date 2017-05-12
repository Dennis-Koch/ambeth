using System;
using System.Threading;
using System.Windows.Input;
using De.Osthus.Ambeth.Ioc;
using De.Osthus.Ambeth.Threading;
using De.Osthus.Ambeth.Util;

namespace De.Osthus.Minerva.Command
{
    public class MultiCommand : IInitializingBean, ICommand
    {
        public virtual event EventHandler CanExecuteChanged;
        
        public virtual IGuiThreadHelper GuiThreadHelper { get; set; }

        public virtual SynchronizationContext SyncContext { get; set; }

        public virtual void AfterPropertiesSet()
        {
            ParamChecker.AssertNotNull(GuiThreadHelper, "GuiThreadHelper");
            ParamChecker.AssertNotNull(SyncContext, "SyncContext");
        }

        public virtual void OnCanExecuteChanged(Object sender, EventArgs e)
        {
            if (CanExecuteChanged != null)
            {
                if (GuiThreadHelper.IsInGuiThread())
                {
                    CanExecuteChanged.Invoke(this, EventArgs.Empty);
                }
                else
                {
                    SyncContext.Send((object state) =>
                    {
                        CanExecuteChanged.Invoke(this, EventArgs.Empty);
                    }, null);
                }
            }
        }

        public virtual bool CanExecute(Object parameter)
        {
            if (!(parameter is ICanExecuteStateProvider))
            {
                return false;
            }
            return (((ICanExecuteStateProvider)parameter).CanExecute());
        }

        public void Execute(Object parameter)
        {
            if (!(parameter is ICanExecuteStateProvider))
            {
                throw new Exception("Parameter for MultiCommand was no " + typeof(ICanExecuteStateProvider).Name);
            }
            ((ICanExecuteStateProvider)parameter).Execute();
        }
    }
}
