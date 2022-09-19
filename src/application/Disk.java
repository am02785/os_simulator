package application;

import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

public class Disk {
	
	private int blockSize = 0;
	private int numBlocks = 0;
	
	private int pointersPerBlock = 0;
	
	private int readCount = 0;
	private int writeCount = 0;
	
	private File file = null;
	private RandomAccessFile disk = null;
	
	public Disk(int blockSize, int numBlocks) {
		this.blockSize = blockSize;
		this.numBlocks = numBlocks;
		try {
			this.file = new File("DISK");
			disk = new RandomAccessFile(this.file, "rw");
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void seek(int blockNum) throws IOException {
		if (blockNum < 0 || blockNum >= this.numBlocks) {
			throw new RuntimeException ("Attempt to read block " + blockNum + " is out of range");
		}
		this.disk.seek((long)(blockNum * this.blockSize));
	}
	
	public void read(int blocknum, byte[] buffer) {
		if (buffer.length != this.blockSize) {
			throw new RuntimeException("Read: bad buffer size " + buffer.length);
		}
		try {
			seek(blocknum);
			this.disk.read(buffer);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		this.readCount++;
	} 
	
	public void read(int blocknum, SuperBlock block) {
		try {
			this.seek(blocknum);
			block.size = this.disk.readInt();
			block.iSize = this.disk.readInt();
			block.freeList = this.disk.readInt();
		}
		catch (EOFException e) {
			if (blocknum != 0) {
				e.printStackTrace();
			}
			block.size = block.iSize = block.freeList = 0;
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		this.readCount++;
	} 
	
	public void read(int blocknum, InodeBlock block) {
		try {
			this.seek(blocknum);
			Inode[] node = block.getNode();
			for (int i=0; i<node.length; i++) {
				node[i].setFlags(this.disk.readInt());
				node[i].setOwner(this.disk.readInt());
				node[i].setFileSize(this.disk.readInt());
				int[] pointer = node[i].getPointer();
				for (int j=0; j<13; j++)
					pointer[j] = this.disk.readInt();
				node[i].setPointer(pointer);
				block.setNode(node);
			}
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		this.readCount++;
	}
	
	public void read(int blocknum, IndirectBlock block) {
		try {
			this.seek(blocknum);
			for (int i=0; i<block.pointer.length; i++)
				block.pointer[i] = this.disk.readInt();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		this.readCount++;
	}
	
	public void write(int blocknum, byte[] buffer) {
		if (buffer.length != this.blockSize) 
			throw new RuntimeException("Write: bad buffer size " + buffer.length);
		try {
			this.seek(blocknum);
			this.disk.write(buffer);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		this.writeCount++;
	}
	
	public void write(int blocknum, SuperBlock block) {
		try {
			this.seek(blocknum);
			this.disk.writeInt(block.size);
			this.disk.writeInt(block.iSize);
			this.disk.writeInt(block.freeList);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		this.writeCount++;
	} 
	
	public void write(int blocknum, InodeBlock block) {
		try {
			this.seek(blocknum);
			Inode[] node = block.getNode();
			for (int i=0; i<node.length; i++) {
				this.disk.writeInt(node[i].getFlags());
				this.disk.writeInt(node[i].getOwner());
				this.disk.writeInt(node[i].getFileSize());
				int[] pointer = node[i].getPointer();
				for (int j=0; j<13; j++)
					this.disk.writeInt(pointer[j]);
			}
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		this.writeCount++;
	} 
	
	public void write(int blocknum, IndirectBlock block) {
		try {
			this.seek(blocknum);
			for (int i=0; i<block.pointer.length; i++)
				this.disk.writeInt(block.pointer[i]);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		this.writeCount++;
	}
	
	public String stop(boolean removeFile) {
		if (removeFile) {
			this.file.delete();
		}
		return generateStats();
	}
	
	public String stop() {
		return this.stop(true);
	}
	
	public String generateStats() {
		return ("DISK: Read count: " + this.readCount + " Write count: " + 
			this.writeCount);
	}

}
