package application;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

public class VM extends Thread {
	
	private int pageSize = 0;
	private int numberOfFrames = 0;
	private int TLBSize = 0;
	
	private File file = null;
	private RandomAccessFile disk = null;
	
	//private int virtualAddress = 0;
	//private String instruction = null;
	//private PCB process = null;
	
	private int pageNumber = 0;
	private int frameNumber = 0;
	private int offset = 0;
	
	private int nextFrameNumber = 0;
	private int nextTLBEntry = 0;
	
	private PageTableEntry[] pageTable = null;
	private Frame[] physicalMemory = null;
	
	private TLBEntry[] TLB = null;
	private List<Integer> TLBEntryNumbers = null;
	
	private byte[] buffer = null;
	
	private String pageReplacementAlgorithm = null;
	private List<Integer> FIFOQueue = null;
	private List<Integer> LRUStack = null;
	
	private int pageFaults = 0;
	private int TLBHits = 0;
	private double pageFaultRate = 0;
	private double TLBHitRate = 0;
	private int numberOfTranslatedAddresses = 0;
	
	private ProcessCreation processCreation = null;
	private CPU cpu = null;
	
	private Bank bank = null;
	
	public VM(int pageTableEntries, int numberOfFrames, int pageSize,
			int TLBSize, String pageReplacementAlgorithm,
			ProcessCreation processCreation, CPU cpu,
			Bank bank) {
		super();
		this.pageSize = pageSize;
		this.numberOfFrames = numberOfFrames;
		this.TLBSize = TLBSize;
		this.pageTable = new PageTableEntry[pageTableEntries];
		for (int i = 0; i < pageTableEntries; i++) {
			this.pageTable[i] = new PageTableEntry(pageSize);
		}
		this.TLB = new TLBEntry[TLBSize];
		this.TLBEntryNumbers = new ArrayList<>();
		for (int i = 0; i < TLBSize; i++) {
			this.TLB[i] = new TLBEntry();
			this.TLBEntryNumbers.add(i);
		}
		this.physicalMemory = new Frame[numberOfFrames];
		for (int i = 0; i < numberOfFrames; i++) {
			this.physicalMemory[i] = new Frame(pageSize);
		}
		this.buffer = new byte[pageSize];
		this.pageReplacementAlgorithm = pageReplacementAlgorithm;
		this.FIFOQueue = new ArrayList<>();
		this.LRUStack = new ArrayList<>();
		this.processCreation = processCreation;
		this.cpu = cpu;
		this.bank = bank;
	}
	
