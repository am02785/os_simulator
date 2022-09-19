package application;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class ProcessCreation extends Thread {
	
	private Vector readyBuffer = null;
	private int sizeOfReadyBuffer = 0;
	private File file = null;
	private int processID = 0;
	private int initialTime = 0;
	
	public ProcessCreation(Vector readyBuffer, int sizeOfReadyBuffer, File file, int initialTime) {
		super();
		this.readyBuffer = readyBuffer;
		this.sizeOfReadyBuffer = sizeOfReadyBuffer;
		this.file = file;
		this.processID = 1;
		this.initialTime = initialTime;
	}

	@Override
	public void run() {
		try {
			while (true) {
				this.createProcess();
			}
		}
		catch (InterruptedException e) {
		}
	}
	
	private synchronized void createProcess() throws InterruptedException {
		while (this.readyBuffer.size() == this.sizeOfReadyBuffer) { 
			wait();
		}
		try {
			//File file = new File(this.fileName);
			Scanner myReader = new Scanner(this.file);
			if (myReader.hasNextLine()) {
				String process = myReader.nextLine();
				String[] processArray = process.split(";");
				int processID = this.processID;
				this.processID++;
				String instructions = "";
				for (int i = 0; i < processArray.length - 3; i++) {
					instructions += processArray[i] + ";";
				}
				int CPUBurst = Integer.parseInt(processArray[processArray.length - 3]);
				int priority = Integer.parseInt(processArray[processArray.length - 2]);
				int memoryRequired = Integer.parseInt(processArray[processArray.length - 1]);
				PCB pcb = new PCB(processID, instructions, CPUBurst, priority, memoryRequired);
				for (String commandLine : pcb.getInstructions().split(";")) {
					pcb.addCommand(commandLine);
				}
				this.readyBuffer.addElement(pcb);
				int arrivalTime = (int) ((LocalTime.now().toNanoOfDay() / Math.pow(10, 6)) - this.initialTime);
				pcb.setArrivalTime(arrivalTime);
				pcb.setTimeAddedToReadyBuffer(arrivalTime);
				List<String> nextProcesses = new ArrayList<>();
				while (myReader.hasNextLine()) {
					nextProcesses.add(myReader.nextLine());
				}
				FileWriter myWriter = new FileWriter(this.file);
				for (String nextProcess : nextProcesses) {
					myWriter.write(nextProcess + "\n");
				}
				myWriter.close();
			}
			myReader.close();
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		notify();
	}
	
	public synchronized void putProcess(PCB process) throws InterruptedException {
		//System.out.println(process.getInstructionNumber());
		while (this.readyBuffer.size() == this.sizeOfReadyBuffer) {
			wait();
		}
		this.readyBuffer.addElement(process);
		process.setTimeAddedToReadyBuffer((int) ((LocalTime.now().toNanoOfDay() / Math.pow(10, 6)) - this.initialTime));
		notify();
	}
	
	public synchronized PCB getFirstProcess() throws InterruptedException {
		notify();
		while (this.readyBuffer.size() == 0) {
			wait();
		}
		PCB pcb = (PCB) this.readyBuffer.remove(0);
		return pcb;
	}
	
	public synchronized PCB getShortestJob() throws InterruptedException {
		notify();
		while (this.readyBuffer.size() == 0) {
			wait();
		}
		PCB shortestJob = (PCB) this.readyBuffer.get(0);
		int shortestCPUBurst = shortestJob.getCPUBurst();
		for (Object object : this.readyBuffer) {
			PCB pcb = (PCB) object;
			if (pcb.getCPUBurst() < shortestCPUBurst) {
				shortestJob = pcb;
				shortestCPUBurst = pcb.getCPUBurst();
			}
		}
		this.readyBuffer.remove(shortestJob);
		return shortestJob;
	}
	
	public synchronized PCB getHighestPriorityProcess() throws InterruptedException {
		notify();
		while (this.readyBuffer.size() == 0) {
			wait();
		}
		PCB highestPriorityProcess = (PCB) this.readyBuffer.get(0);
		int highestPriority = highestPriorityProcess.getPriority();
		for (Object object : this.readyBuffer) {
			PCB pcb = (PCB) object;
			if (pcb.getPriority() > highestPriority) {
				highestPriorityProcess = pcb;
				highestPriority = pcb.getPriority();
			}
		}
		this.readyBuffer.remove(highestPriorityProcess);
		return highestPriorityProcess;
	}
	
	public void setFile(File file) {
		this.file = file;
	}
	
	public int getInitialTime() {
		return this.initialTime;
	}
	
}
