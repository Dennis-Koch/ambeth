using System.Collections.Generic;
using System;

namespace De.Osthus.Ambeth.Merge
{
    public interface IRevertChangesHelper
    {
        IRevertChangesSavepoint CreateSavepoint(Object source);

        void RevertChanges(Object objectsToRevert);

        void RevertChanges(Object objectsToRevert, bool recursive);

        void RevertChanges(Object objectsToRevert, RevertChangesFinishedCallback revertChangesFinishedCallback);

        void RevertChanges(Object objectsToRevert, RevertChangesFinishedCallback revertChangesFinishedCallback, bool recursive);

        void RevertChangesGlobally(Object objectsToRevert);

        void RevertChangesGlobally(Object objectsToRevert, bool recursive);

        void RevertChangesGlobally(Object objectsToRevert, RevertChangesFinishedCallback revertChangesFinishedCallback);

        void RevertChangesGlobally(Object objectsToRevert, RevertChangesFinishedCallback revertChangesFinishedCallback, bool recursive);
    }
}
