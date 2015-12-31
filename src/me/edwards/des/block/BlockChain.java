/*============================================================================*\
 | Copyright (C) 2015 Matthew Edwards                                         |
 |                                                                            |
 | Licensed under the Apache License, Version 2.0 (the "License"); you may    |
 | not use this file except in compliance with the License. You may obtain a  |
 | copy of the License at                                                     |
 |                                                                            |
 |     http://www.apache.org/licenses/LICENSE-2.0                             |
 |                                                                            |
 | Unless required by applicable law or agreed to in writing, software        |
 | distributed under the License is distributed on an "AS IS" BASIS,          |
 | WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.   |
 | See the License for the specific language governing permissions and        |
 | limitations under the License.                                             |
\*============================================================================*/

package me.edwards.des.block;

import java.nio.ByteBuffer;
import java.util.ArrayList;

// -----------------------------------------------------------------------------
/**
 * Data structure to maintain {@link Block Blocks} in the BlockChain. Blocks can
 * be randomly appended to the BlockChain using
 * {@linkplain BlockChain#append(Block)} and will be put into the correct order
 * to form a continuous tree of Blocks backwards-linked through the
 * {@link Block#getPrevHash() previous hash} fields. The BlockChain maintains a
 * queue of orphan Blocks to ensure that Blocks received by the Node
 * out-of-order in the tree can be appended to the BlockChain correctly. The
 * BlockChain also maintains a list of all the Blocks acting as leaves on the
 * top of the tree, but will always choose the longest continuous chain of
 * Blocks as the main chain.<br>
 * <br>
 * The branching abilities of the BlockChain provide the mechanism of the
 * network to "vote" on valid Blocks and come to a consensus of what the "real"
 * BlockChain is. Blocks that are generated and deemed valid are added to the
 * BlockChain while invalid Blocks will never be added to the BlockChain. When
 * two valid Blocks with the same parent are added to the BlockChain, a branch
 * is created, which may cause a split in the network. Further explanation
 * provided with {@linkplain BlockChain#append(Block)}.<br>
 * <br>
 * Created on: Nov 2, 2015 at 2:23:14 PM
 * 
 * @author Matthew Edwards
 */
public class BlockChain
{
    // ~ Static/Instance variables .............................................

    // -------------------------------------------------------------------------
    /**
     * The maximum size, in bytes, that a Block may be
     */
    public static final int MAXIMUM_BLOCK_SIZE = 1024 * 1024 * 10;
    
    
    // -------------------------------------------------------------------------
    private Node              top;
    private ArrayList<Block>  queue;
    private ArrayList<Node>   topList;


    // ~ Constructors ..........................................................

    // -------------------------------------------------------------------------
    /**
     * Creates new BlockChain with the first {@link Block Block} (the Genesis
     * Block). This constructor is only used when the Node is starting a new
     * BlockChain, otherwise the BlockChain is downloaded from a peer Node or
     * loaded from file.
     * 
     * @param genesis
     *            First Block in the new BlockChain
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


    // -------------------------------------------------------------------------
    /**
     * Initializes BlockChain from binary data as a byte array. This constructor
     * is used when a BlockChain is loaded from a file or downloaded from a peer
     * Node.
     * 
     * @param size
     *            Number of chunks in which this BlockChain was saved
     * @param binary
     *            Loaded array of chunks (byte arrays containing {@link Block
     *            Blocks}) representing this BlockChain
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
                data.get(blockData);
                Node child = new Node();
                child.height = height--;
                child.block = new Block(blockData);
                child.parent = null;
                if (n != null)
                {
                    n.parent = child;
                }
                if (i == 0 && j == 0)
                {
                    this.top = child;
                }
                n = child;
            }
        }
        this.topList.add(this.top);
    }


    // ~ Methods ...............................................................

    // -------------------------------------------------------------------------
    /**
     * Returns the BlockChain in binary format as a chunk array.
     * 
     * @return Array of chunks (byte arrays containing {@link Block Blocks})
     *         representing this BlockChain
     */
    public byte[][] getBytes()
    {
        long length = 0;
        Node n = top;
        for (long i = 0; getSize() > i; i++)
        {
            length += 4 + n.block.getBytes().length;
            n = n.parent;
        }
        byte[][] bytes = new byte[(int)Math.ceil((double)length / MAXIMUM_BLOCK_SIZE)][];
        n = top;
        for (int i = 0; bytes.length > i; i++)
        {
            ByteBuffer data =
                ByteBuffer.allocate((int)Math.min(length, MAXIMUM_BLOCK_SIZE));
            for (int j = 0; getSize() > j; j++)
            {
                if (n.block.getBytes().length + data.position() > data.limit())
                {
                    break;
                }
                data.putInt(n.block.getBytes().length);
                data.put(n.block.getBytes());
                length -= n.block.getBytes().length;
                n = n.parent;
                if (n == null)
                {
                    break;
                }
            }
            int pos = data.position();
            bytes[i] = new byte[pos];
            data.position(0);
            data.get(bytes[i]);
        }
        return bytes;
    }


