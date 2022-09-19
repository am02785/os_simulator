package application;

import java.util.*;
import java.io.*;

public class JavaFileSystem extends Thread implements FileSystem {
	
	private static final boolean DELETE_DISK_ON_SHUTDOWN = false;
	
    public static final int SEEK_SET = 0;
    public static final int SEEK_CUR = 1;
    public static final int SEEK_END = 2;
    
    private Disk disk = null;
    private SuperBlock sb = null;

    private IndirectBlock freeList = null;

    private FileTable ft = null;
    
    private int blockSize = 0;
    private int nodeSize = 0;
    private int maxFiles = 0;
    
    private String diskSchedulingAlgorithm = null;

    private Hashtable vars = new Hashtable();

    private ProcessCreation processCreation = null;
    private CPU cpu = null;
    
    private int usedFiles = 0;

    public JavaFileSystem(int blockSize, int numBlocks, int nodeSize, int maxFiles,
    		String diskSchedulingAlgorithm, ProcessCreation processCreation, CPU cpu) {
    	this.disk = new Disk(blockSize, numBlocks);
    	this.sb = new SuperBlock();
    	try {
    		this.disk.read(0, this.sb);
    	}
    	catch (Exception e) {
    	}
    	this.ft = new FileTable(maxFiles);
    	this.blockSize = blockSize;
    	this.nodeSize = nodeSize;
    	this.maxFiles = maxFiles;
    	this.diskSchedulingAlgorithm = diskSchedulingAlgorithm;
    	this.processCreation = processCreation;
    	this.cpu = cpu;
    }
    
    
    @Override 
    public void run() {
    	try {
    		while (true) {
  //  			System.out.println(this.cpu.getTotalRunningTime());
    			PCB process = null;
    			switch (this.diskSchedulingAlgorithm) {
    			case "FCFS":
    				process = this.cpu.fcfsDiskSchedulingAlgorithm();
    				break;
    			case "SSTF":
    				process = this.cpu.sstfDiskSchedulingAlgorithm();
    				break;
    			}
    			if (process == null) {
    				continue;
    			}
	    		String result = "";
	    		List<String> command = process.getFileSystemCommand();
	    		String target = process.getTarget();
	    		switch (command.get(0)) {
	    		case "read":
		       		result += this.readTest(process, Integer.parseInt(command.get(1)), Integer.parseInt(command.get(2)));
		       		break;
		       	case "write":
		       		result += this.writeTest(process, Integer.parseInt(command.get(1)), command.get(2), Integer.parseInt(command.get(3)));
		       		break;
	    		}
	    		if (target == null) {
		    		result = process.getResult() + result + "\n";
		    	}
		    	else {
		    		this.vars.put(target, new Integer(result));
		    		result = process.getResult() + target + " = " + result + "\n";
		    	}
		    	process.setTarget(null);
		    	process.setResult(result);
		    	this.processCreation.putProcess(process);
    		}
    	}
    	catch (InterruptedException e) {
    	}
    }
    
    public void runCommand(PCB process, List<String> command, String target) {
    	try {
	    	String result = "";
	    	switch (command.get(0)) {
	    	case "formatDisk":
	    		result += this.formatDisk(process, Integer.parseInt(command.get(1)), Integer.parseInt(command.get(2)));
	    		break;
	       	case "shutdown":
	       		result += this.shutdown(process);
	       		break;
	       	case "create":
	       		result += this.create(process);
	       		break;
	       	case "open":
	    		result += this.open(process, Integer.parseInt(command.get(1)));
	    		break;
	       	case "inumber":
	       		result += this.inumber(process, Integer.parseInt(command.get(1)));
	       		break;
	       	case "seek":
	       		result += this.seek(process, Integer.parseInt(command.get(1)), Integer.parseInt(command.get(2)), Integer.parseInt(command.get(3)));
	       		break;
	       	case "close":
	       		result += this.close(process, Integer.parseInt(command.get(1)));
	       		break;
	       	case "delete":
	       		result += this.delete(process, Integer.parseInt(command.get(1)));
	       		break;
	       	case "fileSystemVars":
	       		for (Enumeration e = this.vars.keys(); e.hasMoreElements(); ) {
	                Object key = e.nextElement();
	                Object val = this.vars.get(key);
	                result += key + " = " + val + "\n";
	            }
	       		break;
	    	}
	    	if (target == null) {
	    		result = process.getResult() + result + "\n";
	    	}
	    	else {
	    		this.vars.put(target, new Integer(result));
	    		result = process.getResult() + target + " = " + result + "\n";
	    	}
	    	process.setTarget(null);
	    	process.setResult(result);
	    	this.processCreation.putProcess(process);
    	}
    	catch (InterruptedException e) {
    	}
    }
    
