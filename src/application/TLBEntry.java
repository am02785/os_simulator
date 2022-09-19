package application;

public class TLBEntry {
	
	private int pageNumber = 0;
	private int frameNumber = 0;
	private boolean isValid = false;
	
	public TLBEntry() {
		this.pageNumber = -1;
		this.frameNumber = -1;
	}
	
	public boolean setMapping(int pageNumber, int frameNumber) {
		this.pageNumber = pageNumber;
		this.frameNumber = frameNumber;
		this.isValid = true;
		return isValid;
	}
	
	public boolean checkPageNumber(int pageNumber) {
		boolean pageNumberIsEqual = false;
		if (pageNumber == this.pageNumber) {
			pageNumberIsEqual = true;
		}
		return pageNumberIsEqual;
	}
	
	public int getFrameNumber() {
		return this.frameNumber;
	}

}
