package application;

public interface FileSystem {
	
	public int shutdown(PCB process);
	
	public int create(PCB process);
	
	public int inumber(PCB process, int fd);
	
	public int open(PCB process, int iNumber);
	
	public int read(PCB process, int fd, byte[] buffer);
	
	public int seek(PCB process, int fd, int offset, int whence);
	
    public int close(PCB proecss, int fd);
    
    public int delete(PCB process, int iNumber);

}
