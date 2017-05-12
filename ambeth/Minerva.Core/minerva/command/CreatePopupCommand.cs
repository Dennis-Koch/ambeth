using System;
using System.Collections.Generic;
using System.Windows.Controls;
using System.Windows.Input;
using De.Osthus.Ambeth.Ioc;
using De.Osthus.Ambeth.Log;
using De.Osthus.Ambeth.Util;
using De.Osthus.Minerva.Core;

namespace De.Osthus.Minerva.Command
{
    public class CreatePopupCommand<T> : IInitializingBean, ICommand
	{
        [LogInstance]
        public ILogger Log { private get; set; }

        public virtual event EventHandler CanExecuteChanged;

        public IServiceContext BeanContext { protected get; set; }

        public Type CreateModuleType { protected get; set; }

        public String PopupWindowBean { protected get; set; }

        public virtual void AfterPropertiesSet()
        {
            ParamChecker.AssertNotNull(BeanContext, "BeanContext");
            ParamChecker.AssertNotNull(CreateModuleType, "CreateModuleType");
        }

        public virtual bool CanExecute(object parameter)
        {
            return (parameter != null);
        }

        public virtual void Execute(object parameter)
        {
            IServiceContext childContext = BeanContext.CreateService(CreateModuleType);

            bool success = false;
            try
            {
                EventHandler eventHandler = new EventHandler(delegate(Object sender, EventArgs e)
                {
                    ChildWindow childWindow = childContext.GetService<ChildWindow>(PopupWindowBean);
                    
                    bool? dialogResult = childWindow.DialogResult;
                    if (!dialogResult.HasValue || !dialogResult.Value)
                    {
                        return;
                    }

                    IModelSingleContainer<T> singleContainer = childContext.GetService<IModelSingleContainer<T>>();
                    T newItem = singleContainer.Value;
                    if (parameter is IGenericViewModel<T>)
                    {
                        ((IGenericViewModel<T>)parameter).InsertAt(0, newItem);
                    }
                    else if (parameter is IModelMultiContainer<T>)
                    {
                        ((IModelMultiContainer<T>)parameter).Values.Insert(0, newItem);
                    }
                    else if (parameter is IModelSingleContainer<T>)
                    {
                        ((IModelSingleContainer<T>)parameter).Value = newItem;
                    }
                    else if (parameter is IList<T>)
                    {
                        ((IList<T>)parameter).Insert(0, newItem);
                    }
                    else if (parameter is T)
                    {
                        parameter = newItem;
                    }
                    else
                    {
                        String errorString = "parameter for create command can not hold an item of type " + newItem.GetType().ToString();
                        Log.Error(errorString);
                        throw new ArgumentException(errorString);
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
