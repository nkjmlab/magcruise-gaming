package org.magcruise.gaming.manager.process;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.magcruise.gaming.manager.GameExecutorManager;
import org.magcruise.gaming.manager.resource.GameResourceDownloader;
import org.magcruise.gaming.model.def.GameBuilder;
import org.magcruise.gaming.model.def.boot.BootstrapBuilder;

public class ExternalGameProcessBuilder extends GameProcessBuilder {

	private String javaCommand = "java";
	private String classPath = System.getProperty("java.class.path");
	private List<String> javaOptions;

	ExternalGameProcessBuilder(GameBuilder gameBuilder) {
		super(gameBuilder);
	}

	@Override
	public ExternalGameProcess build() {
		if (gameBuilder.getBootstrapBuilder().isRemoteDebugMode()) {
			setRemoteDebug();
			log.warn("--------- REMOTE DEBUG MODE ---------");
			log.info("--------- REMOTE DEBUG MODE ---------");
		}
		ProcessId processId = gameBuilder.getProcessId();

		javaOptions = gameBuilder.getGameSystemPropertiesBuilder().getJavaOptions();

		ExternalGameProcess process = new ExternalGameProcess(getJavaCommands(), getClasspath(),
				processId, GameExecutorManager.getInstance()
						.getCreateAndRegisterGameExecutorSExpression(gameBuilder));
		process.directory(gameBuilder.getGameSystemPropertiesBuilder().getWorkingDirectory());

		return process;
	}

	private List<String> getJavaCommands() {
		List<String> args = new ArrayList<>();
		args.add(javaCommand);
		args.addAll(javaOptions);
		return args;
	}

	private List<String> getClasspath() {
		List<String> args = new ArrayList<>();
		args.add("-cp");

		List<String> result = new ArrayList<>();

		BootstrapBuilder bp = new BootstrapBuilder(gameBuilder.getBootstrapBuilder());
		bp.setUpBootstrapBuilder(new ProcessId(String.valueOf(System.nanoTime())));

		bp.getClasspath().forEach(jar -> {
			try {
				if (jar.getPath().toURL().toString().startsWith("http")) {
					String downloadedJarPath = GameResourceDownloader
							.downloadFile(gameBuilder.getProcessId(), jar.getPath().toUri())
							.toAbsolutePath().toString();
					log.info("Downloaded jar path={}", downloadedJarPath);
					result.add(downloadedJarPath);
				} else {
					result.add(jar.getPath().toFile().getAbsolutePath());
				}

			} catch (Exception e) {
				throw e;
			}
		});
		result.add(classPath);
		args.add(String.join(File.pathSeparator, result));
		log.info("classpath is {}", result);
		return args;
	}

	public void setJavaCommand(String javaCmd) {
		this.javaCommand = javaCmd;
	}

	public void setClassPath(String classPath) {
		this.classPath = classPath;
	}

	private void addJavaOption(String option) {
		javaOptions.add(option);
	}

	public void setRemoteDebug() {
		addJavaOption("-agentlib:jdwp=transport=dt_socket,address=8888,server=y,suspend=y");
	}

}
