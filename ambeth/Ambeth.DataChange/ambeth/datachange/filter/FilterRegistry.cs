//using System;
//using System.Net;
//using System.Collections.Generic;
//using De.Osthus.Ambeth.Util;
//using De.Osthus.Ambeth.Log;
//using De.Osthus.Ambeth.Event;
//using De.Osthus.Ambeth.Datachange.Transfer;
//using De.Osthus.Ambeth.Datachange.Model;

//namespace De.Ostus.Ambeth.DataChange.Filter
//{
//    public class FilterRegistry : IFilterExtendable, IEventDispatcher
//    {
//        protected static ILogger logger = LoggerFactory.GetLogger(typeof(FilterRegistry));

//        protected IDictionary<String, IFilter> topicToFilterDict = new Dictionary<String, IFilter>();

//        public IEventDispatcher EventDispatcher { get; set; }

//        public void DispatchEvent(Object eventObject, DateTime dispatchTime, long sequenceId)
//        {
//            if (eventObject is IDataChange)
//            {
//                IDataChange dataChange = (IDataChange)eventObject;
//                lock (topicToFilterDict)
//                using (IEnumerator<KeyValuePair<String, IFilter>> dictIter = topicToFilterDict.GetEnumerator())
//                {
//                    foreach (IDataChangeEntry dataChangeEntry in dataChange.All)
//                    {
//                        if (!(dataChangeEntry is DataChangeEntry))
//                        {
//                            continue;
//                        }
//                        String[] topics = EvaluateMatchingTopics(dataChangeEntry, dictIter);
//                        ((DataChangeEntry)dataChangeEntry).Topics = topics;
//                    }
//                }
//            }
//            EventDispatcher.DispatchEvent(eventObject, dispatchTime, sequenceId);
//        }

//        protected String[] EvaluateMatchingTopics(IDataChangeEntry dataChangeEntry, IEnumerator<KeyValuePair<String, IFilter>> dictIter)
//        {
//            List<String> topics = new List<String>();
//            DictionaryExtension.Loop(dictIter, delegate(String topic, IFilter filter)
//            {
//                try
//                {
//                    if (filter.DoesFilterMatch(dataChangeEntry))
//                    {
//                        topics.Add(topic);
//                    }
//                }
//                catch (Exception e)
//                {
//                    if (logger.ErrorEnabled)
//                    {
//                        logger.Error("Error while handling filter '" + filter + "' on topic '" + topic + "'. Skipping this filter", e);
//                    }
//                }
//            });
//            if (topics.Count == 0)
//            {
//                return null;
//            }
//            return topics.ToArray();
//        }


//        public void RegisterFilter(IFilter filter, String topic)
//        {
//            if (filter == null)
//            {
//                throw new ArgumentException("Argument must not be null", "filter");
//            }
//            if (topic == null)
//            {
//                throw new ArgumentException("Argument must not be null", "topic");
//            }
//            topic = topic.Trim();
//            if (topic.Length == 0)
//            {
//                throw new ArgumentException("Argument must be valid", "topic");
//            }
//            lock (topicToFilterDict)
//            {
//                if (topicToFilterDict.ContainsKey(topic))
//                {
//                    throw new ArgumentException("Given topic already registered with a filter");
//                }
//                topicToFilterDict.Add(topic, filter);
//            }
//        }

//        public void UnregisterFilter(IFilter filter, String topic)
//        {
//            if (filter == null)
//            {
//                throw new ArgumentException("Argument must not be null", "filter");
//            }
//            if (topic == null)
//            {
//                throw new ArgumentException("Argument must not be null", "topic");
//            }
//            topic = topic.Trim();
//            if (topic.Length == 0)
//            {
//                throw new ArgumentException("Argument must be valid", "topic");
//            }
//            lock (topicToFilterDict)
//            {
//                IFilter registeredFilter = DictionaryExtension.ValueOrDefault(topicToFilterDict, topic);
//                if (!Object.ReferenceEquals(registeredFilter, filter))
//                {
//                    throw new ArgumentException("Given topic is registered with another filter. Unregistering illegal");
//                }
//                topicToFilterDict.Remove(topic);
//            }
//        }
//    }
//}
