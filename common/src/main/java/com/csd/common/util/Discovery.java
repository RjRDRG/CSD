package com.csd.common.util;

import java.net.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * <p>A class to perform service discovery, based on periodic service contact endpoint 
 * announcements over multicast communication.</p>
 * 
 * <p>Servers announce their *name* and contact *uri* at regular intervals. The server actively
 * collects received announcements.</p>
 * 
 * <p>Service announcements have the following format:</p>
 * 
 * <p>&lt;service-name-string&gt;&lt;delimiter-char&gt;&lt;service-uri-string&gt;</p>
 */
public class Discovery {
	private static Logger Log = Logger.getLogger(Discovery.class.getName());

	static {
		// addresses some multicast issues on some TCP/IP stacks
		System.setProperty("java.net.preferIPv4Stack", "true");
		// summarizes the logging format
		System.setProperty("java.util.logging.SimpleFormatter.format", "%4$s: %5$s");
	}

	public static final int DISCOVERY_PERIOD = 1000;

	private static final String URI_DELIMITER = "\t";

	private static InetSocketAddress addr;
	private static String serviceName;
	private static String serviceURI;

	private static Map<String,String> services;

	private static MulticastSocket ms;

	/**
	 * @param  serviceName the name of the service to announce
	 * @param  serviceURI an uri string - representing the contact endpoint of the service being announced
	 */
	public static void init(String serviceName, String serviceURI) throws Exception {
		Discovery.addr = new InetSocketAddress("226.226.226.226", 2266);
		Discovery.serviceName = serviceName;
		Discovery.serviceURI  = serviceURI;
		Discovery.services = new ConcurrentHashMap<>();
		Discovery.ms = new MulticastSocket(addr.getPort());
		Discovery.ms.joinGroup(addr, NetworkInterface.getByInetAddress(InetAddress.getLocalHost()));
	}

	/**
	 * Starts sending service announcements at regular intervals... 
	 */
	public static void startSendingAnnouncements() {
		Log.info(String.format("Starting Discovery announcements on: %s for: %s -> %s\n", addr, serviceName, serviceURI));
		byte[] announceBytes = (serviceName+ URI_DELIMITER +serviceURI).getBytes();
		DatagramPacket announcePkt = new DatagramPacket(announceBytes, announceBytes.length, addr);

		try {
			new Thread(() -> {
				for (;;) {
					try {
						ms.send(announcePkt);
						Thread.sleep(DISCOVERY_PERIOD);
					} catch (Exception e) {
						e.printStackTrace();
						// do nothing
					}
				}
			}).start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Starts collecting service announcements at regular intervals...
	 */
	public static void startCollectingAnnouncements() {
		try {
			new Thread(() -> {
				DatagramPacket pkt = new DatagramPacket(new byte[1024], 1024);
				for (;;) {
					try {
						pkt.setLength(1024);
						ms.receive(pkt);

						String msg = new String( pkt.getData(), 0, pkt.getLength());
						String[] msgElems = msg.split(URI_DELIMITER);

						if( msgElems.length == 2) {
							String service = msgElems[0];
							String uri = msgElems[1];
							services.put(service,uri);
						}
					} catch (Exception ignored) {}
				}
			}).start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public Map<String, String> knownUris() {
		return services;
	}
}
