using System;
using System.Collections.Generic;
#if WPF
using TelerikIFilterDescriptor = Telerik.Windows.Data.IFilterDescriptor;
#else
#if SILVERLIGHT
using TelerikIFilterDescriptor = Telerik.Windows.Data.IFilterDescriptor;
#else
using TelerikIFilterDescriptor = Telerik.WinControls.Data.FilterDescriptor;
#endif
#endif
using AmbethIFilterDescriptor = De.Osthus.Ambeth.Filter.Model.IFilterDescriptor;
using AmbethLogicalOperator = De.Osthus.Ambeth.Filter.Model.LogicalOperator;


namespace De.Osthus.Minerva.Converter
{
    public interface IFilterDescriptorConverter
    {
        AmbethIFilterDescriptor AddTelerikFilterDescriptor(AmbethIFilterDescriptor ambethFilter, TelerikIFilterDescriptor telerikFilterDescriptor, AmbethLogicalOperator logicalOperator = AmbethLogicalOperator.AND);

        AmbethIFilterDescriptor ConvertTelerikFilterCollection(IList<TelerikIFilterDescriptor> filterCollection, AmbethLogicalOperator logicalOperator = AmbethLogicalOperator.AND);

        AmbethIFilterDescriptor ConvertTelerikFilterDescriptor(TelerikIFilterDescriptor telerikFilterDescriptor);

        AmbethIFilterDescriptor RemoveTelerikFilterDescriptor(AmbethIFilterDescriptor ambethFilter, TelerikIFilterDescriptor telerikFilterDescriptor);

        AmbethIFilterDescriptor Compose(AmbethIFilterDescriptor filter1, AmbethIFilterDescriptor filter2, AmbethLogicalOperator logicalOperator = AmbethLogicalOperator.AND);

#if DEBUG
        String DebugVisualize(AmbethIFilterDescriptor filter, String prefix, String logicalOperator);
#endif
    }
}
