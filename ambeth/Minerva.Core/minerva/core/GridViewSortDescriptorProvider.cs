using System;
using System.Collections.Generic;
using System.ComponentModel;
using De.Osthus.Ambeth.Ioc;
using De.Osthus.Ambeth.Util;
using De.Osthus.Minerva.Converter;
using Telerik.Windows.Controls;
using Telerik.Windows.Controls.GridView;
using Telerik.Windows.Data;
using AmbethISortDescriptor = De.Osthus.Ambeth.Filter.Model.ISortDescriptor;
using AmbethSortDescriptor = De.Osthus.Ambeth.Filter.Model.SortDescriptor;
using System.Windows;
using De.Osthus.Ambeth.Collections;

namespace De.Osthus.Minerva.Core
{
    public class GridViewSortDescriptorProvider : ISortDescriptorProvider, IInitializingBean, INotifyPropertyChanged
    {
        public virtual event PropertyChangedEventHandler PropertyChanged;

        public virtual GridViewDataControl GridViewDataControl { get; set; }

        public virtual ISortDescriptorConverter SortDescriptorConverter { get; set; }

        protected IList<AmbethISortDescriptor> sortDescriptorList;
        public IList<AmbethISortDescriptor> SortDescriptorList
        {
            get
            {
                return sortDescriptorList;
            }
            private set
            {
                if (Object.ReferenceEquals(sortDescriptorList, value))
                {
                    return;
                }
                sortDescriptorList = value;
                DisplayCurrentSortStates();
                OnPropertyChanged("SortDescriptorList");
            }
        }

        public void AfterPropertiesSet()
        {
            ParamChecker.AssertNotNull(GridViewDataControl, "GridViewDataControl");
            ParamChecker.AssertNotNull(SortDescriptorConverter, "SortDescriptorConverter");

            SortDescriptorList = new List<AmbethISortDescriptor>();
        }

        protected virtual void OnPropertyChanged(String propertyName)
        {
            if (PropertyChanged != null)
            {
                PropertyChanged.Invoke(this, new PropertyChangedEventArgs(propertyName));
            }
        }

        public virtual void ControlSorting(Object sender, GridViewSortingEventArgs e)
        {
            // Sorting happens on server-side, hence the the own sorting of the UIControl must be
            // canceled and therefore, the SortingStates must be switched manually here.
            if (e.OldSortingState == SortingState.None)
            {
                e.NewSortingState = SortingState.Ascending;
            }
            else if (e.OldSortingState == SortingState.Ascending)
            {
                e.NewSortingState = SortingState.Descending;
            }
            else if (e.OldSortingState == SortingState.Descending)
            {
                e.NewSortingState = SortingState.None;
            }

            List<ISortDescriptor> sortDescriptorList = new List<ISortDescriptor>();
            bool modifiedOrRemoved = false;
            // Not only the EventArgs, but all SortDescriptors set on the Control must be evaluated.
            // (E.g. in a RadGridView, the user can specify multiple descriptors by holding the shift key)
            // As the Controls own sorting is canceled, the descriptors are not directly available from the
            // sender and must be tracked manually.
            if (!e.IsMultipleColumnSorting)
            {
                if (SortDescriptorList.Count > 0)
                {
                    SortDescriptorList.Clear();
                    modifiedOrRemoved = true;
                }
            }

            // The MemberName of the property to sort is currently still available as SortPropertyName in the
            // EventArgs, but it is marked as obsolet and thus removed in future versions. Therefore the PropertyName
            // will be evaluated by casting the the Column to a GridViewBoundColumnBase and calling its
            // GetDataMemberName() method.
            String sortPropertyName = ((GridViewBoundColumnBase)e.Column).GetDataMemberName();
            AmbethISortDescriptor ambethDescriptor = SortDescriptorConverter.ConvertTelerikSortingState(e.NewSortingState, sortPropertyName);

            foreach (AmbethSortDescriptor sortDescriptor in SortDescriptorList)
            {
                if (sortDescriptor.Member != sortPropertyName)
                {
                    continue;
                }
                if (ambethDescriptor != null)
                {
                    sortDescriptor.SortDirection = ambethDescriptor.SortDirection;
                    ambethDescriptor = null;
                }
                else
                {
                    SortDescriptorList.Remove(sortDescriptor); // OK to remove directly, because break is following
                }
                modifiedOrRemoved = true;
                break;
            }

            if (ambethDescriptor != null)
            {
                SortDescriptorList.Add(ambethDescriptor);
                modifiedOrRemoved = true;
            }

            if (modifiedOrRemoved)
            {
                OnPropertyChanged("SortDescriptorList");
            }

            // As sorting is canceled for the control, the status of all other descriptors must be reset manually:
            //DisplayCurrentSortStates();

            e.Cancel = true; // Prevent the UIControl from own sorting
        }

        public virtual void DisplayCurrentSortStates()
        {
            GridViewColumnCollection columns = GridViewDataControl.Columns;
            ISet<GridViewBoundColumnBase> unmatchedColumns = new IdentityHashSet<GridViewBoundColumnBase>();
            foreach (GridViewBoundColumnBase column in columns)
            {
                unmatchedColumns.Add(column);
            }

            foreach (AmbethSortDescriptor sortDescriptor in SortDescriptorList)
            {
                foreach (GridViewBoundColumnBase column in columns)
                {
                    if (column.GetDataMemberName() == sortDescriptor.Member)
                    {
                        column.SortingState = SortDescriptorConverter.GetTelerikSortingState(sortDescriptor);
                        unmatchedColumns.Remove(column);
                        break;
                    }
                }
            }
            foreach (GridViewBoundColumnBase unmatchedColumn in unmatchedColumns)
            {
                unmatchedColumn.SortingState = SortingState.None;
            }
        }

        public virtual void Clear()
        {
            SortDescriptorList = new List<AmbethISortDescriptor>();
        }
    }
}
