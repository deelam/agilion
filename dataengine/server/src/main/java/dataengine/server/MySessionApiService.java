package dataengine.server;

import static dataengine.server.RestParameterHelper.makeBadRequestResponse;
import static dataengine.server.RestParameterHelper.makeResponseIfIdInvalid;
import static dataengine.server.RestParameterHelper.makeResponseIfNotSecure;
import static dataengine.server.RestParameterHelper.makeResultResponse;
import static dataengine.server.RestParameterHelper.tryCreateObject;

import java.util.Map;

import javax.inject.Inject;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import dataengine.api.NotFoundException;
import dataengine.api.Session;
import dataengine.api.SessionApiService;
import dataengine.apis.RpcClientProvider;
import dataengine.apis.SessionsDB_I;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Inject) )
public class MySessionApiService extends SessionApiService {
  private static final String OBJECT_TYPE = "Session";

  final RpcClientProvider<SessionsDB_I> sessDb;
  
  @Override
  public Response createSession(Session session, SecurityContext securityContext) throws NotFoundException {
    log.info("REST {}: createSession: {}", securityContext.getUserPrincipal(), session);
    Response resp = makeResponseIfNotSecure(securityContext);
    if (resp != null)
      return resp;

    return tryCreateObject(OBJECT_TYPE, session, "session/", (s) -> s.getId(),
        sessDb.rpc()::hasSession,
        () -> sessDb.rpc().createSession(session));
  }

  @Override
  public Response getSession(String id, SecurityContext securityContext) throws NotFoundException {
    log.info("REST {}: getSession: {}", securityContext.getUserPrincipal(), id);
    Response resp = makeResponseIfNotSecure(securityContext);
    if (resp != null)
      return resp;
    resp = makeResponseIfIdInvalid(OBJECT_TYPE, id);
    if (resp != null)
      return resp;
    return makeResultResponse(OBJECT_TYPE, "session/", id, sessDb.rpc().getSession(id));
  }

  @SuppressWarnings("unchecked")
  @Override
  public Response setSessionMetadata(String id, @SuppressWarnings("rawtypes") Map props,
      SecurityContext securityContext) throws NotFoundException {
    log.info("REST {}: setSessionMetadata: {} {}", securityContext.getUserPrincipal(), id, props);
    Response resp = makeResponseIfNotSecure(securityContext);
    if (resp != null)
      return resp;

    resp = makeResponseIfIdInvalid(OBJECT_TYPE, id);
    if (resp != null)
      return resp;

    for (Object k : props.keySet())
      if (!(k instanceof String))
        return makeBadRequestResponse(
            "All keys in property map must be strings! Found: " + k.getClass() + " for key=" + k);

    return makeResultResponse(OBJECT_TYPE, "session/", id, sessDb.rpc().setSessionMetadata(id, props));
  }

}