    public FileTable getFileTable() {
    	return this.ft;
    }
    
  //  public void setProcessCreation(ProcessCreation processCreation) {
  //  	this.processCreation = processCreation;
  //  }
    
  //  public void setCPU(CPU cpu) {
  //  	this.cpu = cpu;
  //  }
    
    public Hashtable getVars() {
    	return this.vars;
    }
    
    public int getUsedFiles() {
    	return this.usedFiles;
    }
    
    public int getRemainingFiles() {
    	return this.maxFiles - this.usedFiles;
    }
    
    private int readTest(PCB process, int fd, int size) {
        byte[] buffer = new byte[size];
        int length;
        String result = "";
        for (int i = 0; i < size; i++)
            buffer[i] = (byte) '*';
        length = read(process, fd, buffer);
        for (int i = 0; i < length; i++) 
           result += this.showchar(buffer[i]);
        if (length != -1) {
        	result = process.getResult() + result;
        	result += "\n";
        	process.setResult(result);
        }
        return length;
    }
    
    private int writeTest (PCB process, int fd, String str, int size) {
        byte[] buffer = new byte[size];
        for (int i = 0; i < buffer.length; i++) 
            buffer[i] = (byte)str.charAt(i % str.length());
        return this.write(process, fd, buffer);
    }
    
    private String showchar(byte b) {
    	String result = "";
        if (b < 0) {
            result += "M-";
            b += 0x80;
        }
        if (b >= ' ' && b <= '~') {
            result += (char)b;
            return result;
        }
        switch (b) {
            case '\0': result += "\\0"; break;
            case '\n': result += "\\n"; break;
            case '\r': result += "\\r"; break;
            case '\b': result += "\\b"; break;
            case 0x7f: result += "\\?"; break;
            default:   result += "^" + (char)(b + '@'); break;
        }
        return result;
    }
    
    public int formatDisk(PCB process, int size, int iSize) {
    	String result = process.getResult();
	    	if (iSize < 1) {
	    	    result += error("The number of inode blocks is too small") + "\n";
		    	process.setResult(result);
	    	    return -1;
	    	}
	    	
	    	if (size < (2 * iSize + 2)) {
	    	    result += error("The size of the disk is too small") + "\n";
		    	process.setResult(result);
	    	    return -1;
	    	}

	    	InodeBlock ib = new InodeBlock(this.blockSize, this.nodeSize);
	    	for (int i = 0; i < ib.getNode().length; i++) {
	    	    ib.getNode()[i].setFlags(0);
	    	}
	    	
	    	for (int i = 1; i <= iSize; i++) {
	    	    this.disk.write(i, ib);
	    	}


	    	int dataBlocks = size - iSize - 1;
	    	int offset = this.blockSize / 4 - 1;
	    	int lastFreeListBlock = 0;

	    	IndirectBlock B = new IndirectBlock(this.blockSize);

	    	for (int i = size - 1; i >= iSize + 1; i--) {

	    	    if ((offset == 0) || (i == iSize + 1)) {
		    		
		    		B.pointer[0] = lastFreeListBlock;
	
		    		this.disk.write(i, B);
	
		    				    		lastFreeListBlock = i;
	
	
		    		if (i > iSize + 1) {
		    		    offset = this.blockSize / 4 - 1;
		    		    B.clear();
		    		}
	    	    }
	    	    else {
	    	    		B.pointer[offset--] = i;
	    	    }
	    	}
	    	
	    	this.sb.size = size;
	    	this.sb.iSize = iSize;
	    	this.sb.freeList = iSize + 1;

	    	this.disk.write(0, sb);

	    	this.freeList = B;
	    	return 0;
    } 

