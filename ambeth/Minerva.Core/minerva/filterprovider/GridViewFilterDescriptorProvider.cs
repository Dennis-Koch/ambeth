﻿using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Linq;
using De.Osthus.Ambeth.Ioc;
using De.Osthus.Ambeth.Log;
using De.Osthus.Ambeth.Util;
using De.Osthus.Minerva.Converter;
#if WPF
using Telerik.Windows.Controls.GridView;
using Telerik.Windows.Data;
using TelerikFieldFilterDescriptor = Telerik.Windows.Controls.GridView.FieldFilterDescriptor;
using TelerikIFilterDescriptor = Telerik.Windows.Data.IFilterDescriptor;
#else
#if SILVERLIGHT
using Telerik.Windows.Controls.GridView;
using Telerik.Windows.Data;
using TelerikFieldFilterDescriptor = Telerik.Windows.Controls.GridView.FieldFilterDescriptor;
using TelerikIFilterDescriptor = Telerik.Windows.Data.IFilterDescriptor;
#else
using Telerik.WinControls.Data;
#endif
#endif
using AmbethIFilterDescriptor = De.Osthus.Ambeth.Filter.Model.IFilterDescriptor;

namespace De.Osthus.Minerva.FilterProvider
{
    public class GridViewFilterDescriptorProvider : IFilterDescriptorProvider, IInitializingBean
    {
        [LogInstance]
		public ILogger Log { private get; set; }

        protected readonly PropertyChangedEventHandler fieldFilterHasChangedDelegate;

        public virtual event PropertyChangedEventHandler PropertyChanged;

        public virtual IFilterDescriptorConverter FilterDescriptorConverter { get; set; } // Converts Telerik FilterDescriptor types into Ambeth equivalents

        public virtual HashSet<TelerikFieldFilterDescriptor> ObservedFieldFilters { get; set; } // To check whether a FieldFilter is still observed

        public virtual FilterDescriptorCollection UIControlFilterList { get; set; }

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
#if DEBUG
                if (AmbethFilterDescriptor != null)
                {
                    Log.Info("Set new FilterDescriptor:\n" + this.FilterDescriptorConverter.DebugVisualize(AmbethFilterDescriptor, "", "") + "\n\n");
                }
#endif
                OnPropertyChanged("AmbethFilterDescriptor");
            }
        }

        public GridViewFilterDescriptorProvider()
        {
            fieldFilterHasChangedDelegate = new PropertyChangedEventHandler(FieldFilterHasChanged);
        }

        public virtual void AfterPropertiesSet()
        {
            ParamChecker.AssertNotNull(FilterDescriptorConverter, "FilterDescriptorConverter");

            this.ObservedFieldFilters = new HashSet<TelerikFieldFilterDescriptor>();
        }

        protected virtual void OnPropertyChanged(String propertyName)
        {
            if (PropertyChanged != null)
            {
                PropertyChanged.Invoke(this, new PropertyChangedEventArgs(propertyName));
            }
        }

        protected virtual void FieldFilterHasChanged(Object sender, PropertyChangedEventArgs e)
        {
            AmbethFilterDescriptor = FilterDescriptorConverter.ConvertTelerikFilterCollection(UIControlFilterList);
        }

        public virtual void ControlFiltering(Object sender, GridViewFilteringEventArgs e)
        {
            UIControlFilterList = ((GridViewDataControl)sender).FilterDescriptors;

            if (ObservedFieldFilters.Count > UIControlFilterList.Count)
            {
                IList<TelerikFieldFilterDescriptor> toDelete = new List<TelerikFieldFilterDescriptor>();
                foreach (TelerikFieldFilterDescriptor fieldFilter in ObservedFieldFilters)
                {
                    if (!fieldFilter.IsActive)
                    {
                        toDelete.Add(fieldFilter);
                    }
                }
                foreach (TelerikFieldFilterDescriptor fieldFilter in toDelete)
                {
                    if (ObservedFieldFilters.Remove(fieldFilter))
                    {
                        fieldFilter.PropertyChanged -= fieldFilterHasChangedDelegate;
                    }
                }
            }

            // If only the logical operator of a FieldFilter is changed, then no Filtering event is fired
            // => Changes in the FieldFilters must be tracked in addition
            if (e.ColumnFilterDescriptor.IsActive && e.ColumnFilterDescriptor.FieldFilter != null)
            {
                if (!ObservedFieldFilters.Contains(e.ColumnFilterDescriptor.FieldFilter))
                {
                    ObservedFieldFilters.Add(e.ColumnFilterDescriptor.FieldFilter);
                    e.ColumnFilterDescriptor.FieldFilter.PropertyChanged += fieldFilterHasChangedDelegate;
                }
            }

            AmbethIFilterDescriptor tempDescriptor = FilterDescriptorConverter.ConvertTelerikFilterCollection(UIControlFilterList);

            if (e.ColumnFilterDescriptor.IsActive == false && e.Added.Count<TelerikIFilterDescriptor>() > 0)
            {
                AmbethFilterDescriptor = this.FilterDescriptorConverter.AddTelerikFilterDescriptor(tempDescriptor, ((IList<TelerikIFilterDescriptor>)e.Added)[0]);
                return;
            }
            AmbethFilterDescriptor = tempDescriptor;
        }

        public virtual void Clear()
        {
            foreach (TelerikFieldFilterDescriptor fieldFilter in ObservedFieldFilters)
            {
                fieldFilter.PropertyChanged -= fieldFilterHasChangedDelegate;
            }
            ObservedFieldFilters.Clear();
            AmbethFilterDescriptor = null;
        }
    }
}