package me.edwards.des.block;

/**
 * Data structure to maintain the entire (or a section of the) block chain
 * Created on: Nov 2, 2015 at 2:23:14 PM
 * @author Matthew Edwards
 */
public class BlockChain
{
    private Node genesis;
    private Node top;
    
    /**
     * Creates new BlockChain from the genesis block
     * @param genesis 
     */
    public BlockChain(Block genesis)
    {
        this.genesis = new Node();
        this.genesis.height = 0;
        this.genesis.block = genesis;
        this.top = this.genesis;
    }
    
    /**
     * Adds a block to the end of this BlockChain
     * @param block
     */
    public void addBlock(Block block)
    {
        Node second = top;
        top = new Node();
        second.child = top;
        top.parent = second;
        top.height = second.height + 1;
        top.block = block;
    }
    
    /**
     * Returns the block on the top of the BlockChain
     * @return
     */
    public Block getTop()
    {
        return top.block;
    }
    
    private class Node
    {
        private Node parent;
        private Node child;
        private int height;
        private Block block;
    }
}
