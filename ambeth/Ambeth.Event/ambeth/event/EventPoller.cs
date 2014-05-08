using System;
using System.Collections.Generic;
using System.Threading;
using De.Osthus.Ambeth.Config;
using De.Osthus.Ambeth.Event.Config;
using De.Osthus.Ambeth.Event.Model;
using De.Osthus.Ambeth.Ioc;
using De.Osthus.Ambeth.Ioc.Annotation;
using De.Osthus.Ambeth.Log;
using De.Osthus.Ambeth.Service;
using De.Osthus.Ambeth.Util;

namespace De.Osthus.Ambeth.Event
{
    public class EventPoller : IEventPoller, IOfflineListener, IStartingBean, IDisposableBean
    {
        [LogInstance]
        public ILogger Log { private get; set; }

        [Autowired]
        public IEventDispatcher EventDispatcher { protected get; set; }

        [Autowired]
        public IEventService EventService { protected get; set; }

        [Property(EventConfigurationConstants.PollingSleepInterval, DefaultValue = "500")]
        public TimeSpan PollSleepInterval { protected get; set; }

        [Property(EventConfigurationConstants.MaxWaitInterval, DefaultValue = "30000")]
        public TimeSpan MaxWaitInterval { protected get; set; }

        [Property(EventConfigurationConstants.StartPausedActive, DefaultValue = "false")]        
        public bool StartPaused { protected get; set; }

        protected readonly Object writeLock = new Object();

        protected volatile bool stopRequested = false;
        
        protected volatile bool pauseRequested = false;

        protected int iterationId = 1;

        public virtual void AfterStarted()
        {
            if (StartPaused)
            {
                PausePolling();
            }
            StartPolling();
        }

        public virtual void Destroy()
        {
            StopPolling();
        }

        public void StopPolling()
        {
            lock (writeLock)
            {
                stopRequested = true;
                pauseRequested = false;
                iterationId++;
                Monitor.PulseAll(writeLock);
            }
        }

        public void StartPolling()
        {
            int stackIterationId = iterationId;
            Thread thread = new Thread(new ThreadStart(delegate()
            {
                try
                {
                    long currentServerSession = EventService.GetCurrentServerSession();
                    long currentEventSequence = EventService.GetCurrentEventSequence();
                    while (!stopRequested && stackIterationId == iterationId)
                    {
                        lock (writeLock)
                        {
                            while (pauseRequested && !stopRequested)
                            {
                                Monitor.Wait(writeLock);
                                if (stopRequested)
                                {
                                    break;
                                }
                            }
                        }
                        bool errorOccured;
                        currentEventSequence = TryPolling(currentServerSession, currentEventSequence, out errorOccured);
                        if (errorOccured)
                        {
                            Thread.Sleep(TimeSpan.FromMilliseconds(Math.Max(5000, PollSleepInterval.TotalMilliseconds)));
                        }
                        else
                        {
                            Thread.Sleep(PollSleepInterval);
                        }
                    }
                }
                catch (Exception e)
                {
                    if (Log.ErrorEnabled)
                    {
                        Log.Error(e);
                    }
                }
            }));
            thread.Name = "Event Polling";
            thread.IsBackground = true;
            thread.Start();
        }

        protected long TryPolling(long currentServerSession, long currentEventSequence, out bool errorOccured)
        {
            errorOccured = true;
            IList<IEventItem> events = null;
            try
            {
                events = EventService.PollEvents(currentServerSession, currentEventSequence, MaxWaitInterval);
                errorOccured = false;
            }
            catch (Exception e)
            {
                if (Log.ErrorEnabled)
                {
                    Log.Error(e);
                }
            }
            if (events == null || events.Count == 0)
            {
                return currentEventSequence;
            }            
            long timeBeforeDispatch = DateTimeUtil.CurrentTimeMillis();
            EventDispatcher.EnableEventQueue();
            try
            {
                for (int a = 0, size = events.Count; a < size; a++)
                {
                    IEventItem eventObject = events[a];
                    EventDispatcher.DispatchEvent(eventObject.EventObject, eventObject.DispatchTime, eventObject.SequenceNumber);
                    currentEventSequence = eventObject.SequenceNumber;
                }
            }
            finally
            {
                EventDispatcher.FlushEventQueue();
            }
            if (Log.InfoEnabled)
            {
                long timeAfterDispatch = DateTimeUtil.CurrentTimeMillis();
                Log.Info("Dispatching " + events.Count + " event(s) took " + (timeAfterDispatch - timeBeforeDispatch) + " ms.");
            }
            return currentEventSequence;
        }

        public void PausePolling()
        {
            lock (writeLock)
            {
                if (pauseRequested)
                {
                    return;
                }
                if (Log.InfoEnabled)
                {
                    Log.Info("Polling activated, but paused for concurrency reasons till " + typeof(IEventPoller).Name + ".ResumePolling() is called");
                }
                pauseRequested = true;
                Monitor.PulseAll(writeLock);
            }
        }

        public void ResumePolling()
        {
            lock (writeLock)
            {
                if (!pauseRequested)
                {
                    return;
                }
                if (Log.InfoEnabled)
                {
                    Log.Info("Polling resumed");
                }
                pauseRequested = false;
                Monitor.PulseAll(writeLock);
            }
        }

        public void BeginOnline()
        {
            StopPolling();
        }

        public void HandleOnline()
        {
            // Intended blank
        }

        public void EndOnline()
        {
            StartPolling();
        }

        public void BeginOffline()
        {
            StopPolling();
        }

        public void HandleOffline()
        {
            // Intended blank
        }

        public void EndOffline()
        {
            StartPolling();
        }
    }
}