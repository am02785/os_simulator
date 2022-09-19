package application;

public class FileDescriptor {
	
	private Inode inode;
    private int inumber;
    private int seekptr;
    
    public FileDescriptor(Inode inode, int inumber) {
		this.inode = inode;
		this.inumber = inumber;
		this.seekptr = 0;
    }
    
    public Inode getInode() {
		return this.inode;
    }
    
    public int getInumber() {
		return this.inumber;
    }
    
    public int getSeekPointer() {
		return this.seekptr;
    }

    public void setSeekPointer(int i) {
		this.seekptr = i;
    }

    public void setFileSize(int newSize) {
		this.inode.setFileSize(newSize);
    }

}
