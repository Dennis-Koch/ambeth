using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Windows.Input;
using De.Osthus.Ambeth.Ioc;
using De.Osthus.Ambeth.Log;
using De.Osthus.Minerva.Core;
using AmbethIDataObject = De.Osthus.Ambeth.Model.IDataObject;
using De.Osthus.Ambeth.Merge;
using De.Osthus.Ambeth.Util;
using System.Collections;


namespace De.Osthus.Minerva.Command
{
    public class AssignToBeDeletedCommand : AbstractModelContainerRelatedCommand, IAsyncCommand
    {
        [LogInstance]
		public new ILogger Log { private get; set; }

        public virtual IRevertChangesHelper ChangeHelper { get; set; }

        public virtual bool DeleteToBeCreatedDirectly { get; set; }

        public AssignToBeDeletedCommand()
        {
            DeleteToBeCreatedDirectly = true;
            AlwaysExecutable = false;
        }

        public override void AfterPropertiesSet()
        {
            base.AfterPropertiesSet();
            ParamChecker.AssertNotNull(ChangeHelper, "ChangeHelper");
        }

        public virtual void OnSelectedItemsChanged(Object sender, PropertyChangedEventArgs e)
        {
            OnCanExecuteChanged();
        }
        
        public override bool CanExecute(Object parameter)
        {
            if (AlwaysExecutable)
            {
                return true;
            }
            return CanExecuteIntern(parameter, true);
        }

        public override void Execute(Object parameter)
        {
            Execute(parameter, null);
        }

        public virtual void Execute(Object parameter, INextCommandDelegate commandFinishedCallback)
        {
            if (parameter == null)
            {
                if (commandFinishedCallback != null)
                {
                    commandFinishedCallback.Invoke(true);
                }
                return;
            }
            IList<Object> businessObjects = ExtractBusinessObjects(parameter, true);
            IList<Object> directDeletes = new List<Object>();
            foreach (Object obj in businessObjects)
            {
                if (obj is AmbethIDataObject)
                {
                    AmbethIDataObject aido = (AmbethIDataObject)obj;
                    if (DeleteToBeCreatedDirectly && aido.ToBeCreated)
                    {
                        directDeletes.Add(aido);
                        continue;
                    }
                    aido.ToBeDeleted = true;
                }
            }
            
            if (commandFinishedCallback == null)
            {
                ChangeHelper.RevertChanges(directDeletes);
                return;
            }

            RevertChangesFinishedCallback revertChangesFinishedCallback = null;
            if (commandFinishedCallback != null)
            {
                revertChangesFinishedCallback = new RevertChangesFinishedCallback(delegate(bool success)
                {
                    commandFinishedCallback.Invoke(success);
                });
            }
            ChangeHelper.RevertChanges(directDeletes, revertChangesFinishedCallback);
        }
    }
}