    public int shutdown(PCB process) {
	    	for (int i = 0; i < this.maxFiles; i++) {
	    	    if (this.ft.isValid(i)) close(process, i);
	    	}
	
	    	if ((this.freeList != null) && (this.sb.freeList > 0)) {
	    	    this.disk.write(sb.freeList, freeList);
	    	}
	    	
	    	this.disk.write(0, sb);
	    	
	    	String result = process.getResult();
	    	result += this.disk.stop(DELETE_DISK_ON_SHUTDOWN) + "\n";
	    	process.setResult(result);
	    	return 0;
    } 

    public int create(PCB process) {
    	String result = process.getResult();
	    	int fd = ft.allocate();
	    	if (fd < 0) {
	    	    result += error("Too many files are open") + "\n";
	    	    process.setResult(result);
	    	    return -1;
	    	}
	
	    	this.ft.free(fd);
	
	    	Inode inode = new Inode(this.nodeSize);
	    	
	    	inode.setFlags(1);
	    	inode.setOwner(0);
	    	inode.setFileSize(0);
	    	
	    	int[] pointer = inode.getPointer();
	    	for (int i = 0; i < pointer.length; i++) {
	    	    pointer[i] = 0;
	    	}
	    	inode.setPointer(pointer);
	    	
	
	    	int ind = allocateInode(process, inode);
	    	if (ind < 0) 
	    		return -1;
	    	return open(process, ind, inode);
    }

    public int inumber(PCB process, int fd) {
	
	    	if (!this.ft.isValid(fd)) {
	    		String result = process.getResult();
	    	    result += error("The given file descriptor does not point to an open file.") + "\n";
	    	    process.setResult(result);
	    	    return -1;
	    	}
	
	    	return this.ft.getInumber(fd);
    }

    public int open(PCB process, int iNumber) {
	    	
	    	if ((iNumber <= 0) || (iNumber > sb.iSize * (this.blockSize/this.nodeSize))) {
	    		String result = process.getResult();
	    	    result += error("Inode number out of range") + "\n";
	    	    process.setResult(result);
	    	    return -1;
	    	}
	    	
	
	    	Inode inode = readInode(process, iNumber);
	    	if (inode == null) 
	    		return -1;
	    	return open(process, iNumber, inode);
    }
    
    public int read(PCB process, int fd, byte[] buffer) {
	    
	    	if (!this.ft.isValid(fd)) {
	    		String result = process.getResult();
	    	    result += error("The given file descriptor does not point to an open file.") + "\n";
	    	    process.setResult(result);
	    	    return -1;
	    	}
	
	
	    	int sp = this.ft.getSeekPointer(fd);
	
	
	    	if (buffer.length <= 0) return 0;
	
	
	    	Inode I = this.ft.getInode(fd);
	
	
	    	int block  = sp / this.blockSize;
	    	int offset = sp % this.blockSize;
	
	    	int bp = 0;
	
	    	byte[] readBytes = new byte[this.blockSize];

	    	while ((bp < buffer.length) && (sp < I.getFileSize())) {


	    	    int disk_block = getBlock(process, I, block);
	    	    if (disk_block < 0) return bp;    


	    	    int readSize = buffer.length - bp;
	    	    if ((offset + readSize) > this.blockSize) readSize =this.blockSize - offset;
	    	    if ((sp + readSize) > I.getFileSize()) readSize = I.getFileSize() - sp;

	    	   

	    	    if (disk_block == 0) {
		    		for (int i = offset; i < offset + readSize; i++) {
		    		    buffer[bp++] = 0;
		    		}
	
		    		
	    	    }
	    	    else {
	
		    		this.disk.read(disk_block, readBytes);
	
		    		for (int i = offset; i < offset + readSize; i++) {
		    		    buffer[bp++] = readBytes[i];
		    		}
	    	    }


	    	    offset += readSize;
	    	    if (offset >= this.blockSize) {
		    		offset = 0;
		    		block++;
	    	    }

	    	    sp += readSize;
	    	    this.ft.setSeekPointer(fd, sp);
	    	}

	    	return bp;

    }

