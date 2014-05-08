//using System.ServiceModel.Channels;
//using System.ServiceModel.Dispatcher;
//using System.IO;
//using System;
//using System.ServiceModel;
//using Ionic.Zlib;
//using System.Xml.Linq;
//using System.ServiceModel.Description;
//using System.Collections.ObjectModel;

//namespace De.Osthus.Ambeth.Compression
//{
//    public class ZipClientMessageInspector : IEndpointBehavior, IClientMessageInspector
//#if !SILVERLIGHT
//        , IDispatchMessageInspector, IServiceBehavior
//#endif
//    {
//        public void AfterReceiveReply(ref Message reply, object correlationState)
//        {
//            if (reply.IsFault)
//            {
//                return;
//            }
//            Byte[] reader = reply.GetBody<Byte[]>();
//            using (MemoryStream ms = new MemoryStream(reader))
//            using (DeflateStream zip = new DeflateStream(ms, CompressionMode.Decompress))
//            {
//                XElement xml = XElement.Load(zip);
//                reply = Message.CreateMessage(
//                    reply.Version,
//                    reply.Headers.Action,
//                    xml
//                    );
//            }
//        }
//        public object BeforeSendRequest(ref Message request, IClientChannel channel)
//        {
//            if (request.IsFault)
//            {
//                return null;
//            }
//            using (MemoryStream ms = new MemoryStream())
//            using (DeflateStream zip = new DeflateStream(ms, CompressionMode.Compress, CompressionLevel.BestCompression))
//            {
//                zip.FlushMode = FlushType.Sync;
//                request.GetBody<XElement>().Save(zip, SaveOptions.DisableFormatting);
//                zip.FlushMode = FlushType.Finish;
//                zip.Flush();
//                byte[] zipresult = ms.ToArray();
//                request = Message.CreateMessage(
//                    request.Version,
//                    request.Headers.Action,
//                    zipresult
//                    );
//            }
//            return null;
//        }

//#if !SILVERLIGHT
//        public object AfterReceiveRequest(ref Message request, IClientChannel channel, InstanceContext instanceContext)
//        {
//            if (request.IsFault)
//            {
//                return null;
//            }
//            Byte[] reader = request.GetBody<Byte[]>();
//            using (MemoryStream ms = new MemoryStream(reader))
//            using (DeflateStream zip = new DeflateStream(ms, CompressionMode.Decompress))
//            {
//                XElement xml = XElement.Load(zip);
//                request = Message.CreateMessage(
//                    request.Version,
//                    request.Headers.Action,
//                    request
//                    );
//            }
//            return null;
//        }
//#endif

//        public void BeforeSendReply(ref Message reply, object correlationState)
//        {
//            if (reply.IsFault)
//            {
//                return;
//            }
//            using (MemoryStream ms = new MemoryStream())
//            using (DeflateStream zip = new DeflateStream(ms, CompressionMode.Compress, CompressionLevel.BestCompression))
//            {
//                zip.FlushMode = FlushType.Sync;
//                reply.GetBody<XElement>().Save(zip, SaveOptions.DisableFormatting);
//                zip.FlushMode = FlushType.Finish;
//                zip.Flush();

//                byte[] zipresult = ms.ToArray();
//                reply = Message.CreateMessage(
//                    reply.Version,
//                    reply.Headers.Action,
//                    zipresult
//                    );
//            }
//        }

//        public void AddBindingParameters(ServiceEndpoint endpoint, BindingParameterCollection bindingParameters)
//        {
//        }

//#if !SILVERLIGHT
//        public void AddBindingParameters(ServiceDescription serviceDescription, ServiceHostBase serviceHostBase, Collection<ServiceEndpoint> endpoints, BindingParameterCollection bindingParameters)
//        {
//        }
//#endif

//        public void ApplyClientBehavior(ServiceEndpoint endpoint, ClientRuntime clientRuntime)
//        {
//            //clientRuntime.MessageInspectors.Add(this);
//        }

//        public void ApplyDispatchBehavior(ServiceEndpoint endpoint, EndpointDispatcher endpointDispatcher)
//        {
//            endpointDispatcher.DispatchRuntime.MessageInspectors.Add(this);
//        }

//#if !SILVERLIGHT
//        public void ApplyDispatchBehavior(ServiceDescription serviceDescription, ServiceHostBase serviceHostBase)
//        {
//            //foreach (ChannelDispatcher chDisp in serviceHostBase.ChannelDispatchers)
//            //{
//            //    foreach (EndpointDispatcher epDisp in chDisp.Endpoints)
//            //    {
//            //        epDisp.DispatchRuntime.MessageInspectors.Add(this);
//            //    }
//            //}
//        }
//#endif

//        public void Validate(ServiceEndpoint endpoint)
//        {
//        }

//#if !SILVERLIGHT
//        public void Validate(ServiceDescription serviceDescription, ServiceHostBase serviceHostBase)
//        {
//        }
//#endif
//    }
//}