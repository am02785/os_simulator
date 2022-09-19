package application;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public class MemoryMappedFile {
	
	private String deviceName = null;
	private String fileName = null;
	private int numberOfWriteOperations = 0;
	private int numberOfReadOperations = 0;
	
	public MemoryMappedFile(String deviceName, String fileName) {
		if (deviceName != null) {
			this.setDeviceName(deviceName);
		}
		else {
			this.setDeviceName("");
		}
		if (fileName != null)
			this.fileName = fileName;
		else
			this.fileName = "largeFile.txt";
	}
	
	public int write (PCB process, int position, int size, Byte[] bytesBuffer) {
		this.numberOfWriteOperations ++;
		int count = 0;
		RandomAccessFile memoryMappedFile;
		try {
			memoryMappedFile = new RandomAccessFile(this.fileName, "rw");
			MappedByteBuffer out = memoryMappedFile.getChannel().map(FileChannel.MapMode.READ_WRITE, position, size);
			for (int i = 0; i < size; i++) {
				if (i<bytesBuffer.length) {
					out.put(bytesBuffer[i]);
					count ++;
				}
				else
					break;
			}
			String result = process.getResult();
			result += "Writing to Memory Mapped File " + count + " bytes is completed\n";
			process.setResult(result);
		}
		catch (IOException e) {
			e.printStackTrace();
		}		
		return count;		
	}
	
	public Byte[] read (PCB process, int position, int size) {
		this.numberOfReadOperations ++;
		String result = process.getResult();
		Byte[] data = new Byte[size];
		RandomAccessFile memoryMappedFile;
		try {
			memoryMappedFile = new RandomAccessFile(this.fileName, "rw");
			MappedByteBuffer in = memoryMappedFile.getChannel().map(FileChannel.MapMode.READ_ONLY, position, size);
			for (int i = 0; i < size ; i++) {
				data[i] = in.get(i);
				result += (char) in.get(i);
			}
			result += "Reading from Memory Mapped File is completed\n";
			process.setResult(result);
		} 
		catch (IOException e) {
			e.printStackTrace();
		}	
		return data;
	}
	
	public String getDeviceName() {
		return deviceName;
	}

	public void setDeviceName(String deviceName) {
		this.deviceName = deviceName;
	}
	
	public int getNumberOfWriteOperations() {
		return this.numberOfWriteOperations;
	}
	
	public int getNumberOfReadOperations() {
		return this.numberOfReadOperations;
	}

}
