package application;

public class Frame {
	
	private int frameSize = 0;
	private byte[] frameValue = null;
	private PCB[] contents = null;
	
	public Frame(int frameSize) {
		this.frameSize = frameSize;
		this.frameValue = new byte[frameSize];
		this.contents = new PCB[frameSize];
	}
	
	public void setFrame(byte[] bytes) {
		System.arraycopy(bytes, 0, this.frameValue, 0, this.frameSize);
	}
	
	public PCB[] getContents() {
		return this.contents;
	}
	
	public void setContents(PCB[] pageContents) {
		System.arraycopy(pageContents, 0, this.contents, 0, this.frameSize);
	}
	
	
	public void allocateMemory(int offset, PCB process) {
		this.contents[offset] = process;
	}
	
	public PCB deallocateMemory(int offset) {
		PCB process = this.contents[offset];
		this.contents[offset] = null;
		return process;
	}

}
