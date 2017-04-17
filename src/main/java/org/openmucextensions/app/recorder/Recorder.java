package org.openmucextensions.app.recorder;

import java.util.Timer;

import org.openmuc.framework.config.ConfigService;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Recorder {
	
	private static Logger logger = LoggerFactory.getLogger(Recorder.class);

	private ConfigService configService = null;
	private Timer updateConfigurationTimer = null;
	
	private int loggingInterval = 60 * 1000;
	private int samplingInterval = 10 * 1000;
	
	private long updatePeriod = 1000 * 60 * 60 * 24; // 24 hours
	
	private final String DEFAULT_BLACKLIST_FILENAME = "./conf/blacklist.xml";
	private String blacklistFilename = null;
	
	protected void activate(ComponentContext context) {
		logger.info("Activating Building Flight Recorder App");
		
		try {
			loggingInterval = Integer.parseInt(System.getProperty("org.openmucextensions.app.recorder.loggingInterval", Integer.toString(loggingInterval)));
			samplingInterval = Integer.parseInt(System.getProperty("org.openmucextensions.app.recorder.samplingInterval", Integer.toString(samplingInterval)));
			updatePeriod = Long.parseLong(System.getProperty("org.openmucextensions.app.recorder.updatePeriod", Long.toString(updatePeriod)));
		} catch (NumberFormatException e) {
			logger.error("Logging or sampling interval or update period property is not a valid number, using default value");
		}
		
		blacklistFilename = System.getProperty("org.openmucextensions.app.recorder.blacklistFilename", DEFAULT_BLACKLIST_FILENAME);
		
		startConfigurationUpdateTimer();
		
	}
	
	protected void deactivate(ComponentContext context) {
		logger.info("Deactivating Building Flight Recorder App");
		
		if(updateConfigurationTimer!=null) updateConfigurationTimer.cancel();
	}
	
	protected void setConfigService(ConfigService service) {
		this.configService = service;
	}
	
	protected void unsetConfigService(ConfigService service) {
		this.configService = null;
	}
	
	private void startConfigurationUpdateTimer() {
			
		updateConfigurationTimer = new Timer("Channel configuration update timer", true);
		UpdateConfigurationTask updateTask = new UpdateConfigurationTask(configService, loggingInterval, samplingInterval, blacklistFilename);
		updateConfigurationTimer.scheduleAtFixedRate(updateTask, 15*1000, updatePeriod);
		
	}
	
}
