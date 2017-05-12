//using System;
//using System.Collections.Generic;
//using System.ComponentModel;
//using System.Reflection;
//using System.Threading;
//using System.Windows.Input;
//using De.Osthus.Ambeth.Ioc;
//using De.Osthus.Ambeth.Merge;
//using De.Osthus.Ambeth.Threading;
//using De.Osthus.Ambeth.Util;
//using De.Osthus.Minerva.Core;
//using AmbethIDataObject = De.Osthus.Ambeth.Model.IDataObject;

//namespace De.Osthus.Minerva.Command
//{
//    public class UndoNotPersistedChangesCommand : IInitializingBean, ICommand, IAsyncCommand
//    {
//        public virtual event EventHandler CanExecuteChanged;

//        public virtual bool AlwaysExecutable { get; set; }

//        public virtual IRevertChangesHelper ChangeHelper { get; set; }

//        public virtual IGuiThreadHelper GuiThreadHelper { get; set; }

//        public virtual SynchronizationContext SyncContext { get; set; }

//        public UndoNotPersistedChangesCommand()
//        {
//            // If used for a cancel button, AlwaysExecutable should be set true.
//            AlwaysExecutable = false;
//        }

//        public virtual void AfterPropertiesSet()
//        {
//            ParamChecker.AssertNotNull(ChangeHelper, "ChangeHelper");
//            ParamChecker.AssertNotNull(GuiThreadHelper, "GuiThreadHelper");
//            ParamChecker.AssertNotNull(SyncContext, "SyncContext");
//        }

//        public virtual void OnNotPersistedChanged(Object sender, PropertyChangedEventArgs e)
//        {
//            if (AlwaysExecutable)
//            {
//                return;
//            }
//            if (CanExecuteChanged != null)
//            {
//                if (GuiThreadHelper.IsInGuiThread())
//                {
//                    CanExecuteChanged.Invoke(this, EventArgs.Empty);
//                }
//                else
//                {
//                    SyncContext.Send((object state) =>
//                    {
//                        CanExecuteChanged.Invoke(this, EventArgs.Empty);
//                    }, null);
//                }
//            }
//        }

//        // A cancel button should always be enabled, even if there are no local changes:
//        public virtual bool CanExecute(object parameter)
//        {
//            if (AlwaysExecutable)
//            {
//                return true;
//            }
//            else if (parameter == null)
//            {
//                return false;
//            }

//            if (parameter is IModelContainerRegistry)
//            {
//                IList<Object>  modelContainers = ((IModelContainerRegistry)parameter).GetModelContainers();
//                foreach (Object modelContainer in modelContainers)
//                {
//                    if (modelContainer is INotPersistedDataContainer)
//                    {
//                        INotPersistedDataContainer container = (INotPersistedDataContainer)modelContainer;
//                        if (container.HasNotPersisted())
//                        {
//                            return true;
//                        }
//                    }
//                    else if (modelContainer is IModelMultiContainer)
//                    {
//                        IList<Object> multiData = ((IModelMultiContainer)modelContainer).ValuesData;
//                        foreach (Object data in multiData)
//                        {
//                            if (data != null && data is AmbethIDataObject)
//                            {
//                                AmbethIDataObject aio = (AmbethIDataObject)data;
//                                if (aio.HasPendingChanges)
//                                {
//                                    return true;
//                                }
//                            }
//                        }
//                    }
//                    else if (modelContainer is IModelSingleContainer)
//                    {
//                        PropertyInfo valueProperty = modelContainer.GetType().GetProperty("Value");
//                        Object val = valueProperty.GetValue(modelContainer, null);
//                        if (val != null && val is AmbethIDataObject)
//                        {
//                            AmbethIDataObject aio = (AmbethIDataObject)val;
//                            if (aio.HasPendingChanges)
//                            {
//                                return true;
//                            }
//                        }
//                    }
//                    else if (modelContainer is AmbethIDataObject)
//                    {
//                        AmbethIDataObject aio = (AmbethIDataObject)modelContainer;
//                        if (aio.HasPendingChanges)
//                        {
//                            return true;
//                        }
//                    }
//                }
//            }
//            else
//            {
//                if (parameter is AmbethIDataObject)
//                {
//                    if (((AmbethIDataObject)parameter).HasPendingChanges)
//                    {
//                        return true;
//                    }
//                }
//                else if (parameter is IModelMultiContainer)
//                {
//                    IList<Object> multiData = ((IModelMultiContainer)parameter).ValuesData;
//                    foreach (Object data in multiData)
//                    {
//                        if (data != null && data is AmbethIDataObject)
//                        {
//                            AmbethIDataObject aio = (AmbethIDataObject)data;
//                            if (aio.HasPendingChanges)
//                            {
//                                return true;
//                            }
//                        }
//                    }
//                }
//                else if (parameter is IModelSingleContainer)
//                {
//                    PropertyInfo valueProperty = parameter.GetType().GetProperty("Value");
//                    Object val = valueProperty.GetValue(parameter, null);
//                    if (val != null && val is AmbethIDataObject)
//                    {
//                        if (((AmbethIDataObject)val).HasPendingChanges)
//                        {
//                            return true;
//                        }
//                    }
//                }
//                return (parameter != null);
//            }

//            return false;
//        }

//        // Undo all not persisted changes:
//        public virtual void Execute(Object parameter)
//        {
//            Execute(parameter, null, 0);
//        }

//        public virtual void Execute(Object parameter, INextCommandDelegate commandFinishedCallback, long processSequenceId)
//        {
//            if (parameter == null)
//            {
//                return;
//            }
//            IList<Object> values = new List<Object>();
//            if (parameter is IModelContainerRegistry)
//            {
//                IList<Object> modelContainers = ((IModelContainerRegistry)parameter).GetModelContainers();
//                foreach (Object modelContainer in modelContainers)
//                {
//                    Object value = modelContainer;
//                    if (value is INotPersistedDataContainer)
//                    {
//                        INotPersistedDataContainer container = (INotPersistedDataContainer)value;
//                        if (!container.HasNotPersisted())
//                        {
//                            continue;
//                        }
//                        value = container.GetNotPersistedDataRaw();
//                    }
//                    else if (value is IModelMultiContainer)
//                    {
//                        value = ((IModelMultiContainer)value).ValuesData;
//                    }
//                    else if (value is IModelSingleContainer)
//                    {
//                        value = ((IModelSingleContainer)value).ValueData;
//                    }
//                    if (value == null)
//                    {
//                        continue;
//                    }
//                    values.Add(value);
//                }
//            }
//            else
//            {
//                Object value = parameter;
//                if (parameter is IModelMultiContainer)
//                {
//                    value = ((IModelMultiContainer)value).ValuesData;
//                }
//                else if (parameter is IModelSingleContainer)
//                {
//                    value = ((IModelSingleContainer)value).ValueData;
//                }
//                values.Add(value);
//            }
//            if (commandFinishedCallback != null)
//            {
//                ChangeHelper.RevertChangesFinishedCallback = new RevertChangesFinishedCallback(delegate(bool success)
//                {
//                    commandFinishedCallback.Invoke(processSequenceId, success);
//                });
//            }
//            ChangeHelper.RevertChanges(values);
//        }
//    }
//}
