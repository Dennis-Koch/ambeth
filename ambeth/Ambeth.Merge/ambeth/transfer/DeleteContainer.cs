using System;
using System.Collections.Generic;
using System.Runtime.Serialization;

namespace De.Osthus.Ambeth.Merge.Transfer
{
    [DataContract(Name = "DeleteContainer", Namespace = "http://schemas.osthus.de/Ambeth")]
    public class DeleteContainer : AbstractChangeContainer
    {
        // Intended blank
    }
}
