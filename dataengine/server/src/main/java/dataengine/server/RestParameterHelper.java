package dataengine.server;

import java.net.URI;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.SecurityContext;
import dataengine.api.ApiResponseMessage;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class RestParameterHelper {
  //TODO: 5: makeResponseIfNotSecure
  static Response makeResponseIfNotSecure(SecurityContext securityContext) {
    return null;
  }

  static Response makeBadRequestResponse(String msg) {
    log.warn("Bad request: {}", msg);
    Response response = Response.status(Status.BAD_REQUEST)
        .entity(new ApiResponseMessage(ApiResponseMessage.ERROR, msg))
        .build();
    return log(response);
  }

  static Response makeResponseIfIdInvalid(String idType, String id) {
    if (!isValidIdString(id))
      return makeBadRequestResponse("ID for " + idType + " is not valid: " + id + " allowedChars="+allowedChars);
    return null;
  }

  private static String allowedChars="a-zA-Z0-9.-";
  private static Pattern illegalCharsRegex = Pattern.compile("[^"+allowedChars+"]");
  private static boolean isValidIdString(String id) {
    return !illegalCharsRegex.matcher(id).find();
  }


  static Response makeResultResponse(String msg, Future<?> responseObj) {
    Response response;
    try {
      if (responseObj.get() == null) {
        String errMsg = "Null response: " + msg;
        log.warn(errMsg);
        response = Response.status(Status.NOT_FOUND)
            .entity(new ApiResponseMessage(ApiResponseMessage.ERROR, errMsg)).build();
      } else {
        response=Response.ok().entity(responseObj.get()).build();
      }
    } catch (InterruptedException | ExecutionException e) {
      String errMsg = "Exception caught during " + msg + ": " + e.getCause();
      response = createExceptionResponse(Status.INTERNAL_SERVER_ERROR, ApiResponseMessage.ERROR, 
          e.getCause(), errMsg);
    }
    return log(response);
  }

  static Response makeResultResponse(String objectType, String relativeLocationUriPrefix, String id,
      Future<?> responseObj) {
    Response response;
    try {
      Object result = responseObj.get();
      if (result == null) {
        String errMsg = "Object of type " + objectType + " not found with id: " + id;
        log.warn(errMsg);
        response = Response.status(Status.NOT_FOUND)
            .entity(new ApiResponseMessage(ApiResponseMessage.ERROR, errMsg)).build();
      } else {
        response = Response.ok(result)
            .location(URI.create(relativeLocationUriPrefix + id))
            .build();
      }
    } catch (InterruptedException | ExecutionException e) {
      String errMsg = objectType + " object, id=" + id + " caused exception: " + e.getCause();
      response = createExceptionResponse(Status.INTERNAL_SERVER_ERROR, ApiResponseMessage.ERROR, 
          e.getCause(), errMsg);
    }
    return log(response);
  }

  static <T> Response tryCreateObject(String objectType, T inputObj, String relativeLocationUriPrefix,
      Function<T, String> getId, Function<String, Future<Boolean>> hasObject, Supplier<Future<T>> createObject) {
    Response response;
    if (inputObj == null) {
      String errMsg = "Submitted " + objectType + " cannot be null!";
      log.warn(errMsg);
      return makeBadRequestResponse(errMsg);
    } else {
      try {
        String id = getId.apply(inputObj);
        if (id != null && hasObject.apply(id).get()) {
          String errMsg = "Object already exists of type " + objectType + ": " + id;
          log.warn(errMsg);
          response = Response.status(Status.CONFLICT)
              .entity(new ApiResponseMessage(ApiResponseMessage.ERROR, errMsg))
              .build();
        } else {
          Future<T> createF = createObject.get();
          T newObj = createF.get(); // blocks until object created
          if (newObj == null) {
            String errMsg = "Object of type " + objectType + " was not created with id=" + id;
            log.warn(errMsg);
            response = Response.status(Status.NO_CONTENT)
                .entity(new ApiResponseMessage(ApiResponseMessage.ERROR, errMsg))
                .build();
          } else {
            response = Response
                .created(URI.create(relativeLocationUriPrefix + getId.apply(newObj)))
                .status(Status.CREATED).entity(newObj).build();
          }
        }
      } catch (Exception e) {
        String errMsg = objectType + " object cannot be created from " + inputObj + " : " + e;
        response = createExceptionResponse(Status.INTERNAL_SERVER_ERROR, ApiResponseMessage.ERROR, e, errMsg);
      }
    }
    return log(response);
  }

  private static Response log(Response response) {
    if(response.getStatus()<400)
      log.info("REST response: {}", response);
    else if(response.getStatus()<500)
      log.info("REST Client error: {}", response);
    else
      log.warn("REST Server error: {}", response);
    return response;
  }

  private static Response createExceptionResponse(Status status, int code, Throwable e, String errMsg) {
    log.warn("REST error: "+errMsg, e);
    return Response.status(status).entity(new ApiResponseMessage(code, errMsg + "\n" + toString(e)))
        .build();
  }

  private static String toString(Throwable t) {
    if (t == null)
      return "null";
    // throws NPException: org.apache.commons.lang.exception.ExceptionUtils.getStackTrace(t)
    StringBuilder sb = new StringBuilder();
    do {
      sb.append(t.getClass().getName()).append(": ").append(t.getMessage());
      for (StackTraceElement e : t.getStackTrace()) {
        sb.append("\n\t").append(e);
      }
      t=t.getCause();
    }while(t!=null);
    return sb.toString();
  }
}
