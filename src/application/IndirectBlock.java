package application;

public class IndirectBlock {
	
	public int[] pointer = null;
	
	public IndirectBlock(int blockSize) {
		this.pointer = new int[blockSize / 4];
	}
	
	public void clear() {
		for (int i = 0; i < this.pointer.length; i++) {
			this.pointer[i] = 0;
		}
	}
	
	@Override
	public String toString() {
		String result = "INDIRECTBLOCK:\n";
		for (int i = 0; i < this.pointer.length; i++) {
			result += this.pointer[i] + "|";
		}
		return result;
	}
	
	
}