    public int write(PCB process, int fd, byte[] buffer) {
	
	    	if (!this.ft.isValid(fd)) {
	    		String result = process.getResult();
	    	    result += error("The given file descriptor does not point to an open file.") + "\n";
	    	    process.setResult(result);
	    	    return -1;
	    	}
	    	
	    	if (buffer.length <= 0) return 0;
	    	
	    	int sp = ft.getSeekPointer(fd);
	    	
	    	Inode I = ft.getInode(fd);

	    	int block  = sp / this.blockSize;
	    	int offset = sp % this.blockSize;

	    	int bp = 0;


	    	while (bp < buffer.length) {


	    	    int disk_block = getBlock(process, I, block);
	    	    

	    	    boolean justAllocated = false;

	    	    if (disk_block == 0) {
		    	    	disk_block = allocateBlock(process, fd, block);
		    		justAllocated = true;
		    		
		    		if (disk_block < 0) 
		    			return bp;
	    	    }
	    	    

	    	    if (disk_block <= 0) {
		    		String result = process.getResult();
		    		result += error("Unable to translate the block address to disk address") + "\n";
		    		result += error("  seek pointer: " + sp + " = (" + block + ", " + offset + ")") + "\n";
		    		process.setResult(result);
		    		return -1;
	    	    }


	    	    int writeSize = buffer.length - bp;
	    	    if ((offset + writeSize) > this.blockSize) writeSize = this.blockSize - offset;


	    	    boolean writingBeyondEOF = writeSize + sp > I.getFileSize();

	    	    boolean needToRead = true;

	    	    if ((offset == 0) && (writeSize >= this.blockSize)) {

	
		    		needToRead = false;
	    	    }
	    	    else if ((offset == 0) && (writingBeyondEOF)) {
	
	
		    		needToRead = false;
	    	    }
	    	    else if (justAllocated) {
	    	    	
	
		    		needToRead = false;
	    	    }


	    	    byte[] writeBytes = new byte[this.blockSize];

	    	    if (justAllocated) for (int i = 0; i < this.blockSize; i++) writeBytes[i] = 0;

	    	    if (needToRead) this.disk.read(disk_block, writeBytes);
	    	    for (int i = offset; i < offset + writeSize; i++) writeBytes[i] = buffer[bp++];


	    	    this.disk.write(disk_block, writeBytes);


	    	    offset += writeSize;
	    	    if (offset >= this.blockSize) {
		    		offset = 0;
		    		block++;
	    	    }

	    	    sp += writeSize;
	    	    this.ft.setSeekPointer(fd, sp);


	    	    if (writingBeyondEOF) {
		    		I.setFileSize(block * this.blockSize + offset);	 
	    	    }
	    	    
	    	}
	
	String result = process.getResult();
	result += "SB = " + sb + "\n";
	process.setResult(result);    	
    		return bp;
    } 

    public int seek(PCB process, int fd, int offset, int whence) {
	
	    	if (!this.ft.isValid(fd)) {
	    		String result = process.getResult();
	    	    result += error("The given file descriptor does not point to an open file.") + "\n";
	    	    process.setResult(result);
	    	    return -1;
	    	}
	
	    	int p = -1;
	
	    	int oldSeek = ft.getSeekPointer(fd);
	    	int fileSize = ft.getInode(fd).getFileSize();
	
	    	switch (whence) {
	    	case SEEK_SET: p = offset; break;
	    	case SEEK_CUR: p = oldSeek + offset; break;
	    	case SEEK_END: p = fileSize + offset; break;
	    	default:
	    		String result = process.getResult();
	    	    result += error("Invalid seek whence constant.") + "\n";
	    	    process.setResult(result);
	    	    return -1;
	    	}
	
	    	if (p < 0) {
	    		String result = process.getResult();
	    	    result += error("Negative seek pointer.") + "\n";
	    	    process.setResult(result);
	    	    return -1;
	    	}
	
	    	if (this.ft.setSeekPointer(fd, p) < 0) 
	    		return -1;
	    	
	    	return p;


    }

