using System;
using System.Collections.Generic;
using System.ServiceModel;
using De.Osthus.Ambeth.Merge.Model;
using De.Osthus.Ambeth.Cache.Model;
using De.Osthus.Ambeth.Model;
using De.Osthus.Ambeth.Annotation;

namespace De.Osthus.Ambeth.Service
{
    [XmlType]
    public interface ICacheRetriever
    {
        IList<ILoadContainer> GetEntities(IList<IObjRef> orisToLoad);

        IList<IObjRelationResult> GetRelations(IList<IObjRelation> objRelations);
    }
}
