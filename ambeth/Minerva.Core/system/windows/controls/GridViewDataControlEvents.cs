using System.Reflection;
using De.Osthus.Minerva.Bind;
using Telerik.Windows.Controls;
using Telerik.Windows.Controls.GridView;
using De.Osthus.Ambeth.Ioc.Link;

namespace System.Windows.Controls
{
    public class GridViewDataControlEvents : ContentControlEvents
    {
        static GridViewDataControlEvents()
        {
            EventsUtil.initFields(typeof(GridViewDataControlEvents), delegate(FieldInfo field, Object eventDelegate)
            {
                field.SetValue(null, eventDelegate);
            });
        }

        public static IEventDelegate<EventHandler<GridViewAddingNewEventArgs>> AddingNewDataItem { get; private set; }

        public static IEventDelegate<EventHandler<GridViewCurrentCellChangedEventArgs>> CurrentCellChanged { get; private set; }

        public static IEventDelegate<EventHandler<GridViewFilteringEventArgs>> Filtering { get; private set; }

        public static IEventDelegate<EventHandler<RowLoadedEventArgs>> RowLoaded { get; private set; }

        public static IEventDelegate<EventHandler<GridViewRowEditEndedEventArgs>> RowEditEnded { get; private set; }

        public static IEventDelegate<EventHandler<SelectionChangingEventArgs>> SelectionChanging { get; private set; }

        public static IEventDelegate<EventHandler<SelectionChangeEventArgs>> SelectionChanged { get; private set; }

        public static IEventDelegate<EventHandler<GridViewSortingEventArgs>> Sorting { get; private set; }
    }
}
