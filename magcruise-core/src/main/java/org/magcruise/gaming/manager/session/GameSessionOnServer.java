package org.magcruise.gaming.manager.session;

import org.magcruise.gaming.manager.process.GameOnServerProcess;
import org.magcruise.gaming.manager.process.ProcessId;
import org.magcruise.gaming.model.def.GameBuilder;
import org.magcruise.gaming.model.def.sys.DefUIService;

@SuppressWarnings("serial")
public class GameSessionOnServer extends GameSessionCore<GameOnServerProcess> {

  public GameSessionOnServer(Class<? extends ResourceLoader> clazz) {
    super(clazz);

  }

  @Override
  public GameBuilder build() {
    if (brokerUrl == null) {
      useDefaultPublicBroker();
    }
    resourceLoader.addDefGameSystemProperties(new DefUIService(brokerUrl.toString()));
    return super.build();
  }

  @Override
  public ProcessId start() {
    checkExecuted();
    try {
      buildIfNotBuilt();
      log.info("temporal pid={}", getProcessId());
      this.process = new GameOnServerProcess(brokerUrl.toString(), toDefBootstrap());
      process.start();
      String newPid = process.getProcessId().getValue();
      log.info("ResourceLoader\n=========\n{}===========\n",
          resourceLoader.getDefBootstrapScript().getScript());
      log.info("[created] processId on server ={}", newPid);
      executed = true;
      resourceLoader.processId = new ProcessId(newPid);
      resourceLoader.gameBuilder.setProcessId(resourceLoader.processId);
      return getProcessId();
    } catch (Exception e) {
      log.error("broker url is {}", brokerUrl);
      log.error(e, e);
      throw new RuntimeException(e);
    }
  }

}
