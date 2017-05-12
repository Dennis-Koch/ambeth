using System;
using System.Windows;
using De.Osthus.Ambeth.Config;
using System.Reflection;
using System.Windows.Input;
using De.Osthus.Minerva.Bind;
using De.Osthus.Ambeth.Ioc.Link;

namespace System.Windows
{
    public class UIElementEvents
    {
        static UIElementEvents()
        {
            EventsUtil.initFields(typeof(UIElementEvents), delegate(FieldInfo field, Object eventDelegate)
            {
                field.SetValue(null, eventDelegate);
            });
        }

        public static IEventDelegate<DragEventHandler> DragEnter { get; private set; }

        public static IEventDelegate<DragEventHandler> DragLeave { get; private set; }

        public static IEventDelegate<DragEventHandler> DragOver { get; private set; }

        public static IEventDelegate<DragEventHandler> Drop { get; private set; }
        
        public static IEventDelegate<RoutedEventHandler> GotFocus { get; private set; }
        
        public static IEventDelegate<KeyEventHandler> KeyDown { get; private set; }
        
        public static IEventDelegate<KeyEventHandler> KeyUp { get; private set; }
        
        public static IEventDelegate<RoutedEventHandler> LostFocus  { get; private set; }
        
        public static IEventDelegate<MouseEventHandler> LostMouseCapture { get; private set; }
        
        //public static IEventDelegate<> ManipulationCompleted { get; private set; }
        
        //public static IEventDelegate<> ManipulationDelta { get; private set; }
        
        //public static IEventDelegate<> ManipulationStarted { get; private set; }
        
        public static IEventDelegate<MouseEventHandler> MouseEnter { get; private set; }
        
        public static IEventDelegate<MouseEventHandler> MouseLeave { get; private set; }
        
        public static IEventDelegate<MouseButtonEventHandler> MouseLeftButtonDown { get; private set; }
        
        public static IEventDelegate<MouseButtonEventHandler> MouseLeftButtonUp { get; private set; }
        
        public static IEventDelegate<MouseEventHandler> MouseMove { get; private set; }
        
        public static IEventDelegate<MouseButtonEventHandler> MouseRightButtonDown { get; private set; }
        
        public static IEventDelegate<MouseButtonEventHandler> MouseRightButtonUp { get; private set; }
        
        public static IEventDelegate<MouseWheelEventHandler> MouseWheel { get; private set; }
        
        public static IEventDelegate<TextCompositionEventHandler> TextInput { get; private set; }
        
        public static IEventDelegate<TextCompositionEventHandler> TextInputStart { get; private set; }

        public static IEventDelegate<TextCompositionEventHandler> TextInputUpdate { get; private set; }
    }
}
