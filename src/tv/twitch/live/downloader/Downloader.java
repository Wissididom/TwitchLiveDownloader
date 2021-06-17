package tv.twitch.live.downloader;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.Callable;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import tv.twitch.live.util.StreamNotFoundException;

public class Downloader {
	public static final int GUI = 0;
	public static final int DOWNLOAD = 1;
	public static final int PREVIEW = 2;
	public static final int SHOW_URL = 3;
	public static final int COPY_URL = 4;
	
	private static int mode = Downloader.GUI;
	private static String lastException = "";
	
	public static void printUsage(PrintStream stream) {
		stream.println("Usage:");
		stream.println("TwitchLiveDownloader.jar gui <url> <destination.extension> [<--hide_url|-hu>|<--hide_banner|-hb>]*");
		stream.println("TwitchLiveDownloader.jar gui <url> [<--hide_url|-hu>|<--hide_banner|-hb>]*");
		stream.println("TwitchLiveDownloader.jar gui [<--hide_url|-hu>|<--hide_banner|-hb>]*");
		stream.println("TwitchLiveDownloader.jar [<--hide_url|-hu>|<--hide_banner|-hb>]*");
		stream.println("TwitchLiveDownloader.jar download <url> <destination.extension> [<--hide_url|-hu>|<--hide_banner|-hb>|<--wait_for|-wf>]*");
		stream.println("TwitchLiveDownloader.jar preview <url> [<--hide_url|-hu>|<--hide_banner|-hb>|<--wait_for|-wf>]*");
		stream.println("TwitchLiveDownloader.jar show_url <url>");
		stream.println("TwitchLiveDownloader.jar copy_url <url>");
	}
	
	public static void main(String[] args) throws IOException {
		boolean showUrl = true;
		boolean showBanner = true;
		boolean waitFor = false;
		if (args.length > 0) {
			switch (args[0].toLowerCase()) {
			case "gui":
				Downloader.mode = Downloader.GUI;
				break;
			case "download":
				Downloader.mode = Downloader.DOWNLOAD;
				break;
			case "preview":
				Downloader.mode = Downloader.PREVIEW;
				break;
			case "show_url":
				Downloader.mode = Downloader.SHOW_URL;
				break;
			case "copy_url":
				Downloader.mode = Downloader.COPY_URL;
				break;
			default:
				Downloader.printUsage(System.err);
				return;
			}
		}
		if (Downloader.mode == Downloader.GUI)
			showUrl = false;
		for (String arg : args) {
			if (arg.equalsIgnoreCase("--hide_url") || arg.equalsIgnoreCase("-hu"))
				showUrl = false;
			if (arg.equalsIgnoreCase("--show_url") || arg.equalsIgnoreCase("-su"))
				showUrl = true;
			if (arg.equalsIgnoreCase("--hide_banner") || arg.equalsIgnoreCase("-hb"))
				showBanner = false;
			if (arg.equalsIgnoreCase("--show_banner") || arg.equalsIgnoreCase("-sb"))
				showBanner = true;
			if (arg.equalsIgnoreCase("--wait_for") || arg.equalsIgnoreCase("-wf"))
				waitFor = true;
		}
		if (args.length > 1) {
			switch (Downloader.mode) {
			case Downloader.GUI:
				String url = args[1];
				String file = null;
				if (args.length > 2)
					file = args[2];
				Downloader.gui(url, file, showUrl, showBanner);
				break;
			case Downloader.DOWNLOAD:
				if (args.length > 2)
					Downloader.download(args[1], new File(args[2]), System.out, showUrl, showBanner, waitFor);
				else
					Downloader.printUsage(System.err);
				break;
			case Downloader.PREVIEW:
				Downloader.preview(args[1], System.out, showUrl, showBanner, waitFor);
				break;
			case Downloader.SHOW_URL:
				Downloader.getUrl(args[1], showUrl); // Makes no sense if --hide_url or -hu is set
				break;
			case Downloader.COPY_URL:
				try {
					String downloadUrl = Downloader.getUrl(args[1]);
					if (downloadUrl.startsWith("http")) {
						StringSelection selection = new StringSelection(downloadUrl);
						Clipboard cb = Toolkit.getDefaultToolkit().getSystemClipboard();
						cb.setContents(selection, (Clipboard clipboard, Transferable contents) -> {
							System.exit(0);
						});
						System.out.println("Copied! This application exits when you copy something else because of a limitation on Ubuntu.");
						while (true) {
							try {
								Thread.sleep(5000);
							} catch (InterruptedException ex) {
								ex.printStackTrace();
							}
						}
					} else
						System.out.println("Error (Check your entered values or internet connection): " + downloadUrl);
				} catch (IOException ex) {
					ex.printStackTrace();
				}
				break;
			}
		} else {
			Downloader.gui(null, null, showUrl, showBanner);
		}
	}
	
