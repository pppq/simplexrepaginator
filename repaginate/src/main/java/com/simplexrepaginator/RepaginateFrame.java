package com.simplexrepaginator;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JProgressBar;
import javax.swing.ProgressMonitor;
import javax.swing.SwingConstants;
import javax.swing.TransferHandler;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.lang.StringUtils;
import org.apache.pdfbox.pdmodel.PDDocument;

/**
 * Main application GUI frame
 * @author robin
 *
 */
public class RepaginateFrame extends JFrame {
	private static final IOFileFilter IS_PDF_FILE = new SuffixFileFilter("pdf", IOCase.INSENSITIVE);
	
	private static final Icon REPAGINATE_ICON = new ImageIcon(RepaginateFrame.class.getResource("repaginate.png"));
	private static final Icon UNREPAGINATE_ICON = new ImageIcon(RepaginateFrame.class.getResource("unrepaginate.png"));
	private static final Icon PDF_1342 = new ImageIcon(RepaginateFrame.class.getResource("pdf1342.png"));
	private static final Icon PDF_1234 = new ImageIcon(RepaginateFrame.class.getResource("pdf1234.png"));
	
	protected JButton input;
	protected JButton repaginate;
	protected JButton unrepaginate;
	protected JButton output;

	protected FileRepaginator repaginator;
	
	public RepaginateFrame(FileRepaginator repaginator) {
		super("Simplex Repaginator version " + Repaginate.getVersion());
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		
		this.repaginator = repaginator;
		
		setJMenuBar(createMenuBar());
		
		input = createInputButton();
		repaginate = createRepaginateButton();
		unrepaginate = createUnrepaginateButton();
		output = creatOutputButton();

		setLayout(new GridBagLayout());

		final GridBagConstraints c = new GridBagConstraints(0, 0, 1, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 0, 0);

		c.gridwidth = 2; add(input, c);

		c.gridy++; c.gridwidth = 1; c.fill = GridBagConstraints.VERTICAL; add(repaginate, c);
		c.gridx++; add(unrepaginate, c);

		c.gridy++; c.gridx = 0; c.gridwidth = 2; c.fill = GridBagConstraints.BOTH; add(output, c);

		Runnable r = new Runnable() {
			@Override
			public void run() {
				UpdateChecker uc = new UpdateChecker();
				try {
					if(!uc.isUpdateAvailable())
						return;
					final JLabel label = new JLabel(
							"<html>Simplex Repaginator version " + uc.getLatestVersion() + " is available.  "
									+ "Choose <b>File > Check For Updates</b> to download.");
					EventQueue.invokeLater(new Runnable() {
						@Override
						public void run() {
							c.gridy++; c.weighty = 0; c.fill = GridBagConstraints.HORIZONTAL;
							add(label, c);
							revalidate();
						}
					});
				} catch(IOException ioe) {
				}
			}
		};
		new Thread(r).start();
		
		pack();
		setSize(800, 400);
	}

	protected JMenuBar createMenuBar() {
		JMenuBar mb = new JMenuBar();
		JMenu m;
		
		m = new JMenu("File");
		m.add(new AbstractAction("Select Input") {
			@Override
			public void actionPerformed(ActionEvent e) {
				input.doClick();
			}
		});
		m.add(new AbstractAction("Select Output") {
			@Override
			public void actionPerformed(ActionEvent e) {
				output.doClick();
			}
		});
		m.add(new UpdateCheckerAction());
		m.add(new AbstractAction("Exit") {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				dispose();
			}
		});
		mb.add(m);
		
