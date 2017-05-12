using System;
using System.ComponentModel;
using De.Osthus.Ambeth.Ioc;
using De.Osthus.Ambeth.Util;
using De.Osthus.Minerva.Converter;
using AmbethIFilterDescriptor = De.Osthus.Ambeth.Filter.Model.IFilterDescriptor;
using De.Osthus.Ambeth.Ioc.Extendable;
using De.Osthus.Ambeth.Filter.Model;
using System.Collections.Generic;

namespace De.Osthus.Minerva.FilterProvider
{
    public class MasterFilterDescriptorProvider : IFilterDescriptorProviderExtendable, IFilterDescriptorProvider, IInitializingBean, IDisposableBean
    {
        protected readonly PropertyChangedEventHandler propertyChangedDelegate;

        protected readonly IExtendableContainer<IFilterDescriptorProvider> providerList = new DefaultExtendableContainer<IFilterDescriptorProvider>("FilterDescriptorProvider");

        protected AmbethIFilterDescriptor ambethFilterDescriptor;
        public virtual AmbethIFilterDescriptor AmbethFilterDescriptor
        {
            get
            {
                return ambethFilterDescriptor;
            }
            private set
            {
                ambethFilterDescriptor = value;
                OnPropertyChanged("AmbethFilterDescriptor");
            }
        }

        public virtual event PropertyChangedEventHandler PropertyChanged;

        public virtual IFilterDescriptorConverter FilterDescriptorConverter { get; set; }

        public MasterFilterDescriptorProvider()
        {
            propertyChangedDelegate = new PropertyChangedEventHandler(OnFilterHasChanged);
        }

        public virtual void AfterPropertiesSet()
        {
            ParamChecker.AssertNotNull(FilterDescriptorConverter, "FilterDescriptorConverter");
        }

        // It is possible to have different sources that provide filter criteria.
        // In such cases, one master provider is used for the VMDCC and all other
        // providers should be registered by the master.
        public virtual void RegisterProvider(IFilterDescriptorProvider provider)
        {
            providerList.Register(provider);
            provider.PropertyChanged += propertyChangedDelegate;
            OnFilterHasChanged(null, null);
        }

        public virtual void UnregisterProvider(IFilterDescriptorProvider provider)
        {
            provider.PropertyChanged -= propertyChangedDelegate;
            providerList.Unregister(provider);
            OnFilterHasChanged(null, null);
        }

        protected virtual void OnPropertyChanged(String propertyName)
        {
            if (PropertyChanged != null)
            {
                PropertyChanged.Invoke(this, new PropertyChangedEventArgs(propertyName));
            }
        }

        protected virtual void OnFilterHasChanged(Object sender, PropertyChangedEventArgs e)
        {
            if (e != null && e.PropertyName != "AmbethFilterDescriptor")
            {
                return;
            }
            IFilterDescriptorProvider[] filterList = providerList.GetExtensions();

            if (filterList.Length == 0)
            {
                AmbethFilterDescriptor = null;
                return;
            }

            AmbethIFilterDescriptor filter = CopyFilterDescriptor(filterList[0].AmbethFilterDescriptor);

            for (int i = 1, length = filterList.Length; i < length; i++)
            {
                filter = FilterDescriptorConverter.Compose(filter, CopyFilterDescriptor(filterList[i].AmbethFilterDescriptor));
            }
            AmbethFilterDescriptor = filter;
        }

        protected virtual AmbethIFilterDescriptor CopyFilterDescriptor(AmbethIFilterDescriptor filterToCopy)
        {
            if (filterToCopy == null)
            {
                return null;
            }
            if (filterToCopy is CompositeFilterDescriptor)
            {
                var filter = new CompositeFilterDescriptor();
                if (filterToCopy.ChildFilterDescriptors != null)
                {
                    filter.ChildFilterDescriptors = new List<AmbethIFilterDescriptor>();
                    foreach (var child in filterToCopy.ChildFilterDescriptors)
                    {
                        filter.ChildFilterDescriptors.Add(CopyFilterDescriptor(child));
                    }
                }
                filter.LogicalOperator = filterToCopy.LogicalOperator;
                return filter;
            }
            else
            {
                var filter = new FilterDescriptor();
                filter.IsCaseSensitive = filterToCopy.IsCaseSensitive;
                filter.Member = filterToCopy.Member;
                filter.Operator = filterToCopy.Operator;
                if (filterToCopy.Value != null)
                {
                    filter.Value = new List<String>();
                    foreach (var val in filterToCopy.Value)
                    {
                        filter.Value.Add(val);
                    }
                }
                return filter;
            }
        }

        public virtual void Destroy()
        {
            IFilterDescriptorProvider[] filterList = providerList.GetExtensions();
            for (int i = 0, length = filterList.Length; i < length; i++)
            {
                filterList[i].PropertyChanged -= propertyChangedDelegate;
            }
        }
    }
}
