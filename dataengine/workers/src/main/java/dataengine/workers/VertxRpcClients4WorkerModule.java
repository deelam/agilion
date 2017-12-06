package dataengine.workers;

import java.util.concurrent.CompletableFuture;
import javax.jms.Connection;
import com.google.inject.Provides;
import dataengine.apis.DepJobService_I;
import dataengine.apis.RpcClientProvider;
import dataengine.apis.SessionsDB_I;
import dataengine.apis.Tasker_I;
import dataengine.apis.VerticleConsts;
import io.vertx.core.Vertx;
import lombok.extern.slf4j.Slf4j;
import net.deelam.vertx.jobboard.JobBoardOutput_I;
import net.deelam.vertx.rpc.VertxRpcClientsModule;

/// provides verticle clients used by Worker service
@Slf4j
class VertxRpcClients4WorkerModule extends VertxRpcClientsModule {

  public VertxRpcClients4WorkerModule(CompletableFuture<Vertx> vertxF, Connection connection) {
    super(vertxF, connection);
//    debug = true;
    log.debug("VertxRpcClients4WorkerModule configured");
  }

  @Provides
  RpcClientProvider<Tasker_I> tasker_RpcClient(){
    return new RpcClientProvider<>(getAmqClientSupplierFor(Tasker_I.class, VerticleConsts.taskerBroadcastAddr));
  }

  @Provides
  RpcClientProvider<DepJobService_I> depJobService_RpcClient(){
    return new RpcClientProvider<>(getAmqClientSupplierFor(DepJobService_I.class, VerticleConsts.depJobMgrBroadcastAddr));
  }

  @Provides
  RpcClientProvider<SessionsDB_I> sessionsDb_RpcClient(){
    return new RpcClientProvider<>(getAmqClientSupplierFor(SessionsDB_I.class, VerticleConsts.sessionDbBroadcastAddr));
  }

  @Provides
  RpcClientProvider<JobBoardOutput_I> jobConsumer_RpcClient(){
    return new RpcClientProvider<>(getAmqClientSupplierFor(JobBoardOutput_I.class, VerticleConsts.jobBoardBroadcastAddr));
  }

}
