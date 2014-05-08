using System;
using System.Collections.Specialized;
using System.ComponentModel;
using De.Osthus.Ambeth.Ioc;
using De.Osthus.Ambeth.Util;
using De.Osthus.Minerva.Converter;
using De.Osthus.Minerva.Core;
using Telerik.Windows.Controls;
using AmbethIFilterDescriptor = De.Osthus.Ambeth.Filter.Model.IFilterDescriptor;

namespace De.Osthus.Minerva.FilterProvider
{
    public class RadDataFilterDescriptorProvider : IFilterDescriptorProvider, IInitializingBean
    {
        public virtual event PropertyChangedEventHandler PropertyChanged;

        public virtual IFilterDescriptorConverter FilterDescriptorConverter { get; set; } // Converts Telerik FilterDescriptor types into Ambeth equivalents

        protected AmbethIFilterDescriptor ambethFilterDescriptor;
        public virtual AmbethIFilterDescriptor AmbethFilterDescriptor
        {
            get
            {
                return ambethFilterDescriptor;
            }
            private set
            {
                if (Object.Equals(ambethFilterDescriptor, value))
                {
                    return;
                }
                ambethFilterDescriptor = value;
                OnPropertyChanged("AmbethFilterDescriptor");
            }
        }

        public virtual void AfterPropertiesSet()
        {
            ParamChecker.AssertNotNull(FilterDescriptorConverter, "FilterDescriptorConverter");
        }

        protected virtual void OnPropertyChanged(String propertyName)
        {
            if (PropertyChanged != null)
            {
                PropertyChanged.Invoke(this, new PropertyChangedEventArgs(propertyName));
            }
        }

        public virtual void ControlFiltering(Object sender, NotifyCollectionChangedEventArgs e)
        {
            AmbethFilterDescriptor = FilterDescriptorConverter.ConvertTelerikFilterCollection(((RadDataFilter)sender).FilterDescriptors);
        }
        
        public virtual void Clear()
        {
            AmbethFilterDescriptor = null;
        }
    }
}
