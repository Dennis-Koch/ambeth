using System;
using System.ComponentModel;
using De.Osthus.Ambeth.Ioc;
using Telerik.Windows.Controls;
using AmbethISortDescriptor = De.Osthus.Ambeth.Filter.Model.ISortDescriptor;
using AmbethSortDescriptor = De.Osthus.Ambeth.Filter.Model.SortDescriptor;
using AmbethSortDirection = De.Osthus.Ambeth.Filter.Model.SortDirection;
using TelerikISortDescriptor = Telerik.Windows.Data.ISortDescriptor;

namespace De.Osthus.Minerva.Converter
{
    public class SortDescriptorConverter : ISortDescriptorConverter, IInitializingBean
    {
        public virtual void AfterPropertiesSet()
        {
            // Intended blank
        }

        // RadGridView-sort descriptors use ListSortDescriptor
        public virtual AmbethISortDescriptor ConvertTelerikSortDescriptor(TelerikISortDescriptor sortDescriptor, String memberName)
        {
            AmbethSortDescriptor result = new AmbethSortDescriptor();
            result.Member = memberName;
            if (sortDescriptor.SortDirection == ListSortDirection.Ascending)
            {
                result.SortDirection = AmbethSortDirection.ASCENDING;
            }
            else
            {
                result.SortDirection = AmbethSortDirection.DESCENDING;
            }
            return result;
        }

        // RadGridView-columns use SortingState-enum:
        public virtual AmbethISortDescriptor ConvertTelerikSortingState(SortingState state, String memberName)
        {
            if (state == SortingState.None)
            {
                return null;
            }
            AmbethSortDescriptor result = new AmbethSortDescriptor();
            result.Member = memberName;
            if (state == SortingState.Ascending)
            {
                result.SortDirection = AmbethSortDirection.ASCENDING;
            }
            else if (state == SortingState.Descending)
            {
                result.SortDirection = AmbethSortDirection.DESCENDING;
            }
            return result;
        }

        // Used to set the sorting state of RadGridView columns:
        public virtual SortingState GetTelerikSortingState(AmbethISortDescriptor ambethDescriptor)
        {
            if (ambethDescriptor.SortDirection == AmbethSortDirection.ASCENDING)
            {
                return SortingState.Ascending;
            }
            return SortingState.Descending;
        }
    }
}
