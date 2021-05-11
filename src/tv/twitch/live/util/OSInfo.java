package tv.twitch.live.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class OSInfo {
	
	public static final String UNDEFINED_OS = "UNDEFINED_OS";
	public static final String WINDOWS = "WINDOWS";
	public static final String UNIX = "UNIX";
	
	public static final ArrayList<String> TERMINALS = new ArrayList<>();
	static {
		OSInfo.TERMINALS.add("urxvt");
		OSInfo.TERMINALS.add("rxvt");
		OSInfo.TERMINALS.add("termit");
		OSInfo.TERMINALS.add("terminator");
		OSInfo.TERMINALS.add("Eterm");
		OSInfo.TERMINALS.add("aterm");
		OSInfo.TERMINALS.add("uxterm");
		OSInfo.TERMINALS.add("xterm");
		OSInfo.TERMINALS.add("gnome-terminal");
		OSInfo.TERMINALS.add("roxterm");
		OSInfo.TERMINALS.add("xfce4-terminal");
		OSInfo.TERMINALS.add("termite");
		OSInfo.TERMINALS.add("lxterminal");
		OSInfo.TERMINALS.add("mate-terminal");
		OSInfo.TERMINALS.add("terminology");
		OSInfo.TERMINALS.add("st");
		OSInfo.TERMINALS.add("qterminal");
		OSInfo.TERMINALS.add("lilyterm");
		OSInfo.TERMINALS.add("tilix");
		OSInfo.TERMINALS.add("terminix");
		OSInfo.TERMINALS.add("konsole");
		OSInfo.TERMINALS.add("kitty");
		OSInfo.TERMINALS.add("guake");
		OSInfo.TERMINALS.add("tilda");
		OSInfo.TERMINALS.add("alacritty");
		OSInfo.TERMINALS.add("hyper");
		String termEnv = System.getenv("TERMINAL");
		if (OSInfo.TERMINALS.remove(termEnv)) // Returns false if termEnv is not in TERMINALS
			OSInfo.TERMINALS.add(0, termEnv);
	}
	
	public static String getOperatingSystem() {
		String osName = System.getProperty("os.name");
		if (osName.contains("win"))
			return OSInfo.WINDOWS;
		if (osName.contains("darwin") || osName.contains("nix") || osName.contains("nux"))
			return OSInfo.UNIX;
		return OSInfo.UNDEFINED_OS;
	}
	
	public static File getTerminalExec() throws IOException {
		if (OSInfo.getOperatingSystem().equals(WINDOWS))
			return new File(System.getenv("WINDIR") + File.separator + "cmd.exe");
		ArrayList<File> path = new ArrayList<>();
		String envPath = System.getenv("PATH");
		for (String p : envPath.split("(?<!\\\\):")) {
			File dir = new File(p);
			if (dir.exists() && dir.isDirectory()) {
				path.add(dir);
			}
		}
		for (String term : TERMINALS) {
			for (File dir : path) {
				String filePath = dir.getAbsolutePath() + File.separator + term;
				File f = new File(filePath);
				if (f.exists() && f.isFile())
					return f;
			}
		}
		return null;
	}
}
