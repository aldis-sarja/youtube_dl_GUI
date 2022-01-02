package com.asarja.youtube_dl_GUI;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.Dimension;
import java.awt.Color;
// import javax.swing.JTextField;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.JTextArea;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.BoxLayout;
import javax.swing.Box;
import javax.swing.JProgressBar;
import javax.swing.JOptionPane;
// import javax.swing.SwingConstants;
// import javax.swing.SwingUtilities;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.File;

import java.util.regex.Pattern;
import java.util.List;
import java.util.regex.Matcher;

public class Downloader extends  JPanel {
	Downloader me;
	JPanel parrentPanel;
	JLabel fileName;
	JLabel progressText;
	JTextArea errMessages;
	JProgressBar progressBar;
	Process proc;
	final static int NORMAL_SPACING = 8;
	final static int SMALL_SPACING = 4;
	final static int FRAME_MIN_X_SIZE = 320;
	final static int FRAME_PREF_X_SIZE = 400;
	final static Pattern regexFileName = Pattern.compile("\\[download\\] Destination:\\s+(.+$)");
	// final static Pattern regexProgressPercent = Pattern.compile("\\[download\\]\\s+(\\d+)");
	final static Pattern regexProgressPercent = Pattern.compile("\\d+");
	final static Pattern regexProgressLine = Pattern.compile("\\[download\\]\\s+(.+$)");

	public Downloader(List<String> cmdArgs, File path, JPanel parrentPanel) {
		super();
		me = this;
		this.parrentPanel = parrentPanel;

		setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
		//setMinimumSize(new Dimension(FRAME_MIN_X_SIZE, 40));
		setMinimumSize(new Dimension(200, 40));
		//setPreferredSize(new Dimension(FRAME_PREF_X_SIZE, 20));
		//setMaximumSize(new Dimension(Short.MAX_VALUE, 20));

		JPanel firstColumn = new JPanel();
		firstColumn.setLayout(new BoxLayout(firstColumn, BoxLayout.PAGE_AXIS));

		JPanel progressPanel = new JPanel();
		progressPanel.setLayout(new BoxLayout(progressPanel, BoxLayout.LINE_AXIS));

		JButton removeButton = new JButton("REMOVE");
		removeButton.setForeground(Color.RED);
		//removeButton.setMinimumSize(new Dimension(50, 20));
		progressPanel.add(removeButton);

		progressPanel.add(Box.createHorizontalStrut(NORMAL_SPACING));

		progressBar = new JProgressBar(0, 100);
		progressBar.setMinimumSize(new Dimension(200, 20));
		progressBar.setMaximumSize(new Dimension(Short.MAX_VALUE, 20));
		progressBar.setValue(0);
		progressBar.setStringPainted(true);
		progressPanel.add(progressBar);


		fileName = new JLabel();
		fileName.setMinimumSize(new Dimension(200, 20));
		fileName.setMaximumSize(new Dimension(Short.MAX_VALUE, 20));
		JPanel fileNamePanel = new JPanel();
		fileNamePanel.setLayout(new BoxLayout(fileNamePanel, BoxLayout.LINE_AXIS));
		fileNamePanel.add(fileName);

		firstColumn.add(Box.createVerticalStrut(NORMAL_SPACING));

		firstColumn.add(fileNamePanel);
		firstColumn.add(Box.createVerticalStrut(SMALL_SPACING));
		firstColumn.add(progressPanel);

		add(Box.createHorizontalStrut(NORMAL_SPACING));
		add(firstColumn);

		add(Box.createHorizontalStrut(NORMAL_SPACING));

		errMessages = new JTextArea();
		errMessages.setMinimumSize(new Dimension(200, 40));
		errMessages.setMaximumSize(new Dimension(Short.MAX_VALUE, 60));
		errMessages.setForeground(Color.RED);
		errMessages.setEditable(false);
		errMessages.setToolTipText("This is Error Box! Sometimes it shows strange warnings, while video is normally downloading. It\'s OK.");
		JScrollPane scrollBar = new JScrollPane(errMessages);
		scrollBar.setMinimumSize(new Dimension(200, 40));
		scrollBar.setMaximumSize(new Dimension(Short.MAX_VALUE, 60));
		add(scrollBar);


		removeButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent ae) {
					boolean running = false;
					int answer = 0;
					try {
						proc.exitValue();
					} catch (Exception e) { running = true; }
					if (running) {
						answer = JOptionPane.showConfirmDialog(null, "Do You want to kill downloading process?",
															   "Are You sure?", JOptionPane.YES_NO_OPTION);

						if (answer == 0)
							proc.destroy();
					}
					if (answer == 0) {
						parrentPanel.invalidate();
						parrentPanel.remove(me);
						//me.getParrent().remove(me);
						parrentPanel.validate();
					}
				}
			});

		DownloaderThread downloaderThread = new DownloaderThread(cmdArgs, path);
		downloaderThread.start();
	}

	private void updateFileName(String message) {
		SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					fileName.setText(message);
				}
			});
	}

	private void updateProgress(String message) {
		SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					progressBar.setString(message);
					Matcher matcher = regexProgressPercent.matcher(message);
					if (matcher.find()) {
						String number = matcher.group();
						Integer percent = tryParseInt(number);
						if (percent != null) {
							progressBar.setValue(percent);
						}
					}
				}
			});
	}

	private void updateErrMessages(String message) {
		SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					errMessages.append(message + "\n");
				}
			});
	}

	class DownloaderThread extends Thread {
		List<String> cmdArgs;
		File path;
		DownloaderThread(List<String> cmdArgs, File path) {
			this.cmdArgs = cmdArgs;
			this.path = path;
		}
		@Override
		public void run() {
			ProcessBuilder procBuild = new ProcessBuilder(cmdArgs);
			procBuild.directory(path); // uzstādam darba direktoriju
			try {
				proc = procBuild.start();
				try   (BufferedReader stdInput =
					   new BufferedReader(new InputStreamReader(proc.getInputStream()));
					   BufferedReader stdError =
					   new BufferedReader(new InputStreamReader(proc.getErrorStream()));) {
					ErrReceiver errorThread = new ErrReceiver(stdError);
					errorThread.start();
					String line = null;
					Matcher matcher;
					while ((line = stdInput.readLine()) != null) {
						matcher = regexFileName.matcher(line);
						if (matcher.find()) {
							updateFileName(matcher.group(1));
						} else {
							matcher = regexProgressLine.matcher(line);
							if (matcher.find()) {
								line = matcher.group(1);
								updateProgress(line);
							}
						}
					}

				} catch (Exception e) {
					updateErrMessages(e.getMessage());
				}
			} catch (Exception e) {
				updateErrMessages(e.getMessage());
			}

			SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						if (progressBar.getValue() < 100) {
							progressBar.setForeground(Color.RED);
							progressBar.setString("ERROR! Check the error box!");
						} else {
							progressBar.setString("OK!");
						}

					}
				});
		}


		class ErrReceiver extends Thread {
			BufferedReader stdError;
			ErrReceiver(BufferedReader stdError) {
				this.stdError = stdError;
			}
			@Override
			public void run() {
				String errLine = null;
				try {
					while ((errLine = stdError.readLine()) != null) {
						updateErrMessages(errLine);
					}
				} catch (Exception e) {}
			}
		}
	}

	static Integer tryParseInt(String txt) {
		try {
			return Integer.parseInt(txt);
		} catch (NumberFormatException e) {
			return null;
		}
	}
}
