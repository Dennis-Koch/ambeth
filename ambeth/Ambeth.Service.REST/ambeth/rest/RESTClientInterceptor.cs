using System;
using System.Net;

#if SILVERLIGHT
using Castle.Core.Interceptor;
using Ionic.Zlib;
using System.Net.Browser;
#else
using Castle.DynamicProxy;
using System.IO.Compression;
#endif
using De.Osthus.Ambeth.Remote;
using De.Osthus.Ambeth.Ioc;
using De.Osthus.Ambeth.Util;
using De.Osthus.Ambeth.Config;
using De.Osthus.Ambeth.Log;
using System.Threading;
using System.Reflection;
using System.IO;
using De.Osthus.Ambeth.Xml;
using System.Collections;
using System.Collections.Generic;
using System.Text;
using De.Osthus.Ambeth.Service;
using De.Osthus.Ambeth.Crypto;
using De.Osthus.Ambeth.Security.Config;
using De.Osthus.Ambeth.Threading;
using De.Osthus.Ambeth.Security.Transfer;
using De.Osthus.Ambeth.Ioc.Annotation;
using De.Osthus.Ambeth.Proxy;

namespace De.Osthus.Ambeth.Rest
{
    public class RESTClientInterceptor : AbstractSimpleInterceptor, IRemoteInterceptor, IInitializingBean, IOfflineListener
    {
        public const String DEFLATE_MIME_TYPE = "application/octet-stream";

        static RESTClientInterceptor()
        {
            System.Net.ServicePointManager.DefaultConnectionLimit = 10;
        }

        [LogInstance]
        public ILogger Log { private get; set; }

        [Autowired]
        public IAuthenticationHolder AuthenticationHolder { protected get; set; }

        [Autowired]
        public IConversionHelper ConversionHelper { protected get; set; }

        [Autowired(XmlModule.CYCLIC_XML_HANDLER)]
        public ICyclicXmlHandler CyclicXmlHandler { protected get; set; }

        [Autowired]
        public IGuiThreadHelper GuiThreadHelper { protected get; set; }

        [Autowired]
        public IEncryption Encryption { protected get; set; }

        [Property(ServiceConfigurationConstants.ServiceBaseUrl)]
        public String ServiceBaseUrl { protected get; set; }

        [Property(RESTConfigurationConstants.HttpUseClient, DefaultValue = "false")]
        public bool HttpUseClient { protected get; set; }

        [Property(RESTConfigurationConstants.HttpAcceptEncodingZipped, DefaultValue = "true")]
        public bool HttpAcceptEncodingZipped { protected get; set; }

        [Property(RESTConfigurationConstants.HttpContentEncodingZipped, DefaultValue = "true")]
        public bool HttpContentEncodingZipped { protected get; set; }

        public String ServiceName { get; set; }

        protected readonly Object clientLock = new Object();

        protected bool connectionChangePending = false;

        protected readonly HashSet<HttpRequestHeader> headerAlreadyTried = new HashSet<HttpRequestHeader>();

        protected String authorizationValue;

        //public virtual ISecurityScopeProvider SecurityScopeProvider { get; set; }

        public virtual void AfterPropertiesSet()
        {
            ParamChecker.AssertNotNull(ServiceName, "ServiceName");

            //String authInfo = UserName + ":" + Encryption.Encrypt(.password;//TODO Encryption.encrypt(password);
            //authInfo = Convert.ToBase64String(Encoding.UTF8.GetBytes(authInfo));
            //request.Headers[HttpRequestHeader.Authorization] = "Basic " + authInfo;

            //ParamChecker.AssertNotNull(SecurityScopeProvider, "SecurityScopeProvider");
            //bool httpResult = WebRequest.RegisterPrefix("http://", WebRequestCreator.ClientHttp);
            //bool httpsResult = WebRequest.RegisterPrefix("https://", WebRequestCreator.ClientHttp);
        }