    public int close(PCB process, int fd) {
    	
	    	if (!this.ft.isValid(fd)) {
	    		String result = process.getResult();
	    	    result += error("The given file descriptor does not point to an open file.") + "\n";
	    	    process.setResult(result);
	    	    return -1;
	    	}
	    	

	    	int inum = this.ft.getInumber(fd);
	    	Inode inode = this.ft.getInode(fd);

	    	this.ft.free(fd);

	    	return writeInode(process, inum, inode);
    }

    public int delete(PCB process, int iNumber) {
	 
	    	if (this.ft.getFDfromInumber(iNumber) >= 0) {
	    		String result = process.getResult();
	    	    result += error("Cannot delete an open file") + "\n";
	    	    process.setResult(result);
	    	    return -1;
	    	}
	            
	    	Inode I = readInode(process, iNumber);
	    	if (I == null) return -1;
	
	    	if (I.getFlags() == 0) {
	    		String result = process.getResult();
	    	    result += error("File not found") + "\n";
	    	    process.setResult(result);
	    	    return -1;
	    	}
	
	    	int size = (I.getFileSize() + this.blockSize - 1) / this.blockSize;
	
	    	
	
	    	int count = 0;
	
	
	    	for (int i = 0; i < 10; i++) {
	    	    if ((i < size) && (I.getPointer()[i] > 0)) {
	    		freeBlock(I.getPointer()[i]);
	    		count++;
	    	    }
	    	}
	
	
	    	count += freeIndirect(I.getPointer()[10], 1);
	
	
	    	count += freeIndirect(I.getPointer()[11], 2);
	
	    	count += freeIndirect(I.getPointer()[12], 3);
	
	    	I.setFlags(0);
	
	    	return writeInode(process, iNumber, I);
	       
    }

    public String toString() {
        throw new RuntimeException("not implemented");
    }
    
    private String error(String msg) {
	    	return "[ERROR] " + msg;
    }
    
    private int allocateInode(PCB process, Inode inode) {
	
		InodeBlock ib = new InodeBlock(this.blockSize, this.nodeSize);
		
		for (int i = 1; i <= this.sb.iSize; i++) {
			
		    this.disk.read(i, ib);
		    
		    Inode[] node = ib.getNode();
		    for (int j = 0; j < node.length; j++) {
				if (node[j].getFlags() == 0) {
				    node[j] = inode;
				    ib.setNode(node);
				    this.disk.write(i, ib);
				    return inodeNumber(i, j); 
				}
		    }
		}
		String result = process.getResult();
		result += error("The disk contains too many files") + "\n";
		process.setResult(result);
		return -1;
    }
    
    private int inodeNumber(int iblock, int ioffset) {
    		return (iblock - 1) * (this.blockSize / this.nodeSize) + ioffset + 1;
    }

    private int open(PCB process, int inum, Inode inode) {
		if (inode.getFlags() == 0) {
			String result = process.getResult();
		    result += error("File not found") + "\n";
		    process.setResult(result);
		    return -1;
		}
	
		if (this.ft.getFDfromInumber(inum) >= 0) {
			String result = process.getResult();
		    result += error("The file is already open") + "\n";
		    process.setResult(result);
		    return -1;
		}
		
		int fd = this.ft.allocate();
		if ((fd < 0) || (fd >= this.maxFiles)) return -1;
	
		int status = this.ft.add(inode, inum, fd);
		if (status < 0) {
			String result = process.getResult();
		    result += error("Error occured while opening the file") + "\n";
		    process.setResult(result);
		    this.ft.free(fd);
		    return -1;
		}
		if (process.getCommands().get(0).contains("create")
				|| (process.getCommands().size() > 2 && process.getCommands().get(2).contains("create"))) {
			this.usedFiles ++;
		}
		return fd;
    }

