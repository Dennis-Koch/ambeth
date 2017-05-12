using System;
using System.Collections.Generic;
using De.Osthus.Ambeth.Typeinfo;
using System.Collections;

namespace De.Osthus.Ambeth.Util
{
    /**
     * All output values are UTC!
     */
    public class DateTimeUtil
    {
        public const String DateTimeAsUTC = "ambeth.datetime.utc";

        protected static readonly DateTime UTCBaseTime = new DateTime(1970, 1, 1, 0, 0, 0, 0, new System.Globalization.GregorianCalendar(), System.DateTimeKind.Utc);

        public static long CurrentTimeMillis()
        {
            return ConvertDateTimeToJavaMillis(DateTime.UtcNow);
        }

        public static long ConvertDateTimeToJavaMillis(DateTime dateTime)
        {
            return (dateTime.ToUniversalTime().Ticks - UTCBaseTime.Ticks) / TimeSpan.TicksPerMillisecond;
        }

        public static DateTime ConvertJavaMillisToDateTime(long javaMillis)
        {
            return UTCBaseTime.AddMilliseconds(javaMillis);
        }
    }
}
