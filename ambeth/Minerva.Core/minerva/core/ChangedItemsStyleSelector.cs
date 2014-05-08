using System;
using De.Osthus.Ambeth.Ioc;
#if WPF
using System.Windows.Input;
using Telerik.Windows.Controls;
using Telerik.Windows.Controls.GridView;
using StyleSelector = Telerik.Windows.Controls.StyleSelector;
#else
#if SILVERLIGHT
using Telerik.Windows.Controls;
using Telerik.Windows.Controls.GridView;
#else
using System.Windows.Media;
#endif
#endif
using AmbethIDataObject = De.Osthus.Ambeth.Model.IDataObject;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Media;

namespace De.Osthus.Minerva.Core
{
    // This bean can be used as StyleSelector for IDataObjects (returns a default style for items that are
    // not IDataObjects). If unconfigured, the default values for all relevant properties are:
    //  -> TargetType               : GridViewRow
    //  -> TargetProperty           : Control.ForegroundProperty
    //  -> UnchangedColor           : Colors.Black
    //  -> ToBeCreatedColor         : Colors.Green
    //  -> ToBeUpdatedColor         : Colors.Red
    //  -> ToBeDeletedColor         : Colors.LightGray
    //  -> Unchanged/ToBe... Styles : Created from the set TargetProperty and corresponding colors
    // It may be wise to put these defaults into a separate configuration file in future versions.
    public class ChangedItemsStyleSelector : StyleSelector, IInitializingBean
    {
        protected Type targetType = typeof(GridViewRow);
        public Type TargetType
        {
            get
            {
                return targetType;
            }
            set
            {
                targetType = value;
            }
        }

        protected DependencyProperty targetProperty = Control.ForegroundProperty;
        public DependencyProperty TargetProperty
        {
            get
            {
                return targetProperty;
            }
            set
            {
                targetProperty = value;
            }
        }

        protected Color unchangedColor = Colors.Black;
        public Color UnchangedColor
        {
            get
            {
                return unchangedColor;
            }
            set
            {
                unchangedColor = value;
            }
        }

        protected Color toBeCreatedColor = Colors.Green;
        public Color ToBeCreatedColor
        {
            get
            {
                return toBeCreatedColor;
            }
            set
            {
                toBeCreatedColor = value;
            }
        }

        protected Color toBeUpdatedColor = Colors.Red;
        public Color ToBeUpdatedColor
        {
            get
            {
                return toBeUpdatedColor;
            }
            set
            {
                toBeUpdatedColor = value;
            }
        }

        protected Color toBeDeletedColor = Colors.LightGray;
        public Color ToBeDeletedColor
        {
            get
            {
                return toBeDeletedColor;
            }
            set
            {
                toBeDeletedColor = value;
            }
        }
        
        public Style UnchangedStyle { get; set; }
        public Style ToBeCreatedStyle { get; set; }
        public Style ToBeUpdatedStyle { get; set; }
        public Style ToBeDeletedStyle { get; set; }

        public void AfterPropertiesSet()
        {
            if (UnchangedStyle == null)
            {
                UnchangedStyle = new Style(TargetType);
                Brush newBrush = new SolidColorBrush(UnchangedColor);
                Setter newSetter = new Setter(TargetProperty, newBrush);
                UnchangedStyle.Setters.Add(newSetter);
            }

            if (ToBeCreatedStyle == null)
            {
                ToBeCreatedStyle = new Style(TargetType);
                Brush newBrush = new SolidColorBrush(ToBeCreatedColor);
                Setter newSetter = new Setter(TargetProperty, newBrush);
                ToBeCreatedStyle.Setters.Add(newSetter);
            }

            if (ToBeUpdatedStyle == null)
            {
                ToBeUpdatedStyle = new Style(TargetType);
                Brush newBrush = new SolidColorBrush(ToBeUpdatedColor);
                Setter newSetter = new Setter(TargetProperty, newBrush);
                ToBeUpdatedStyle.Setters.Add(newSetter);
            }

            if (ToBeDeletedStyle == null)
            {
                ToBeDeletedStyle = new Style(TargetType);
                Brush newBrush = new SolidColorBrush(ToBeDeletedColor);
                Setter newSetter = new Setter(TargetProperty, newBrush);
                ToBeDeletedStyle.Setters.Add(newSetter);
            }
        }

        public override Style SelectStyle(object item, DependencyObject container)
        {
            if (!(item is AmbethIDataObject))
            {
                return UnchangedStyle;
            }
            AmbethIDataObject aido = (AmbethIDataObject)item;
            if (aido.ToBeDeleted)
            {
                return ToBeDeletedStyle;
            }
            else if (aido.ToBeCreated)
            {
                return ToBeCreatedStyle;
            }
            else if (aido.ToBeUpdated)
            {
                return ToBeUpdatedStyle;
            }
            return UnchangedStyle;
        }
    }
}