    private Inode readInode(PCB process, int inode) {
    		if (inode <= 0) {
    			String result = process.getResult();
    			result += error("Illegal inode number") + "\n";
    			process.setResult(result);
    		    return null;
    		}
    		
    		int block = iBlock(inode);
    		
    		if ((block < 1) || (block > this.sb.iSize)) {
    			String result = process.getResult();
    		    result += error("Illegal inode number") + "\n";
    		    process.setResult(result);
    		    return null;
    		}
    		
    		
    		InodeBlock ib = new InodeBlock(this.blockSize, this.nodeSize);
    		this.disk.read(block, ib);
    		

    		return ib.getNode()[ioffset(inode)];	
	}
    
    private int writeInode(PCB process, int inum, Inode inode) {
	
	    	if (inum <= 0) {
	    		String result = process.getResult();
	    	    result += error("Illegal inode number") + "\n";
	    	    process.setResult(result);
	    	    return -1;
	    	}
	
	    	int block = iBlock(inum);
	
	    	if ((block < 1) || (block > this.sb.iSize)) {
	    		String result = process.getResult();
	    	    result += error("Illegal inode number") + "\n";
	    	    process.setResult(result);
	    	    return -1;
	    	}
	    	
	    	InodeBlock ib = new InodeBlock(this.blockSize, this.nodeSize);
	    	this.disk.read(block, ib);
	    	
	    	Inode[] node = ib.getNode();
	    	node[ioffset(inum)] = inode;
	    	ib.setNode(node);
	    	this.disk.write(block, ib);
	    	if (process.getCommands().get(0).contains("delete")
	    			|| (process.getCommands().size() > 2 && process.getCommands().get(2).contains("delete"))) {
				this.usedFiles --;
			}
	    	return 0;  	
    }
    
    private int iBlock(int inode) {
	    	return (inode - 1) / (this.blockSize / this.nodeSize) + 1;
    }
    
    private int ioffset(int inode) {
		return (inode - 1) % (this.blockSize / this.nodeSize);
    }

    private int getBlock(PCB process, Inode inode, int block) {

	int size = (inode.getFileSize() + this.blockSize - 1) / this.blockSize;

	if (block < 0) 
		return -1;
	if (block >= size) 
		return 0;

	final int N = this.blockSize / 4;

	int level = 0;
	int p = 0;
	int i0 = 0;
	int i1 = 0;
	int i2 = 0;
	int i3 = 0;

	if (block <= 9) {
	    level = 0;
	    i0 = p = block;
	}
	else if (block <= (9 + N)) {
	    level = 1;
	    p = block - 10;
	    i0 = 10;
	    i1 = p;
	}
	else if (block <= (9 + N + N * N)) {
	    level = 2;
	    p = block - (10 + N);
	    i0 = 11;
	    i1 = p / N;
	    i2 = p % N;
	}
	else if (block <= (9 + N + N * N + N * N * N)) {
	    level = 3;
	    p = block - (10 + N + N * N);
	    i0 = 12;
	    i1 = p / (N * N);
	    i2 = (p / N) % N;
	    i3 = p % N;
	}
	else {
		String result = process.getResult();
	    result += error("The file is too big") + "\n";
	    process.setResult(result);
	    return -1;
	}

	if (level == 0) 
		return inode.getPointer()[i0];

	IndirectBlock ib = new IndirectBlock(this.blockSize);

	int disk_i1 = inode.getPointer()[i0];
	if (disk_i1 <= 0) 
		return -1; 
	else this.disk.read(disk_i1, ib);

	if (level == 1) 
		return ib.pointer[i1];

	int disk_i2 = ib.pointer[i1];
	if (disk_i2 <= 0) 
		return -1; 
	else 
		this.disk.read(disk_i2, ib);

	if (level == 2) 
		return ib.pointer[i2];

	int disk_i3 = ib.pointer[i2];
	if (disk_i3 <= 0) 
		return -1; 
	else 
		this.disk.read(disk_i3, ib);

	return ib.pointer[i3];
    }

