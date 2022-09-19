package application;

import java.io.*;
import java.util.*;

public class BankImpl implements Bank {
	
	private int numberOfprocesses;		
	private int numberOfResources;	
	private int[] available; 	
	private int[][] maximum; 	
	private int[][] allocation;		
	private int[][] need;			

	public BankImpl(int[] resources) {
		this.numberOfResources = resources.length;
		this.available = new int[this.numberOfResources];
		System.arraycopy(resources,0,available,0,this.numberOfResources);
		this.maximum = new int[this.numberOfprocesses][];
		this.allocation = new int[this.numberOfprocesses][];
		this.need = new int[this.numberOfprocesses][];
	}

	public void addCustomer(PCB process, int[] maxDemand) {
		process.setBankProcessNum(this.numberOfprocesses);
		this.maximum = Arrays.copyOf(this.maximum, this.numberOfprocesses + 1);
		this.allocation = Arrays.copyOf(this.allocation, this.numberOfprocesses + 1);
		this.need = Arrays.copyOf(this.need, this.numberOfprocesses + 1);
		this.maximum[this.numberOfprocesses] = new int[this.numberOfResources];
		this.allocation[this.numberOfprocesses] = new int[this.numberOfResources];
		this.need[this.numberOfprocesses] = new int[this.numberOfResources];
		System.arraycopy(maxDemand, 0, this.maximum[this.numberOfprocesses], 0, maxDemand.length);
		System.arraycopy(maxDemand, 0, this.need[this.numberOfprocesses], 0, maxDemand.length);
		this.numberOfprocesses ++;
	}

	/**
	 * Outputs the state for each thread
	 */

	public void getState(PCB process) {
		String result = process.getResult();
		result += "Available = \t[";
		for (int i = 0; i < this.numberOfResources-1; i++)
			result += available[i]+" ";
		result += available[this.numberOfResources-1]+"]" + "\n";
		result += "Allocation = \t";
		for (int i = 0; i < this.numberOfprocesses; i++) {
			result += "[";
			for (int j = 0; j < this.numberOfResources-1; j++)
				result += allocation[i][j]+" ";
			result += allocation[i][this.numberOfResources-1]+"]";
		}
		result += "\nMax = \t\t";
		for (int i = 0; i < this.numberOfprocesses; i++) {
			result += "[";
			for (int j = 0; j < this.numberOfResources-1; j++)
				result += maximum[i][j]+" ";
			result += maximum[i][this.numberOfResources-1]+"]";
		}
		result += "\nNeed = \t\t";
		for (int i = 0; i < this.numberOfprocesses; i++) {
			result += "[";
			for (int j = 0; j < this.numberOfResources-1; j++)
				result += need[i][j]+" ";
			result += need[i][this.numberOfResources-1]+"]";
		}

		result += "\n";
		process.setResult(result);
	}

	private boolean isSafeState (PCB process, int processNum, int[] request) {
		String result = process.getResult();
		result += "Customer # " + processNum + " requesting ";
		for (int i = 0; i < this.numberOfResources; i++) result += request[i] + " ";

		result += "Available = ";
		for (int i = 0; i < this.numberOfResources; i++)
			result += available[i] + "  ";


		for (int i = 0; i < this.numberOfResources; i++) 
			if (request[i] > available[i]) {
				result += "\nINSUFFICIENT RESOURCES";
				return false;
			}


		boolean[] canFinish = new boolean[this.numberOfprocesses];
		for (int i = 0; i < this.numberOfprocesses; i++)
			canFinish[i] = false;

		int[] avail = new int[this.numberOfResources];
		System.arraycopy(available,0,avail,0,available.length);

		for (int i = 0; i < this.numberOfResources; i++) {
			avail[i] -= request[i];
			need[processNum][i] -= request[i];
			allocation[processNum][i] += request[i];
		}
		
		for (int i = 0; i < this.numberOfprocesses; i++) {
			// first find a thread that can finish
			for (int j = 0; j < this.numberOfprocesses; j++) {
				if (!canFinish[j]) {
					boolean temp = true;
					for (int k = 0; k < this.numberOfResources; k++) {
						if (need[j][k] > avail[k])
							temp = false;
					}
					if (temp) { // if this thread can finish
						canFinish[j] = true;
						for (int x = 0; x < this.numberOfResources; x++)
							avail[x] += allocation[j][x];
					}
				}	
			}
		}
		
		for (int i = 0; i < this.numberOfResources; i++) {
			need[processNum][i] += request[i];
			allocation[processNum][i] -= request[i];
		}

	
		boolean returnValue = true;
		for (int i = 0; i < this.numberOfprocesses; i++)
			if (!canFinish[i]) {
				returnValue = false;
				break;
			}
		process.setResult(result);
		return returnValue;
	}

	public synchronized boolean requestResources(PCB process, int processNum, int[] request)  {
		if (!isSafeState(process, processNum,request)) {
			return false;
		}


		for (int i = 0; i < this.numberOfResources; i++) {
			available[i] -= request[i];
			allocation[processNum][i] += request[i];
			need[processNum][i] = maximum[processNum][i] - allocation[processNum][i];
		}
		return true;
	}

	public synchronized void releaseResources(PCB process, int processNum, int[] release)  {
		String result = process.getResult();
		result += "Customer # " + processNum + " releasing ";
		for (int i = 0; i < this.numberOfResources; i++) result += release[i] + " ";

		for (int i = 0; i < this.numberOfResources; i++) {
			available[i] += release[i];
			allocation[processNum][i] -= release[i];
			need[processNum][i] = maximum[processNum][i] + allocation[processNum][i];
		}

		result += "Available = ";
		for (int i = 0; i < this.numberOfResources; i++)
			result += available[i] + "  ";

		result += "Allocated = [";
		for (int i = 0; i < this.numberOfResources; i++)
			result += allocation[processNum][i] + "  "; 
		result += "]\n"; 
		process.setResult(result);
	}
	
}