        protected override void InterceptIntern(IInvocation invocation)
        {
            if (GuiThreadHelper != null && GuiThreadHelper.IsInGuiThread())
            {
                throw new Exception("It is not allowed to call this interceptor from GUI thread");
            }
            lock (clientLock)
            {
                if (connectionChangePending)
                {
                    // Wait till the connection change finished
                    Monitor.Wait(clientLock);
                }
            }
            DateTime m1 = DateTime.Now;
            MethodInfo method = invocation.Method;
            String url = ServiceBaseUrl + "/" + ServiceName + "/" + method.Name;

            HttpWebRequest webRequest;
#if SILVERLIGHT
            if (HttpUseClient)
            {
                webRequest = (HttpWebRequest)WebRequestCreator.ClientHttp.Create(new Uri(url));
            }
            else
            {
                webRequest = WebRequest.CreateHttp(url);
            }
#else
            webRequest = (HttpWebRequest)WebRequest.Create(url);
#endif
            webRequest.Proxy = null;
            webRequest.KeepAlive = true;
            DateTime m2 = DateTime.Now;
            Object result = null;
            bool hasResult = false;
            Exception ex = null;

            lock (webRequest)
            {
                DateTime m3 = DateTime.Now;
                webRequest.Accept = "text/plain";
                if (HttpAcceptEncodingZipped)
                {
                    TryToSetHeader(HttpRequestHeader.AcceptEncoding, webRequest, "gzip");
                    webRequest.Headers["Accept-Encoding-Workaround"] = "gzip";
                }
                SetAuthorization(webRequest);

                if (invocation.Arguments.Length == 0)
                {
                    webRequest.Method = "GET";
                    webRequest.BeginGetResponse(delegate(IAsyncResult asyncResult2)
                    {
                        DateTime m4 = DateTime.Now;
                        try
                        {
                            HttpWebResponse response = (HttpWebResponse)webRequest.EndGetResponse(asyncResult2);
                            DateTime m5 = DateTime.Now;
                            using (Stream responseStream = response.GetResponseStream())
                            using (Stream memoryStream = new MemoryStream())
                            {
                                int b;
                                while ((b = responseStream.ReadByte()) != -1)
                                {
                                    memoryStream.WriteByte((byte)b);
                                }
                                memoryStream.Position = 0;
                                try
                                {
                                    Stream deflateStream = GetResponseStream(response, memoryStream);
                                    result = CyclicXmlHandler.ReadFromStream(deflateStream);
                                }
                                catch (XmlTypeNotFoundException)
                                {
                                    throw;
                                }
                                catch (Exception)
                                {
                                    memoryStream.Position = 0;
                                    result = CyclicXmlHandler.ReadFromStream(memoryStream);
                                }
                            }
                            hasResult = true;
                            DateTime m6 = DateTime.Now;
                            Log.Warn(url + " 6:" + (m6 - m5).TotalMilliseconds + ",5:" + (m5 - m4).TotalMilliseconds + ",4:" + (m4 - m3).TotalMilliseconds + ",3:" + (m3 - m2).TotalMilliseconds + ",2:" + (m2 - m1).TotalMilliseconds);
                        }
                        catch (WebException e)
                        {
                            ex = ParseWebException(e);
                            using (HttpWebResponse response = (HttpWebResponse)e.Response)
                            {
                                HandleException(e, response);
                            }
                        }
                        catch (Exception e)
                        {
                            Log.Error(e);
                            ex = e;
                        }
                        finally
                        {
                            lock (webRequest)
                            {
                                Monitor.PulseAll(webRequest);
                            }
                        }
                    }, null);
                }
                else
                {
                    webRequest.Method = "POST";
                    webRequest.ContentType = "text/plain";
                    if (HttpContentEncodingZipped)
                    {
                        TryToSetHeader(HttpRequestHeader.ContentEncoding, webRequest, "gzip");
                        webRequest.Headers["Content-Encoding-Workaround"] = "gzip";
                    }
                    DateTime m3_4 = DateTime.Now;
                    webRequest.BeginGetRequestStream(delegate(IAsyncResult asyncResult)
                    {
                        DateTime m3_5 = DateTime.Now;
                        try
                        {
                            DateTime m3_6, m3_7;
                            using (Stream stream = webRequest.EndGetRequestStream(asyncResult))
#if SILVERLIGHT
                            using (Stream deflateStream = HttpContentEncodingZipped ? new GZipStream(stream, CompressionMode.Compress, CompressionLevel.BestCompression, false) : stream)
#else
                            using (Stream deflateStream = HttpContentEncodingZipped ? new GZipStream(stream, CompressionMode.Compress, false) : stream)
#endif
                            {
                                m3_6 = DateTime.Now;
                                CyclicXmlHandler.WriteToStream(deflateStream, invocation.Arguments);
                                m3_7 = DateTime.Now;
                            }
                            webRequest.BeginGetResponse(delegate(IAsyncResult asyncResult2)
                            {
                                DateTime m4 = DateTime.Now;
                                try
                                {
                                    HttpWebResponse response = (HttpWebResponse)webRequest.EndGetResponse(asyncResult2);
                                    DateTime m5 = DateTime.Now;
                                    using (Stream responseStream = response.GetResponseStream())
                                    using (Stream memoryStream = new MemoryStream())
                                    {
                                        int b;
                                        while ((b = responseStream.ReadByte()) != -1)
                                        {
                                            memoryStream.WriteByte((byte)b);
                                        }
                                        memoryStream.Position = 0;
                                        try
                                        {
                                            Stream deflateStream = GetResponseStream(response, memoryStream);
                                            result = CyclicXmlHandler.ReadFromStream(deflateStream);
                                        }
                                        catch (XmlTypeNotFoundException)
                                        {
                                            throw;
                                        }
                                        catch (Exception)
                                        {
                                            memoryStream.Position = 0;
                                            result = CyclicXmlHandler.ReadFromStream(memoryStream);
                                        }
                                    }
                                    hasResult = true;
                                    DateTime m6 = DateTime.Now;
                                    Log.Warn(url + " 7:" + (m3_7 - m3_6).TotalMilliseconds + ",6:" + (m3_6 - m3_5).TotalMilliseconds + ",5:" + (m3_5 - m3_4).TotalMilliseconds + ",4:" + (m3_4 - m3).TotalMilliseconds);
                                }
                                catch (WebException e)
                                {
                                    ex = ParseWebException(e);
                                    using (HttpWebResponse response = (HttpWebResponse)e.Response)
                                    {
                                        HandleException(e, response);
                                    }
                                }
                                catch (Exception e)
                                {
                                    ex = e;
                                }
                                finally
                                {
                                    lock (webRequest)
                                    {
                                        Monitor.PulseAll(webRequest);
                                    }
                                }
                            }, null);
                        }
                        catch (Exception e)
                        {
                            ex = e;
                            lock (webRequest)
                            {
                                Monitor.PulseAll(webRequest);
                            }
                        }
                    }, null);
                }
                while (!hasResult && ex == null)
                {
                    Monitor.Wait(webRequest);
                }
            }
            if (result is AmbethServiceException)
            {
                ex = ParseServiceException((AmbethServiceException)result);
                throw new Exception("Error occured while calling " + webRequest.Method + " " + webRequest.RequestUri, ex);
            }
            if (ex != null)
            {
                if (ex is WebException)
                {
                    throw new Exception(ex.Message + "\r\rError occured while calling " + webRequest.Method + " " + webRequest.RequestUri + ". " + CyclicXmlHandler.Write(invocation.Arguments), ex);
                }
                throw new Exception("Error occured while calling " + webRequest.Method + " " + webRequest.RequestUri + ". " + CyclicXmlHandler.Write(invocation.Arguments), ex);
            }
            if (!hasResult)
            {
                throw new Exception("This must never happen");
            }
            invocation.ReturnValue = ConvertToExpectedType(method.ReturnType, result);
        }

