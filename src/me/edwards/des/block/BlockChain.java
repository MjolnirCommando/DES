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
    private Node top;
    private ArrayList<Block> queue;
    private ArrayList<Node> topList;
    
    /**
     * Creates new BlockChain from the genesis block
     * @param genesis 
     */
    public BlockChain(Block genesis)
    {
        this.top = new Node();
        this.top.height = 0;
        this.top.block = genesis;
        this.queue = new ArrayList<Block>();
        this.topList = new ArrayList<Node>();
        this.topList.add(top);
    }
    
    /**
     * Creates new BlockChain from binary data
     * @param size 
     * @param binary
     */
    public BlockChain(int size, byte[][] binary)
    {
        this.queue = new ArrayList<Block>();
        this.topList = new ArrayList<Node>();
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
        this.topList.add(this.top);
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
        for (long i = 0; getSize() > i; i++)
        {
            length += 4 + n.block.getBytes().length;
            n = top.parent;
        }
        byte[][] bytes = new byte[(int) Math.ceil((double) length / MAX_SIZE)][];
        n = top;
        for (int i = 0; bytes.length > i; i++)
        {
            ByteBuffer data = ByteBuffer.allocate((int) Math.min(length, MAX_SIZE));
            for (int j = 0; getSize() > j; j++)
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
        return top.height + 1;
    }
    
    /**
     * Queues the specified block to be added to this BlockChain
     * @param block
     */
    public void append(Block block)
    {
        queue.add(0, block);
        
        for (int i = 0; queue.size() > i; i++)
        {
            for (int j = 0; topList.size() > j; j++)
            {
                if (topList.get(j).getBlock().getHash().equalsIgnoreCase(queue.get(i).getPrevHash()))
                {
                    Node n = new Node();
                    n.parent = topList.get(j);
                    n.height = topList.get(j).getHeight() + 1;
                    n.block = queue.get(i);
                    topList.remove(j);
                    topList.add(n);
                    queue.remove(i);
                    i = -1;
                    break;
                }
            }
            if (i > -1)
            {
                Node conNode = null;
                for (int k = 0; topList.size() > k; k++)
                {
                    Node n = topList.get(k);
                    int height = n.height;
                    while (n.height >= 0 && height - n.height < 10)
                    {
                        if (n.block.getHash().equalsIgnoreCase(queue.get(i).getPrevHash()))
                        {
                            conNode = n;
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
                    if (conNode != null)
                    {
                        Node n2 = new Node();
                        n2.parent = conNode;
                        n2.height = conNode.height + 1;
                        n2.block = queue.get(i);
                        topList.add(n2);
                        queue.remove(i);
                        i = -1;
                        break;
                    }
                }
            }
        }
        
        Node temp = top;
        for (int i = 0; topList.size() > i; i++)
        {
            if (topList.get(i).height + 4 < getSize())
            {
                topList.remove(i);
                i--;
                continue;
            }
            if (topList.get(i).height > temp.height)
            {
                temp = topList.get(i);
            }
        }
        top = temp;
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
    
    @Override
    public String toString()
    {
        StringBuffer sb = new StringBuffer();
        sb.append("------------------------------------\n");
        sb.append("Size: " + getSize() + "\n");
        sb.append("TOPLIST:\n");
        for (int i = 0; topList.size() > i; i++)
        {
            sb.append("\t" + topList.get(i).getBlock().getHash() + "\n");
        }
        sb.append("QUEUE:\n");
        for (int i = 0; queue.size() > i; i++)
        {
            sb.append("\t" + queue.get(i).getHash() + "\n");
        }
        sb.append("LONGEST:\n");
        Node n = top;
        while (n.parent != null)
        {
            sb.append("\t" + n.getBlock().getHash() + "\n");
            n = n.parent;
        }
        sb.append("\t" + n.getBlock().getHash() + "\n");
        return sb.toString();
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