    private int allocateBlock(PCB process, int fd, int where) {
	
		if (!this.ft.isValid(fd)) {
			String result = process.getResult();
		    result += error("The given file descriptor does not point to an open file.") + "\n";
		    process.setResult(result);
		    return -1;
		}
	
		if (this.sb.freeList <= 0) {
			String result = process.getResult();
		    result += error("Disk full.") + "\n";
		    process.setResult(result);
		    return -1;
		}
	
		int block = allocateBlock(process);
		if (block < 0) return -1;
		
		Inode I = this.ft.getInode(fd);
	
		if (addBlock(process, I, block, where) < 0) {
		    freeBlock(block);
		    return -1;
		}
	
		return block;
    }

    private int allocateBlock(PCB process) {
	
		if (this.sb.freeList <= 0) {
			String result = process.getResult();
		    result += error("Disk full.") + "\n";
		    process.setResult(result);
		    return -1;
		}
	
		if (this.freeList == null) {
	
		    this.freeList = new IndirectBlock(this.blockSize);
	
		    this.disk.read(this.sb.freeList, this.freeList);
		}
	
		int offset;
	
		for (offset = 1; (offset < this.blockSize / 4 - 1) && (freeList.pointer[offset] <= 0);
		     offset++) ; 
	
	
		int freeBlock = this.freeList.pointer[offset];
	
		this.freeList.pointer[offset] = 0;
	
		if (freeBlock == 0) {
	
		    freeBlock = this.sb.freeList;
	
		    this.sb.freeList = this.freeList.pointer[0];
	
		    offset = 0;
		    this.freeList = null;
		}
	
		return freeBlock;
    }
    
    private int freeBlock(int block) {

	
		if (block <= 0) return -1;
	
		if  (this.sb.freeList <= 0) {
	
		    this.sb.freeList = block;
	
		    this.freeList = new IndirectBlock(this.blockSize);
		    this.freeList.clear();
	
		    return 0;
		}
	
		if (this.freeList == null) {
	
		    this.freeList = new IndirectBlock(this.blockSize);
	
		    this.disk.read(this.sb.freeList, this.freeList);
		}

		int offset;

		for (offset = this.blockSize / 4 - 1;
		    (offset > 0) && (this.freeList.pointer[offset] > 0);
		     offset--) ;
	
		if (offset <= 0) {
	
		    this.disk.write(this.sb.freeList, this.freeList);
	
		    this.freeList = new IndirectBlock(this.blockSize);
		    this.freeList.clear();
		    this.freeList.pointer[0] = this.sb.freeList;
	
		    this.sb.freeList = block;
		}
		else {
		    this.freeList.pointer[offset] = block;
		}
		
		return 0;
    }
    
