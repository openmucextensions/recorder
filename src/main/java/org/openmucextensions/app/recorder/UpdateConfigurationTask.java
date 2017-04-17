package org.openmucextensions.app.recorder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.TimerTask;

import org.openmuc.framework.config.ArgumentSyntaxException;
import org.openmuc.framework.config.ChannelConfig;
import org.openmuc.framework.config.ChannelScanInfo;
import org.openmuc.framework.config.ConfigService;
import org.openmuc.framework.config.ConfigWriteException;
import org.openmuc.framework.config.DeviceConfig;
import org.openmuc.framework.config.DeviceScanInfo;
import org.openmuc.framework.config.DriverConfig;
import org.openmuc.framework.config.DriverNotAvailableException;
import org.openmuc.framework.config.IdCollisionException;
import org.openmuc.framework.config.RootConfig;
import org.openmuc.framework.config.ScanException;
import org.openmuc.framework.config.ScanInterruptedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UpdateConfigurationTask extends TimerTask {

	private static Logger logger = LoggerFactory.getLogger(Recorder.class);
	
	private final ConfigService configService;
	
	private int loggingInterval;
	private int samplingInterval;
	private String blacklistFilename;
	
	public UpdateConfigurationTask(final ConfigService configService, int loggingInterval, int samplingInterval, String blacklistFilename) {
		this.configService = configService;
		this.loggingInterval = loggingInterval;
		this.samplingInterval = samplingInterval;
		this.blacklistFilename = blacklistFilename;
	}
	
	@Override
	public void run() {
		
		logger.info("Starting update of channel configuration...");
		long starttime = System.currentTimeMillis();
		
		RootConfig newConfig = updateChannelConfiguration(configService.getConfig());
		configService.setConfig(newConfig);
		try {
			configService.writeConfigToFile();
			long duration = System.currentTimeMillis() - starttime;
			logger.info("Successfully updated channel configuration in {}ms", duration);
		} catch (ConfigWriteException e) {
			logger.error("Error while writing channel configuration to file: {}", e.getMessage());
		}	
	}
	
	private RootConfig updateChannelConfiguration(RootConfig rootConfig) {
		
		final String noSettings = "";
		Blacklist blacklist = new Blacklist();
		try {
			blacklist.loadConfiguration(blacklistFilename);
		} catch (IOException e) {
			logger.error("Error while loading blacklist configuration: {}", e.getMessage());
		}
		
		for (String driver : configService.getIdsOfRunningDrivers()) {
			
			// blacklist blocks driver id?
			if(blacklist.isDriverBlocked(driver)) continue;
			
			DriverConfig driverConfig = rootConfig.getOrAddDriver(driver);
			List<DeviceScanInfo> devices = null;
			try {
				devices = configService.scanForDevices(driver, noSettings);
			} catch (UnsupportedOperationException e) {
				logger.warn("Driver {} doesn't support device scan, automated channel configuration not possible", driver);
				continue;
			} catch (DriverNotAvailableException e) {
				logger.warn("Driver {} not available, automated channel configuration not possible", driver);
				continue;
			} catch (ArgumentSyntaxException e) {
				logger.error("The settings string for driver {} is not valid: {}", driver, noSettings);
				continue;
			} catch (ScanException e) {
				logger.error("Error while scanning for devices with driver {}: {}", driver, e.getMessage());
				continue;
			} catch (ScanInterruptedException e) {
				logger.warn("Device scan interrupted for driver {}", driver);
				continue;
			}
			
			for (DeviceScanInfo device : devices) {
				
				// blacklist blocks device address?
				if(blacklist.isDeviceBlocked(device.getDeviceAddress())) continue;
				
				// is device already configured?
				if(!getConfiguredDevices(driverConfig).contains(device.getId())) {
					try {
						// add device to configuration
						DeviceConfig config = driverConfig.addDevice(device.getId());
						config.setDeviceAddress(device.getDeviceAddress());
						config.setSettings(device.getSettings());
						config.setDescription(device.getDescription());
						logger.debug("Added device {} to configuration", device.getId());
					} catch (IdCollisionException e) {
						logger.error("Couldn't add device {} with address {} because of an ID collision", device.getId(), device.getDeviceAddress());
					}
				}
				
				DeviceConfig deviceConfig = driverConfig.getDevice(device.getId());
				List<ChannelScanInfo> channels = null;
				try {
					channels = configService.scanForChannels(device.getId(), noSettings);
				} catch (UnsupportedOperationException e) {
					logger.warn("Driver {} doesn't support channel scan, automated channel configuration not possible", driver);
					continue;
				} catch (DriverNotAvailableException e) {
					logger.warn("Driver {} not available, automated channel configuration not possible", driver);
					continue;
				} catch (ArgumentSyntaxException e) {
					// empty settings string passed, so this should not happen
					logger.error("The channel scan settings string for device {} is not valid", device.getId());
					continue;
				} catch (ScanException e) {
					logger.error("Error while scanning for channels with driver {}: {}", driver, e.getMessage());
					continue;
				}
				
				for (ChannelScanInfo channel : channels) {
					
					// blacklist blocks channel address?
					if(blacklist.isChannelBlocked(channel.getChannelAddress())) continue;
					
					// is channel already configured?
					if(!getConfiguredChannels(deviceConfig).contains(channel.getChannelAddress())) {
						try {
							// add channel to configuration
							ChannelConfig config = deviceConfig.addChannel(generateChannelId(channel));
							config.setDescription(channel.getDescription());
							config.setChannelAddress(channel.getChannelAddress());
							config.setValueType(channel.getValueType());
							config.setValueTypeLength(channel.getValueTypeLength());
							config.setLoggingInterval(loggingInterval);
							config.setSamplingInterval(samplingInterval);
							logger.debug("Added channel {} from device {} to config", channel.getChannelAddress(), device.getId());
						} catch (IdCollisionException e) {
							logger.error("Couldn't add channel {} with id {} because of an ID collision", channel.getChannelAddress(), generateChannelId(channel));
						}
					}
										
				} // channel
				
			} // device
			
		} // driver
		
		return rootConfig;
			
	} // method
	
	private List<String> getConfiguredDevices(final DriverConfig driverConfig) {
		
		List<String> result = new ArrayList<>();
		
		if(driverConfig == null) return result;
		
		Collection<DeviceConfig> devices = driverConfig.getDevices();
		for (DeviceConfig device : devices) {
			result.add(device.getId());
		}
		
		return result;
	}
	
	private List<String> getConfiguredChannels(final DeviceConfig deviceConfig) {
		
		List<String> result = new ArrayList<>();
		
		if(deviceConfig == null) return result;
		
		Collection<ChannelConfig> channels = deviceConfig.getChannels();
		for (ChannelConfig channel : channels) {
			result.add(channel.getChannelAddress());
		}
		
		return result;
		
	}
		
	private String generateChannelId(ChannelScanInfo scanInfo) {
		return scanInfo.getChannelAddress().replaceAll("[^a-zA-Z0-9]+", "_");
	}
	
}
