package me.edwards.des.block;

import java.util.ArrayList;

/**
 * Data structure to maintain the entire (or a section of the) block chain
 * Created on: Nov 2, 2015 at 2:23:14 PM
 * @author Matthew Edwards
 */
public class BlockChain
{
    private int size;
    private Node top;
    private ArrayList<Block> queue;
    
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
        queue = new ArrayList<Block>();
    }
    
    /**
     * Adds a block to the top of this BlockChain
     * @param block
     */
    private void append(Block block)
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
    
    /**
     * Queues the specified block to be added to this BlockChain
     * @param block
     */
    public void queue(Block block)
    {
        if (getTop().getHash().equalsIgnoreCase(block.getPrevHash()))
        {
            append(block);
        }
        else
        {
            queue.add(block);
        }
    }
    
    /**
     * Returns true if the specified hash belongs to a block in this BlockChain
     * @param hash
     * @return
     */
    public boolean contains(String hash)
    {
        Node n = top;
        while (n.height >= 0)
        {
            if (n.block.getHash().equalsIgnoreCase(hash))
            {
                return true;
            }
            if (n.height > 0)
            {
                n = n.parent;
            }
            else
            {
                break;
            }
        }
        return false;
    }
    
    /**
     * Returns the block in this BlockChain with the specified hash
     * @param hash
     * @return
     */
    public Block get(String hash)
    {
        Node n = top;
        while (n.height >= 0)
        {
            if (n.block.getHash().equalsIgnoreCase(hash))
            {
                return n.block;
            }
            if (n.height > 0)
            {
                n = n.parent;
            }
            else
            {
                break;
            }
        }
        return null;
    }
    
    private class Node
    {
        private Node parent;
        private int height;
        private Block block;
    }
}
