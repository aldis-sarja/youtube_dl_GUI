package com.asarja.youtube_dl_GUI;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.Dimension;
import javax.swing.JFrame;
import javax.swing.JTextField;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.BoxLayout;
import javax.swing.Box;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JFileChooser;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.File;

import java.util.regex.Pattern;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

public class MainApp implements  ActionListener {
	JPanel mainPanel;
	JPanel youtubeProgressPanel;
	JTextField urlInput;
	JRadioButton audioVideoRB, videoRB, audioRB, combineRB, defaultRB;
	ButtonGroup optionRadioGroup;
	JComboBox<String> audioVideoComboBox, videoComboBox, audioComboBox;
	File rememberPah = new File("");
	static String youtubecmd = "youtube-dl";
	final static Pattern regexVideoInfo = Pattern.compile("\\d+{3,5}p|\\d+{3,5}x\\d+{3,5}");
	final static Pattern regexGarbage = Pattern.compile("&.+$");
	final static Pattern regexFormatCode = Pattern.compile("^[\\w\\-]+");
	final static int FRAME_MIN_X_SIZE = 300;
	final static int FRAME_PREF_X_SIZE = 450;
	final static int NORMAL_SPACING = 8;
	final static int SMALL_SPACING = 4;
	final static int NOTHING = 0;
	final static int AUDIO_VIDEO = 1;
	final static int VIDEO_ONLY = 2;
	final static int AUDIO_ONLY = 3;

