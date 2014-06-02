using De.Osthus.Ambeth.Util;
using System;
using System.Text;

namespace De.Osthus.Ambeth.Event
{
    public class PropertyChangeEvent : IPrintable
    {
	    public Object Source { get; private set; }

        public String PropertyName { get; private set; }

        public Object OldValue { get; private set; }

        public Object NewValue { get; private set; }

        public PropertyChangeEvent(Object source, String propertyName, Object oldValue, Object newValue)
	    {
            this.Source = source;
            this.PropertyName = propertyName;
            this.OldValue = oldValue;
            this.NewValue = newValue;
	    }

        public override String ToString()
        {
            StringBuilder sb = new StringBuilder();
            ToString(sb);
            return sb.ToString();
        }

        public void ToString(StringBuilder sb)
        {
            sb.Append("[propertyName=").Append(PropertyName);
            AppendTo(sb);
            sb.Append("; oldValue=").Append(OldValue);
            sb.Append("; newValue=").Append(NewValue);
            sb.Append("; source=").Append(Source);
        }

        protected virtual void AppendTo(StringBuilder sb)
        {
            // Intended blank
        }
    }
}
