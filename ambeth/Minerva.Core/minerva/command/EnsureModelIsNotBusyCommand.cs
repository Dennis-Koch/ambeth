using System;
using System.Linq;
using System.ComponentModel;
using De.Osthus.Ambeth.Ioc;
using De.Osthus.Ambeth.Util;
using De.Osthus.Minerva.Command;
using De.Osthus.Minerva.Core;
using System.Windows.Threading;

namespace De.Osthus.Minerva.Command
{
    public class EnsureModelIsNotBusyCommand : CommandBean<String>, IInitializingBean, IDisposableBean, IStartingBean
    {
        //public virtual IModelMultiContainer<IGenericViewModel> Models { get; set; }

        public virtual IGenericViewModel Model { get; set; }

        public virtual int Delay { get; set; }

        protected bool started = false;

        protected IModelSingleContainer<Int32> busyCalls = new ModelMultiContainer<Int32>();

        public override void AfterPropertiesSet()
        {
            base.AfterPropertiesSet();
            busyCalls.Value = 0;
            ParamChecker.AssertNotNull(Model, "Model");
            IGenericViewModel model = Model;
            //foreach (IGenericViewModel model in Models.Values)
            //{
                INotifyPropertyChanged npc = model as INotifyPropertyChanged;

                if (npc != null)
                {
                    npc.PropertyChanged += OnIsBusyChanged;
                }
            //}
        }

        public override void AfterStarted()
        {
            started = true;
            //Intended last call
            base.AfterStarted();
        }

        protected void OnIsBusyChanged(Object sender, PropertyChangedEventArgs ea)
        {
            if (ea.PropertyName.Equals("IsBusy"))
            {
                if (Delay != 0)
                {
                    lock (busyCalls)
                    {
                        busyCalls.Value += 1;
                    }
                    DispatcherTimer timer = new DispatcherTimer();
                    timer.Interval = new TimeSpan(0, 0, 0, 0, Delay);
                    timer.Tick += timeout;
                    timer.Start();
                }
                else
                {
                    RaiseCanExecuteChanged();
                }
            }
        }

        protected void timeout(Object s, EventArgs arg)
        {
            DispatcherTimer timer = s as DispatcherTimer;
            lock (busyCalls)
            {
                busyCalls.Value -= 1;
            }
            RaiseCanExecuteChanged();
            timer.Stop();
            timer.Tick -= timeout;
        }

        public override void Destroy()
        {
            IGenericViewModel model = Model;
            //foreach (IGenericViewModel model in Models.Values)
            //{
                INotifyPropertyChanged npc = model as INotifyPropertyChanged;

                if (npc != null)
                {
                    npc.PropertyChanged -= OnIsBusyChanged;
                }
            //}
            base.Destroy();
        }

        protected override bool CanExecuteIntern(String parameter)
        {
            bool result = base.CanExecuteIntern(parameter) && started;
            result &= busyCalls.Value == 0;
            if (!result)
            {
                return false;
            }

            IGenericViewModel model = Model;
            //foreach (IGenericViewModel model in Models.Values)
            //{
                if (model.IsBusy)
                {
                    result = false;
                    //break;
                }
            //}
            return result;
        }

        protected override void ExecuteIntern(String obj)
        {
            //Intended blank
        }
    }
}