        protected Exception ParseServiceException(AmbethServiceException serviceException)
        {
            AmbethServiceException serviceCause = serviceException.Cause;
            Exception cause = null;
            if (serviceCause != null)
            {
                cause = ParseServiceException(serviceCause);
            }
            return new Exception(serviceException.Message + "\n" + serviceException.StackTrace, cause);
        }

        /// <summary>
        /// Parse the given WebException and create a new ApplicationException with the parsed error message
        /// </summary>
        /// <param name="webException">The WebException</param>
        /// <returns>An ApplicationException (if the WebException could be parsed) or the original WebException</returns>
        protected Exception ParseWebException(WebException webException)
        {
            Exception exception = null;
            HttpWebResponse httpResponse = webException.Response as HttpWebResponse;

            if (httpResponse != null && httpResponse.StatusCode == HttpStatusCode.InternalServerError)
            {
                // handle internal server error (500)
                string failureReason = null;
                using (Stream responseStream = GetResponseStream(httpResponse, httpResponse.GetResponseStream()))
                {
                    if (responseStream != null && responseStream.CanRead)
                    {
                        using (StreamReader reader = new StreamReader(responseStream))
                        {
                            failureReason = reader.ReadToEnd();
                        }
                    }
                }

                if (failureReason != null)
                {
                    exception = new WebException(failureReason, webException);
                }
                else
                {
                    exception = webException;
                }
            }
            else
            {
                // handle other than internal server error (500)
                exception = webException;
            }
            return exception;
        }

