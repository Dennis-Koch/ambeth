using System;
using System.Reflection;
using De.Osthus.Minerva.Bind;
using De.Osthus.Ambeth.Ioc.Link;

namespace De.Osthus.Minerva.Command
{
    public class CommandEvents
    {
        static CommandEvents()
        {
            EventsUtil.initFields(typeof(CommandEvents), delegate(FieldInfo field, Object eventDelegate)
            {
                field.SetValue(null, eventDelegate);
            });
        }

        public static IEventDelegate<EventHandler> CanExecuteChanged { get; private set; }
    }
}
