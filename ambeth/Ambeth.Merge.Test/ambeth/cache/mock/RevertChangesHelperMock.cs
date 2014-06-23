using De.Osthus.Ambeth.Merge;
using System;

namespace De.Osthus.Ambeth.Cache.Mock
{
    /**
     * Support for unit tests that do not include jAmbeth.Cache
     */
    public class RevertChangesHelperMock : IRevertChangesHelper
    {
        public IRevertChangesSavepoint CreateSavepoint(Object source)
        {
            return null;
        }

        public void RevertChanges(Object objectsToRevert)
        {
            
        }

        public void RevertChanges(Object objectsToRevert, bool recursive)
        {
            
        }

        public void RevertChanges(Object objectsToRevert, RevertChangesFinishedCallback revertChangesFinishedCallback)
        {
            
        }

        public void RevertChanges(Object objectsToRevert, RevertChangesFinishedCallback revertChangesFinishedCallback, bool recursive)
        {
            
        }

        public void RevertChangesGlobally(Object objectsToRevert)
        {
            
        }

        public void RevertChangesGlobally(Object objectsToRevert, bool recursive)
        {
            
        }

        public void RevertChangesGlobally(Object objectsToRevert, RevertChangesFinishedCallback revertChangesFinishedCallback)
        {
            
        }

        public void RevertChangesGlobally(Object objectsToRevert, RevertChangesFinishedCallback revertChangesFinishedCallback, bool recursive)
        {
            
        }
    }
}