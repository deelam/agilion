package dataengine.main;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.configuration2.Configuration;
import org.apache.curator.framework.CuratorFramework;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Names;
import lombok.extern.slf4j.Slf4j;
import net.deelam.zkbasedinit.ConfigReader;
import net.deelam.zkbasedinit.Constants;
import net.deelam.zkbasedinit.GModuleZkComponentStarter;
import net.deelam.zkbasedinit.GModuleZooKeeper;
import net.deelam.zkbasedinit.ZkComponentStarter;
import net.deelam.zkbasedinit.ZkConnector;

@Slf4j
public class MainZkComponentStarter {
  public static final String COMPONENT_IDS = "componentIds";

  public static void main(String[] args) {
    if (args.length > 0) {
      System.setProperty(COMPONENT_IDS, args[0]);
    }
    new Thread(() -> {
      try {
        startZkComponentStarter("startup.props");
      } catch (Exception e) {
        throw new IllegalStateException("While running myZkComponentStarterThread", e);
      }
    }, "myZkComponentStarterThread").start();
  }

  static List<String> startZkComponentStarter(String propFile) throws Exception {
    Configuration config = ConfigReader.parseFile(propFile);
    //log.info("{}\n------", ConfigReader.toStringConfig(config, config.getKeys()));

    String componentIds = System.getProperty(COMPONENT_IDS, config.getString(COMPONENT_IDS, ""));
    List<String> compIdList =
        Arrays.stream(componentIds.split(",")).map(String::trim).collect(Collectors.toList());
    log.info("---------- componentIds to start: {}", compIdList);
    
    GModuleZkComponentStarter moduleZkComponentStarter =
        new GModuleZkComponentStarter(compIdList.size());
    Injector injector = Guice.createInjector( //
        new GModuleZooKeeper(config), //
        moduleZkComponentStarter);

    CuratorFramework cf = injector.getInstance(CuratorFramework.class);
    String startupPath =
        injector.getInstance(Key.get(String.class, Names.named(Constants.ZOOKEEPER_STARTUPPATH)));

    // starts components given an compId and ComponentI subclass
    for (String compId : compIdList) {
      log.info("---------- Starting {}", compId);
      ZkComponentStarter.startComponent(injector, compId);
      log.info("Tree after starting {}: {}", compId, ZkConnector.treeToString(cf, startupPath));
    }

    Thread.sleep(1000);
    log.info("---------- Waiting for components to start: {}",
        moduleZkComponentStarter.getStartedLatch().getCount());
    moduleZkComponentStarter.getStartedLatch().await();

    log.info("---------- All components started: {}", compIdList);
    log.info("Tree after all components started: {}", ZkConnector.treeToString(cf, startupPath));
    Thread.sleep(1000);
    log.info("Waiting for components to end: {}",
        moduleZkComponentStarter.getCompletedLatch().getCount());
    moduleZkComponentStarter.getCompletedLatch().await();

    log.info("---------- Tree after components stopped: {}", ZkConnector.treeToString(cf, startupPath));



    return compIdList;
  }
}
