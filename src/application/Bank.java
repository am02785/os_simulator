package application;

public interface Bank {
	
		public void addCustomer(PCB process, int[] maximumDemand);
		
		public void getState(PCB process);
	
		public boolean requestResources(PCB process, int customerNumber, int[] request);
		
        public void releaseResources(PCB process, int customerNumber, int[] release);
	
}
