package org.mcupdater;

import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

public class DistributionParser {

	private static final String VERSION = "1.3";

	public static Document readXmlFromFile(File packFile) throws Exception
	{
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		
		try {
			DocumentBuilder db = dbf.newDocumentBuilder();
			return db.parse(packFile);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static Document readXmlFromUrl(String serverUrl) throws Exception
	{
		final URL server;
		try {
			server = new URL(serverUrl);
		} catch( MalformedURLException e ) {
			e.printStackTrace();
			return null;
		}
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		URLConnection serverConn = server.openConnection();
		serverConn.setRequestProperty("User-Agent", "MCUpdater-Bootstrap/" + VERSION);
		serverConn.setConnectTimeout(5000);
		serverConn.setReadTimeout(5000);
		try {
			DocumentBuilder db = dbf.newDocumentBuilder();
			return db.parse(serverConn.getInputStream());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static Distribution parseDocument(Document dom, String distName, String javaVersion, PlatformType pt) {
		Element parent = dom.getDocumentElement();
		Element docEle;
		if (parent.getNodeName().equals("Distributions")) {
			System.out.println("Iterating defined distributions:");
			NodeList distributions = parent.getElementsByTagName("Distribution");
			for (int i = 0; i < distributions.getLength(); i++) {
				docEle = (Element)distributions.item(i);
				System.out.println(docEle.getAttribute("name") + " - " + docEle.getElementsByTagName("FriendlyName").item(0).getTextContent());
				if (docEle.getAttribute("name").equals(distName)) {
					NodeList versions = docEle.getElementsByTagName("JavaVersion");
					for (int verIndex = 0; verIndex < versions.getLength(); verIndex++) {
						System.out.println("--" + versions.item(verIndex).getTextContent());
						if (versions.item(verIndex).getTextContent().equals(javaVersion)) { return getDistribution(docEle, pt); }
					}
				}
			}
		} else {
			throw new RuntimeException("Malformed XML!");
		}
		return null;
	}

	private static String getTextValue(Element ele, String tagName) {
		String textVal = null;
		NodeList nl = ele.getElementsByTagName(tagName);
		if(nl != null && nl.getLength() > 0) {
			Element el = (Element)nl.item(0);
			if(el != null) {
				Node node = el.getFirstChild();
				if(node != null) textVal = unescapeXML(node.getNodeValue());
			}
		}
		return textVal;
	}

	private static String unescapeXML(String nodeValue) {
		return nodeValue.replace("&amp;", "&").replace("&quot;", "\"").replace("&apos;","'").replace("&lt;", "<").replace("&gt;", ">");
	}

	private static Distribution getDistribution(Element el, PlatformType pt) {
		String name = el.getAttribute("name");
		String friendlyName = getTextValue(el, "FriendlyName");
		String javaVersion = getTextValue(el, "JavaVersion");
		String mainClass = getTextValue(el, "Class");
		String params = getTextValue(el, "Params");
		List<Library> libraries = new ArrayList<Library>();
		NodeList nl = el.getElementsByTagName("Library");
		for(int i = 0; i < nl.getLength(); i++) {
			Element elLib = (Element)nl.item(i);
			Library l = getLibrary(elLib, pt);
			if (!(l == null)) {
				libraries.add(l);
			}
		}
		return new Distribution(name, friendlyName, javaVersion, mainClass, params, libraries);
	}

	private static Library getLibrary(Element el, PlatformType pt) {
		boolean validOnPlatform = false;
		NodeList platforms = el.getElementsByTagName("Platform");
		for (int i = 0; i < platforms.getLength(); i++){
			if (platforms.item(i).getTextContent().equals(pt.toString())) {
				validOnPlatform = true;
				break;
			}
		}
		if (!validOnPlatform) {
			return null;
		}
		String name = el.getAttribute("name");
		String filename = getTextValue(el, "Filename");
		long size = Long.parseLong(getTextValue(el, "Size"));
		String md5 = getTextValue(el, "MD5");
		List<URL> downloadURLs = new ArrayList<URL>();
		NodeList nl = el.getElementsByTagName("DownloadURL");
		for (int i = 0; i < nl.getLength(); i++) {
			Element elURL = (Element)nl.item(i);
			try {
				URL dlURL = new URL(elURL.getTextContent());
				downloadURLs.add(dlURL);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return new Library(name, filename, size, md5, pt.toString(), downloadURLs);
	}

	public static Distribution loadFromFile(File packFile, String distName, String javaVersion, PlatformType pt) {
		try {
			return parseDocument(readXmlFromFile(packFile), distName, javaVersion, pt);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static Distribution loadFromURL(String serverUrl, String distName, String javaVersion, PlatformType pt)
	{
		try {
			return parseDocument(readXmlFromUrl(serverUrl), distName, javaVersion, pt);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

}
