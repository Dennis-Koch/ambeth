using System;
using System.ComponentModel;
using System.Windows.Controls;
using System.Windows.Input;
using De.Osthus.Ambeth.Ioc;
using De.Osthus.Ambeth.Ioc.Factory;
using De.Osthus.Ambeth.Log;
using De.Osthus.Ambeth.Merge;
using De.Osthus.Ambeth.Util;
using De.Osthus.Minerva.Core;


namespace De.Osthus.Minerva.Command
{
    public class EditPopupCommand<T> : IInitializingBean, ICommand
    {
        [LogInstance]
        public ILogger Log { private get; set; }

        public virtual event EventHandler CanExecuteChanged;

        public virtual IServiceContext BeanContext { get; set; }

        public virtual Type EditModuleType { get; set; }

        public virtual String PopupWindowBean { get; set; }

        public virtual IRevertChangesHelper RevertChangesHelper { get; set; }

        public virtual String SelectedItemContainerBean { get; set; }

        public virtual void AfterPropertiesSet()
        {
            ParamChecker.AssertNotNull(BeanContext, "BeanContext");
            ParamChecker.AssertNotNull(EditModuleType, "EditModuleType");
            ParamChecker.AssertNotNull(RevertChangesHelper, "RevertChangesHelper");
        }

        // Handle PropertyChanged of the ModelContainer is equivalent to handling
        // SelectedItem changes of the view (If the container is used consequently):
        public virtual void OnSelectedItemsChanged(Object sender, PropertyChangedEventArgs e)
        {
            if (CanExecuteChanged != null)
            {
                CanExecuteChanged.Invoke(this, EventArgs.Empty);
            }
        }

        // CanExecute determines whether a button is enabled or disabled:
        public virtual bool CanExecute(object parameter)
        {
            if (parameter is IModelSingleContainer<T>)
            {
                return ((IModelSingleContainer<T>)parameter).Value != null;
            }
            return (parameter != null);
        }

        public virtual void Execute(object parameter)
        {
            IServiceContext childContext = BeanContext.CreateService(delegate(IBeanContextFactory bcf)
            {
                if (SelectedItemContainerBean != null)
                {
                    bcf.RegisterExternalBean(SelectedItemContainerBean, parameter);
                }
            }, EditModuleType);

            bool success = false;
            try
            {
                EventHandler eventHandler = new EventHandler(delegate(Object sender, EventArgs e)
                {
                    ChildWindow childWindow = childContext.GetService<ChildWindow>(PopupWindowBean);
                    bool? dialogResult = childWindow.DialogResult;
                    if (!dialogResult.HasValue || !dialogResult.Value)
                    {
                        Object editedItem = null;
                        if (parameter is IModelMultiContainer<T>)
                        {
                            editedItem = ((IModelSingleContainer<T>)parameter).Value;
                        }
                        else
                        {
                            editedItem = parameter;
                        }
                        RevertChangesHelper.RevertChanges(editedItem);
                        return;
                    }
                });
                childContext.Link(eventHandler).To(PopupWindowBean, ChildWindowEvents.Closed);
                success = true;
            }
            catch (Exception ex)
            {
                Log.Error(ex);
                throw;
            }
            finally
            {
                if (!success)
                {
                    childContext.Dispose();
                }
            }
        }
    }
}