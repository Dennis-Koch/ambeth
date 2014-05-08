using System;
using System.Net;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Documents;
using System.Windows.Ink;
using System.Windows.Input;
using System.Windows.Media;
using System.Windows.Media.Animation;
using System.Windows.Shapes;
using System.Reflection;
using De.Osthus.Minerva.Extendable;
using De.Osthus.Ambeth.Ioc;
using System.Collections.Generic;
using De.Osthus.Ambeth.Util;

namespace De.Osthus.Minerva.Core
{
    /// <summary>
    /// Used to get the generated token before data exchange.
    /// IDataProvider is used to return the Token before transmission. The application has to decide how to pass this token.
    /// This is in contrast to the receiving side, which uses IDataProvider to receive the token for the data lookup after transmission.
    /// In the latter case, a sample implementation of the gette can extract a token out of an URL.
    /// Usage:
    /// before transmission: use the getter to receive a generated token.
    ///     
    /// after transmission: implement the getter to receive the generated token again.
    ///     The Getter is called once by "DataConsumer" in the "AfterStarted"-Stage.
    ///     After this the Token must not be changed anymore.
    /// </summary>
    public interface IDataProvider
    {
        String Token { get; }
    }
}
