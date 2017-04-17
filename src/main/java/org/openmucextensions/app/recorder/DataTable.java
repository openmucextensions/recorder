package org.openmucextensions.app.recorder;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.openmuc.framework.data.Record;
import org.openmuc.framework.data.Value;

public class DataTable {
	
	private final Map<Long, Map<String,Value>> table = new TreeMap<>();
	private final List<String> channelIds;
	
	private long startTime = 0;
	
	public DataTable(long startTime, long endTime, long interval, List<String> channelIds) {
		
		this.channelIds = new ArrayList<>(channelIds);
		this.startTime = startTime;
		
		// create row template
		Map<String, Value> rowTemplate = new TreeMap<>();
		for (String channelId : channelIds) {
			rowTemplate.put(channelId, null);
		}
		
		// create table
		for(long timestamp=startTime; timestamp<=endTime; timestamp+=interval) {
			table.put(timestamp, new TreeMap<>(rowTemplate));
		}
		
	}
	
	public void insertRecords(String channelId, List<Record> records) {
		
		if(!channelIds.contains(channelId)) return;
		
		for (Record record : records) {
			Map<String, Value> row = table.get(record.getTimestamp());
			if(row!=null) {
				row.put(channelId, record.getValue());
			}
		}
		
	}
	
	public void writeCSVFile(String filename, String separator, String dateFormat) throws IOException {
		
		SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);
		
		BufferedWriter writer = null;
		
		try {
			writer = new BufferedWriter(new FileWriter(filename));
			
			// write header
			writer.write("timestamp");
			writer.write(separator);
			
			Set<String> channels = table.get(startTime).keySet();
			
			for (String channel : channels) {
				writer.write(channel);
				writer.write(separator);
			}
			writer.newLine();
			
			// write data
			StringBuilder line = null;
			for (Long timestamp : table.keySet()) {
				line = new StringBuilder();
				line.append(formatter.format(new Date(timestamp)));
				line.append(separator);

				Map<String, Value> values = table.get(timestamp);
				
				for (String channel : values.keySet()) {
					line.append(values.get(channel)==null ? "" : values.get(channel));
					line.append(separator);
				}
				
				writer.write(line.toString());
				writer.newLine();
			}
			writer.flush();
		} finally {
			if(writer!=null) writer.close();
		}
	}
	
}
