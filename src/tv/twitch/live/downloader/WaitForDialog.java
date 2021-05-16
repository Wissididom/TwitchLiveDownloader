package tv.twitch.live.downloader;

import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.concurrent.Callable;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

public class WaitForDialog extends JDialog {
	private static final long serialVersionUID = 5600061656739541101L;
	private boolean cancelled = false;
	
	public WaitForDialog(JFrame parent, Callable<Boolean> testForWaiting, Runnable afterWaiting) {
		super(parent, "Wait for Streamer");
		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				WaitForDialog.this.cancelled = true;
				WaitForDialog.this.dispose();
			}
		});
		this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		this.setSize(250, 100);
		this.setLocationRelativeTo(parent);
		this.setLayout(new BorderLayout());
		this.setResizable(false);
		JPanel topPadding = new JPanel();
		this.add(topPadding, BorderLayout.NORTH);
		JProgressBar jpb = new JProgressBar();
		jpb.setIndeterminate(true);
		this.add(jpb, BorderLayout.CENTER);
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BorderLayout());
		JPanel bottomPadding = new JPanel();
		buttonPanel.add(bottomPadding, BorderLayout.NORTH);
		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener((e) -> {
			this.cancelled = true;
			this.dispose();
		});
		buttonPanel.add(cancelButton, BorderLayout.SOUTH);
		this.add(buttonPanel, BorderLayout.SOUTH);
		Thread t = new Thread(() -> {
			try {
				while (!this.cancelled && testForWaiting.call())
					Thread.sleep(60 * 1000); // 1 Minute
				if (!this.cancelled) {
					afterWaiting.run();
					this.dispose();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
		t.start();
	}
}
