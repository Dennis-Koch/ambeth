using System;
using System.Collections.Generic;
using De.Osthus.Ambeth.Log;
using De.Osthus.Ambeth.Merge;
using De.Osthus.Ambeth.Util;

namespace De.Osthus.Minerva.Command
{
    public class SaveNotPersistedDataCommand : AbstractModelContainerRelatedCommand, IAsyncCommand
    {
        [LogInstance]
        public new ILogger Log { private get; set; }

        public ICUDResultPreprocessor CUDResultPreprocessor { protected get; set; }

        public IMergeProcess MergeProcess { protected get; set; }

        public override void AfterPropertiesSet()
        {
            base.AfterPropertiesSet();
            ParamChecker.AssertNotNull(MergeProcess, "MergeProcess");
        }

        // Persist all Changes:
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
            if (businessObjects.Count < 1)
            {
                if (commandFinishedCallback != null)
                {
                    commandFinishedCallback.Invoke(true);
                }
                return;
            }
            ProceedWithMergeHook proceedWithMergeHook = null;
            if (CUDResultPreprocessor != null)
            {
                proceedWithMergeHook = CUDResultPreprocessor.GetProceedWithMergeHook();
            }
            MergeFinishedCallback mergeFinishedCallback = new MergeFinishedCallback(delegate(bool success)
            {
                bool result = success;
                if (proceedWithMergeHook != null)
                {
                    if (CUDResultPreprocessor.GetPreprocessSuccess(proceedWithMergeHook) != true)
                    {
                        result = false;
                    }
                    CUDResultPreprocessor.CleanUp(proceedWithMergeHook);
                }
                if (commandFinishedCallback != null)
                {
                    commandFinishedCallback.Invoke(result);
                }
            });
            MergeProcess.Process(businessObjects, null, proceedWithMergeHook, mergeFinishedCallback);
        }
    }
}