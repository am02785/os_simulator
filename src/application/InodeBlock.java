package application;

public class InodeBlock {
	
	private Inode[] node = null;
	
	public InodeBlock(int blockSize, int size) {
		this.node = new Inode[blockSize / size];
		for (int i = 0; i < this.node.length; i++) {
			this.node[i] = new Inode(size);
		}
	}

	public Inode[] getNode() {
		return this.node;
	}

	public void setNode(Inode[] node) {
		this.node = node;
	}
	
	@Override
	public String toString() {
		String result = "INODEBLOCK:\n";
		for (int i = 0; i < this.node.length; i++) {
			result += this.node[i] + "\n";
		}
		return result;
	}

}
