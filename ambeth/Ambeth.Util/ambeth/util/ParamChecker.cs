using System;

namespace De.Osthus.Ambeth.Util
{
    public class ParamChecker
    {
        private static bool unitTestMode = false;

        public static void AssertNull(Object value, String name)
        {
            if (value != null)
            {
                if (!unitTestMode) throw new ArgumentException("Property must not be valid here", name);
            }
        }

        /**
         * &Uuml;berpr&uuml;fung ob der entsprechende Value w&auml;hrend der
         * Bean-Initialisierung gesetzt wurde. F&uuml;r die Ausgabe der Fehlermeldung
         * wird hier der übergebene Parameter <code>name</code> ausgegeben.<br>
         * 
         * @param value Das zu &uuml;berpr&uuml;fende Objekt.
         * @param name Der in der Fehlermeldung auszugebende Name.
         */
        public static void AssertNotNull(Object value, String name)
        {
            if (value == null)
            {
                if (!unitTestMode) throw new ArgumentNullException("Property '" + name + "' must be valid");
            }
        }

        public static void AssertNotNull(double value, String name)
        {
            if (value == 0.0)
            {
                if (!unitTestMode) throw new ArgumentNullException("Property '" + name + "' must be valid");
            }
        }

        public static void AssertTrue(bool value, String name)
        {
            if (!value)
            {
                if (!unitTestMode) throw new ArgumentException("Property '" + name + "' must be valid");
            }
        }

        public static void AssertFalse(bool value, String name)
        {
            if (value)
            {
                if (!unitTestMode) throw new ArgumentException("Property '" + name + "' must be valid");
            }
        }

        public static void AssertParamNotNull(Object value, String name)
        {
            if (value == null)
            {
                throw new ArgumentNullException("Property '" + name + "' must be valid");
            }
        }

        public static void AssertParamNotNullOrEmpty(String value, String name)
        {
            if (String.IsNullOrEmpty(value))
            {
                throw new ArgumentNullException("Property '" + name + "' must be valid");
            }
        }

        public static void AssertParamOfType(Object value, String name, Type type)
        {
            ParamChecker.AssertNotNull(value, name);
            if (!type.IsAssignableFrom(value.GetType()))
            {
                if (!unitTestMode) throw new ArgumentException("Parameter must be an instance of " + type.ToString(), name);
            }
        }

        public static void SetUnitTestMode(bool unitTestMode)
        {
            ParamChecker.unitTestMode = unitTestMode;
        }
    }
}
