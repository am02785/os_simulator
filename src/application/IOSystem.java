package application;

import java.util.ArrayList;

public interface IOSystem {
	
	ArrayList<MemoryMappedFile> peripherals = new ArrayList<MemoryMappedFile>();  
	
	public int addDevice (String deviceName, String BufferFileName);
	
	public int write(PCB process, String deviceName, int position, Byte[] bytesBuffer);
	
	public Byte[] read(PCB process, String deviceName, int position, int size);
	
	public int removeDevice(String deviceName);

}
