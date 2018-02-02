package dataengine.api.impl;

import dataengine.api.*;
import dataengine.api.*;

import dataengine.api.Dataset;

import java.util.List;
import dataengine.api.NotFoundException;

import java.io.InputStream;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;


public class DatasetApiServiceImpl extends DatasetApiService {
    @Override
    public Response getDataset(String id, SecurityContext securityContext) throws NotFoundException {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
}
