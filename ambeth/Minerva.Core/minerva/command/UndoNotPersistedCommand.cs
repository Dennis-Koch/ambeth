using System;
using De.Osthus.Ambeth.Log;
using De.Osthus.Ambeth.Merge;
using De.Osthus.Ambeth.Util;
using System.Collections.Generic;

namespace De.Osthus.Minerva.Command
{
    public class UndoNotPersistedCommand : AbstractModelContainerRelatedCommand, IAsyncCommand
    {
        [LogInstance]
		public new ILogger Log { private get; set; }

        public IRevertChangesHelper RevertChangesHelper { protected get; set; }

        public override void AfterPropertiesSet()
        {
            base.AfterPropertiesSet();
            ParamChecker.AssertNotNull(RevertChangesHelper, "ChangeHelper");
        }

        public override bool CanExecute(object parameter)
        {
            bool result = base.CanExecute(parameter);
            return result;
        }

        // Undo all not persisted changes:
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
            IList<Object> businessObjects = ExtractBusinessObjects(parameter);
            
            if (commandFinishedCallback == null)
            {
                RevertChangesHelper.RevertChanges(businessObjects);
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
            RevertChangesHelper.RevertChanges(businessObjects, revertChangesFinishedCallback);
        }
    }
}
