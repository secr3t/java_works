package application;

import java.util.ArrayList;

public class Writer {
	private String title;
	private int level;
	private String content;
	private ArrayList<Replier> reps;
	
	public Writer() {
		reps = new ArrayList<>();
	}
	
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
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

	public ArrayList<Replier> getReps() {
		return reps;
	}

	public void setReps(ArrayList<Replier> reps) {
		this.reps = reps;
	}

	@Override
	public String toString() {
		return "Writer [title=" + title + ", level=" + level + ", content=" + content + ", reps=" + reps + "]";
	}
	
	
	
}
