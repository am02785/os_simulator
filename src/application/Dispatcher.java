package application;

import java.util.Queue;
import java.util.Vector;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class Dispatcher extends Thread {
	
	private ProcessCreation processCreation = null;
	private Vector runningBuffer = null;
	private int sizeOfrunningBuffer = 0;
	private String schedulingAlgorithm = null;
	
	public Dispatcher(ProcessCreation processCreation, Vector runningBuffer, 
			int sizeOfrunningBuffer, String schedulingAlgorithm) {
		super();
		this.processCreation = processCreation;
		this.runningBuffer = runningBuffer;
		this.sizeOfrunningBuffer = sizeOfrunningBuffer;
		this.schedulingAlgorithm = schedulingAlgorithm;
	}

	@Override
	public void run() {
		try {
			while (true) {
				switch (this.schedulingAlgorithm) {
				case "FCFS":
					this.fcfs();
					break;
				case "SJF":
					this.sjf();
					break;
				case "RR":
					this.rr();
					break;
				case "HPFSP":
					this.hpfsp();
					break;
				case "HPFSN":
					this.hpfsn();
					break;
				}
			}
		}
		catch (InterruptedException e) {
		}
	}
	
	private void fcfs() throws InterruptedException {
		PCB process = this.processCreation.getFirstProcess();
		this.putProcess(process);
	}
	
	private void sjf() throws InterruptedException {
		PCB process = this.processCreation.getShortestJob();
		this.putProcess(process);
	}
	
	private void rr() throws InterruptedException {
		PCB process = this.processCreation.getFirstProcess();
		this.putProcess(process);
	}
	
	private void hpfsp() throws InterruptedException {
		PCB process = this.processCreation.getHighestPriorityProcess();
		this.putProcess(process);
	}
	
	private void hpfsn() throws InterruptedException {
		PCB process = this.processCreation.getHighestPriorityProcess();
		this.putProcess(process);
	}
	
	private synchronized void putProcess(PCB process) throws InterruptedException {
		while (this.runningBuffer.size() == this.sizeOfrunningBuffer) { 
			wait();
		}
		//System.out.println(process.getInstructionNumber());
		this.runningBuffer.addElement(process);
		notify();
	}
	
	public synchronized PCB getProcess() throws InterruptedException {
		notify();
		while (this.runningBuffer.size() == 0) {
			wait();
		}
		PCB pcb = (PCB) this.runningBuffer.remove(0);
		return pcb;
	}

}
