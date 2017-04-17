package org.openmucextensions.app.recorder;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class Blacklist {
	
	private List<String> blockedDrivers = Collections.synchronizedList(new ArrayList<String>());
	private List<String> blockedDevices = Collections.synchronizedList(new ArrayList<String>());
	private List<String> blockedChannels = Collections.synchronizedList(new ArrayList<String>());
	
	public void loadConfiguration(String filename) throws IOException {
		
		File file = new File(filename);
		if(!file.exists()) {
			createEmptyConfiguration(file);
			blockedDrivers.clear();
			blockedDevices.clear();
			blockedChannels.clear();
			return;
		}
		
		try {
			
			DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document doc = db.parse(filename);
			
			doc.getDocumentElement().normalize();
			
			// get blocked drivers
			blockedDrivers.clear();
			NodeList nodes = doc.getElementsByTagName("driver");
						
			for(int index=0; index<nodes.getLength(); index++) {
							
				Node node = nodes.item(index);
							
				if(node.getNodeType() == Node.ELEMENT_NODE) {
					Element element = (Element) node;
								
					if(element.getElementsByTagName("driver").getLength() > 0) {
						String driverId = element.getElementsByTagName("driver").item(0).getTextContent();
						blockedDrivers.add(driverId);
					}
				}
			}
			
			// get blocked devices
			blockedDevices.clear();
			nodes = doc.getElementsByTagName("device");
			
			for(int index=0; index<nodes.getLength(); index++) {
				
				Node node = nodes.item(index);
				
				if(node.getNodeType() == Node.ELEMENT_NODE) {
					Element element = (Element) node;
					
					if(element.getElementsByTagName("device").getLength() > 0) {
						String deviceAddress = element.getElementsByTagName("device").item(0).getTextContent();
						blockedDevices.add(deviceAddress);
					}
				}
			}
			
			// get blocked channels
			blockedChannels.clear();
			nodes = doc.getElementsByTagName("channel");
						
			for(int index=0; index<nodes.getLength(); index++) {
							
				Node node = nodes.item(index);
							
				if(node.getNodeType() == Node.ELEMENT_NODE) {
					Element element = (Element) node;
								
					if(element.getElementsByTagName("channel").getLength() > 0) {
						String channelAddress = element.getElementsByTagName("channel").item(0).getTextContent();
						blockedChannels.add(channelAddress);
					}
				}
			}
			
		} catch (ParserConfigurationException e) {
			throw new IOException(e);
		} catch (SAXException e) {
			throw new IOException(e);
		} 	
	}
	
	public boolean isDriverBlocked(String driverId) {
		return (blockedDrivers.contains(driverId) ? true : false);
	}
	
	public boolean isDeviceBlocked(String deviceAddress) {
		return (blockedDevices.contains(deviceAddress) ? true : false);
	}
	
	public boolean isChannelBlocked(String channelAddress) {
		return (blockedChannels.contains(channelAddress) ? true : false);
	}
	
	public static void createEmptyConfiguration(File file) throws IOException {
		
		BufferedWriter writer = null;
		
		try {
			writer = new BufferedWriter(new FileWriter(file));
			
			writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>");
			writer.newLine();
			writer.write("<blacklist>");
			writer.newLine();
			writer.write("  <drivers>");
			writer.newLine();
			writer.write("    <!-- <driver>driverId</driver> -->");
			writer.newLine();
			writer.write("  </drivers>");
			writer.newLine();
			writer.write("  <devices>");
			writer.newLine();
			writer.write("    <!-- <device>deviceAddress</device> -->");
			writer.newLine();
			writer.write("  </devices>");
			writer.newLine();
			writer.write("  <channels>");
			writer.newLine();
			writer.write("    <!-- <channel>channelAddress</channel> -->");
			writer.newLine();
			writer.write("  </channels>");
			writer.newLine();
			writer.write("</blacklist>");
			writer.newLine();
			writer.flush();
		} finally {
			if(writer!=null) writer.close();
		}
		
	}
	
}