        protected void HandleException(Exception e, HttpWebResponse response)
        {
            if (response != null)
            {
                if (response.StatusCode == HttpStatusCode.Unauthorized)
                {
                    throw new UnauthorizedAccessException("The username or password is wrong while calling '" + response.Method + " " + response.ResponseUri.ToString() + "'", e);
                }
                else
                {
                    throw new Exception("The http request returned http error '" + (int)response.StatusCode + "(" + response.StatusCode.ToString() + ") while calling '" + response.Method + " " + response.ResponseUri.ToString() + "'", e);
                }
            }
            else
            {
                throw new Exception("The http request returned an error", e);
            }
        }

        protected void TryToSetHeader(HttpRequestHeader requestHeader, HttpWebRequest request, String value)
        {
            if (headerAlreadyTried.Contains(requestHeader))
            {
                return;
            }
            try
            {
                request.Headers[requestHeader] = value;
            }
            catch (ArgumentException)
            {
                headerAlreadyTried.Add(requestHeader);
            }
        }

        protected Stream GetResponseStream(HttpWebResponse response, Stream responseStream)
        {
            String contentEncoding = response.Headers["Content-Encoding"];
            if ("gzip".Equals(contentEncoding))
            {
                return new GZipStream(responseStream, CompressionMode.Decompress);
            }
            else
            {
                return responseStream;
            }
        }

        protected Object ConvertToExpectedType(Type expectedType, Object result)
        {
            if (typeof(void).Equals(expectedType) || result == null)
            {
                return null;
            }
            else if (expectedType.IsAssignableFrom(result.GetType()))
            {
                return result;
            }
            if (typeof(IEnumerable).IsAssignableFrom(expectedType) && !typeof(String).Equals(expectedType))
            {
                Object targetCollection = ListUtil.CreateCollectionOfType(expectedType);

                MethodInfo addMethod = targetCollection.GetType().GetMethod("Add");
                Type addType = addMethod.GetParameters()[0].ParameterType;
                Object[] parameters = new Object[1];

                if (result is IEnumerable)
                {
                    foreach (Object item in (IEnumerable)result)
                    {
                        Object convertedItem = ConversionHelper.ConvertValueToType(addType, item);
                        parameters[0] = convertedItem;
                        addMethod.Invoke(targetCollection, parameters);
                    }
                }
                return targetCollection;
            }
            else if (expectedType.IsArray)
            {
                List<Object> list = new List<Object>();
                if (result is IEnumerable)
                {
                    foreach (Object item in (IEnumerable)result)
                    {
                        list.Add(item);
                    }
                }

                Array array = Array.CreateInstance(expectedType.GetElementType(), list.Count);
                for (int a = 0, size = list.Count; a < size; a++)
                {
                    array.SetValue(list[a], a);
                }
                return array;
            }
            throw new Exception("Can not convert result " + result + " to expected type " + expectedType.FullName);
        }

        protected virtual void SetAuthorization(HttpWebRequest request)
        {
            String[] authentication = AuthenticationHolder.GetAuthentication();
            String userName = authentication[0];
            String password = authentication[1];
            if (userName == null && password == null)
            {
                return;
            }
            String authInfo = userName + ":" + password; //userName + ":" + Encryption.Encrypt(.password;//TODO Encryption.encrypt(password);
            authInfo = Convert.ToBase64String(Encoding.UTF8.GetBytes(authInfo));
            request.Headers[HttpRequestHeader.Authorization] = "Basic " + authInfo;
        }

        public void BeginOnline()
        {
            BeginOffline();
        }

        public void HandleOnline()
        {
            HandleOffline();
        }

        public void EndOnline()
        {
            EndOffline();
        }

        public void BeginOffline()
        {
            lock (clientLock)
            {
                connectionChangePending = true;
            }
        }

        public void HandleOffline()
        {
            // Intended blank
        }

        public void EndOffline()
        {
            lock (clientLock)
            {
                connectionChangePending = false;
                Monitor.PulseAll(clientLock);
            }
        }
    }
}