		m = new JMenu("Help");
		m.add(new AbstractAction("Website") {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					URL url = new URL("http://www.simplexrepaginator.com/");
					Desktop.getDesktop().browse(url.toURI());
				} catch(IOException ioe) {
				} catch(URISyntaxException urise) {
				}
			}
		});
		m.add(new AbstractAction("About") {
			@Override
			public void actionPerformed(ActionEvent e) {
				String license;
				try {
					license = IOUtils.toString(RepaginateFrame.class.getResource("LICENSE.txt"));
				} catch(IOException ioe) {
					license = "An error occured reading the license file:\n" + ioe;
				}
				JOptionPane.showMessageDialog(RepaginateFrame.this, license);
			}
		});
		mb.add(m);
		
		return mb;
	}
	
	protected JButton createRepaginateButton() {
		JButton b = new JButton("<html><center>Click to<br>repaginate", REPAGINATE_ICON);

		b.setHorizontalTextPosition(SwingConstants.LEFT);
		b.setIconTextGap(25);
		
		b.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					int[] documentsPages = repaginator.repaginate();
					JOptionPane.showMessageDialog(
							RepaginateFrame.this,
							"Repaginated " + documentsPages[0] + " documents with " + documentsPages[1] + " pages.",
							"Repagination Complete",
							JOptionPane.INFORMATION_MESSAGE);
				} catch(Exception ex) {
					ex.printStackTrace();
					JOptionPane.showMessageDialog(
							RepaginateFrame.this,
							ex.toString(),
							"Error During Repagination",
							JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		
		return b;
	}

	protected JButton createUnrepaginateButton() {
		JButton b = new JButton("<html><center>Click to<br>un-repaginate", UNREPAGINATE_ICON);

		b.setHorizontalTextPosition(SwingConstants.RIGHT);
		b.setIconTextGap(25);
		
		b.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					int[] documentsPages = repaginator.unrepaginate();
					JOptionPane.showMessageDialog(
							RepaginateFrame.this,
							"Unrepaginated " + documentsPages[0] + " documents with " + documentsPages[1] + " pages.",
							"Unrepagination Complete",
							JOptionPane.INFORMATION_MESSAGE);
				} catch(Exception ex) {
					ex.printStackTrace();
					JOptionPane.showMessageDialog(
							RepaginateFrame.this,
							ex.toString(),
							"Error During Unrepagination",
							JOptionPane.ERROR_MESSAGE);
				}
			}
		});

		return b;
	}
	
	protected JButton createInputButton() {
		JButton b = new JButton("Click or drag to set input files", PDF_1342);

		b.setHorizontalTextPosition(SwingConstants.RIGHT);
		b.setIconTextGap(25);
		
		b.setTransferHandler(new InputButtonTransferHandler());
		
		b.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser chooser = new JFileChooser();
				chooser.setMultiSelectionEnabled(true);
				chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
				if(chooser.showOpenDialog(RepaginateFrame.this) != JFileChooser.APPROVE_OPTION)
					return;
				setInput(Arrays.asList(chooser.getSelectedFiles()));
				if(JOptionPane.showConfirmDialog(
						RepaginateFrame.this, 
						"Use input paths as output paths?", 
						"Use Input As Output?", 
						JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
					setOutput(new ArrayList<File>(repaginator.getInputFiles()));
				}
			}
		});

		return b;
	}

	protected JButton creatOutputButton() {
		JButton b = new JButton("Click or drag to set output file", PDF_1234);

		b.setHorizontalTextPosition(SwingConstants.LEFT);
		b.setIconTextGap(25);
		
		b.setTransferHandler(new OutputButtonTransferHandler());
		
		b.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser chooser = new JFileChooser();
				chooser.setMultiSelectionEnabled(false);
				chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
				if(chooser.showOpenDialog(RepaginateFrame.this) != JFileChooser.APPROVE_OPTION)
					return;
				repaginator.setOutputFiles(Arrays.asList(chooser.getSelectedFiles()));
				output.setText("<html><center>" + StringUtils.join(repaginator.getOutputFiles(), "<br>"));
			}
		});

		return b;
	}
	
	protected void setInput(List<File> files) {
		repaginator.setInputFiles(files);
		input.setText("<html><p align=left>" + StringUtils.join(repaginator.getInputFiles(), "<br>"));
	}
	
	protected void setOutput(List<File> files) {
		repaginator.setOutputFiles(files);
		output.setText("<html><p align=right>" + StringUtils.join(repaginator.getOutputFiles(), "<br>"));
	}

	private class UpdateCheckerAction extends AbstractAction {
		private UpdateCheckerAction() {
			super("Check For Updates");
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			Runnable r = new Runnable() {
				@Override
				public void run() {
					final JDialog pmd = new JDialog(RepaginateFrame.this);
					pmd.setTitle("Checking for Updates");
					pmd.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
					pmd.setLayout(new BorderLayout());
					JProgressBar pb = new JProgressBar();
					pb.setIndeterminate(true);
					pb.setStringPainted(true);
					pb.setString("Checking for updates...");
					pmd.add(pb, BorderLayout.CENTER);
					pmd.pack();
					pmd.setSize(300, 100);
					EventQueue.invokeLater(new Runnable() {
						@Override
						public void run() {
							pmd.setVisible(true);
							pmd.setLocationRelativeTo(RepaginateFrame.this);
						}
					});
					try {
						final URL url = new UpdateChecker().getUpdateURL();
						EventQueue.invokeLater(new Runnable() {
							@Override
							public void run() {
								if(pmd.isVisible()) {
									pmd.setVisible(false);
									updateChecked(url);
								}
							}
						});
					} catch(IOException ioe) {
					} finally {
						EventQueue.invokeLater(new Runnable() {
							@Override
							public void run() {
								pmd.setVisible(false);
							}
						});
					}
				}
			};
			new Thread(r).start();
		}
		
		public void updateChecked(URL updated) {
			try {
				if(updated == null) {
					JOptionPane.showMessageDialog(
							RepaginateFrame.this,
							"You are already running the latest version of Simplex Repaginator",
							"No Update Available",
							JOptionPane.INFORMATION_MESSAGE);
					return;
				}
				if(JOptionPane.showConfirmDialog(
						RepaginateFrame.this,
						"An udated version of Simplex Repaginator is available at:\n\n" 
						+ updated
						+ "\n\nDownload new version?",
						"Update Available",
						JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
					Desktop.getDesktop().browse(updated.toURI());
				}
			} catch(Exception ex) {
				JOptionPane.showMessageDialog(
						RepaginateFrame.this,
						"Unable to check for updates: " + ex,
						"Update Check Failed",
						JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	protected class InputButtonTransferHandler extends TransferHandler {
		@Override
		public boolean canImport(TransferSupport support) {
			for(DataFlavor f : support.getDataFlavors()) {
				if(DataFlavor.javaFileListFlavor.equals(f))
					return true;
			}
			return false;
		}

		@Override
		public boolean importData(TransferSupport support) {
			try {
				setInput((List<File>) support.getTransferable().getTransferData(DataFlavor.javaFileListFlavor));
				if(JOptionPane.showConfirmDialog(
						RepaginateFrame.this, 
						"Use input paths as output paths?", 
						"Use Input As Output?", 
						JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION)
					setOutput(new ArrayList<File>(repaginator.getInputFiles()));
				return true;
			} catch(IOException ioe) {
			} catch(UnsupportedFlavorException ufe) {
			}
			return false;
		}
	}

	protected class OutputButtonTransferHandler extends TransferHandler {
		@Override
		public boolean canImport(TransferSupport support) {
			for(DataFlavor f : support.getDataFlavors()) {
				if(DataFlavor.javaFileListFlavor.equals(f))
					return true;
			}
			return false;
		}

		@Override
		public boolean importData(TransferSupport support) {
			try {
				setOutput((List<File>) support.getTransferable().getTransferData(DataFlavor.javaFileListFlavor));
				return true;
			} catch(IOException ioe) {
			} catch(UnsupportedFlavorException ufe) {
			}
			return false;
		}
	}
}

