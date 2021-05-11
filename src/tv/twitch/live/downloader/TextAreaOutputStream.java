package tv.twitch.live.downloader;

import java.awt.EventQueue;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JTextArea;

public class TextAreaOutputStream extends OutputStream {
	private byte[] oneByte;
	private Appender appender;
	
	public TextAreaOutputStream(JTextArea textArea) {
		this(textArea, 1000);
	}
	
	public TextAreaOutputStream(JTextArea textArea, int maxLines) {
		if(maxLines < 1)
			throw new IllegalArgumentException("TextAreaOutputStream maximum lines must be positive (value=" + maxLines + ")");
		this.oneByte = new byte[1];
		this.appender = new Appender(textArea, maxLines);
	}
	
	public synchronized void clear() {
		if (appender != null)
			appender.clear();
	}
	
	@Override
	public synchronized void close() {
		this.appender = null;
	}
	
	@Override
	public synchronized void flush() {}
	
	@Override
	public synchronized void write(int value) {
		this.oneByte[0] = (byte) value;
		this.write(this.oneByte, 0, 1);
	}
	
	@Override
	public synchronized void write(byte[] ba) {
		this.write(ba, 0, ba.length);
	}
	
	@Override
	public synchronized void write(byte[] ba, int str, int len) {
		if (appender != null)
			appender.append(TextAreaOutputStream.bytesToString(ba, str, len));
	}
	
	private static String bytesToString(byte[] ba, int str, int len) {
		try {
			return new String(ba, str, len, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			return new String(ba, str, len);
		} // all JVMs are required to support UTF-8
	}
	
	static class Appender implements Runnable {
		private final JTextArea textArea;
		private final int maxLines;
		private final LinkedList<Integer> lengths;
		private final List<String> values;
		private int curLength;
		private boolean clear;
		private boolean queue;
		
		Appender(JTextArea textArea, int maxLines) {
			this.textArea = textArea;
			this.maxLines = maxLines;
			this.lengths = new LinkedList<Integer>();
			this.values = new ArrayList<String>();
			this.curLength = 0;
			this.clear = false;
			this.queue = true;
		}
		
		synchronized void append(String value) {
			this.values.add(value);
			if (this.queue) {
				this.queue = false;
				EventQueue.invokeLater(this);
			}
		}
		
		synchronized void clear() {
			this.clear = true;
			this.curLength = 0;
			this.lengths.clear();
			this.values.clear();
			if (this.queue) {
				this.queue = false;
				EventQueue.invokeLater(this);
			}
		}
		
		public synchronized void run() {
			if (this.clear)
				this.textArea.setText("");
			for (String value : values) {
				this.curLength += value.length();
				if (value.endsWith("\n") || value.endsWith(System.getProperty("line.separator", "\n"))) {
					if (this.lengths.size() >= this.maxLines)
						this.textArea.replaceRange("", 0, this.lengths.removeFirst());
					this.lengths.addLast(this.curLength);
					this.curLength = 0;
				}
				this.textArea.append(value);
			}
			this.values.clear();
			this.clear = false;
			this.queue = true;
		}
	}
}
