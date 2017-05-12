using System;
using System.Collections;
using System.Collections.Generic;
using System.ComponentModel;
using System.Threading;
using System.Windows.Input;
using De.Osthus.Ambeth.Collections;
using De.Osthus.Ambeth.Ioc;
using De.Osthus.Ambeth.Log;
using De.Osthus.Ambeth.Threading;
using De.Osthus.Ambeth.Util;
using De.Osthus.Minerva.Core;
using AmbethIDataObject = De.Osthus.Ambeth.Model.IDataObject;
using De.Osthus.Ambeth.Ioc.Annotation;

namespace De.Osthus.Minerva.Command
{
    public abstract class AbstractModelContainerRelatedCommand : IInitializingBean, ICommand
    {
        [LogInstance]
		public ILogger Log { private get; set; }

        private static readonly Object[] EMPTY_BO_ARRAY = new Object[0];

        public virtual bool AlwaysExecutable { get; set; }

        public virtual event EventHandler CanExecuteChanged;

        [Autowired]
        public IGuiThreadHelper GuiThreadHelper { protected get; set; }
        
        public AbstractModelContainerRelatedCommand()
        {
            AlwaysExecutable = false;
        }

        public virtual void AfterPropertiesSet()
        {
            // Intended blank
        }

        public void OnCanExecuteChanged()
        {
            if (CanExecuteChanged != null)
            {
                CanExecuteChanged.Invoke(this, EventArgs.Empty);
            }
        }

        public virtual void OnNotPersistedChanged(Object sender, PropertyChangedEventArgs e)
        {
            GuiThreadHelper.InvokeInGui(delegate()
            {
                OnCanExecuteChanged();
            });
        }

        // CanExecute determines whether a control bound to the command is enabled or disabled.
        // There is no MultiBinding in Silverlight (a workaround could be "http://www.scottlogic.co.uk/blog/colin/2009/06/silverlight-multibindings-how-to-attached-mutiple-bindings-to-a-single-property/")
        // => Thus, multiple viewmodels could be passed in a Multicontainer
        //    => In that case T must be INotPersistedDataContainer, hence parameter would be of type IModelMultiContainer<INotPersistedDataContainer>
        public virtual bool CanExecute(Object parameter)
        {
            if (AlwaysExecutable)
            {
                return true;
            }
            return CanExecuteIntern(parameter);
        }

        protected virtual bool CanExecuteIntern(Object parameter, bool extractAlsoUnchanged = false)
        {
            if (parameter == null)
            {
                return false;
            }
            else if (parameter is INotPersistedDataContainer)
            {
                INotPersistedDataContainer container = (INotPersistedDataContainer)parameter;
                return container.HasNotPersisted();
            }
            else if (parameter is AmbethIDataObject)
            {
                if (extractAlsoUnchanged)
                {
                    return true;
                }
                AmbethIDataObject aio = (AmbethIDataObject)parameter;
                return aio.HasPendingChanges;
            }
            else if (parameter is IModelContainerRegistry)
            {
                IList<Object> modelContainers = ((IModelContainerRegistry)parameter).GetModelContainers();
                foreach (Object modelContainer in modelContainers)
                {
                    if (CanExecuteIntern(modelContainer, extractAlsoUnchanged))
                    {
                        return true;
                    }
                }
                return false;
            }
            else if (parameter is IModelMultiContainer<INotPersistedDataContainer>)
            {
                IModelMultiContainer<INotPersistedDataContainer> mmc = (IModelMultiContainer<INotPersistedDataContainer>)parameter;
                foreach (INotPersistedDataContainer npdc in mmc.Values)
                {
                    if (CanExecuteIntern(npdc, extractAlsoUnchanged))
                    {
                        return true;
                    }
                }
                return false;
            }
            else if (parameter is IModelMultiContainer)
            {
                IEnumerable multiData = ((IModelMultiContainer)parameter).ValuesData;
                foreach (Object data in multiData)
                {
                    if (CanExecuteIntern(data, extractAlsoUnchanged))
                    {
                        return true;
                    }
                }
                return false;
            }
            else if (parameter is IModelSingleContainer)
            {
                Object data = ((IModelSingleContainer)parameter).ValueData;
                return CanExecuteIntern(data, extractAlsoUnchanged);
            }
            return (parameter != null);
        }

        public abstract void Execute(Object parameter);

        protected virtual IList<Object> ExtractBusinessObjects(Object parameter, bool extractAlsoUnchanged = false)
        {
            if (parameter == null)
            {
                return EMPTY_BO_ARRAY;
            }
            List<Object> targetObjects = new List<Object>();
            IdentityHashSet<Object> alreadyScannedSet = new IdentityHashSet<Object>();
            ExtractBusinessObjectsIntern(parameter, targetObjects, alreadyScannedSet, extractAlsoUnchanged);
            return targetObjects;
        }

        protected virtual void ExtractBusinessObjectsIntern(Object parameter, IList<Object> targetObjects, ISet<Object> alreadyScannedSet, bool extractAlsoUnchanged)
        {
            if (parameter == null || !alreadyScannedSet.Add(parameter))
            {
                // Nothing to do
                return;
            }

            if (parameter is INotPersistedDataContainer)
            {
                IList<Object> content = ((INotPersistedDataContainer)parameter).GetNotPersistedDataRaw();
                ExtractBusinessObjectsIntern(content, targetObjects, alreadyScannedSet, extractAlsoUnchanged);
            }
            else if (parameter is AmbethIDataObject)
            {
                if (extractAlsoUnchanged || ((AmbethIDataObject)parameter).HasPendingChanges)
                {
                    targetObjects.Add(parameter);
                }
            }
            else if (parameter is IModelContainerRegistry)
            {
                IList<Object> modelContainers = ((IModelContainerRegistry)parameter).GetModelContainers();
                foreach (Object modelContainer in modelContainers)
                {
                    ExtractBusinessObjectsIntern(modelContainer, targetObjects, alreadyScannedSet, extractAlsoUnchanged);
                }
            }
            else if (parameter is IModelMultiContainer<INotPersistedDataContainer>)
            {
                IModelMultiContainer<INotPersistedDataContainer> mmc = (IModelMultiContainer<INotPersistedDataContainer>)parameter;
                foreach (INotPersistedDataContainer npdc in mmc.Values)
                {
                    ExtractBusinessObjectsIntern(npdc, targetObjects, alreadyScannedSet, extractAlsoUnchanged);
                }
            }
            else if (parameter is IModelMultiContainer)
            {
                IEnumerable multiData = ((IModelMultiContainer)parameter).ValuesData;
                foreach (Object data in multiData)
                {
                    ExtractBusinessObjectsIntern(data, targetObjects, alreadyScannedSet, extractAlsoUnchanged);
                }
            }
            else if (parameter is IModelSingleContainer)
            {
                Object data = ((IModelSingleContainer)parameter).ValueData;
                ExtractBusinessObjectsIntern(data, targetObjects, alreadyScannedSet, extractAlsoUnchanged);
            }
            else if (parameter is IEnumerable)
            {
                foreach (Object item in (IEnumerable)parameter)
                {
                    ExtractBusinessObjectsIntern(item, targetObjects, alreadyScannedSet, extractAlsoUnchanged);
                }
            }
            else
            {
                targetObjects.Add(parameter);
            }
        }
    }
}
