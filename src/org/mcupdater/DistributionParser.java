package org.mcupdater;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class DistributionParser {

	private static final String VERSION = "1.0";

	public static Document readXmlFromFile(File packFile) throws Exception
	{
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		
		try {
			DocumentBuilder db = dbf.newDocumentBuilder();
			return db.parse(packFile);
		}catch(ParserConfigurationException pce) {
			//MCUpdater.apiLogger.log(Level.SEVERE, "Parser error", pce);
		}catch(SAXException se) {
			//MCUpdater.apiLogger.log(Level.SEVERE, "Parser error", se);
		}catch(IOException ioe) {
			//MCUpdater.apiLogger.log(Level.SEVERE, "I/O error", ioe);
		}
		return null;
	}
	
	public static Document readXmlFromUrl(String serverUrl) throws Exception
	{
		//MCUpdater.apiLogger.fine("readXMLFromUrl(" + serverUrl + ")");
		final URL server;
		try {
			server = new URL(serverUrl);
		} catch( MalformedURLException e ) {
			e.printStackTrace();
			//MCUpdater.apiLogger.log(Level.WARNING, "Malformed URL", e);
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
		}catch(ParserConfigurationException pce) {
			//MCUpdater.apiLogger.log(Level.SEVERE, "Parser error", pce);
		}catch(SAXException se) {
			//MCUpdater.apiLogger.log(Level.SEVERE, "Parser error", se);
		}catch(IOException ioe) {
			//MCUpdater.apiLogger.log(Level.SEVERE, "I/O error", ioe);
		}
		return null;
	}

	public static Distribution parseDocument(Document dom, String distName, String javaVersion, PlatformType pt) {
		Element parent = dom.getDocumentElement();
		Element docEle = null;
		if (parent.getNodeName().equals("Distributions")) {
			NodeList distributions = parent.getElementsByTagName("Distribution");
			for (int i = 0; i < distributions.getLength(); i++) {
				docEle = (Element)distributions.item(i);
				if (docEle.getAttribute("name").equals(distName) && getTextValue(docEle, "JavaVersion").equals(javaVersion)) { break; }
			}
		} else {
			throw new RuntimeException("Malformed XML!");
		}
		return getDistribution(docEle, pt); 
		
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
		String releaseType = getTextValue(el, "ReleaseType");
		String javaVersion = getTextValue(el, "JavaVersion");
		String mainClass = getTextValue(el, "Class");
		String params = getTextValue(el, "Params");
		List<Library> libraries = new ArrayList<Library>();
		NodeList nl = el.getElementsByTagName("Library");
		for(int i = 0; i < nl.getLength(); i++) {
			Element elLib = (Element)nl.item(i);
			Library l = getLibrary(elLib, pt); //TODO
			libraries.add(l);
		}
		return new Distribution(name, releaseType, javaVersion, mainClass, params, libraries);
	}

	private static Library getLibrary(Element el, PlatformType pt) {
		String name = el.getAttribute("name");
		String filename = getTextValue(el, "Filename");
		long size = Long.parseLong(getTextValue(el, "Size"));
		String md5 = getTextValue(el, "MD5");
		PlatformType platform = pt;
		List<URL> downloadURLs = new ArrayList<URL>();
		NodeList nl = el.getElementsByTagName("DownloadURL");
		for (int i = 0; i < nl.getLength(); i++) {
			Element elURL = (Element)nl.item(i);
			try {
				URL dlURL = new URL(elURL.getTextContent());
				downloadURLs.add(dlURL);
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (DOMException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return new Library(name, filename, size, md5, platform.toString(), downloadURLs);
	}

	public static Distribution loadFromFile(File packFile, String distName, String javaVersion, PlatformType pt) {
		try {
			return parseDocument(readXmlFromFile(packFile), distName, javaVersion, pt);
		} catch (Exception e) {
			//MCUpdater.apiLogger.log(Level.SEVERE, "General error", e);
			return null;
		}
		//return modList;
	}
	
	public static Distribution loadFromURL(String serverUrl, String distName, String javaVersion, PlatformType pt)
	{
		try {
			return parseDocument(readXmlFromUrl(serverUrl), distName, javaVersion, pt);
		} catch (Exception e) {
			//MCUpdater.apiLogger.log(Level.SEVERE, "General error", e);
			e.printStackTrace();
			return null;
		}
		//return modList;
	}

}
