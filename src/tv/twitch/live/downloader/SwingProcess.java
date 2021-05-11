package tv.twitch.live.downloader;

import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.InputStream;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class SwingProcess extends JDialog {
	private static final long serialVersionUID = -6943285478152475272L;
	
	public SwingProcess(JFrame owner, final Process p) {
		super(owner);
		super.setSize(1000, 1000);
		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				if (p.isAlive())
					p.destroy();
			}
		});
		this.setLayout(new BorderLayout());
		JTextArea txtOutput = new JTextArea();
		txtOutput.setWrapStyleWord(true);
		txtOutput.setLineWrap(true);
		JScrollPane spOutput = new JScrollPane(txtOutput);
		this.add(spOutput, BorderLayout.CENTER);
		JPanel lowerPart = new JPanel();
		lowerPart.setLayout(new BorderLayout());
		JButton btnCancel = new JButton("Cancel");
		btnCancel.addActionListener((e) -> {
			if (p.isAlive())
				p.destroy();
			this.dispose();
		});
		lowerPart.add(btnCancel, BorderLayout.WEST);
		JButton btnCancelForcibly = new JButton("Cancel Forcibly");
		btnCancelForcibly.addActionListener((e) -> {
			if (p.isAlive())
				p.destroyForcibly();
			this.dispose();
		});
		lowerPart.add(btnCancelForcibly, BorderLayout.EAST);
		this.add(lowerPart, BorderLayout.SOUTH);
		new Thread(() -> {
			InputStream is = p.getInputStream();
			TextAreaOutputStream taos = new TextAreaOutputStream(txtOutput);
			int read = 0x20; // Space
			try {
				while ((read = is.read()) >= 0)
					taos.write(read);
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}).start();
	}
}
