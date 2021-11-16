package org.mcupdater;

import joptsimple.*;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.io.Charsets;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.text.StrSubstitutor;
import org.mcupdater.downloadlib.DownloadQueue;
import org.mcupdater.downloadlib.Downloadable;
import org.mcupdater.downloadlib.SSLExpansion;
import org.mcupdater.downloadlib.TrackerListener;

import javax.swing.*;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.*;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.io.File.pathSeparator;
import static java.io.File.separator;

public class BootstrapForm extends JWindow implements TrackerListener
{
	private static final ResourceBundle config = ResourceBundle.getBundle("config"); //$NON-NLS-1$
	
	@Serial
	private static final long serialVersionUID = 1L;
	private static String bootstrapUrl;
	private static String distribution;
	private static String localBootstrap;
	private final JProgressBar progressBar;
	private final JLabel lblStatus;
	private final Logger logger;
	private Distribution distro;
	private List<Distribution> distributions;
	private static File basePath;// = new File("/home/sbarbour/Bootstrap-test");
	private static PlatformType thisPlatform;
	private String[] passthroughParams;
	private static String defaultPack;
	protected static BootstrapForm frame;
	private static boolean debug = false;

	/**
	 * Launch the application.
	 */
	public static void main(final String[] args) {
		System.out.println("MCUpdater Bootstrap");
		System.setProperty("java.net.preferIPv4Stack", "true");
		OptionParser optParser = new OptionParser();
		optParser.allowsUnrecognizedOptions();
		optParser.accepts("help", "Show help").forHelp();
		optParser.formatHelpWith(new BuiltinHelpFormatter(160,3));
		ArgumentAcceptingOptionSpec<String> bootstrapSpec = optParser.accepts("bootstrap", "Bootstrap URL").withRequiredArg().ofType(String.class).defaultsTo(config.getString("bootstrapURL"));
		ArgumentAcceptingOptionSpec<String> localBootstrapSpec = optParser.accepts("bootstrapfile", "Bootstrap from local XML").withRequiredArg().ofType(String.class).defaultsTo("");
		ArgumentAcceptingOptionSpec<String> distSpec = optParser.accepts("distribution", "MCUpdater distribution").withRequiredArg().ofType(String.class).defaultsTo(config.getString("distribution"));
		ArgumentAcceptingOptionSpec<String> defaultpackSpec = optParser.accepts("defaultpack", "Default pack URL").withRequiredArg().ofType(String.class).defaultsTo(config.getString("defaultPack"));
		ArgumentAcceptingOptionSpec<String> rootSpec = optParser.accepts("MCURoot", "Custom folder for MCUpdater").withRequiredArg().ofType(String.class).defaultsTo(config.getString("customPath"));
		final NonOptionArgumentSpec<String> nonOpts = optParser.nonOptions();
		optParser.accepts("debug","Show console output from MCUpdater");
		final OptionSet options = optParser.parse(args);
		defaultPack = options.valueOf(defaultpackSpec);
		if (options.has("debug")) {
			debug = true;
		}
		if (options.has("help")) {
			try {
				optParser.printHelpOn(System.out);
			} catch (IOException e) {
				e.printStackTrace();
			}
			return;
		}
		bootstrapUrl = bootstrapSpec.value(options);
		localBootstrap = localBootstrapSpec.value(options);
		distribution = distSpec.value(options);

		String customPath = options.valueOf(rootSpec);
		if(System.getProperty("os.name").startsWith("Windows")) {
			basePath = new File(new File(System.getenv("APPDATA")),".MCUpdater");
			thisPlatform = PlatformType.valueOf("WINDOWS" + System.getProperty("sun.arch.data.model"));
		} else if(System.getProperty("os.name").startsWith("Mac")) {
			basePath = new File(new File(new File(new File(System.getProperty("user.home")),"Library"),"Application Support"),"MCUpdater");
			thisPlatform = PlatformType.valueOf("OSX64");
		} else {
			basePath = new File(new File(System.getProperty("user.home")),".MCUpdater");
			thisPlatform = PlatformType.valueOf("LINUX" + System.getProperty("sun.arch.data.model"));
		}
		if (!customPath.isEmpty()) {
			basePath = new File(customPath);
		}
		final Map<String, Object> opts = new HashMap<>();
		opts.put("bootstrapURL", options.valueOf(bootstrapSpec));
		opts.put("distribution", options.valueOf(distSpec));

		EventQueue.invokeLater(() -> {
			try {
				for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
					//System.out.println(info.getName() + " : " + info.getClassName());
					if ("Nimbus".equals(info.getName())) {
						UIManager.setLookAndFeel(info.getClassName());
						break;
					}
				}
				if (UIManager.getLookAndFeel().getName().equals("Metal")) {
					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
				}
				frame = new BootstrapForm();
				List<String> passthrough = new ArrayList<String>();
				passthrough.addAll(options.valuesOf(nonOpts));
				passthrough.addAll(Arrays.asList(config.getString("passthroughArgs").split(" ")));
				frame.setPassthroughParams(passthrough.toArray(new String[passthrough.size()]));
				frame.setLocationRelativeTo( null );
				frame.setVisible(true);
				frame.doWork(opts);
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}

	public void setPassthroughParams(String[] args) {
		this.passthroughParams = args;
	}
	
	protected void doWork(Map<String, Object> opts) {
// *** Debug section
		System.out.println("System.getProperty('os.name') == '" + System.getProperty("os.name") + "'");
		System.out.println("System.getProperty('os.version') == '" + System.getProperty("os.version") + "'");
		System.out.println("System.getProperty('os.arch') == '" + System.getProperty("os.arch") + "'");
		System.out.println("System.getProperty('java.version') == '" + System.getProperty("java.version") + "'");
		System.out.println("System.getProperty('java.vendor') == '" + System.getProperty("java.vendor") + "'");
		System.out.println("System.getProperty('sun.arch.data.model') == '" + System.getProperty("sun.arch.data.model") + "'");
// ***
		SSLExpansion ssle = SSLExpansion.getInstance();
		try {
			List<String> resources = IOUtils.readLines(BootstrapForm.class.getResourceAsStream("/org/mcupdater/certs/certlist.txt"), Charsets.UTF_8);
			for (String rsrc : resources) {
				if (rsrc.endsWith(".pem")) {
					String certName = rsrc.substring(0, rsrc.length() - 4);
					ssle.addCertificateFromStream(BootstrapForm.class.getResourceAsStream("/org/mcupdater/certs/" + rsrc), certName);
					System.out.println("Registered root certificate: " + certName);
				}
			}
			ssle.updateSSLContext();
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (localBootstrap.isEmpty()) {
			System.out.println("Attempting from: " + bootstrapUrl);
			distributions = DistributionParser.loadFromURL(bootstrapUrl);
			if (distributions.size() != 0) {
				try {
					FileUtils.copyURLToFile(new URL(bootstrapUrl), new File(basePath, "Bootstrap-cache.xml"));
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else {
				System.out.print("Warning! No distribution was found via URL. Attempting to use cache.");
				distributions = DistributionParser.loadFromFile(new File(basePath, "Bootstrap-cache.xml"));
			}
		} else {
			distributions = DistributionParser.loadFromFile(new File(localBootstrap));
		}
		System.out.printf("Total distros: %d%n",distributions.size());
		List<Distribution> matchingDistros = distributions.stream().filter(entry -> entry.getName().equals(distribution)).toList();
		System.out.printf("Matching distros: %d%n",matchingDistros.size());
		if (matchingDistros.size() == 0) {
			JOptionPane.showMessageDialog(this, "No configuration found that matches distribution \"" + opts.get("distribution") + "\"!  Make sure you are connected to the internet, otherwise please report this issue via Discord.","MCU-Bootstrap",JOptionPane.ERROR_MESSAGE);
			System.exit(-1);
		} else if (matchingDistros.size() > 1) {
			JOptionPane.showMessageDialog(this, "Multiple configurations found that match distribution \"" + distribution + "\"!  Please report this issue via Discord.", "MCU-Bootstrap", JOptionPane.WARNING_MESSAGE);
		}
		distro = matchingDistros.get(0);
		lblStatus.setText("Downloading " + distro.getFriendlyName());
		Collection<Downloadable> dl = new ArrayList<>();
		for (Library library : distro.getRelevantLibraries(thisPlatform)) {
			Hash bestHash = library.getHashes().stream().max(Comparator.comparing(Hash::getType)).orElse(new Hash(HashEnum.MD5,""));
			Downloadable dlEntry = new Downloadable(library.getName(),("lib" + separator + library.getFilename()), Downloadable.HashAlgorithm.valueOf(bestHash.getType().name()),bestHash.getValue(),library.getSize(),library.getURLs());
			dl.add(dlEntry);
		}
		for (DistributionRuntime runtime : distro.getRelevantRuntimes(thisPlatform)) {
			Hash bestHash = runtime.getHashes().stream().max(Comparator.comparing(Hash::getType)).orElse(new Hash(HashEnum.MD5,""));
			Downloadable dlEntry = new Downloadable(runtime.getName(),("runtime" + separator + runtime.getFilename()), Downloadable.HashAlgorithm.valueOf(bestHash.getType().name()),bestHash.getValue(),runtime.getSize(),runtime.getURLs());
			dl.add(dlEntry);
		}
		for (Extract extract : distro.getRelevantExtracts(thisPlatform)) {
			Hash bestHash = extract.getHashes().stream().max(Comparator.comparing(Hash::getType)).orElse(new Hash(HashEnum.MD5,""));
			Downloadable dlEntry = new Downloadable(extract.getName(),("extract" + separator + extract.getFilename()), Downloadable.HashAlgorithm.valueOf(bestHash.getType().name()),bestHash.getValue(),extract.getSize(),extract.getURLs());
			dl.add(dlEntry);
		}
		DownloadQueue queue = new DownloadQueue("Bootstrap", "Bootstrap", this, dl, basePath, null, logger);
		queue.processQueue(4, () -> {
			Path extractPath = basePath.toPath().resolve("extract");
			for (Extract extract : distro.getRelevantExtracts(thisPlatform)) {
				File currentFile = extractPath.resolve(extract.getFilename()).toFile();
				try {
					ArchiveInputStream extractStream;
					if (currentFile.getAbsolutePath().endsWith(".tar.gz") || currentFile.getAbsolutePath().endsWith(".tgz")) {
						extractStream = new TarArchiveInputStream(new GzipCompressorInputStream(new BufferedInputStream(new FileInputStream(currentFile))));
						extractFromStream(basePath.toPath().resolve(extract.getPath()), extractStream);
//						basePath.toPath().resolve(extract.getPath()).
					} else if (currentFile.getAbsolutePath().endsWith(".zip")) {
						extractStream = new ZipArchiveInputStream(new BufferedInputStream(new FileInputStream(currentFile)));
						extractFromStream(basePath.toPath().resolve(extract.getPath()), extractStream);
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			Path runtimePath = basePath.toPath().resolve("runtime");
			for (DistributionRuntime runtime : distro.getRelevantRuntimes(thisPlatform)) {
				File currentFile = runtimePath.resolve(runtime.getFilename()).toFile();
				try {
					ArchiveInputStream extractStream;
					if (currentFile.getAbsolutePath().endsWith(".tar.gz") || currentFile.getAbsolutePath().endsWith(".tgz")) {
						extractStream = new TarArchiveInputStream(new GzipCompressorInputStream(new BufferedInputStream(new FileInputStream(currentFile))));
						extractFromStream(runtimePath.resolve(runtime.getName() + '-' + runtime.getVersion()), extractStream);
					} else if (currentFile.getAbsolutePath().endsWith(".zip")) {
						extractStream = new ZipArchiveInputStream(new BufferedInputStream(new FileInputStream(currentFile)));
						extractFromStream(runtimePath.resolve(runtime.getName() + '-' + runtime.getVersion()), extractStream);
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
	}

	private void extractFromStream(Path path, ArchiveInputStream extractStream) throws IOException {
		ArchiveEntry entry;
		while ((entry = extractStream.getNextEntry()) != null) {
			if (!extractStream.canReadEntryData(entry)) {
				System.err.println("Unable to read entry: " + entry);
				continue;
			}
			File destFile = path.resolve(entry.getName()).toFile();
			if (entry.isDirectory()) {
				if (!destFile.isDirectory() && !destFile.mkdirs()) {
					throw new IOException("Failed to create directory " + destFile);
				}
			} else {
				File parent = destFile.getParentFile();
				if (!parent.isDirectory() && !parent.mkdirs()) {
					throw new IOException("Failed to create directory " + parent);
				}
				try (OutputStream out = Files.newOutputStream(destFile.toPath())) {
					IOUtils.copy(extractStream, out);
				}
			}
		}
	}

	private String getJavaVersionString() {
		String fullVersion = System.getProperty("java.version");
		int pos = fullVersion.indexOf('.');
		pos = fullVersion.indexOf('.', pos+1);
		return fullVersion.substring(0, pos);
	}
	/**
	 * Create the frame.
	 */
	public BootstrapForm() {
		// Establish logger
		logger = Logger.getLogger("MCU-Bootstrap");
		logger.setLevel(Level.ALL);
		ConsoleHandler handler = new ConsoleHandler();
		logger.addHandler(handler);
		///
		// setBounds(100, 100, 450, 300);
		JPanel contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);
		
		JPanel progressPanel = new JPanel();
		progressPanel.setBorder(new EmptyBorder(3, 0, 0, 0));
		contentPane.add(progressPanel, BorderLayout.SOUTH);
		progressPanel.setLayout(new BorderLayout(0, 0));
		
		JPanel primaryProgress = new JPanel();
		progressPanel.add(primaryProgress, BorderLayout.CENTER);
		primaryProgress.setLayout(new BorderLayout(0, 0));
		
		progressBar = new JProgressBar();
		progressBar.setStringPainted(true);
		progressBar.setMaximum(10000);
		primaryProgress.add(progressBar, BorderLayout.CENTER);
		
		lblStatus = new JLabel();
		primaryProgress.add(lblStatus, BorderLayout.SOUTH);
		
		JPanel logoPanel = new JPanel() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 8686753828984892019L;
			final ImageIcon image = new ImageIcon(BootstrapForm.class.getResource("/bg_main.png"));
			
			@Override
			protected void paintComponent(Graphics g) {
				Image source = this.image.getImage();
				int w = source.getWidth(null);
				int h = source.getHeight(null);
				BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
				Graphics2D g2d = (Graphics2D)image.getGraphics();
				g2d.drawImage(source, 0, 0, null);
				g2d.dispose();
				int width = getWidth();
				int height = getHeight();
				int imageW = image.getWidth(this);
				int imageH = image.getHeight(this);

				// Tile the image to fill our area.
				for (int x = 0; x < width; x += imageW) {
					for (int y = 0; y < height; y += imageH) {
						g.drawImage(image, x, y, this);
					}
				}
			}
			
			
		};
		contentPane.add(logoPanel, BorderLayout.CENTER);
		logoPanel.setLayout(new BorderLayout(0, 0));
		
		JLabel lblLogo = new JLabel("");
		lblLogo.setHorizontalAlignment(SwingConstants.CENTER);
		lblLogo.setIcon(new ImageIcon(BootstrapForm.class.getResource("/mcu-logo-new.png")));
		logoPanel.add(lblLogo, BorderLayout.CENTER);
		
		setSize(480, 250);
	}

	@Override
	public void onQueueFinished(DownloadQueue queue) {
		if (queue.getFailedFileCount() > 0) {
			lblStatus.setText("Failed!");
			StringBuilder msg = new StringBuilder("Failed to download:\n");
			for (Downloadable entry : queue.getFailures()) {
				msg.append("   ").append(entry.getFilename()).append("\n");
			}
			JOptionPane.showMessageDialog(this, msg.toString(),"MCU-Bootstrap",JOptionPane.ERROR_MESSAGE);
			System.exit(-2);
		} else {
			lblStatus.setText("Finished!");
			StringBuilder sbClassPath = new StringBuilder();
			StringBuilder sbModulePath = new StringBuilder();
			StringBuilder sbModules = new StringBuilder();
			if (System.getProperty("os.name").startsWith("Mac")) {
				sbClassPath.append(pathSeparator).append(".");
			}
			for (Library lib : distro.getRelevantLibraries(thisPlatform)) {
				if (lib.getFilename().endsWith("jar")) {
					if (lib.hasModules()) {
						sbModulePath.append(pathSeparator).append((new File(new File(basePath, "lib"), lib.getFilename())).getAbsolutePath());
						for (String module : lib.getModuleNames()) {
							sbModules.append(",").append(module);
						}
					} else {
						sbClassPath.append(pathSeparator).append((new File(new File(basePath, "lib"), lib.getFilename())).getAbsolutePath());
					}
				}
			}
			for (Extract extract : distro.getRelevantExtracts(thisPlatform)) {
				if (extract.hasModules()) {
					sbModulePath.append(pathSeparator).append((new File(basePath, extract.getIncludePath())).getAbsolutePath());
					for (String module : extract.getModuleNames()) {
						sbModules.append(",").append(module);
					}
				} else {
					sbClassPath.append(pathSeparator).append((new File(new File(basePath, "extract"), extract.getIncludePath())).getAbsolutePath()).append(separator).append("*");
				}
			}
			DistributionRuntime runtime = distro.getPrimaryRuntime(thisPlatform);
			String javaBin;
			if (runtime != null) {
				javaBin = basePath.toPath().resolve("runtime").resolve(runtime.getName() + "-" + runtime.getVersion()).resolve(runtime.getExecutable()).toString();
			} else {
				javaBin = "java";
				File binDir;
				if (System.getProperty("os.name").startsWith("Mac")) {
					binDir = new File(new File(System.getProperty("java.home")), "Commands");
				} else {
					binDir = new File(new File(System.getProperty("java.home")), "bin");
				}
				if (binDir.exists()) {
					javaBin = (new File(binDir, "java")).getAbsolutePath();
				}
			}
			try {

				List<String> args = new ArrayList<>();
				args.add(javaBin);
				args.add("-Djavafx.verbose=true");
				if (System.getProperty("os.name").equalsIgnoreCase("Mac OS X")) {
					//args.add("-XstartOnFirstThread");
					args.add("-Xdock:name=" + distro.getFriendlyName());
					args.add("-Xdock:icon=" + (new File(new File(basePath, "lib"), "mcu-icon.icns")).getAbsolutePath());
				}
				if (!sbModules.isEmpty()) {
					args.add("--module-path");
					args.add(sbModulePath.substring(1));
					args.add("--add-modules");
					args.add(sbModules.substring(1));
				}
				args.add("-cp");
				args.add(sbClassPath.substring(1));
				args.add(distro.getMainClass());
				Map<String, String> fields = new HashMap<>();
				StrSubstitutor fieldReplacer = new StrSubstitutor(fields);
				fields.put("defaultPack", defaultPack);
				fields.put("MCURoot", basePath.getAbsolutePath());
				StringBuilder runtimeString = new StringBuilder();
				distro.getRelevantRuntimes(thisPlatform).forEach(entry -> runtimeString.append(runtimeString.isEmpty() ? "" : ";").append(entry.getVersion()).append("#").append(basePath.toPath().resolve("runtime").resolve(String.format("%s-%s",entry.getName(), entry.getVersion())).resolve(entry.getExecutable()).toAbsolutePath()));
				fields.put("runtimes", runtimeString.toString());
				//if (distro.getParams() != null) { args.addAll(Arrays.asList(fieldReplacer.replace(distro.getParams()).split(" ")));}
				if (distro.getParams() != null) {
					String[] fieldArr = distro.getParams().split(" ");
					for (int i = 0; i < fieldArr.length; i++) {
						fieldArr[i] = fieldReplacer.replace(fieldArr[i]);
					}
					args.addAll(Arrays.asList(fieldArr));
				}

				args.addAll(Arrays.asList(this.passthroughParams));
				String[] params = args.toArray(new String[args.size()]);
				if (!debug) {
					Process p = Runtime.getRuntime().exec(params);
					if (p != null) {
						Thread.sleep(5000);
						System.exit(0);
					}
				} else {
					for (String s : args) {
						System.out.print((s.contains(" ") ? ("\"" + s + "\"") : s) + " ");
					}
					System.out.print("\n");
					ProcessBuilder pb = new ProcessBuilder(params);
					pb.redirectErrorStream(true);
					Process p = pb.start();
					Thread.sleep(2000);
					frame.setVisible(false);
					if (p != null) {
						String line;
						BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
						while ((line = input.readLine()) != null) {
							System.out.println(line);
						}
						System.exit(0);
					}
				}
			} catch (IOException | InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void onQueueProgress(DownloadQueue queue) {
		lblStatus.setText("Downloading: " + queue.getName());
		progressBar.setValue((int) (queue.getProgress()*10000.0F));
	}

	@Override
	public void printMessage(String msg) {
		System.out.println(msg);
	}

}
