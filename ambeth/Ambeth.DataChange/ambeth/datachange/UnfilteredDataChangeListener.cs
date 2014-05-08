using System.Collections.Generic;
using System.Collections;
using De.Osthus.Ambeth.Event;
using System;
using De.Osthus.Ambeth.Datachange.Transfer;
using De.Osthus.Ambeth.Datachange.Model;
using De.Osthus.Ambeth.Ioc;
using De.Osthus.Ambeth.Util;

namespace De.Osthus.Ambeth.Datachange
{
    public class UnfilteredDataChangeListener : IEventListener, IDataChangeListener, IInitializingBean, IDisposable
    {
        public static IDataChangeListener Create(IDataChangeListener dataChangeListener)
        {
            UnfilteredDataChangeListener dcListener = new UnfilteredDataChangeListener();
            dcListener.DataChangeListener = dataChangeListener;
            return dcListener;
        }

        public static IEventListener CreateEventListener(IDataChangeListener dataChangeListener)
        {
            UnfilteredDataChangeListener dcListener = new UnfilteredDataChangeListener();
            dcListener.DataChangeListener = dataChangeListener;
            return dcListener;
        }

        public virtual IDataChangeListener DataChangeListener { protected get; set; }

        public virtual void AfterPropertiesSet()
        {
            ParamChecker.AssertNotNull(DataChangeListener, "DataChangeListener");
        }

        public virtual void Dispose()
        {
            DataChangeListener = null;
        }

        public virtual void HandleEvent(Object eventObject, DateTime dispatchTime, long sequenceId)
        {
            if (!(eventObject is IDataChange))
            {
                return;
            }
            DataChanged((IDataChange)eventObject, dispatchTime, sequenceId);
        }

        public virtual void DataChanged(IDataChange dataChange, DateTime dispatchTime, long sequenceId)
        {
            if (dataChange.IsEmpty)
            {
                return;
            }
            DataChangeListener.DataChanged(dataChange, dispatchTime, sequenceId);
        }
    }
}
