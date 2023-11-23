package com.koch.ambeth.server.rest;

import com.koch.ambeth.dot.IDotUtil;
import com.koch.ambeth.merge.model.ICUDResult;
import com.koch.ambeth.merge.service.IMergeService;
import com.koch.ambeth.merge.transfer.EntityMetaDataTransfer;
import com.koch.ambeth.service.merge.IEntityMetaDataProvider;
import com.koch.ambeth.service.merge.model.IEntityMetaData;
import com.koch.ambeth.service.rest.Constants;
import com.koch.ambeth.util.IConversionHelper;
import com.koch.ambeth.util.collections.IdentityLinkedMap;
import com.koch.ambeth.util.model.IMethodDescription;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.StreamingOutput;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Path("/MergeService")
@Consumes({ Constants.AMBETH_MEDIA_TYPE })
@Produces({ Constants.AMBETH_MEDIA_TYPE })
public class MergeServiceREST extends AbstractServiceREST {
    protected IMergeService getMergeService() {
        return getService(IMergeService.class);
    }

    @GET
    @Path("createMetaDataDOT")
    public StreamingOutput createMetaDataDOT(@Context HttpServletRequest request, @Context HttpServletResponse response) {
        return defaultStreamingRequest(request, response, () -> getMergeService().createMetaDataDOT());
    }

    @GET
    @Path("fim")
    @Produces("image/png")
    public StreamingOutput fim(@Context HttpServletRequest request, @Context final HttpServletResponse response) {
        return defaultStreamingRequest(request, response, () -> {
            var dotUtil = getService(IDotUtil.class);
            var dot = getMergeService().createMetaDataDOT();
            var pngBytes = dotUtil.writeDotAsPngBytes(dot);
            return (StreamingOutput) output -> {
                response.setHeader(HttpHeaders.CONTENT_TYPE, "image/png");
                output.write(pngBytes);
            };
        });
    }

    @POST
    @Path("merge")
    public StreamingOutput merge(InputStream is, @Context HttpServletRequest request, @Context HttpServletResponse response) {
        return defaultStreamingRequest(request, response, is, args -> getMergeService().merge((ICUDResult) args[0], (String[]) args[1], (IMethodDescription) args[2]));
    }

    @SuppressWarnings("unchecked")
    @POST
    @Path("getMetaData")
    public StreamingOutput getMetaData(InputStream is, @Context HttpServletRequest request, @Context HttpServletResponse response) {
        return defaultStreamingRequest(request, response, is, args -> {
            var conversionHelper = getService(IConversionHelper.class);
            var result = getService(IEntityMetaDataProvider.class).getMetaData((List<Class<?>>) args[0]);

            var emdTransferMap = IdentityLinkedMap.<IEntityMetaData, EntityMetaDataTransfer>create(result.size());

            var emdTransfer = new ArrayList<EntityMetaDataTransfer>(result.size());
            for (int a = 0, size = result.size(); a < size; a++) {
                var source = result.get(a);
                var target = emdTransferMap.get(source);
                if (target == null) {
                    target = conversionHelper.convertValueToType(EntityMetaDataTransfer.class, source);
                    emdTransferMap.put(source, target);
                }
                emdTransfer.add(target);
            }
            return emdTransfer;
        });
    }
}
