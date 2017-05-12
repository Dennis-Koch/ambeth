using System.ComponentModel;
using AmbethIFilterDescriptor = De.Osthus.Ambeth.Filter.Model.IFilterDescriptor;

namespace De.Osthus.Minerva.FilterProvider
{
    // Providers must ALWAYS implement INotifyPropertyChanged
    public interface IFilterDescriptorProvider : INotifyPropertyChanged
    {
        AmbethIFilterDescriptor AmbethFilterDescriptor { get; }
    }
}
