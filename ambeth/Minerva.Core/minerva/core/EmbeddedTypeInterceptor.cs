//using System;
//using De.Osthus.Ambeth.Cache;
//using De.Osthus.Ambeth.Ioc;
//using De.Osthus.Ambeth.Proxy;
//using De.Osthus.Ambeth.Util;
//#if SILVERLIGHT
//using Castle.Core.Interceptor;
//#else
//using Castle.DynamicProxy;
//#endif
//using System.Reflection;
//using System.ComponentModel;
//using System.Text.RegularExpressions;
//using De.Osthus.Ambeth.Typeinfo;
//using De.Osthus.Ambeth.Merge;
//using De.Osthus.Ambeth.Merge.Model;
//using System.Collections.Generic;
//using De.Osthus.Ambeth.Collections;
//using De.Osthus.Minerva.Core.Config;
//using De.Osthus.Ambeth.Config;
//using De.Osthus.Ambeth.Annotation;
//using De.Osthus.Ambeth.Model;
//using De.Osthus.Ambeth.Threading;
//using System.Collections;

//namespace De.Osthus.Minerva.Core
//{
//    public class EmbeddedTypeInterceptor : AbstractClientEntityInterceptor
//    {
//        protected WeakReference parentProxyR;

//        public EmbeddedTypeInterceptor(Object parentProxy, IDictionary<String, ClientEntityFactory.GetterItem> setterToGetterMethodDict,
//            IDictionary<String, ClientEntityFactory.GetterItem> getterToSetterMethodDict, ICacheModification cacheModification) : base(setterToGetterMethodDict, getterToSetterMethodDict, cacheModification)
//        {
//            this.parentProxyR = new WeakReference(parentProxy);
//        }

//        protected override void SetToBeUpdated(bool value)
//        {
//            Object parentProxy = parentProxyR.Target;
//            if (parentProxy is IDataObject)
//            {
//                ((IDataObject)parentProxy).ToBeUpdated = true;
//            }
//        }
//    }
//}
