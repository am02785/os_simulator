package application;

public class SuperBlock {
	
	int size = 0;
	int iSize = 0;
	int freeList = 0;
	
	public int getSize() {
		return this.size;
	}
	public void setSize(int size) {
		this.size = size;
	}
	public int getiSize() {
		return this.iSize;
	}
	public void setiSize(int iSize) {
		this.iSize = iSize;
	}
	public int getFreeList() {
		return this.freeList;
	}
	public void setFreeList(int freeList) {
		this.freeList = freeList;
	}
	
	@Override
	public String toString() {
		String result = "SUPERBLOCK:\n"
				+ "Size: " + this.size
				+ "  Isize: " + this.iSize
				+ "  freeList: " + this.freeList;
		return result;
	}

}
