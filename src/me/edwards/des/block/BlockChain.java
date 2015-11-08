package me.edwards.des.block;

/**
 * Data structure to maintain the entire (or a section of the) block chain
 * Created on: Nov 2, 2015 at 2:23:14 PM
 * @author Matthew Edwards
 */
public class BlockChain
{
    private int size;
    private Node top;
    
    /**
     * Creates new BlockChain from the genesis block
     * @param genesis 
     */
    public BlockChain(Block genesis)
    {
        this.top = new Node();
        this.top.height = 0;
        this.top.block = genesis;
        this.size = 1;
    }
    
    /**
     * Adds a block to the end of this BlockChain
     * @param block
     */
    public void addBlock(Block block)
    {
        Node second = top;
        top = new Node();
        top.parent = second;
        top.height = second.height + 1;
        top.block = block;
        size++;
    }
    
    /**
     * Returns the block on the top of the BlockChain
     * @return
     */
    public Block getTop()
    {
        return top.block;
    }
    
    /**
     * Returns the number of nodes in this BlockChain
     * @return
     */
    public int getSize()
    {
        return size;
    }
    
    private class Node
    {
        private Node parent;
        private int height;
        private Block block;
    }
}
