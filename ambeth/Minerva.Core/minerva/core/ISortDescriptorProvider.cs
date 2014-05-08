using System.Collections.Generic;
using System.ComponentModel;
using AmbethISortDescriptor = De.Osthus.Ambeth.Filter.Model.ISortDescriptor;

namespace De.Osthus.Minerva.Core
{
    public interface ISortDescriptorProvider
    {
        IList<AmbethISortDescriptor> SortDescriptorList { get; }
        
        void DisplayCurrentSortStates();
    }
}
