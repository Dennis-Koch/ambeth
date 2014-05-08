using De.Osthus.Ambeth.Annotation;
using De.Osthus.Ambeth.Cache.Model;
using De.Osthus.Ambeth.Model;

namespace De.Osthus.Ambeth.Service
{
    [XmlType]
    public interface ICacheService : ICacheRetriever
    {
        IServiceResult GetORIsForServiceRequest(IServiceDescription serviceDescription);
    }
}
