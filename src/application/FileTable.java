package application;

public class FileTable {
	
	public int maxFiles = 0;
	public int[] bitmap = null;
	public FileDescriptor[] fdArray = null;
	
	public FileTable(int maxFiles) {
		this.maxFiles = maxFiles;
		this.bitmap = new int[maxFiles];
		this.fdArray = new FileDescriptor[maxFiles];
	}

	public int allocate() {
		for (int i = 0; i < this.maxFiles; i++) {
			if (this.bitmap[i] == 0) {
				return i;
			}
		}
		return -1;
	}
	
	public int add(Inode inode, int inumber, int fd) {
		if (this.bitmap[fd] != 0) {
			return -1;
		}
		this.bitmap[fd] = 1;
		this.fdArray[fd] = new FileDescriptor(inode, inumber);
		return 0;
	}
	
	public void free(int fd) {
		this.bitmap[fd] = 0;
	}
	
	public boolean isValid(int fd) {
		if (fd < 0 || fd >= this.maxFiles) {
			return false;
		}
		else if (this.bitmap[fd] > 0) {
			return true;
		}
		else {
			return false;
		}
	}
	
	public Inode getInode(int fd) {
		if (this.bitmap[fd] == 0) {
			return null;
		}
		else {
			return this.fdArray[fd].getInode();
		}
	}
	
	public int getInumber(int fd) {
		if (this.bitmap[fd] == 0) {
			return 0;
		}
		else {
			return this.fdArray[fd].getInumber();
		}
	}
	
	public int getSeekPointer(int fd) {
		if (this.bitmap[fd] == 0) {
			return 0;
		}
		else {
			return this.fdArray[fd].getSeekPointer();
		}
	}
	
	public int setSeekPointer(int fd, int newPointer) {
		if (this.bitmap[fd] == 0) {
		    return 0;  
		}
		else {
		    this.fdArray[fd].setSeekPointer(newPointer);
		    return 1;
		}
	}
	
	public int setFileSize(int fd, int newFileSize) {
		if (this.bitmap[fd] == 0) {
			return 0;
		}
		else {
			this.fdArray[fd].setFileSize(newFileSize);
			return -1;
		}
	}
	
	public int getFDfromInumber(int inumber) {
		for (int i = 0; i < this.maxFiles; i++) {
			if (this.bitmap[i] == 1) {
				if (this.fdArray[i].getInumber() == inumber) {
					return i;
				}
			}
		}
		return -1;
	}
	
}
