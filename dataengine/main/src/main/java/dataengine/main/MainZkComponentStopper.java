package dataengine.main;

import java.io.IOException;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import org.apache.curator.framework.CuratorFramework;
import com.google.inject.Guice;
import com.google.inject.Injector;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.deelam.utils.PropertiesUtil;
import net.deelam.zkbasedinit.ConstantsZk;
import net.deelam.zkbasedinit.GModuleZooKeeper;
import net.deelam.zkbasedinit.ZkComponentStopper;
import net.deelam.zkbasedinit.ZkConnector;

@Slf4j
public class MainZkComponentStopper {

  public static void main(String[] args) {
    String propsFile = (args.length > 0) ? args[0] : "startup.props";
    new MainZkComponentStopper().stopComponents(propsFile);
  }

  public void stopComponents(String propsFile) {
    try {
      boolean keepPrevConfig = Boolean.parseBoolean(System.getProperty("KEEP_PREV_ZKCONFIG"));
      stopZookeeperComponents(propsFile, !keepPrevConfig);
    } catch (Exception e) {
      throw new IllegalStateException("While running MainZkComponentStopper", e);
    }
  }

  @Getter
  CompletableFuture<String> componentIdsF = new CompletableFuture<>();

  CuratorFramework cf;

  void shutdown() {
    if(cf!=null) {
      cf.close();
      cf=null;
    }
  }

  @Getter(lazy=true)
  private final Properties properties = privateGetProperties();
  String propFile;
  private Properties privateGetProperties() {
    Properties properties = new Properties();
    try {
      PropertiesUtil.loadProperties(propFile, properties);
    } catch (IOException e) {
      log.warn("ZK: Couldn't load property file={}", propFile, e);
    }
    return properties;
  }
  
  void stopZookeeperComponents(String propertyFile, boolean cleanUp)
      throws Exception {
    propFile = propertyFile;
    Injector injector = Guice.createInjector(new GModuleZooKeeper(() -> getProperties()));
    ZkComponentStopper stopper = injector.getInstance(ZkComponentStopper.class);
    
    String zkStartupPathHome=System.getProperty(ConstantsZk.ZOOKEEPER_STARTUPPATH);
    if(MainJetty.DEBUG) log.info("ZK: ---------- Tree before stopping: {}",
        ZkConnector.treeToString(cf, zkStartupPathHome));

    List<String> compIds = stopper.listRunningComponents();
    log.info("ZK: Components to stop: {}", compIds);
    compIds.forEach(compId -> {
      try {
        stopper.stop(compId);
      } catch (Exception e) {
        log.error("When stopping compId=" + compId, e);
      }
    });

    try {
      Thread.sleep(2*MainJetty.SLEEPTIME); // allow time for modifications to take effect
      log.info("ZK: Tree after stopping all components: {}",
          ZkConnector.treeToString(cf, zkStartupPathHome));
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    if (cleanUp) {
      while (true)
        try {
          log.info("ZK: cleanup: {}", zkStartupPathHome);
          stopper.cleanup();
          break;
        } catch (Exception e) {
          log.error("Trying to clean up Zookeeper configs again", e);
        }
    }

    shutdown();
    log.info("ZK: ---------- Done stopping components: {}", compIds);
  }

}
