package application;

public class Inode {
	
	private int size = 0;
	private int flags = 0;
	private int owner = 0;
	private int fileSize = 0;
	private int[] pointer = null;
	
	public Inode(int size) {
		this.size = size;
		this.pointer = new int[13];
	}

	public int getSize() {
		return this.size;
	}

	public int getFlags() {
		return this.flags;
	}

	public void setFlags(int flags) {
		this.flags = flags;
	}

	public int getOwner() {
		return this.owner;
	}

	public void setOwner(int owner) {
		this.owner = owner;
	}

	public int getFileSize() {
		return this.fileSize;
	}

	public void setFileSize(int fileSize) {
		this.fileSize = fileSize;
	}

	public int[] getPointer() {
		return this.pointer;
	}

	public void setPointer(int[] pointer) {
		this.pointer = pointer;
	}
	
	@Override
	public String toString() {
		String result = "[Flags: " + this.flags
		+ "  Size: " + this.fileSize + "  ";
		for (int i = 0; i < 13; i++) {
			result += "|" + this.pointer[i];
		}
		return result + "]";
	}

}