	@Override
	public void run() {
		try {
			while (true) {
				PCB process = this.cpu.getProcessFromVMBuffer();
				this.runTranslation(process);
			}
			
		}
		catch (InterruptedException e) {
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	//public void setProcessCreation(ProcessCreation processCreation) {
	//	this.processCreation = processCreation;
	//}
	
	//public void setVirtualAddress(int virtualAddress) {
		//this.virtualAddress = virtualAddress;
	//}
	
	//public void setInstruction(String instruction) {
		//this.instruction = instruction;
	//}
	
	//public void setProcess(PCB process) {
		//this.process = process;
	//}
	
	public void runTranslation(PCB process) throws IOException, InterruptedException {
		try {
			this.file = new File("BACKING_STORE");
			this.disk = new RandomAccessFile(this.file, "r");
			int physicalAddress = this.getPhysicalAddress(process);
			this.numberOfTranslatedAddresses ++;
			if (process.getVMInstruction().equals("allocateMemory")) {
				this.allocateMemory(process, physicalAddress);
				process.setVMInstruction(null);
			}
			else if (process.getVMInstruction().equals("deallocateMemory")) {
				this.deallocateMemory(physicalAddress);
				process.setVMInstruction(null);
			}
			this.generateStatistics();
			//System.out.println("\n");
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
		finally {
			disk.close();
		}
	}
	
	public int getPhysicalAddress(PCB process) throws IOException {
		this.pageNumber = this.getPageNumber(process);
		if (this.pageReplacementAlgorithm.equals("LRU") && this.LRUStack.contains(this.pageNumber)) {
			this.LRUStack.remove(this.LRUStack.indexOf(this.pageNumber));
			this.LRUStack.add(this.pageNumber);
		}
		this.offset = this.getOffset(process);
		if ( (this.frameNumber = this.checkTLB()) == -1 ) {
			if (this.pageTable[this.pageNumber].getValidBit()) {
				this.frameNumber = this.pageTable[this.pageNumber].getFrameNumber();
			}
			else {
				if (this.nextFrameNumber == this.numberOfFrames) {
					if (this.pageReplacementAlgorithm.equals("FIFO")) {
						int firstPageNumberIn = this.FIFOQueue.remove(0);
						this.FIFOQueue.add(this.pageNumber);
						this.frameNumber = this.pageTable[firstPageNumberIn].getFrameNumber();
					}
					else if (this.pageReplacementAlgorithm.equals("LRU")) {
						int leastRecentlyUsedPageNumber = this.LRUStack.remove(0);
						this.LRUStack.add(this.pageNumber);
						this.frameNumber = this.pageTable[leastRecentlyUsedPageNumber].getFrameNumber();
					}
				}
				else {
					if (this.pageReplacementAlgorithm.equals("FIFO")) {
						this.FIFOQueue.add(this.pageNumber);
					}
					else if (this.pageReplacementAlgorithm.equals("LRU")) {
						this.LRUStack.add(this.pageNumber);
					}
					this.frameNumber = this.getNextFrame();
				}
				this.disk.seek(this.pageNumber * this.pageSize);
				this.disk.readFully(this.buffer);
				this.physicalMemory[this.frameNumber].setFrame(this.buffer);
				this.pageTable[this.pageNumber].setMapping(this.frameNumber);
				this.pageFaults++;
			}
			this.setTLBMapping();
		}
		int physicalAddress = (this.frameNumber * this.pageSize) + this.offset;
		return physicalAddress;
	}
	
	public int getPageNumber(PCB process) {
		return process.getVirtualAddress() / this.pageSize;
	}
	
	public int getOffset(PCB process) {
		return process.getVirtualAddress() % this.pageSize;
	}
	
	public int checkTLB() {
		int frameNumber = -1;
		for (int i = 0; i < this.TLBSize; i++) {
			if (this.TLB[i].checkPageNumber(this.pageNumber)) {
				frameNumber = this.TLB[i].getFrameNumber();
				this.TLBHits++;
				if (this.pageReplacementAlgorithm.equals("LRU")) {
					this.TLBEntryNumbers.remove(this.TLBEntryNumbers.indexOf(i));
					this.TLBEntryNumbers.add(i);
				}
				break;
			}
		}
		return frameNumber;
	}
	
	public int getNextFrame() {
		return this.nextFrameNumber++;
	}
	
	public void setTLBMapping() {
		this.TLB[this.nextTLBEntry].setMapping(this.pageNumber, this.frameNumber);
		if (this.pageReplacementAlgorithm.equals("FIFO")) {
			this.nextTLBEntry = (this.nextTLBEntry + 1) % this.TLBSize;
		}
		else if (this.pageReplacementAlgorithm.equals("LRU")) {
			if (this.nextTLBEntry == this.TLBSize - 1) {
				this.nextTLBEntry = this.TLBEntryNumbers.remove(0);
			}
			else {
				this.nextTLBEntry = this.nextTLBEntry + 1;
			}
		}
	}
	
	public void allocateMemory(PCB process, int physicalAddress) {
		this.physicalMemory[physicalAddress / this.pageSize].allocateMemory(physicalAddress % this.pageSize, process);
		this.pageTable[this.pageNumber].addContent(process);
	}
	
	public void deallocateMemory(int physicalAddress) throws InterruptedException {
		PCB process = this.physicalMemory[physicalAddress / this.pageSize].deallocateMemory(physicalAddress % this.pageSize);
		if (process == null) {
			process = this.pageTable[this.pageNumber].getContent();
		}
		int[] release = new int[1];
		release[0] = 1;
		this.bank.releaseResources(process, process.getBankProcessNum(), release);
		this.processCreation.putProcess(process);
	}
	
	public void generateStatistics() {
		this.pageFaultRate = this.pageFaults / numberOfTranslatedAddresses;
		this.TLBHitRate = this.TLBHits / numberOfTranslatedAddresses;
	}
	
	public int getNumberOfTranslatedAddresses() {
		return this.numberOfTranslatedAddresses;
	}
	
	public int getPageFaults() {
		return this.pageFaults;
	}
	
	public double getPageFaultRate() {
		return this.pageFaultRate;
	}
	
	public int getTLBHits() {
		return this.TLBHits;
	}
	
	public double getTLBHitRate() {
		return this.TLBHitRate;
	}

}
