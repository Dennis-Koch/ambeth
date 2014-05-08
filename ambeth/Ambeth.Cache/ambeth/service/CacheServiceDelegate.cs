//using System;
//using De.Osthus.Ambeth.Ioc;
//using De.Osthus.Ambeth.Util;
//using De.Osthus.Ambeth.Cache.Model;
//using System.Collections.Generic;
//using De.Osthus.Ambeth.Merge.Model;
//using De.Osthus.Ambeth.Model;
//using De.Osthus.Ambeth.Merge.Transfer;
//using De.Osthus.Ambeth.Cache.Transfer;
//using De.Osthus.Ambeth.Transfer;

//namespace De.Osthus.Ambeth.Service
//{
//    public class CacheServiceDelegate : ICacheService, IInitializingBean
//    {
//        public virtual ICacheServiceWCF CacheServiceWCF { get; set; }

//        public virtual void AfterPropertiesSet()
//        {
//            ParamChecker.AssertNotNull(CacheServiceWCF, "CacheServiceWCF");
//        }

//        public virtual IList<ILoadContainer> GetEntities(IList<IObjRef> orisToLoad)
//        {
//            ObjRef[] paramWCF = new ObjRef[orisToLoad.Count];
//            for (int a = orisToLoad.Count; a-- > 0;)
//            {
//                paramWCF[a] = (ObjRef)orisToLoad[a];
//            }
//            LoadContainer[] resultWCF = CacheServiceWCF.GetEntities(paramWCF);
//            List<ILoadContainer> result = new List<ILoadContainer>(resultWCF.Length);
//            for (int a = 0, size = resultWCF.Length; a < size; a++)
//            {
//                result.Add(resultWCF[a]);
//            }
//            return result;
//        }

//        public virtual IList<IObjRelationResult> GetRelations(IList<IObjRelation> objRelations)
//        {
//            ObjRelation[] paramWCF = new ObjRelation[objRelations.Count];
//            for (int a = objRelations.Count; a-- > 0; )
//            {
//                paramWCF[a] = (ObjRelation)objRelations[a];
//            }
//            ObjRelationResult[] resultWCF = CacheServiceWCF.GetRelations(paramWCF);
//            List<IObjRelationResult> result = new List<IObjRelationResult>(resultWCF.Length);
//            for (int a = 0, size = resultWCF.Length; a < size; a++)
//            {
//                result.Add(resultWCF[a]);
//            }
//            return result;
//        }
        
//        public virtual IList<IObjRef> GetORIsForServiceRequest(IServiceDescription rootServiceContext)
//        {
//            ObjRef[] resultWCF = CacheServiceWCF.GetORIsForServiceRequest((ServiceDescription)rootServiceContext);
//            List<IObjRef> result = new List<IObjRef>(resultWCF.Length);
//            for (int a = 0, size = resultWCF.Length; a < size; a++)
//            {
//                result.Add(resultWCF[a]);
//            }
//            return result;
//        }

//    }
//}
