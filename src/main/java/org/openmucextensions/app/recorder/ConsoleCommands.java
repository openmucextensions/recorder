package org.openmucextensions.app.recorder;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.felix.service.command.Descriptor;
import org.openmuc.framework.data.Record;
import org.openmuc.framework.dataaccess.Channel;
import org.openmuc.framework.dataaccess.DataAccessService;
import org.openmuc.framework.dataaccess.DataLoggerNotAvailableException;
import org.osgi.service.component.ComponentContext;

public class ConsoleCommands {
	
	private DataAccessService dataAccessService = null;
	
	protected void deactivate(ComponentContext context) {
		
	}
	
	@Descriptor("exports a data table for the specified time span")
	public void exporttable(
			@Descriptor("path and filename of the export file") String filename,
			@Descriptor("start time with format yyyy-MM-dd HH:mm:ss (use quotes)") String startTimeArg,
			@Descriptor("end time with format yyyy-MM-dd HH:mm:ss (use quotes)") String endTimeArg,
			@Descriptor("interval in minutes") String intervalArg) {
		
		DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		
		try {
			
			long startTime = formatter.parse(startTimeArg).getTime();
			long endTime = formatter.parse(endTimeArg).getTime();
			long interval = Long.parseLong(intervalArg);
			
			List<String> channelIds = getLoggedChannels();
			System.out.println("Found " + channelIds.size() + " logged channels...");
			
			DataTable table = new DataTable(startTime, endTime, interval * 60000, channelIds);
			for (String channelId : channelIds) {
				List<Record> records = dataAccessService.getChannel(channelId).getLoggedRecords(startTime, endTime);
				table.insertRecords(channelId, records);
			}
			
			table.writeCSVFile(filename, ",", "yyyy-MM-dd HH:mm:ss");
			System.out.println("Data exported to file " + filename);
			
		} catch (ParseException e) {
			System.out.println("Cannot parse begin or end date, correct format is yyyy-MM-dd HH:mm:ss");
		} catch (NumberFormatException e) {
			System.out.println("Cannot parse interval");
		} catch (DataLoggerNotAvailableException e) {
			System.out.println("Cannot export table because data logger is not available");
		} catch (IOException e) {
			System.out.println("IO error: " + e.getMessage());
		}
		
	}
	
	protected void setDataAccessService(DataAccessService service) {
		this.dataAccessService = service;
	}
	
	protected void unsetDataAccessService(DataAccessService service) {
		this.dataAccessService = null;
	}
	
	private List<String> getLoggedChannels() {
		
		List<String> allChannelIds = dataAccessService.getAllIds();
		List<String> channelsToLog = new ArrayList<>();
		
		for (String channelId : allChannelIds) {
			Channel channel = dataAccessService.getChannel(channelId);
			if(channel!=null) {
				if(channel.getLoggingInterval()>0) channelsToLog.add(channelId);
			}
		}
		
		return channelsToLog;
	}
	
}
