package me.edwards.des.block;

import java.nio.ByteBuffer;
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
     * Creates new BlockChain from binary data
     * @param size 
     * @param binary
     */
    public BlockChain(int size, byte[][] binary)
    {
        this.queue = new ArrayList<Block>();
        Node n = null;
        int height = size - 1;
        for (int i = 0; binary.length > i; i++)
        {
            ByteBuffer data = ByteBuffer.wrap(binary[i]);
            for (int j = 0; size > j; j++)
            {
                if (data.position() == data.limit())
                {
                    break;
                }
                int length = data.getInt();
                byte[] blockData = new byte[length];
                data.get(blockData, 0, length);
                Node child = new Node();
                child.height = height--;
                child.block = new Block(blockData);
                child.parent = n;
                if (i == 0 && j == 0)
                {
                    this.top = child;
                }
                n = child;
            }
        }
    }
    
    /**
     * Returns this BlockChain in binary format
     * @return
     */
    public byte[][] getBytes()
    {
        int MAX_SIZE = 1024 * 1024 * 5;
        long length = 0;
        Node n = top;
        for (long i = 0; size > i; i++)
        {
            length += 4 + n.block.getBytes().length;
            n = top.parent;
        }
        byte[][] bytes = new byte[(int) Math.ceil((double) length / MAX_SIZE)][];
        n = top;
        for (int i = 0; bytes.length > i; i++)
        {
            ByteBuffer data = ByteBuffer.allocate((int) Math.min(length, MAX_SIZE));
            for (int j = 0; size > j; j++)
            {
                if (n.block.getBytes().length + data.position() > data.limit())
                {
                    break;
                }
                data.putInt(n.block.getBytes().length);
                data.put(n.block.getBytes());
                length -= n.block.getBytes().length;
                n = top.parent;
            }
            int pos = data.position();
            bytes[i] = new byte[pos];
            data.position(0);
            data.get(bytes[i], 0, pos);
        }
        return bytes;
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
            //TODO
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
     * Returns the Block in this BlockChain with the specified hash
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
    
    /**
     * Returns the node in this BlockChain containing the Block with the specified hash
     * @param hash
     * @return
     */
    public Node getNode(String hash)
    {
        Node n = top;
        while (n.height >= 0)
        {
            if (n.block.getHash().equalsIgnoreCase(hash))
            {
                return n;
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
    
    /**
     * Represents a node in the BlockChain
     * Created on: Dec 21, 2015 at 5:20:30 PM
     * @author Matthew Edwards
     */
    public class Node
    {
        private Node parent;
        private int height;
        private Block block;
        
        /**
         * Returns the parent node
         * @return
         */
        public Node getParent()
        {
            return parent;
        }
        
        /**
         * Returns the height of this node in the BlockChain
         * @return
         */
        public int getHeight()
        {
            return height;
        }
        
        /**
         * Returns the Block contained in this node
         * @return
         */
        public Block getBlock()
        {
            return block;
        }
    }
}