	public static String getHtml(String text) {
		return "<html><body><p style='width: 400px;'>" + text + "</p></body></html>";
	}
	
	public static boolean checkAccessible(String downloadUrl) {
		try {
			HttpURLConnection connection = (HttpURLConnection) new URL(downloadUrl).openConnection();
			connection.setRequestMethod("HEAD");
			int responseCode = connection.getResponseCode();
			if (responseCode == 200)
				return true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	public static Process getPreviewProcess(String url, boolean printUrlToConsole, boolean showBanner) throws IOException {
		List<String> command = new ArrayList<String>();
		command.add("ffplay");
		if (!showBanner)
			command.add("-hide_banner");
		command.add("-autoexit");
		command.add("-i");
		String downloadUrl = Downloader.getUrl(url, printUrlToConsole);
		if (!downloadUrl.startsWith("http")) {
			System.err.println(downloadUrl);
			return null;
		}
		command.add(downloadUrl);
		if (!Downloader.checkAccessible(downloadUrl))
			throw new StreamNotFoundException();
		ProcessBuilder pb = new ProcessBuilder(command.toArray(new String[command.size()]));
		pb.redirectErrorStream(true);
		return pb.start();
	}
	
	public static Process getDownloadProcess(String url, File file, boolean printUrlToConsole, boolean showBanner) throws IOException {
		List<String> command = new ArrayList<String>();
		command.add("ffmpeg");
		if (!showBanner)
			command.add("-hide_banner");
		command.add("-i");
		String downloadUrl = Downloader.getUrl(url, printUrlToConsole);
		if (!downloadUrl.startsWith("http")) {
			System.err.println(downloadUrl);
			return null;
		}
		command.add(downloadUrl);
		command.add(file.getAbsolutePath());
		ProcessBuilder pb = new ProcessBuilder(command.toArray(new String[command.size()]));
		pb.redirectErrorStream(true);
		if (!Downloader.checkAccessible(downloadUrl))
			throw new StreamNotFoundException();
		return pb.start();
	}
	
	public static void gui(String url, String file, boolean showUrl, boolean showBanner) {
		JFrame frame = new JFrame("TwitchLiveDownloader");
		frame.setBounds(100, 100, 600, 200);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLayout(null);
		frame.setLocationRelativeTo(null);
		frame.setResizable(false);
		JLabel lblUrl = new JLabel("URL:");
		lblUrl.setBounds(10, 10, 50, 25);
		frame.add(lblUrl);
		JTextField txtUrl = new JTextField();
		txtUrl.setBounds(10, 35, 585, 25);
		if (url != null && url.trim().length() > 0)
			txtUrl.setText(url.trim());
		frame.add(txtUrl);
		JLabel lblFile = new JLabel("File:");
		lblFile.setBounds(10, 65, 50, 25);
		frame.add(lblFile);
		JTextField txtFile = new JTextField();
		txtFile.setBounds(10, 90, 520, 25);
		if (file != null && file.trim().length() > 0)
			txtFile.setText(file.trim());
		frame.add(txtFile);
		JButton btnBrowse = new JButton("...");
		btnBrowse.addActionListener((e) -> {
			JFileChooser jFileChooser = new JFileChooser(System.getProperty("user.home"));
			jFileChooser.setAcceptAllFileFilterUsed(true);
			jFileChooser.setDialogTitle("Select Save Destination");
			jFileChooser.setMultiSelectionEnabled(false);
			if (jFileChooser.showSaveDialog(frame) == JFileChooser.APPROVE_OPTION)
				txtFile.setText(jFileChooser.getSelectedFile().getAbsolutePath());
		});
		btnBrowse.setBounds(535, 90, 60, 25);
		frame.add(btnBrowse);
		JButton btnPreview = new JButton("Preview");
		btnPreview.addActionListener((e) -> {
			String innerUrl = txtUrl.getText();
			try {
				Process p = Downloader.getPreviewProcess(innerUrl, showUrl, showBanner);
				if (p == null)
					JOptionPane.showMessageDialog(frame, Downloader.lastException, "Error (Check your entered values or internet connection)", JOptionPane.ERROR_MESSAGE);
				else
					new SwingProcess(frame, p).setVisible(true);
			} catch (StreamNotFoundException ex) {
				if (JOptionPane.showConfirmDialog(frame, "Stream could not be found! It must either be an active live stream or a VOD. Do you want to wait for the streamer to come online?", "Stream not found", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
					new WaitForDialog(frame, () -> {
						String downloadUrl = Downloader.getUrl(innerUrl);
						boolean accessible = Downloader.checkAccessible(downloadUrl);
						if (!accessible)
							System.out.println(innerUrl + ": 404 Not Found");
						return !accessible;
					}, () -> {
						try {
							new SwingProcess(frame, Downloader.getPreviewProcess(innerUrl, showUrl, showBanner));
						} catch (IOException innerEx) {
							innerEx.printStackTrace();
						}
					}).setVisible(true);
				}
			} catch (IOException ex) {
				ex.printStackTrace();
				JOptionPane.showMessageDialog(frame, ex.getLocalizedMessage() + ": Check your entered values or internet connection", "Error", JOptionPane.ERROR_MESSAGE);
			}
		});
		btnPreview.setBounds(10, 120, 100, 25);
		frame.add(btnPreview);
		JButton btnDownload = new JButton("Download");
		btnDownload.addActionListener((e) -> {
			String innerUrl = txtUrl.getText();
			File innerFile = new File(txtFile.getText());
			try {
				Process p = Downloader.getDownloadProcess(innerUrl, innerFile, showUrl, showBanner);
				if (p == null)
					JOptionPane.showMessageDialog(frame, Downloader.lastException, "Error (Check your entered values or internet connection)", JOptionPane.ERROR_MESSAGE);
				else
					new SwingProcess(frame, p).setVisible(true);
			} catch (StreamNotFoundException ex) {
				if (JOptionPane.showConfirmDialog(frame, "Stream could not be found! It must either be an active live stream or a VOD. Do you want to wait for the streamer to come online?", "Stream not found", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
					new WaitForDialog(frame, () -> {
						String downloadUrl = Downloader.getUrl(innerUrl);
						boolean accessible = Downloader.checkAccessible(downloadUrl);
						if (!accessible)
							System.out.println(innerUrl + ": 404 Not Found");
						return !accessible;
					}, () -> {
						try {
							new SwingProcess(frame, Downloader.getDownloadProcess(innerUrl, innerFile, showUrl, showBanner));
						} catch (IOException innerEx) {
							innerEx.printStackTrace();
						}
					}).setVisible(true);
				}
			} catch (IOException ex) {
				ex.printStackTrace();
				JOptionPane.showMessageDialog(frame, ex.getLocalizedMessage() + ": Check your entered values or internet connection", "Error", JOptionPane.ERROR_MESSAGE);
			}
		});
		btnDownload.setBounds(115, 120, 110, 25);
		frame.add(btnDownload);
		JButton btnShowDownloadUrl = new JButton("Show Download-URL");
		btnShowDownloadUrl.addActionListener((e) -> {
			try {
				String downloadUrl = Downloader.getUrl(txtUrl.getText());
				if (downloadUrl.startsWith("http"))
					JOptionPane.showMessageDialog(frame, Downloader.getHtml(downloadUrl),"Download-URL", JOptionPane.INFORMATION_MESSAGE);
				else
					JOptionPane.showMessageDialog(frame, downloadUrl, "Error (Check your entered values or internet connection)", JOptionPane.ERROR_MESSAGE);
			} catch (StreamNotFoundException ex) {
				ex.printStackTrace();
				JOptionPane.showMessageDialog(frame, "Stream could not be found! Are you sure that it is either an active live stream or a VOD?", "Error", JOptionPane.ERROR_MESSAGE);
			} catch (IOException ex) {
				ex.printStackTrace();
				JOptionPane.showMessageDialog(frame, ex.getLocalizedMessage() + ": Check your entered values or internet connection", "Error", JOptionPane.ERROR_MESSAGE);
			}
		});
		btnShowDownloadUrl.setBounds(230, 120, 180, 25);
		frame.add(btnShowDownloadUrl);
		JButton btnCopyDownloadUrl = new JButton("Copy Download-URL");
		btnCopyDownloadUrl.addActionListener((e) -> {
			try {
				String downloadUrl = Downloader.getUrl(txtUrl.getText());
				if (downloadUrl.startsWith("http")) {
					StringSelection selection = new StringSelection(downloadUrl);
					Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
					clipboard.setContents(selection, selection);
				} else
					JOptionPane.showMessageDialog(frame, downloadUrl, "Error (Check your entered values or internet connection)", JOptionPane.ERROR_MESSAGE);
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		});
		btnCopyDownloadUrl.setBounds(415, 120, 180, 25);
		frame.add(btnCopyDownloadUrl);
		frame.setVisible(true);
	}
	
	public static void preview(String url, PrintStream outStream, boolean printUrlToConsole, boolean showBanner, boolean waitFor) throws IOException {
		Process p = null;
		try {
			p = Downloader.getPreviewProcess(url, printUrlToConsole, showBanner);
			if (p == null)
				System.err.println("Error - Check your entered values or internet connection");
		} catch (StreamNotFoundException ex) {
			if (waitFor) {
				try {
					while (((Callable<Boolean>) (() -> {
						String previewUrl = Downloader.getUrl(url);
						boolean accessible = Downloader.checkAccessible(previewUrl);
						if (!accessible)
							System.out.println(url + ": 404 Not Found");
						return !accessible;
					})).call())
						Thread.sleep(60 * 1000); // 1 Minute
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (Exception e) {
					e.printStackTrace();
				}
				p = Downloader.getPreviewProcess(url, printUrlToConsole, showBanner);
			} else {
				System.out.print("Should i wait for the stream to go online (y,n): ");
				Scanner scanner = new Scanner(System.in);
				String input = scanner.next().trim();
				while (!input.equalsIgnoreCase("y") && !input.equalsIgnoreCase("n")) {
					System.out.println("Please either type y or n");
					input = scanner.next();
				}
				scanner.close();
				if (input.equalsIgnoreCase("y"))
					Downloader.preview(url, outStream, printUrlToConsole, showBanner, true);
			}
		}
		if (p == null)
			return;
		System.out.println("ffplay should get started. When not than ffmpeg is not installed or not in the PATH. You can get it from http://ffmpeg.org/");
		BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
		String inputLine;
		while ((inputLine = br.readLine()) != null)
			System.out.println(inputLine);
	}
	
	public static void download(String url, File file, PrintStream outStream, boolean printUrlToConsole, boolean showBanner, boolean waitFor) throws IOException {
		Process p = null;
		try {
			p = Downloader.getDownloadProcess(url, file, printUrlToConsole, showBanner);
			if (p == null)
				System.err.println("Error - Check your entered values or internet connection");
		} catch (StreamNotFoundException ex) {
			if (waitFor) {
				try {
					while (((Callable<Boolean>) (() -> {
						String downloadUrl = Downloader.getUrl(url);
						boolean accessible = Downloader.checkAccessible(downloadUrl);
						if (!accessible)
							System.out.println(url + ": 404 Not Found");
						return !accessible;
					})).call())
						Thread.sleep(60 * 1000); // 1 Minute
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (Exception e) {
					e.printStackTrace();
				}
				p = Downloader.getDownloadProcess(url, file, printUrlToConsole, showBanner);
			} else {
				System.out.print("Should i wait for the stream to go online (y,n): ");
				Scanner scanner = new Scanner(System.in);
				String input = scanner.next().trim();
				while (!input.equalsIgnoreCase("y") && !input.equalsIgnoreCase("n")) {
					System.out.println("Please either type y or n");
					input = scanner.next();
				}
				scanner.close();
				if (input.equalsIgnoreCase("y"))
					Downloader.download(url, file, outStream, printUrlToConsole, showBanner, true);
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		if (p == null)
			return;
		System.out.println("ffmpeg should get started. When not than ffmpeg is not installed or not in the PATH. You can get it from http://ffmpeg.org/");
		BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
		String inputLine;
		while ((inputLine = br.readLine()) != null)
			System.out.println(inputLine);
	}
	
	public static String getUrl(String url) throws IOException {
		return Downloader.getUrl(url, false);
	}
	
	public static String getUrl(String url, boolean printUrlToConsole) throws IOException, JSONException {
		if (url.contains("?"))
			url = url.substring(0, url.indexOf('?'));
		boolean isLive = !url.contains("/videos/");
		boolean isClip = url.contains("clip");
		String channel = url.replaceAll("^.+/(.+?)$", "$1");
		String vodId = "";
		if (!isLive) {
			HttpURLConnection connection = (HttpURLConnection) new URL("https://api.twitch.tv/kraken/videos/" + channel).openConnection();
			connection.setRequestProperty("User-Agent", "Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:88.0) Gecko/20100101 Firefox/88.0");
			connection.setRequestProperty("Client-ID", "kimne78kx3ncx6brgo4mv6wki5h1ko");
			connection.setRequestProperty("Accept", "application/vnd.twitchtv.v5+json");
			BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
			String response = "";
			String inputLine;
			while ((inputLine = br.readLine()) != null)
				response += inputLine + "\n";
			JSONObject videoInfo = new JSONObject(response);
			vodId = channel;
			channel = videoInfo.getJSONObject("channel").getString("name");
		}
		if (isClip) {
			/*HttpURLConnection connection = (HttpURLConnection) new URL("https://api.twitch.tv/kraken/clips/" + channel).openConnection();
			connection.setRequestProperty("User-Agent", "Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:88.0) Gecko/20100101 Firefox/88.0");
			connection.setRequestProperty("Client-ID", "kimne78kx3ncx6brgo4mv6wki5h1ko");
			connection.setRequestProperty("Accept", "application/vnd.twitchtv.v5+json");
			System.out.println(connection.getResponseCode());
			BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
			String response = "";
			String inputLine;
			while ((inputLine = br.readLine()) != null)
				response += inputLine + "\n";
			JSONObject videoInfo = new JSONObject(response);
			vodId = channel;
			channel = videoInfo.getJSONObject("channel").getString("name");*/
			vodId = channel;
		}
		HttpURLConnection connection = (HttpURLConnection) new URL("https://gql.twitch.tv/gql").openConnection();
		connection.setDoInput(true);
		connection.setDoOutput(true);
		connection.setRequestMethod("POST");
		connection.setRequestProperty("User-Agent", "Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:88.0) Gecko/20100101 Firefox/88.0");
		connection.setRequestProperty("Client-ID", "kimne78kx3ncx6brgo4mv6wki5h1ko");
		connection.setRequestProperty("Content-Type", "text/plain; charset=UTF-8");
		connection.setRequestProperty("Origin", "https://www.twitch.tv");
		connection.setRequestProperty("DNT", "1");
		connection.setRequestProperty("Sec-GPC", "1");
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream(), "UTF-8"));
		if (isClip)
			bw.write("[{\"operationName\":\"VideoAccessToken_Clip\",\"variables\":{\"slug\":\"" + vodId + "\"},\"extensions\":{\"persistedQuery\":{\"version\":1,\"sha256Hash\":\"36b89d2507fce29e5ca551df756d27c1cfe079e2609642b4390aa4c35796eb11\"}}}]");
		else
			bw.write("{\"operationName\":\"PlaybackAccessToken_Template\",\"query\":\"query PlaybackAccessToken_Template($login: String!, $isLive: Boolean!, $vodID: ID!, $isVod: Boolean!, $playerType: String!) {  streamPlaybackAccessToken(channelName: $login, params: {platform: \\\"web\\\", playerBackend: \\\"mediaplayer\\\", playerType: $playerType}) @include(if: $isLive) {    value    signature    __typename  }  videoPlaybackAccessToken(id: $vodID, params: {platform: \\\"web\\\", playerBackend: \\\"mediaplayer\\\", playerType: $playerType}) @include(if: $isVod) {    value    signature    __typename  }}\",\"variables\":{\"isLive\":" + (isLive ? "true" : "false") + ",\"login\":\"" + channel + "\",\"isVod\":" + (!isLive ? "true" : "false") + ",\"vodID\":\"" + vodId + "\",\"playerType\":\"site\"}}");
		bw.flush();
		bw.close();
		BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
		String response = "";
		String inputLine;
		while ((inputLine = br.readLine()) != null)
			response += inputLine + "\n";
		if (isClip)
			response = "{\"array\": " + response + "}";
		JSONObject gql = new JSONObject(response);
		String signature;
		String access_token;
		try {
			if (isClip) {
				JSONObject clip = gql.getJSONArray("array").getJSONObject(0).getJSONObject("data").getJSONObject("clip");
				JSONObject playbackAccessToken = clip.getJSONObject("playbackAccessToken");
				signature = playbackAccessToken.getString("signature");
				access_token = playbackAccessToken.getString("value");
				JSONArray videoQualities = clip.getJSONArray("videoQualities");
				int videoQualitiesAmount = videoQualities.length();
				ClipVideoQuality[] qualities = new ClipVideoQuality[videoQualitiesAmount];
				int lastQuality = 0;
				int bestIndex = -1;
				for (int i = 0; i < videoQualitiesAmount; i++) {
					JSONObject videoQuality = videoQualities.getJSONObject(i);
					int quality = videoQuality.getInt("quality");
					if (lastQuality < quality) {
						lastQuality = quality;
						bestIndex = i;
					}
					String sourceURL = videoQuality.getString("sourceURL") + "?sig=" + signature + "&token=" + URLEncoder.encode(access_token, "UTF-8");
					qualities[i] = new ClipVideoQuality(videoQuality.getInt("frameRate"), quality, sourceURL);
				}
				if (bestIndex < 0)
					return null;
				return qualities[bestIndex].getSourceURL();
			} else if (isLive) {
				JSONObject streamPlaybackAccessToken = gql.getJSONObject("data").getJSONObject("streamPlaybackAccessToken");
				signature = streamPlaybackAccessToken.getString("signature");
				access_token = streamPlaybackAccessToken.getString("value");
			} else {
				JSONObject videoPlaybackAccessToken = gql.getJSONObject("data").getJSONObject("videoPlaybackAccessToken");
				signature = videoPlaybackAccessToken.getString("signature");
				access_token = videoPlaybackAccessToken.getString("value");
			}
			StringBuilder urlBuilder = new StringBuilder(isLive ? "https://usher.ttvnw.net/api/channel/hls/" : "https://usher.ttvnw.net/vod/");
			urlBuilder.append(isLive ? channel : vodId).append(".m3u8?sig=").append(URLEncoder.encode(signature, "UTF-8"));
			urlBuilder.append("&token=").append(URLEncoder.encode(access_token, "UTF-8"));
			urlBuilder.append("&allow_source=true&fast_bread=true&cdm=wv&reassignments_supported=true");
			urlBuilder.append("&playlist_include_framerate=true&player_backend=mediaplayer");
			String result = urlBuilder.toString();
			if (printUrlToConsole)
				System.out.println("Download-URL: " + result);
			return result;
		} catch (JSONException ex) {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			ex.printStackTrace(pw);
			String stacktrace = sw.toString();
			Downloader.lastException = stacktrace;
			return stacktrace;
		}
	}
}
