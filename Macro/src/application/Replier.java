package application;

public class Replier {
	private int level;
	private String content;
	
	public int getLevel() {
		return level;
	}
	public void setLevel(int level) {
		this.level = level;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	@Override
	public String toString() {
		return "Replier [level=" + level + ", content=" + content + "]";
	}
	
}
