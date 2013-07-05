package org.smbarbour.mcu;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JLabel;
import javax.swing.ImageIcon;
import javax.swing.JWindow;
import javax.swing.SwingConstants;
import javax.swing.JProgressBar;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

public class BootstrapForm extends JWindow {

	private static final long serialVersionUID = 1L;
	private JPanel contentPane;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
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
					BootstrapForm frame = new BootstrapForm();
					frame.setLocationRelativeTo( null );
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public BootstrapForm() {
		// setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
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
		
		JProgressBar progressBar = new JProgressBar();
		progressBar.setStringPainted(true);
		primaryProgress.add(progressBar, BorderLayout.CENTER);
		progressBar.setValue(65);
		
		JLabel lblStatus = new JLabel("Downloading MCUpdater v3.1.0");
		primaryProgress.add(lblStatus, BorderLayout.SOUTH);
		
		JPanel secondaryProgress = new JPanel();
		progressPanel.add(secondaryProgress, BorderLayout.EAST);
		secondaryProgress.setLayout(new BorderLayout(0, 0));
		
		JProgressBar progressOverall = new JProgressBar();
		progressOverall.setValue(33);
		progressOverall.setStringPainted(true);
		secondaryProgress.add(progressOverall, BorderLayout.CENTER);
		lblStatus.addMouseListener(new MouseListener() {

			@Override
			public void mouseClicked(MouseEvent e) {
				System.exit(0);
			}

			@Override
			public void mousePressed(MouseEvent e) {
			}

			@Override
			public void mouseReleased(MouseEvent e) {
			}

			@Override
			public void mouseEntered(MouseEvent e) {
			}

			@Override
			public void mouseExited(MouseEvent e) {
			}
			
		});
		
		JPanel logoPanel = new JPanel() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 8686753828984892019L;
			ImageIcon image = new ImageIcon(BootstrapForm.class.getResource("/org/smbarbour/mcu/bg_main.png"));
			
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
		lblLogo.setIcon(new ImageIcon(BootstrapForm.class.getResource("/org/smbarbour/mcu/mcu-logo-new.png")));
		logoPanel.add(lblLogo, BorderLayout.CENTER);
		
		setSize(480, 250);
	}

}
