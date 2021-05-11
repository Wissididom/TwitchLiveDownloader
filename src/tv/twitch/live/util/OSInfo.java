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
        TERMINALS.add("urxvt");
        TERMINALS.add("rxvt");
        TERMINALS.add("termit");
        TERMINALS.add("terminator");
        TERMINALS.add("Eterm");
        TERMINALS.add("aterm");
        TERMINALS.add("uxterm");
        TERMINALS.add("xterm");
        TERMINALS.add("gnome-terminal");
        TERMINALS.add("roxterm");
        TERMINALS.add("xfce4-terminal");
        TERMINALS.add("termite");
        TERMINALS.add("lxterminal");
        TERMINALS.add("mate-terminal");
        TERMINALS.add("terminology");
        TERMINALS.add("st");
        TERMINALS.add("qterminal");
        TERMINALS.add("lilyterm");
        TERMINALS.add("tilix");
        TERMINALS.add("terminix");
        TERMINALS.add("konsole");
        TERMINALS.add("kitty");
        TERMINALS.add("guake");
        TERMINALS.add("tilda");
        TERMINALS.add("alacritty");
        TERMINALS.add("hyper");

        String termEnv = System.getenv("TERMINAL");
        if(TERMINALS.remove(termEnv)) // Returns false if termEnv is not in TERMINALS
            TERMINALS.add(0, termEnv);
    }

    public static String getOperatingSystem() {
        String osName = System.getProperty("os.name");
        if(osName.contains("win"))
            return WINDOWS;
        if(osName.contains("darwin") || osName.contains("nix") || osName.contains("nux"))
            return UNIX;
        return UNDEFINED_OS;
    }

    public static File getTerminalExec() throws IOException {
        if(getOperatingSystem().equals(WINDOWS))
            return new File(System.getenv("WINDIR") + File.separator + "cmd.exe");

        ArrayList<File> path = new ArrayList<>();
        String envPath = System.getenv("PATH");
        for(String p : envPath.split("(?<!\\\\):")) {
            File dir = new File(p);
            if(dir.exists() && dir.isDirectory()) {
                path.add(dir);
            }
        }
        for(String term : TERMINALS) {
            for(File dir : path) {
                String filePath = dir.getAbsolutePath() + File.separator + term;
                File f = new File(filePath);
                if(f.exists() && f.isFile())
                    return f;
            }
        }
        return null;
    }

    public static void main(String[] args) throws IOException {
        System.out.println(getTerminalExec().getAbsolutePath());
    }
}