    // -------------------------------------------------------------------------
    /**
     * Returns the {@link Block Block} on the top of the longest branch in this
     * BlockChain.
     * 
     * @return Block at the top of this BlockChain
     */
    public Block getTop()
    {
        return top.block;
    }


    // -------------------------------------------------------------------------
    /**
     * Returns the number of {@link Block Blocks} in the longest branch in this
     * BlockChain
     * 
     * @return Number of Blocks in the longest branch
     */
    public int getSize()
    {
        return top.height + 1;
    }


    // -------------------------------------------------------------------------
    /**
     * Queues the specified {@link Block Block} to be added to this BlockChain.
     * This method accepts orphan Blocks (a Block whose parent is not contained
     * in the BlockChain), child Blocks of the main chain, and child Blocks of
     * branch chains. The algorithm for appending Blocks allows for branching of
     * the BlockChain, and resolution of Blocks to form a consensus between
     * Nodes.<br>
     * <br>
     * For example, if a Node in California generated a valid Block at the same
     * time as a Node in Virginia, Nodes on the East Coast might have a
     * different version of the BlockChain than Nodes on the West Coast. The
     * East Coast would have the Block generated by the Node in Virginia as the
     * top block in the BlockChain, while the West Coast would have the Block
     * generated by the Node in California as the top block in the BlockChain.
     * However, all Nodes would have both the "East Block" and the "West Block"
     * contained in their BlockChains, just disagree on which Block is the at
     * the top of the BlockChain.<br>
     * <br>
     * When the next Block is generated and propagated throughout the network,
     * it will have either the "East Block" or the "West Block" as a parent. As
     * Nodes receive this new Block (which as an example will have the
     * "East Block" as its parent), the branch containing the parent will be
     * extended, and the entire network will come to a consensus concerning the
     * BlockChain. The new block will be at the top of the BlockChain and the
     * "East Block" will be its parent. The branch containing the "West Block"
     * is now shorter, and declared invalid by each Node. Therefore, the entire
     * network will have a single agreed-upon BlockChain.
     * 
     * @param block
     *            Block to be added to this BlockChain
     */
    public void append(Block block)
    {
        queue.add(0, block);

        for (int i = 0; queue.size() > i; i++)
        {
            for (int j = 0; topList.size() > j; j++)
            {
                if (topList.get(j).getBlock().getHash()
                    .equalsIgnoreCase(queue.get(i).getPrevHash()))
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
                        if (n.block.getHash().equalsIgnoreCase(
                            queue.get(i).getPrevHash()))
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
            if (topList.get(i).height + 11 < getSize())
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
        topList.remove(top);
        topList.add(0, top);
        top = temp;
    }


    // -------------------------------------------------------------------------
    /**
     * Returns true if the specified hash belongs to a {@link Block Block} in
     * this BlockChain.
     * 
     * @param hash
     *            Block's 32-digit hexadecimal {@link Block#getHash() hash}
     * @return True if the specified hash belongs to a Block in this BlockChain
     */
    public boolean contains(String hash)
    {
        for (int i = 0; topList.size() > i; i++)
        {
            Node n = topList.get(i);
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
        }
        return false;
    }


    // -------------------------------------------------------------------------
    /**
     * Returns the {@link Block Block} in this BlockChain with the specified
     * hash.
     * 
     * @param hash
     *            Block's 32-digit hexadecimal {@link Block#getHash() hash}
     * @return If the Block exists, it is returned, otherwise, a null value is
     *         returned.
     */
    public Block get(String hash)
    {
        for (int i = 0; topList.size() > i; i++)
        {
            Node n = topList.get(i);
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
        }
        return null;
    }


    // -------------------------------------------------------------------------
    /**
     * Returns the {@link Node node} in this BlockChain containing the Block
     * with the specified hash.
     * 
     * @param hash
     *            Block's 32-digit hexadecimal {@link Block#getHash() hash}
     * @return If the Block exists, the node containing it is returned.
     *         Otherwise, a null value is returned.
     */
    public Node getNode(String hash)
    {
        for (int i = 0; topList.size() > i; i++)
        {
            Node n = topList.get(i);
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
        }
        return null;
    }
    
    
    // -------------------------------------------------------------------------
    /**
     * Returns the Median Time of the last 10 (or available) Blocks.
     * 
     * @param hash
     *            Hash of the Block at which to begin
     * @return The Median Time of the last 10 Blocks, -1 if the Hash could not
     *         be found.
     */
    public long getMedianTime(String hash)
    {
        Node n = getNode(hash);
        if (n == null)
        {
            return -1;
        }
        int h = n.height;
        ArrayList<Long> times = new ArrayList<Long>();
        while (h - n.height < 10)
        {
            long time = n.block.getTime();
            for (int i = 0; times.size() > i; i++)
            {
                if (times.get(i) >= time)
                {
                    times.add(i, time);
                    break;
                }
            }
            if (times.size() == 0)
            {
                times.add(time);
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
        return times.get(times.size() / 2);
    }
    

    // -------------------------------------------------------------------------
    /**
     * Returns True if the specified {@linkplain Ballot} is contained in the
     * longest branch of this BlockChain.
     * 
     * @param uuid UUID of the Ballot
     * @return True if the Ballot exists, False otherwise
     */
    public boolean hasBallot(String uuid)
    {
        Node n = top;
        while (n.height >= 0)
        {
            ArrayList<Ballot> ballots = n.block.getBallots();
            for (int i = 0; ballots.size() > i; i++)
            {
                if (ballots.get(i).getID().equalsIgnoreCase(uuid))
                {
                    return true;
                }
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


    // -------------------------------------------------------------------------
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


    // -------------------------------------------------------------------------
    /**
     * Represents a tree node in the BlockChain<br>
     * <br>
     * Created on: Dec 21, 2015 at 5:20:30 PM
     * 
     * @author Matthew Edwards
     */
    public class Node
    {
        // ~ Static/Instance variables .........................................

        private Node  parent;
        private int   height;
        private Block block;


        // ~ Methods ...........................................................

        // ---------------------------------------------------------------------
        /**
         * Returns the parent node.
         * 
         * @return Parent node of this node. If this node is at the bottom of
         *         the chain, the parent node is null
         */
        public Node getParent()
        {
            return parent;
        }


        // ---------------------------------------------------------------------
        /**
         * Returns the height of this node in the BlockChain.
         * 
         * @return Height of this node. The node at the bottom of the chain has
         *         a height of 0
         */
        public int getHeight()
        {
            return height;
        }


        // ---------------------------------------------------------------------
        /**
         * Returns the {@link Block Block} contained in this node.
         * 
         * @return Block contained in this node
         */
        public Block getBlock()
        {
            return block;
        }
    }
}
