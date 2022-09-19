package application;

import java.util.ArrayList;
import java.util.List;

public class PageTableEntry {
	
	private int frameNumber = 0;
	private boolean valid = false;
	private List<PCB> contents = null;
	
	public PageTableEntry(int pageSize) {
		this.frameNumber = -1;
		this.contents = new ArrayList<>();
	}
	
	public boolean getValidBit() {
		return this.valid;
	}
	
	public int getFrameNumber() {
		return this.frameNumber;
	}
	
	public void addContent(PCB content) {
		this.contents.add(content);
	}
	
	public PCB getContent() {
		return this.contents.remove(this.contents.size() - 1);
	}
	
	public void setMapping(int frameNumber) {
		this.frameNumber = frameNumber;
		this.valid = true;
	}

}
