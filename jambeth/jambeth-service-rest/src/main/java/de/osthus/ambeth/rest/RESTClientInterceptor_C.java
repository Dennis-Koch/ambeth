//package de.osthus.ambeth.rest;
//
//import java.util.concurrent.locks.Lock;
//import java.util.concurrent.locks.ReentrantLock;
//import java.util.concurrent.locks.ReentrantReadWriteLock;
//
//import de.osthus.ambeth.collections.HashSet;
//import de.osthus.ambeth.config.Property;
//import de.osthus.ambeth.config.ServiceConfigurationConstants;
//import de.osthus.ambeth.ioc.IInitializingBean;
//import de.osthus.ambeth.ioc.XmlModule;
//import de.osthus.ambeth.ioc.annotation.Autowired;
//import de.osthus.ambeth.log.LogInstance;
//import de.osthus.ambeth.log.Logger;
//import de.osthus.ambeth.proxy.AbstractSimpleInterceptor;
//import de.osthus.ambeth.remote.IRemoteInterceptor;
//import de.osthus.ambeth.service.IOfflineListener;
//import de.osthus.ambeth.threading.IGuiThreadHelper;
//import de.osthus.ambeth.util.IConversionHelper;
//import de.osthus.ambeth.util.ParamChecker;
//
//    public class RESTClientInterceptor_CS extends AbstractSimpleInterceptor implements IRemoteInterceptor, IInitializingBean, IOfflineListener
//    {
//        public static final String DEFLATE_MIME_TYPE = "application/octet-stream";
//
//        @LogInstance
//        private Logger log;
//
//        @Autowired
//        public IAuthenticationHolder authenticationHolder;
//
//        @Autowired
//        public IConversionHelper ConversionHelper;
//
//        @Autowired(XmlModule.CYCLIC_XML_HANDLER)
//        public ICyclicXmlHandler CyclicXmlHandler;
//
//        @Autowired
//        public IGuiThreadHelper GuiThreadHelper;
//
//        @Autowired
//        public IEncryption Encryption;
//
//        @Property(name=ServiceConfigurationConstants.ServiceBaseUrl)
//        public String ServiceBaseUrl;
//
//        @Property(RESTConfigurationConstants.HttpUseClient, DefaultValue = false)
//        public boolean HttpUseClient;
//
//        @Property(RESTConfigurationConstants.HttpAcceptEncodingZipped, DefaultValue = true)
//        public boolean HttpAcceptEncodingZipped;
//
//        @Property(RESTConfigurationConstants.HttpContentEncodingZipped, DefaultValue = "true")
//        public boolean HttpContentEncodingZipped;
//
//        private String serviceName;
//
//        protected final Lock clientLock = new ReentrantLock();
//
//        protected boolean connectionChangePending = false;
//
////        protected final HashSet<HttpRequestHeader> headerAlreadyTried = new HashSet<HttpRequestHeader>();
//
//        protected String authorizationValue;
//
//
//        public void AfterPropertiesSet()
//        {
//            ParamChecker.assertNotNull(serviceName, "ServiceName");
//        }
//
//        protected override void InterceptIntern(IInvocation invocation)
//        {
//            if (GuiThreadHelper != null && GuiThreadHelper.IsInGuiThread())
//            {
//                throw new Exception("It is not allowed to call this interceptor from GUI thread");
//            }
//            clientLock.lock();
//            try
//            {
//                if (connectionChangePending)
//                {
//                    // Wait till the connection change finished
//                	clientLock.wait();
////                    Monitor.Wait(clientLock);
//                }
//            }
//            finally 
//            {
//            	clientLock.unlock();
//            }
//            MethodInfo method = invocation.Method;
//            String url = ServiceBaseUrl + "/" + ServiceName + "/" + method.Name;
//
//            Lock webRequestLock = new ReentrantLock();
//            HttpWebRequest webRequest;
//            webRequest = (HttpWebRequest)WebRequest.Create(url);
//            
//            
//
//            
//            Object result = null;
//            boolean hasResult = false;
//            Exception ex = null;
//
//            webRequestLock.lock();
//            try
//            {
//                webRequest.Accept = "text/plain";
//                if (HttpAcceptEncodingZipped)
//                {
//                    TryToSetHeader(HttpRequestHeader.AcceptEncoding, webRequest, "gzip");
//                    webRequest.Headers["Accept-Encoding-Workaround"] = "gzip";
//                }
//                SetAuthorization(webRequest);
//
//                if (invocation.Arguments.Length == 0)
//                {
//                    webRequest.Method = "GET";
//                    webRequest.BeginGetResponse(delegate(IAsyncResult asyncResult2)
//                    {
//                        try
//                        {
//                            HttpWebResponse response = (HttpWebResponse)webRequest.EndGetResponse(asyncResult2);
//                            using (Stream responseStream = response.GetResponseStream())
//                            using (Stream memoryStream = new MemoryStream())
//                            {
//                                int b;
//                                while ((b = responseStream.ReadByte()) != -1)
//                                {
//                                    memoryStream.WriteByte((byte)b);
//                                }
//                                memoryStream.Position = 0;
//                                try
//                                {
//                                    Stream deflateStream = GetResponseStream(response, memoryStream);
//                                    result = CyclicXmlHandler.ReadFromStream(deflateStream);
//                                }
//                                catch (XmlTypeNotFoundException)
//                                {
//                                    throw;
//                                }
//                                catch (Exception)
//                                {
//                                    memoryStream.Position = 0;
//                                    result = CyclicXmlHandler.ReadFromStream(memoryStream);
//                                }
//                            }
//                            hasResult = true;
//                        }
//                        catch (WebException e)
//                        {
//                            ex = ParseWebException(e);
//                            using (HttpWebResponse response = (HttpWebResponse)e.Response)
//                            {
//                                HandleException(e, response);
//                            }
//                        }
//                        catch (Exception e)
//                        {
//                            Log.Error(e);
//                            ex = e;
//                        }
//                        finally
//                        {
//                            lock (webRequest)
//                            {
//                                Monitor.PulseAll(webRequest);
//                            }
//                        }
//                    }, null);
//                }
//                else
//                {
//                    webRequest.Method = "POST";
//                    webRequest.ContentType = "text/plain";
//                    if (HttpContentEncodingZipped)
//                    {
//                        TryToSetHeader(HttpRequestHeader.ContentEncoding, webRequest, "gzip");
//                        webRequest.Headers["Content-Encoding-Workaround"] = "gzip";
//                    }
//                    webRequest.BeginGetRequestStream(delegate(IAsyncResult asyncResult)
//                    {
//                        try
//                        {
////                            using (Stream stream = webRequest.EndGetRequestStream(asyncResult))
////#if SILVERLIGHT
////                            using (Stream deflateStream = HttpContentEncodingZipped ? new GZipStream(stream, CompressionMode.Compress, CompressionLevel.BestCompression, false) : stream)
////#else
////                            using (Stream deflateStream = HttpContentEncodingZipped ? new GZipStream(stream, CompressionMode.Compress, false) : stream)
////#endif
//                            {
//                                CyclicXmlHandler.WriteToStream(deflateStream, invocation.Arguments);
//                            }
//                            webRequest.BeginGetResponse(delegate(IAsyncResult asyncResult2)
//                            {
//                                try
//                                {
//                                    HttpWebResponse response = (HttpWebResponse)webRequest.EndGetResponse(asyncResult2);
//                                    using (Stream responseStream = response.GetResponseStream())
//                                    using (Stream memoryStream = new MemoryStream())
//                                    {
//                                        int b;
//                                        while ((b = responseStream.ReadByte()) != -1)
//                                        {
//                                            memoryStream.WriteByte((byte)b);
//                                        }
//                                        memoryStream.Position = 0;
//                                        try
//                                        {
//                                            Stream deflateStream = GetResponseStream(response, memoryStream);
//                                            result = CyclicXmlHandler.ReadFromStream(deflateStream);
//                                        }
//                                        catch (XmlTypeNotFoundException)
//                                        {
//                                            throw;
//                                        }
//                                        catch (Exception)
//                                        {
//                                            memoryStream.Position = 0;
//                                            result = CyclicXmlHandler.ReadFromStream(memoryStream);
//                                        }
//                                    }
//                                    hasResult = true;
//                                }
//                                catch (WebException e)
//                                {
//                                    ex = ParseWebException(e);
//                                    using (HttpWebResponse response = (HttpWebResponse)e.Response)
//                                    {
//                                        HandleException(e, response);
//                                    }
//                                }
//                                catch (Exception e)
//                                {
//                                    ex = e;
//                                }
//                                finally
//                                {
//                                	 webRequestLock.lock();
//                                     try
//                                    {
////                                        Monitor.PulseAll(webRequest);
//                                        webRequest.notifyAll();
//                                    }
//                                     finally {
//                                     	webRequestLock.unlock();	
//                                     }
//                                }
//                            }, null);
//                        }
//                        catch (Exception e)
//                        {
//                            ex = e;
//                            webRequestLock.lock();
//                            try
//                            {
////                                Monitor.PulseAll(webRequest);
//                                webRequest.notifyAll();
//                            }
//                            finally {
//                            	webRequestLock.unlock();	
//                            }
//                        }
//                    }, null);
//                }
//                while (!hasResult && ex == null)
//                {
//                	
//                    Monitor.Wait(webRequest);
//                }
//            }
//            finally {
//            	webRequestLock.unlock();
//            }
//            if (result is AmbethServiceException)
//            {
//                ex = ParseServiceException((AmbethServiceException)result);
//                throw new Exception("Error occured while calling " + webRequest.Method + " " + webRequest.RequestUri, ex);
//            }
//            if (ex != null)
//            {
//                if (ex is WebException)
//                {
//                    throw new Exception(ex.Message + "\r\rError occured while calling " + webRequest.Method + " " + webRequest.RequestUri + ". " + CyclicXmlHandler.Write(invocation.Arguments), ex);
//                }
//                throw new Exception("Error occured while calling " + webRequest.Method + " " + webRequest.RequestUri + ". " + CyclicXmlHandler.Write(invocation.Arguments), ex);
//            }
//            if (!hasResult)
//            {
//                throw new Exception("This must never happen");
//            }
//            invocation.ReturnValue = ConvertToExpectedType(method.ReturnType, result);
//        }
//
//        protected Exception ParseServiceException(AmbethServiceException serviceException)
//        {
//            AmbethServiceException serviceCause = serviceException.Cause;
//            Exception cause = null;
//            if (serviceCause != null)
//            {
//                cause = ParseServiceException(serviceCause);
//            }
//            return new Exception(serviceException.Message + "\n" + serviceException.StackTrace, cause);
//        }
//
//        /// <summary>
//        /// Parse the given WebException and create a new ApplicationException with the parsed error message
//        /// </summary>
//        /// <param name="webException">The WebException</param>
//        /// <returns>An ApplicationException (if the WebException could be parsed) or the original WebException</returns>
//        protected Exception ParseWebException(WebException webException)
//        {
//            Exception exception = null;
//            HttpWebResponse httpResponse = webException.Response as HttpWebResponse;
//
//            if (httpResponse != null && httpResponse.StatusCode == HttpStatusCode.InternalServerError)
//            {
//                // handle internal server error (500)
//                string failureReason = null;
//                using (Stream responseStream = GetResponseStream(httpResponse, httpResponse.GetResponseStream()))
//                {
//                    if (responseStream != null && responseStream.CanRead)
//                    {
//                        using (StreamReader reader = new StreamReader(responseStream))
//                        {
//                            failureReason = reader.ReadToEnd();
//                        }
//                    }
//                }
//
//                if (failureReason != null)
//                {
//                    exception = new WebException(failureReason, webException);
//                }
//                else
//                {
//                    exception = webException;
//                }
//            }
//            else
//            {
//                // handle other than internal server error (500)
//                exception = webException;
//            }
//            return exception;
//        }
//
//        protected void HandleException(Exception e, HttpWebResponse response)
//        {
//            if (response != null)
//            {
//                if (response.StatusCode == HttpStatusCode.Unauthorized)
//                {
//                    throw new UnauthorizedAccessException("The username or password is wrong while calling '" + response.Method + " " + response.ResponseUri.ToString() + "'", e);
//                }
//                else
//                {
//                    throw new Exception("The http request returned http error '" + (int)response.StatusCode + "(" + response.StatusCode.ToString() + ") while calling '" + response.Method + " " + response.ResponseUri.ToString() + "'", e);
//                }
//            }
//            else
//            {
//                throw new Exception("The http request returned an error", e);
//            }
//        }
//
//        protected void TryToSetHeader(HttpRequestHeader requestHeader, HttpWebRequest request, String value)
//        {
//            if (headerAlreadyTried.Contains(requestHeader))
//            {
//                return;
//            }
//            try
//            {
//                request.Headers[requestHeader] = value;
//            }
//            catch (ArgumentException)
//            {
//                headerAlreadyTried.Add(requestHeader);
//            }
//        }
//
//        protected Stream GetResponseStream(HttpWebResponse response, Stream responseStream)
//        {
//            String contentEncoding = response.Headers["Content-Encoding"];
//            if ("gzip".Equals(contentEncoding))
//            {
//                return new GZipStream(responseStream, CompressionMode.Decompress);
//            }
//            else
//            {
//                return responseStream;
//            }
//        }
//
//        protected Object ConvertToExpectedType(Type expectedType, Object result)
//        {
//            if (typeof(void).Equals(expectedType) || result == null)
//            {
//                return null;
//            }
//            else if (expectedType.IsAssignableFrom(result.GetType()))
//            {
//                return result;
//            }
//            if (typeof(IEnumerable).IsAssignableFrom(expectedType) && !typeof(String).Equals(expectedType))
//            {
//                Object targetCollection = ListUtil.CreateCollectionOfType(expectedType);
//
//                MethodInfo addMethod = targetCollection.GetType().GetMethod("Add");
//                Type addType = addMethod.GetParameters()[0].ParameterType;
//                Object[] parameters = new Object[1];
//
//                if (result is IEnumerable)
//                {
//                    foreach (Object item in (IEnumerable)result)
//                    {
//                        Object convertedItem = ConversionHelper.ConvertValueToType(addType, item);
//                        parameters[0] = convertedItem;
//                        addMethod.Invoke(targetCollection, parameters);
//                    }
//                }
//                return targetCollection;
//            }
//            else if (expectedType.IsArray)
//            {
//                List<Object> list = new List<Object>();
//                if (result is IEnumerable)
//                {
//                    foreach (Object item in (IEnumerable)result)
//                    {
//                        list.Add(item);
//                    }
//                }
//
//                Array array = Array.CreateInstance(expectedType.GetElementType(), list.Count);
//                for (int a = 0, size = list.Count; a < size; a++)
//                {
//                    array.SetValue(list[a], a);
//                }
//                return array;
//            }
//            throw new Exception("Can not convert result " + result + " to expected type " + expectedType.FullName);
//        }
//
//        protected void SetAuthorization(HttpWebRequest request)
//        {
//            String[] authentication = AuthenticationHolder.GetAuthentication();
//            String userName = authentication[0];
//            String password = authentication[1];
//            if (userName == null && password == null)
//            {
//                return;
//            }
//            String authInfo = userName + ":" + password; //userName + ":" + Encryption.Encrypt(.password;//TODO Encryption.encrypt(password);
//            authInfo = Convert.ToBase64String(Encoding.UTF8.GetBytes(authInfo));
//            request.Headers[HttpRequestHeader.Authorization] = "Basic " + authInfo;
//        }
//
//        public void BeginOnline()
//        {
//            BeginOffline();
//        }
//
//        public void HandleOnline()
//        {
//            HandleOffline();
//        }
//
//        public void EndOnline()
//        {
//            EndOffline();
//        }
//
//        public void BeginOffline()
//        {
//            lock (clientLock)
//            {
//                connectionChangePending = true;
//            }
//        }
//
//        public void HandleOffline()
//        {
//            // Intended blank
//        }
//
//        public void EndOffline()
//        {
//            clientLock.lock();
//            try
//            {
//                connectionChangePending = false;
//                Monitor.PulseAll(clientLock);
//            }
//            finally
//            {
//            	clientLock.unlock();
//            }
//        }
//
//		@Override
//		public String getServiceName()
//		{
//			return serviceName;
//		}
//
//		@Override
//		public void setServiceName(String serviceName)
//		{
//			this.serviceName = serviceName;
//		}
// }