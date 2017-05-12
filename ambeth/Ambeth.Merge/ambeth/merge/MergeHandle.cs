using System;
using System.Collections.Generic;
using De.Osthus.Ambeth.Cache;
using De.Osthus.Ambeth.Collections;
using De.Osthus.Ambeth.Config;
using De.Osthus.Ambeth.Ioc;
using De.Osthus.Ambeth.Merge.Config;
using De.Osthus.Ambeth.Merge.Model;
using De.Osthus.Ambeth.Threading;

namespace De.Osthus.Ambeth.Merge
{
    public class MergeHandle : IInitializingBean
    {
        public IList<IObjRef> oldOriList = new List<IObjRef>();

        public IList<IObjRef> newOriList = new List<IObjRef>();

        public IdentityHashSet<Object> alreadyProcessedSet = new IdentityHashSet<Object>();

        public IdentityDictionary<Object, IObjRef> objToOriDict = new IdentityDictionary<Object, IObjRef>();

        public Dictionary<IObjRef, Object> oriToObjDict = new Dictionary<IObjRef, Object>();

        public IdentityLinkedMap<Object, IList<IUpdateItem>> objToModDict = new IdentityLinkedMap<Object, IList<IUpdateItem>>();

        public IdentityHashSet<Object> objToDeleteSet = new IdentityHashSet<Object>();

        public ICache Cache { get; set; }

        [Property(MergeConfigurationConstants.FieldBasedMergeActive, DefaultValue = "true")]
        public bool FieldBasedMergeActive { get; set; }

        public bool HandleExistingIdAsNewId { get; set; }

        protected IList<Object> pendingValueHolders = new List<Object>();

        protected IList<IBackgroundWorkerDelegate> pendingRunnables = new List<IBackgroundWorkerDelegate>();

        public IList<Object> PendingValueHolders
        {
            get
            {
                return pendingValueHolders;
            }
        }

        public IList<IBackgroundWorkerDelegate> PendingRunnables
        {
            get
            {
                return pendingRunnables;
            }
        }

        public bool IsDeepMerge { get; set; }

        public MergeHandle()
        {
            IsDeepMerge = true;
        }

        public virtual void AfterPropertiesSet()
        {
            // Intended blank
        }
    }
}
