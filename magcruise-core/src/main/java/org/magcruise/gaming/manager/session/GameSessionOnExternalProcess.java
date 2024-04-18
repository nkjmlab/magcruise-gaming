package org.magcruise.gaming.manager.session;

import org.magcruise.gaming.manager.process.ExternalGameProcess;
import org.magcruise.gaming.manager.process.ExternalGameProcessManager;
import org.magcruise.gaming.manager.process.ProcessId;
import org.magcruise.gaming.model.def.GameBuilder;
import org.magcruise.gaming.model.def.sys.DefRequestToGameExecutorPublisherService;
import org.magcruise.gaming.model.def.sys.DefUIService;

@SuppressWarnings("serial")
public class GameSessionOnExternalProcess extends GameSessionCore<ExternalGameProcess> {

	public GameSessionOnExternalProcess(Class<? extends ResourceLoader> clazz) {
		super(clazz);
	}

	@Override
	public GameBuilder build() {
		if (brokerUrl != null) {
			resourceLoader.addDefGameSystemProperties(
					new DefUIService(brokerUrl.toString()),
					new DefRequestToGameExecutorPublisherService(brokerUrl.toString(),
							useWebsocket));
		}
		return super.build();
	}

	/**
	 * 呼び出したプロセスとは別のプロセス上でKawaインタプリタを動かす．エラーログは，System.getProperty( "java.io.tmpdir")以下のファイルに出力される．
	 *
	 * @return
	 */

	@Override
	public ProcessId start() {
		checkExecuted();
		buildIfNotBuilt();
		process = ExternalGameProcessManager.getInstance()
				.createProcess(System.getProperty("java.class.path"), getGameBuilder());
		process.start();
		executed = true;
		return getProcessId();
	}

}
