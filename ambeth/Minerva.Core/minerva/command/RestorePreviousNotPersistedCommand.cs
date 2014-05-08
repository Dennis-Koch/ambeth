using System;
using De.Osthus.Ambeth.Log;
using De.Osthus.Ambeth.Merge;
using De.Osthus.Ambeth.Util;
using System.Collections.Generic;
using De.Osthus.Ambeth.Collections;

namespace De.Osthus.Minerva.Command
{
    public class RestorePreviousNotPersistedCommand : UndoNotPersistedCommand
    {
        [LogInstance]
        public ILogger log;

        public virtual IRevertChangesSavepoint RevertChangesSavepoint { get; set; }

        public override void Execute(Object parameter, INextCommandDelegate commandFinishedCallback)
        {
            if (RevertChangesSavepoint == null)
            {
                base.Execute(parameter, commandFinishedCallback);
                return;
            }
            if (parameter == null)
            {
                if (commandFinishedCallback != null)
                {
                    commandFinishedCallback.Invoke(true);
                }
                return;
            }
            IList<Object> businessObjects = ExtractBusinessObjects(parameter);

            RevertChangesFinishedCallback revertChangesFinishedCallback = null;
            if (commandFinishedCallback != null)
            {
                revertChangesFinishedCallback = new RevertChangesFinishedCallback(delegate(bool success)
                    {
                        commandFinishedCallback.Invoke(success);
                    });
            }

            IdentityHashSet<Object> mentionedBusinessObjects = new IdentityHashSet<Object>(businessObjects);
            IdentityHashSet<Object> savepointBusinessObjects = new IdentityHashSet<Object>(RevertChangesSavepoint.GetSavedBusinessObjects());
            mentionedBusinessObjects.RemoveAll(savepointBusinessObjects);

            RevertChangesSavepoint.RevertChanges();
            RevertChangesHelper.RevertChanges(mentionedBusinessObjects, revertChangesFinishedCallback);
        }
    }
}
