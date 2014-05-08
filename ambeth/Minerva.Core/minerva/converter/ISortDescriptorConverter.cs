using System;
#if WPF
using Telerik.Windows.Controls;
using TelerikISortDescriptor = Telerik.Windows.Data.ISortDescriptor;
using TelerikSortingState = Telerik.Windows.Controls.SortingState;
#else
#if SILVERLIGHT
using Telerik.Windows.Controls;
using TelerikISortDescriptor = Telerik.Windows.Data.ISortDescriptor;
using TelerikSortingState = Telerik.Windows.Controls.SortingState;
#else
using TelerikISortDescriptor = Telerik.WinControls.Data.SortDescriptor;
using TelerikSortingState = System.ComponentModel.ListSortDirection;
#endif
#endif
using AmbethISortDescriptor = De.Osthus.Ambeth.Filter.Model.ISortDescriptor;

namespace De.Osthus.Minerva.Converter
{
    public interface ISortDescriptorConverter
    {
        AmbethISortDescriptor ConvertTelerikSortDescriptor(TelerikISortDescriptor sortDescriptor, String memberName);

        AmbethISortDescriptor ConvertTelerikSortingState(TelerikSortingState state, String memberName);

        TelerikSortingState GetTelerikSortingState(AmbethISortDescriptor ambethDescriptor);
    }
}
