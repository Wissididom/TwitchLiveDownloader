package tv.twitch.live.downloader;

public class ClipVideoQuality {
	private int frameRate;
	private int/*String*/ quality;
	private String sourceURL;
	
	public ClipVideoQuality(int frameRate, int/*String*/ quality, String sourceURL) {
		this.frameRate = frameRate;
		this.quality = quality;
		this.sourceURL = sourceURL;
	}
	
	public int getFrameRate() {
		return frameRate;
	}
	
	public int getQuality() {
		return quality;
	}
	
	public String getSourceURL() {
		return sourceURL;
	}
}
