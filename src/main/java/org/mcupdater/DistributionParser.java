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

	private static final String VERSION = "2.0";

	public static Document readXmlFromFile(File packFile) throws Exception {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		
		try {
			DocumentBuilder db = dbf.newDocumentBuilder();
			return db.parse(packFile);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static Document readXmlFromUrl(String serverUrl) throws Exception {
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

	@Deprecated
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

	public static List<Distribution> parseDocument(Document dom) {
		List<Distribution> result = new ArrayList<>();
		Element parent = dom.getDocumentElement();
		Element docEle;
		if (parent.getNodeName().equals("Distributions")) {
			NodeList distributions = parent.getElementsByTagName("Distribution");
			for (int i = 0; i < distributions.getLength(); i++) {
				docEle = (Element)distributions.item(i);
				Distribution newDistribution = new Distribution();
				newDistribution.setName(docEle.getAttribute("name"));
				newDistribution.setFriendlyName(getTextValue(docEle, "FriendlyName"));
				newDistribution.setMainClass(getTextValue(docEle,"Class"));
				newDistribution.setParams(getTextValue(docEle, "Params"));
				newDistribution.setMessage(getTextValue(docEle, "Message"));
				List<Library> libraries = new ArrayList<>();
				NodeList nlLibs = docEle.getElementsByTagName("Library");
				for(int iLib = 0; iLib < nlLibs.getLength(); iLib++) {
					Element elLib = (Element)nlLibs.item(iLib);
					Library lib = getLibrary(elLib);
					if (lib != null) {
						libraries.add(lib);
					}
				}
				newDistribution.setLibraries(libraries);
				List<DistributionRuntime> runtimes = new ArrayList<>();
				NodeList nlRuntimes = docEle.getElementsByTagName("Runtime");
				for(int iRT = 0; iRT < nlRuntimes.getLength(); iRT++) {
					Element elRT = (Element)nlRuntimes.item(iRT);
					DistributionRuntime runtime = getRuntime(elRT);
					if (runtime != null) {
						runtimes.add(runtime);
					}
				}
				newDistribution.setRuntimes(runtimes);
				List<Extract> extracts = new ArrayList<>();
				NodeList nlExtracts = docEle.getElementsByTagName("Extract");
				for(int iExtract = 0; iExtract < nlExtracts.getLength(); iExtract++) {
					Element elExtract = (Element)nlExtracts.item(iExtract);
					Extract extract = getExtract(elExtract);
					if (extract != null) {
						extracts.add(extract);
					}
				}
				newDistribution.setExtracts(extracts);
				result.add(newDistribution);
			}
		}
		return result;
	}

	private static Extract getExtract(Element el) {
		Extract newExtract = new Extract();
		newExtract.setName(el.getAttribute("name"));
		newExtract.setFilename(getTextValue(el,"Filename"));
		newExtract.setPath(getTextValue(el, "Path"));
		newExtract.setSize(Long.parseLong(getTextValue(el,"Size")));
		newExtract.setHashes(getHashes(el));
		newExtract.setPlatforms(getPlatforms(el));
		newExtract.setIncludePath(getTextValue(el,"IncludePath"));
		newExtract.setModuleNames(getModules(el));
		newExtract.setDownloadURLs(getDownloadURLS(el));
		return newExtract;
	}

	private static DistributionRuntime getRuntime(Element el) {
		DistributionRuntime newRuntime = new DistributionRuntime();
		newRuntime.setName(el.getAttribute("name"));
		newRuntime.setPrimary(el.hasAttribute("primary") && Boolean.parseBoolean(el.getAttribute("primary")));
		newRuntime.setFilename(getTextValue(el,"Filename"));
		newRuntime.setVersion(getTextValue(el, "Version"));
		newRuntime.setExecutable(getTextValue(el,"Executable"));
		newRuntime.setSize(Long.parseLong(getTextValue(el,"Size")));
		newRuntime.setHashes(getHashes(el));
		newRuntime.setPlatforms(getPlatforms(el));
		newRuntime.setDownloadURLs(getDownloadURLS(el));
		return newRuntime;
	}

	private static List<Hash> getHashes(Element el) {
		List<Hash> hashes = new ArrayList<>();
		NodeList nlHashes = el.getElementsByTagName("Hash");
		for (int iHash = 0; iHash < nlHashes.getLength(); iHash++) {
			Element elHash = (Element) nlHashes.item(iHash);
			hashes.add(new Hash(HashEnum.valueOf(elHash.getAttribute("type")), elHash.getTextContent()));
		}
		if (hashes.size() == 0) {
			hashes.add(new Hash(HashEnum.MD5, getTextValue(el,"MD5")));
		}
		return hashes;
	}

	private static Library getLibrary(Element el) {
		Library newLib = new Library();
		newLib.setName(el.getAttribute("name"));
		newLib.setFilename(getTextValue(el, "Filename"));
		newLib.setSize(Long.parseLong(getTextValue(el, "Size")));
		newLib.setHashes(getHashes(el));
		newLib.setPlatforms(getPlatforms(el));
		newLib.setModuleNames(getModules(el));
		newLib.setDownloadURLs(getDownloadURLS(el));
		return newLib;
	}

	private static List<PrioritizedURL> getDownloadURLS(Element el) {
		List<PrioritizedURL> urls = new ArrayList<>();
		NodeList nlUrls = el.getElementsByTagName("DownloadURL");
		for (int iUrl = 0; iUrl < nlUrls.getLength(); iUrl++) {
			Element elURL = (Element)nlUrls.item(iUrl);
			urls.add(new PrioritizedURL(elURL.getTextContent(), (elURL.hasAttribute("priority") ? Integer.parseInt(elURL.getAttribute("priority")) : 0)));
		}
		return urls;
	}

	private static List<String> getModules(Element el) {
		List<String> modules = new ArrayList<>();
		NodeList nlModules = el.getElementsByTagName("ModuleName");
		for (int iModule = 0; iModule < nlModules.getLength(); iModule++) {
			Element elModule = (Element)nlModules.item(iModule);
			modules.add(elModule.getTextContent());
		}
		return modules;
	}

	private static List<PlatformType> getPlatforms(Element el) {
		List<PlatformType> platforms = new ArrayList<>();
		NodeList nlPlatforms = el.getElementsByTagName("Platform");
		for (int iPlatform = 0; iPlatform < nlPlatforms.getLength(); iPlatform++) {
			Element elPlatform = (Element) nlPlatforms.item(iPlatform);
			platforms.add(PlatformType.valueOf(elPlatform.getTextContent()));
		}
		return platforms;
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
		return new Distribution(name, friendlyName, javaVersion, mainClass, params, libraries, null,null,null);
	}

	@Deprecated
	private static Library getLibrary(Element el, PlatformType pt) {
		/*
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
		 */
		return null;
	}

	public static List<Distribution> loadFromFile(File packFile) {
		try {
			return parseDocument(readXmlFromFile(packFile));
		} catch (Exception e) {
			e.printStackTrace();
			return new ArrayList<>();
		}
	}

	public static List<Distribution> loadFromURL(String serverUrl) {
		try {
			return parseDocument(readXmlFromUrl(serverUrl));
		} catch (Exception e) {
			e.printStackTrace();
			return new ArrayList<>();
		}
	}

	@Deprecated
	public static Distribution loadFromFile(File packFile, String distName, String javaVersion, PlatformType pt) {
		try {
			return parseDocument(readXmlFromFile(packFile), distName, javaVersion, pt);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	@Deprecated
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
