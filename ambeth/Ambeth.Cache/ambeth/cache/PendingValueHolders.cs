//using System;
//using De.Osthus.Ambeth.Util;
//using System.Collections.Generic;
//using System.ComponentModel;
//using System.Threading;

//namespace De.Osthus.Ambeth.Cache
//{
//    public class PendingValueHolders
//    {
//        public class PendingValueHolderItem
//        {
//            public IValueHolder ValueHolder { get; protected set; }

//            public PropertyChangedEventHandler EventHandler { get; protected set; }

//            public PendingValueHolderItem(IValueHolder valueHolder, PropertyChangedEventHandler eventHandler)
//            {
//                this.ValueHolder = valueHolder;
//                this.EventHandler = eventHandler;
//            }
//        }

//        protected static readonly ThreadLocal<IList<PendingValueHolderItem>> pendingValueHoldersTL = new ThreadLocal<IList<PendingValueHolderItem>>();

//        public static IList<PendingValueHolderItem> GetAndClearPendingInterceptors()
//        {
//            IList<PendingValueHolderItem> pendingValueHolders = pendingValueHoldersTL.Value;
//            if (pendingValueHolders != null)
//            {
//                pendingValueHoldersTL.Value = null;
//            }
//            return pendingValueHolders;
//        }

//        public static void PushPendingInterceptor(IValueHolder valueHolder, PropertyChangedEventHandler eventHandler)
//        {
//            IList<PendingValueHolderItem> pendingValueHolders = pendingValueHoldersTL.Value;
//            if (pendingValueHolders == null)
//            {
//                pendingValueHolders = new List<PendingValueHolderItem>();
//                pendingValueHoldersTL.Value = pendingValueHolders;
//            }
//            pendingValueHolders.Add(new PendingValueHolderItem(valueHolder, eventHandler));
//        }
//    }
//}
