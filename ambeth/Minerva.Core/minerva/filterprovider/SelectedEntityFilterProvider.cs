using System;
using System.ComponentModel;
using System.Reflection;
using De.Osthus.Ambeth.Filter.Model;
using De.Osthus.Ambeth.Ioc;
using De.Osthus.Ambeth.Util;
using De.Osthus.Minerva.Core;
using System.Collections.Generic;

namespace De.Osthus.Minerva.FilterProvider
{
    public class SelectedEntityFilterProvider<T> : IFilterDescriptorProvider, IInitializingBean
    {
        protected IFilterDescriptor nothingSelectedFilter;

        protected FilterDescriptor selectedFilter;

        protected T lastSelected = default(T);

        protected Type entityType = typeof(T);

        protected PropertyInfo propertyInfo;

        public virtual event PropertyChangedEventHandler PropertyChanged;

        public virtual String FilterMember { get; set; }

        public virtual String TargetMember { get; set; }

        protected IFilterDescriptor ambethFilterDescriptor;
        public virtual IFilterDescriptor AmbethFilterDescriptor
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

        public virtual void AfterPropertiesSet()
        {
            ParamChecker.AssertNotNull(FilterMember, "FilterMember");
            ParamChecker.AssertNotNull(TargetMember, "TargetMember");

            FilterDescriptor filter = new FilterDescriptor();
            filter.Member = TargetMember;
            filter.Value = new List<String>(1);
            filter.Value.Add(String.Empty);
            filter.Operator = FilterOperator.IS_EQUAL_TO;
            nothingSelectedFilter = filter;

            selectedFilter = new FilterDescriptor();
            selectedFilter.Member = TargetMember;
            selectedFilter.Operator = FilterOperator.IS_EQUAL_TO;
            selectedFilter.Value = new List<String>(1);
            selectedFilter.Value.Add(String.Empty);

            propertyInfo = entityType.GetProperty(FilterMember);
        }

        protected virtual void OnPropertyChanged(String propertyName)
        {
            if (PropertyChanged != null)
            {
                PropertyChanged.Invoke(this, new PropertyChangedEventArgs(propertyName));
            }
        }
        
        public virtual void SelectedEntityChanged(Object sender, PropertyChangedEventArgs e)
        {
            IModelSingleContainer<T> container = (IModelSingleContainer<T>)sender;

            Object containerValue = container.Value;

            if (Object.ReferenceEquals(containerValue, lastSelected))
            {
                return;
            }

            if (containerValue != null)
            {
                containerValue = propertyInfo.GetValue(containerValue, null);
            }

            if (containerValue != null)
            {
                if (selectedFilter.Value == null)
                {
                    selectedFilter.Value = new List<String>();
                }
                selectedFilter.Value.Add(Convert.ToString(containerValue));
                AmbethFilterDescriptor = selectedFilter;
            }
            else
            {
                if (selectedFilter.Value == null)
                {
                    selectedFilter.Value = new List<String>();
                }
                selectedFilter.Value.Add(String.Empty);
                AmbethFilterDescriptor = nothingSelectedFilter;
            }
        }
    }
}