    private int addBlock(PCB process, Inode inode, int block, int where) {
	
		final int N = this.blockSize / 4;
	
		int level = 0;
		int p = 0;
		int i0 = 0;
		int i1 = 0;
		int i2 = 0;
		int i3 = 0;
	
		if (where <= 9) {
		    level = 0;
		    i0 = p = where;
		}
		else if (where <= (9 + N)) {
		    level = 1;
		    p = where - 10;
		    i0 = 10;
		    i1 = p;
		}
		else if (where <= (9 + N + N * N)) {
		    level = 2;
		    p = where - (10 + N);
		    i0 = 11;
		    i1 = p / N;
		    i2 = p % N;
		}
		else if (where <= (9 + N + N * N + N * N * N)) {
		    level = 3;
		    p = where - (10 + N + N * N);
		    i0 = 12;
		    i1 = p / (N * N);
		    i2 = (p / N) % N;
		    i3 = p % N;
		}
		else {
			String result = process.getResult();
		    result += error("The file is too big") + "\n";
		    process.setResult(result);
		    return -1;
		}
		
		if (level == 0) {
			int[] pointer = inode.getPointer();
		    pointer[i0] = block;
		    inode.setPointer(pointer);
		    return 0;
		}
	
		int allocated = 0;
		int[] allocatedBlocks = new int[3];
		IndirectBlock ib = new IndirectBlock(this.blockSize);
	
		int disk_i1 = inode.getPointer()[i0];
		if (disk_i1 <= 0) {
	
		    for (int i = 0; i < level; i++) {
	
			int b = allocateBlock(process);
	
			if (b <= 0) {
			    
			    for (int j = 0; j < i; j++) freeBlock(allocatedBlocks[j]);
			    return -1;
			}
	
			allocatedBlocks[i] = b;
			allocated++;
		    }
	
		    disk_i1 = inode.getPointer()[i0] = allocatedBlocks[--allocated];
	
		    ib.clear();
		}
		else {
		    this.disk.read(disk_i1, ib);
		}
	
		if (level == 1) {
		    ib.pointer[i1] = block;
		    this.disk.write(disk_i1, ib);
		    return 0;
		}
	
		boolean toBeAllocated = allocated > 0;
		int disk_i2 = (toBeAllocated) ? (allocatedBlocks[--allocated]) : (ib.pointer[i1]);
		if (toBeAllocated || (disk_i2 <= 0)) {
		    if (disk_i2 <= 0) {
			
			if (allocated > 0) {
				String result = process.getResult();
			    result += error("Internal error") + "\n";
			    process.setResult(result);
			    for (int j = 0; j < allocated; j++) freeBlock(allocatedBlocks[j]);
			    return -1;
			}
			
			for (int i = 0; i < level - 1; i++) {
			    
			    int b = allocateBlock(process);
			    
			    if (b <= 0) {
				
				for (int j = 0; j < i; j++) freeBlock(allocatedBlocks[j]);
				return -1;
			    }
			    
			    allocatedBlocks[i] = b;
			    allocated++;
			}
	
	 		disk_i2 = ib.pointer[i1] = allocatedBlocks[--allocated];
			this.disk.write(disk_i1, ib);
			
			ib.clear();
		    }
		    else {
	
	 		ib.pointer[i1] = disk_i2;
			this.disk.write(disk_i1, ib);
			
			ib.clear();
		    }
		}
		else {
		    this.disk.read(disk_i2, ib);
		}
	
		if (level == 2) {
		    ib.pointer[i2] = block;
		    this.disk.write(disk_i2, ib);
		    return 0;
		}
	
		toBeAllocated = allocated > 0;
		int disk_i3 = (toBeAllocated) ? (allocatedBlocks[--allocated]) : (ib.pointer[i2]);
		if (toBeAllocated || (disk_i3 <= 0)) {
		    if (disk_i3 <= 0) {
	
			if (allocated > 0) {
				String result = process.getResult();
			    result += error("Internal error") + "\n";
			    process.setResult(result);
			    for (int j = 0; j < allocated; j++) freeBlock(allocatedBlocks[j]);
			    return -1;
			}
			
			int b = allocateBlock(process);
			
			if (b <= 0) {
			    freeBlock(b);
			    return -1;
			}
			
			disk_i3 = ib.pointer[i2] = b;
			this.disk.write(disk_i2, ib);
			
			ib.clear();
		    }
		    else {
	
	 		ib.pointer[i2] = disk_i3;
			this.disk.write(disk_i2, ib);
			
			ib.clear();
		    }
		}
		else {
		    this.disk.read(disk_i3, ib);
		}
		
		ib.pointer[i3] = block;
		this.disk.write(disk_i3, ib);
	
		return 0;
    }
    
    private int freeIndirect(int block, int level) {
	
		if (block == 0) return  0;
		if (block <  0) return -1;
		if (level <  0) return -1;
	
		if (level == 0) {
		    return (freeBlock(block) < 0) ? (-1) : (1);
		}
	
		int count = 0;
		IndirectBlock ib = new IndirectBlock(this.blockSize);
		
		this.disk.read(block, ib);
	
		for (int i = 0; i < this.blockSize / 4; i++) {
	
		    if (ib.pointer[i] <= 0) continue;
	
		    int r = freeIndirect(ib.pointer[i], level - 1);
	
		    if (r > 0) {
			count += r;
		    }
		}
	
		freeBlock(block);
	
		return count;
    }



}
