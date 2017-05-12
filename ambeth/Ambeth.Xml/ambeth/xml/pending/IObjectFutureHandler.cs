using System.Collections.Generic;

namespace De.Osthus.Ambeth.Xml.Pending
{
    public interface IObjectFutureHandler
    {
        void Handle(IList<IObjectFuture> objectFutures);
    }
}