	public JPanel createMainPanel() {
		mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.PAGE_AXIS));
		mainPanel.setMinimumSize(new Dimension(FRAME_MIN_X_SIZE, 60));
		mainPanel.setPreferredSize(new Dimension(FRAME_PREF_X_SIZE, 60));

		mainPanel.add(Box.createVerticalStrut(NORMAL_SPACING));

		// Download Action Panel
		JPanel downloadPanel = new JPanel();
		downloadPanel.setLayout(new BoxLayout(downloadPanel, BoxLayout.LINE_AXIS));
		downloadPanel.setMinimumSize(new Dimension(FRAME_MIN_X_SIZE, 20));
		downloadPanel.setPreferredSize(new Dimension(FRAME_PREF_X_SIZE, 20));
		downloadPanel.setMaximumSize(new Dimension(Short.MAX_VALUE, 20));
		downloadPanel.add(Box.createHorizontalStrut(NORMAL_SPACING));
		downloadPanel.add(new JLabel("URL:", SwingConstants.LEFT));

		urlInput = new JTextField(20);
		urlInput.setMinimumSize(new Dimension(50, 20));
		urlInput.setMaximumSize(new Dimension(Short.MAX_VALUE, 20));
		urlInput.setToolTipText("Ctrl-V here! I know, You are trying to invoke right mouse pop-up menu. It won\'t work! Just push damn Ctrl-V!");
		downloadPanel.add(Box.createHorizontalStrut(SMALL_SPACING));
		downloadPanel.add(urlInput);

		downloadPanel.add(Box.createHorizontalStrut(NORMAL_SPACING));
		JButton getInfoButton = new JButton("Get info");
		downloadPanel.add(getInfoButton);

		downloadPanel.add(Box.createHorizontalStrut(NORMAL_SPACING));
		JButton downloadButton = new JButton("Download video");
		//downloadButton.setEnabled(false);
		downloadPanel.add(downloadButton);

		getInfoButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent ae) {
					String url = urlInput.getText();
					if (url.length() == 0)
						return;
					
					//downloadButton.setEnabled(false);
					audioVideoComboBox.removeAllItems();
					videoComboBox.removeAllItems();
					audioComboBox.removeAllItems();
					url = urlStripGarbage(url);
					urlInput.setText(url);
					Result result = getVideoInfo(url);
					if (result.success) {
						//downloadButton.setEnabled(true);
						audioVideoRB.doClick();
					} else {
						JOptionPane.showMessageDialog(null, result.errorMessage, "Error!", JOptionPane.ERROR_MESSAGE);
					}
				}
			});

		downloadButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent ae) {
					if (urlInput.getText().length() < 1)
						return;
					//String youtubecmd = "youtube-dl";
					List<String> cmdArgs = new ArrayList<>();

					cmdArgs.add(youtubecmd);
					if (!defaultRB.isSelected()) {
						cmdArgs.add("-f");
						String selected;

						if (audioVideoRB.isSelected()) {
								selected = (String)audioVideoComboBox.getSelectedItem();
								if (selected == null)
									return;
								cmdArgs.add(getFormatCode(selected));

							} else if (videoRB.isSelected()) {
								selected = (String)videoComboBox.getSelectedItem();
								if (selected == null)
									return;
								cmdArgs.add(getFormatCode(selected));

						} else if (audioRB.isSelected()) {
							selected = (String)audioComboBox.getSelectedItem();
							if (selected == null)
								return;
							cmdArgs.add(getFormatCode(selected));

						} else if (combineRB.isSelected()) {
							selected = (String)videoComboBox.getSelectedItem();
							if (selected == null)
								return;
							StringBuilder arg = new StringBuilder("");
							arg.append(getFormatCode(selected));
							arg.append("+");
							selected = (String)audioComboBox.getSelectedItem();
							arg.append(getFormatCode(selected));
							cmdArgs.add(arg.toString());
						}
					}
					cmdArgs.add(urlInput.getText());
					JFileChooser pathChooser = new JFileChooser(rememberPah);
					pathChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
					int returnval = pathChooser.showDialog(null, "Choose directory for downloading video");
					if (returnval != JFileChooser.APPROVE_OPTION)
							return;

					rememberPah = pathChooser.getSelectedFile();
					youtubeProgressPanel.invalidate();
					youtubeProgressPanel.add(new Downloader(cmdArgs, rememberPah, youtubeProgressPanel));
					youtubeProgressPanel.validate();
				}
			});

		mainPanel.add(downloadPanel);

		// Option Panel
		JPanel optionsPanel = new JPanel();
		optionsPanel.setLayout(new BoxLayout(optionsPanel, BoxLayout.LINE_AXIS));
		optionsPanel.setMinimumSize(new Dimension(FRAME_MIN_X_SIZE, 130));
		optionsPanel.setPreferredSize(new Dimension(FRAME_PREF_X_SIZE, 130));
		optionsPanel.setMaximumSize(new Dimension(Short.MAX_VALUE, 130));

		// Radio Buttons Panel
		JPanel radioButtonsPanel = new JPanel();
		radioButtonsPanel.setLayout(new BoxLayout(radioButtonsPanel, BoxLayout.PAGE_AXIS));
		radioButtonsPanel.setMinimumSize(new Dimension(114, 130));
		radioButtonsPanel.setMaximumSize(new Dimension(114, 130));

		optionRadioGroup = new ButtonGroup();
		// Audio & Video
		audioVideoRB = new JRadioButton("Audio & Video", true);
		audioVideoRB.addActionListener(this);
		optionRadioGroup.add(audioVideoRB);
		radioButtonsPanel.add(Box.createVerticalStrut(NORMAL_SPACING));
		radioButtonsPanel.add(audioVideoRB);

		// Video only
		videoRB = new JRadioButton("Video only");
		videoRB.addActionListener(this);
		optionRadioGroup.add(videoRB);
		radioButtonsPanel.add(videoRB);

		// Audio only
		audioRB = new JRadioButton("Audio only");
		audioRB.addActionListener(this);
		optionRadioGroup.add(audioRB);
		radioButtonsPanel.add(audioRB);

		// Combine Option
		combineRB = new JRadioButton("Combine");
		combineRB.addActionListener(this);
		optionRadioGroup.add(combineRB);
		radioButtonsPanel.add(combineRB);

		// Default Format
		defaultRB = new JRadioButton("Default");
		defaultRB.setToolTipText("If nothing works, try to download video with youtube-dl default option!");
		defaultRB.addActionListener(this);
		optionRadioGroup.add(defaultRB);
		radioButtonsPanel.add(defaultRB);

		optionsPanel.add(Box.createHorizontalStrut(NORMAL_SPACING));
		optionsPanel.add(radioButtonsPanel);

		// Combo Box List Panel
		JPanel comboBoxPanel = new JPanel();
		comboBoxPanel.setLayout(new BoxLayout(comboBoxPanel, BoxLayout.PAGE_AXIS));
		comboBoxPanel.setMinimumSize(new Dimension(100, 100));
		//comboBoxPanel.setPreferredSize(new Dimension(100, 100));
		comboBoxPanel.setMaximumSize(new Dimension(Short.MAX_VALUE, 100));

		audioVideoComboBox = new JComboBox<>();
		audioVideoComboBox.setMinimumSize(new Dimension(50, 20));
		//audioVideoComboBox.setPreferredSize(new Dimension(50, 20));
		audioVideoComboBox.setMaximumSize(new Dimension(Short.MAX_VALUE, 20));
		comboBoxPanel.add(audioVideoComboBox);

		videoComboBox = new JComboBox<>();
		videoComboBox.setMinimumSize(new Dimension(50, 20));
		//videoComboBox.setPreferredSize(new Dimension(50, 20));
		videoComboBox.setMaximumSize(new Dimension(Short.MAX_VALUE, 20));
		comboBoxPanel.add(Box.createVerticalStrut(SMALL_SPACING));
		comboBoxPanel.add(videoComboBox);

		audioComboBox = new JComboBox<>();
		audioComboBox.setMinimumSize(new Dimension(50, 20));
		//audioComboBox.setPreferredSize(new Dimension(50, 20));
		audioComboBox.setMaximumSize(new Dimension(Short.MAX_VALUE, 20));
		comboBoxPanel.add(Box.createVerticalStrut(SMALL_SPACING));
		comboBoxPanel.add(audioComboBox);

		videoComboBox.setEnabled(false);
		audioComboBox.setEnabled(false);

		optionsPanel.add(comboBoxPanel);

		mainPanel.add(Box.createVerticalStrut(NORMAL_SPACING));
		mainPanel.add(optionsPanel);

		mainPanel.add(Box.createVerticalStrut(NORMAL_SPACING));

		JButton updateButton = new JButton("Update youtube-dl");
		updateButton.setToolTipText("If nothing, nothing works, try to update youtube-dl! But before that, kill all youtube-dl processes!");
		mainPanel.add(updateButton);

		updateButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent ae) {
					updateYoutubedl();
				}
			});

		mainPanel.add(Box.createVerticalStrut(NORMAL_SPACING));
		youtubeProgressPanel = new JPanel();
		youtubeProgressPanel.setLayout(new BoxLayout(youtubeProgressPanel, BoxLayout.PAGE_AXIS));
		youtubeProgressPanel.setMinimumSize(new Dimension(FRAME_MIN_X_SIZE, 40));
		mainPanel.add(new JScrollPane(youtubeProgressPanel));//, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED));

		return mainPanel;
	}
	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == audioVideoRB) {
			audioVideoComboBox.setEnabled(true);
			videoComboBox.setEnabled(false);
			audioComboBox.setEnabled(false);
		} else if (e.getSource() == audioRB) {
			audioVideoComboBox.setEnabled(false);
			videoComboBox.setEnabled(false);
			audioComboBox.setEnabled(true);
		} else if (e.getSource() == videoRB) {
			audioVideoComboBox.setEnabled(false);
			videoComboBox.setEnabled(true);
			audioComboBox.setEnabled(false);
		} else if (e.getSource() == combineRB) {
			audioVideoComboBox.setEnabled(false);
			videoComboBox.setEnabled(true);
			audioComboBox.setEnabled(true);
		} else if (e.getSource() == defaultRB) {
			audioVideoComboBox.setEnabled(false);
			videoComboBox.setEnabled(false);
			audioComboBox.setEnabled(false);
		}
	}

	private String urlStripGarbage(String url) {
		Matcher matcher = regexGarbage.matcher(url);
		return matcher.replaceAll("");
	}

	private int parseInfoText(String line) {
		if (line.contains("audio only"))
			return AUDIO_ONLY;
		if (line.contains("video only"))
			return VIDEO_ONLY;
		Matcher matcher = regexVideoInfo.matcher(line);
		if (matcher.find())
			return AUDIO_VIDEO;
		return NOTHING;
	}

	private Result getVideoInfo(String url) {
		Result result = new Result();
		ProcessBuilder procBuild = new ProcessBuilder("youtube-dl", "-F", url);
		StringBuilder possibleErrorOutput = new StringBuilder("");

		try {
			Process proc = procBuild.start();

			try (BufferedReader stdInput =
				 new BufferedReader(new InputStreamReader(proc.getInputStream()));
				 BufferedReader stdError =
				 new BufferedReader(new InputStreamReader(proc.getErrorStream()));) {

				String line = null;

				while ((line = stdInput.readLine()) != null) {
					int res = parseInfoText(line);
					switch (res) {
					case AUDIO_VIDEO: {
						result.success = true;
						audioVideoComboBox.addItem(line);
						break;
					}
					case VIDEO_ONLY: {
						result.success = true;
						videoComboBox.addItem(line);
						break;
					}
					case AUDIO_ONLY: {
						result.success = true;
						audioComboBox.addItem(line);
						break;
					}
					}
				}
				line = null;
				while ((line = stdError.readLine()) != null) {
					possibleErrorOutput.append(line + "\n");
				}
			} catch (Exception e) {
				possibleErrorOutput.append(e.getMessage() + "\n");
			}

		} catch (Exception e) {
			possibleErrorOutput.append(e.getMessage() + "\n");
		}
		result.errorMessage = possibleErrorOutput.toString();
		return result;
	}

	private String getFormatCode(String line) {
		Matcher matcher = regexFormatCode.matcher(line);
		matcher.find();
		return matcher.group();
	}

	private void updateYoutubedl() {
		ProcessBuilder procBuild = new ProcessBuilder("youtube-dl", "--update");
		StringBuilder possibleErrorOutput = new StringBuilder("");
		StringBuilder msg = new StringBuilder("");

		try {
			Process proc = procBuild.start();
			try (BufferedReader stdInput =
				 new BufferedReader(new InputStreamReader(proc.getInputStream()));
				 BufferedReader stdError =
				 new BufferedReader(new InputStreamReader(proc.getErrorStream()));) {

				String line = null;
				while ((line = stdInput.readLine()) != null) {
					msg.append(line + "\n");
				}
				line = null;
				while ((line = stdError.readLine()) != null) {
					possibleErrorOutput.append(line + "\n");
				}
			} catch (Exception e) {
				possibleErrorOutput.append(e.getMessage() + "\n");
			}

		} catch (Exception e) {
			possibleErrorOutput.append(e.getMessage() + "\n");
		}

		if (possibleErrorOutput.length() > 0) {
			JOptionPane.showMessageDialog(null, possibleErrorOutput.toString(), "Error", JOptionPane.ERROR_MESSAGE);
		} else if (msg.length() > 0) {
			JOptionPane.showMessageDialog(null, msg.toString(), "Info", JOptionPane.INFORMATION_MESSAGE);
			return;
		} else {
			JOptionPane.showMessageDialog(null, "Unknow error!\nTerminate all youtube-dl processes, before updating!",
										  "Error!", JOptionPane.ERROR_MESSAGE);
		}
	}


	public static void main(String[] args) {
		File youtube_dlFile = new File("youtube-dl.exe");
		if (youtube_dlFile.isFile()) {
			youtubecmd = youtube_dlFile.getAbsoluteFile().toString();
		}
		SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					makeGui();
				}
			});
	}

	private static void makeGui() {
		JFrame frame = new JFrame("Simple youtube-dl GUI frontend");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		MainApp mainPanel = new MainApp();

		frame.setContentPane(mainPanel.createMainPanel());
		frame.setMinimumSize(new Dimension(FRAME_MIN_X_SIZE, 200));
		frame.setPreferredSize(new Dimension(FRAME_PREF_X_SIZE, 400));
		frame.pack();
		frame.setVisible(true);
	}


	class Result {
		boolean success;
		String errorMessage;
		Result() {
			success = false;
		}
	}
}
