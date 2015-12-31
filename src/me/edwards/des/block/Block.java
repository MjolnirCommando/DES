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

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import me.edwards.des.Node;
import me.edwards.des.util.ByteUtil;
import me.edwards.des.util.HashUtil;

// -----------------------------------------------------------------------------
/**
 * Data Structure containing information for a Block. The Block is the main unit
 * of storage for the system. Blocks are generated and propagated through Nodes
 * and store a payload of {@link Ballot Ballots}.<br>
 * <br>
 * These Ballots are tied to the Block via a
 * {@link HashUtil#generateMerkleRoot(String, String) Merkle Root}, preventing
 * any Ballot in the payload from being changed. The Block also contains the
 * hash of the Block before it in the {@link BlockChain BlockChain}, which
 * allows for backwards-linking in the BlockChain. The Block contains a
 * timestamp and target for mining by a {@link Node Node}.<br>
 * <br>
 * Every Block is secured using a {@link HashUtil#generateProof(byte[], int)
 * Proof of Work}, preventing changes after the Block is generated. Any change
 * (even a single bit) in a Ballot or Block attribute will change the overall
 * {@link Block#getHash() hash} of the Block, which will be invalid unless the
 * nonce is changed. The nonce can only be changed to a value which makes the
 * hash valid by re-mining the Block and regenerating a new Proof of Work.<br>
 * <br>
 * Created on: Nov 1, 2015 at 3:09:17 PM
 * 
 * @author Matthew Edwards
 */
public class Block
{
    // ~ Static/Instance variables .............................................

    // -------------------------------------------------------------------------
    /**
     * Maximum Target value for DES
     */
    public static final int   MAXIMUM_TARGET = ByteUtil.bytesToInt(new byte[] {
        (byte) 0x1F, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF });
    // (byte) 0x1d, (byte) 0xF0, (byte) 0x00, (byte) 0x00});

    
    // -------------------------------------------------------------------------
    private final int         VERSION        = 1;
    private int               version;
    private String            prevBlockHash;
    private String            merkleRootHash;
    private int               time;
    private int               target;
    private int               nonce;
    private ArrayList<Ballot> ballots;

    private byte[]            headerBytes;
    private byte[]            myBytes;
    private String            myHash;
    private boolean           valid;


    // ~ Constructors ..........................................................

    // -------------------------------------------------------------------------
    /**
     * Creates new Block using the hash from its parent Block, a target (in
     * short-format), and a list of ballots. The list of ballots must contain at
     * least one element (cannot be empty).
     * 
     * @param prevBlockHash
     *            256-bit hash of the previous block in the chain
     * @param target
     *            Short-Format target for hashes
     * @param ballots
     *            A list of votes to be included in this block
     */
    public Block(String prevBlockHash, int target, ArrayList<Ballot> ballots)
    {
        this.version = VERSION;
        this.prevBlockHash = HashUtil.generateLeadingZeros(prevBlockHash);
        this.merkleRootHash = "NOT GENERATED";
        this.target = target;
        this.ballots = ballots;
        this.myHash = "NOT GENERATED";
        this.valid = false;
        this.myBytes = null;
    }


    // -------------------------------------------------------------------------
    /**
     * Initializes Block from binary data as a byte array. This constructor is
     * used by {@link Node Nodes} and {@link Block Blocks} to load Blocks.
     * 
     * @param binary
     *            Byte array representing this Block
     */
    public Block(byte[] binary)
    {
        ByteBuffer data = ByteBuffer.wrap(binary);
        this.version = data.getInt();
        byte[] prevBlockHashBytes = new byte[32];
        data.get(prevBlockHashBytes, 0, prevBlockHashBytes.length);
        this.prevBlockHash = ByteUtil.bytesToHex(prevBlockHashBytes);
        this.time = data.getInt();
        this.target = data.getInt();
        this.ballots = new ArrayList<Ballot>();
        int ballotNum = data.getInt();
        this.nonce = data.getInt();
        for (int i = 0; ballotNum > i; i++)
        {
            int size = data.getInt();
            byte[] bytes = new byte[size];
            data.get(bytes, 0, size);
            this.ballots.add(new Ballot(bytes));
        }
        this.merkleRootHash =
            HashUtil.generateLeadingZeros(getMerkleRoot(0, 0));
        genBytes();
        this.myHash =
            HashUtil.generateLeadingZeros(HashUtil.generateBlockHash(
                headerBytes,
                nonce));
        validate();
    }


    // ~ Methods ...............................................................

    // -------------------------------------------------------------------------
    /**
     * Returns the hash of the header for this Block.
     * 
     * @return 32-digit hexadecimal hash of this Block's header
     */
    public String getHash()
    {
        return myHash;
    }


    // -------------------------------------------------------------------------
    /**
     * Returns the hash for this Block's parent.
     * 
     * @see Block#getHash()
     * @return 32-digit hexadecimal hash of this Block's parent
     */
    public String getPrevHash()
    {
        return prevBlockHash;
    }


    // -------------------------------------------------------------------------
    /**
     * Generates a {@link HashUtil#generateProof(byte[], int) Proof of Work} for
     * this block and validates the Block's contents. If the block is already
     * valid, this method will return.
     */
    public void genProof()
    {
        if (valid)
        {
            return;
        }
        this.time = (int) (System.currentTimeMillis() / 60000);
        this.merkleRootHash =
            HashUtil.generateLeadingZeros(getMerkleRoot(0, 0));
        genBytes();
        this.nonce = HashUtil.generateProof(headerBytes, target);
        this.myHash =
            HashUtil.generateLeadingZeros(HashUtil.generateBlockHash(
                headerBytes,
                nonce));
        this.valid = true;
    }


    // -------------------------------------------------------------------------
    /**
     * Generates the Merkle Root of the list of Ballots contained in this Block.
     * The initial call to this method should be {@code getMerkleRoot(0, 0);}
     * and it will recurse to get the root of the entire list.
     * 
     * @param depth
     *            Depth of the Merkle Tree on this recursive step
     * @param position
     *            Index of the Ballot composing the first child of the Merkle
     *            Tree node returned by this method
     * @return 32-digit hexadecimal Merkle Root of the two leaves at the given
     *         depth and position
     */
    private String getMerkleRoot(int depth, int position)
    {
        if (depth < Math.log(ballots.size()) / Math.log(2))
        {
            String root1 = getMerkleRoot(depth + 1, position * 2);
            String root2 = getMerkleRoot(depth + 1, position * 2 + 1);
            if (root1 == null)
            {
                return null;
            }
            return HashUtil.generateMerkleRoot(root1, root2 == null
                ? root1
                : root2);
        }
        else
        {
            if (position >= ballots.size())
            {
                return null;
            }
            return ballots.get(position).getRoot();
        }
    }


    // -------------------------------------------------------------------------
    /**
     * Populates {@linkplain Block#headerBytes} with this Block's data. This
     * method is used by {@linkplain Block#genProof()} and
     * {@linkplain Block#Block(byte[])}.
     */
    private void genBytes()
    {
        ByteBuffer bytes = ByteBuffer.allocate(4 + 32 + 32 + 4 + 4 + 4);
        bytes.putInt(version);
        bytes.put(ByteUtil.hexToBytes(HashUtil
            .generateLeadingZeros(prevBlockHash)));
        bytes.put(ByteUtil.hexToBytes(HashUtil
            .generateLeadingZeros(merkleRootHash)));
        bytes.putInt(time);
        bytes.putInt(target);
        bytes.putInt(ballots.size());
        headerBytes = bytes.array();
    }


    // -------------------------------------------------------------------------
    /**
     * Returns the Block in binary format as a byte array.
     * 
     * @return Byte array representing this Block
     */
    public byte[] getBytes()
    {
        if (myBytes == null)
        {
            int size = 0;
            for (int i = 0; ballots.size() > i; i++)
            {
                size += 4 + ballots.get(i).getBytes().length;
            }
            ByteBuffer data =
                ByteBuffer.allocate(4 + 32 + 4 + 4 + 4 + 4 + size);
            data.putInt(version);
            data.put(ByteUtil.hexToBytes(HashUtil
                .generateLeadingZeros(prevBlockHash)));
            data.putInt(time);
            data.putInt(target);
            data.putInt(ballots.size());
            data.putInt(nonce);
            for (int i = 0; ballots.size() > i; i++)
            {
                byte[] bytes = ballots.get(i).getBytes();
                data.putInt(bytes.length);
                data.put(bytes);
            }
            myBytes = data.array();
        }
        return myBytes;
    }
    

    // -------------------------------------------------------------------------
    /**
     * Returns a list of the {@link Ballot Ballots} contained by this Block.
     * 
     * @return ArrayList containing the Ballots in the Block
     */
    public ArrayList<Ballot> getBallots()
    {
        return ballots;
    }
    
    
    // -------------------------------------------------------------------------
    /**
     * Returns the time when this Block was mined.
     * 
     * @return Unix Timestamp adjusted to seconds (instead of milliseconds)
     */
    public long getTime()
    {
        return time;
    }


    // -------------------------------------------------------------------------
    /**
     * Validates this Block's data and resets the {@link Block#valid valid}
     * flag.
     * 
     * @see HashUtil#validateProof(byte[], int, int)
     * @return True if this Block is valid, False otherwise
     */
    public boolean validate()
    {
        valid = HashUtil.validateProof(headerBytes, nonce, target);
        return valid;
    }


    // -------------------------------------------------------------------------
    @Override
    public String toString()
    {
        String tar =
            HashUtil.generateLeadingZeros(
                ByteUtil.bytesToHex(ByteUtil.intToBytes(target)),
                8);
        return "---- BLOCK V"
            + version
            + " @ "
            + DateFormat.getDateTimeInstance().format(
                new Date(((long) (time)) * 60000)) + " "
            + (validate() ? "[VALID]" : "[INVALID]") + "\nHash:       "
            + myHash + "\nPrevHash:   " + prevBlockHash + "\nMerkleRoot: "
            + merkleRootHash + "\nTarget:     " + tar + "   Difficulty: "
            + getDifficulty(target) + "\nBallots:    " + ballots.size()
            + "\nNonce:      " + nonce
            + "\n-------------------------------------------";
    }


    // ~ Static Methods ........................................................
    
    // -------------------------------------------------------------------------
    /**
     * Returns the specified target as a BigInteger.
     * 
     * @param target
     *            Short-Format target
     * @return Returns a BigInteger represented by the compressed Short-Format
     *         target
     */
    public static BigInteger getTarget(int target)
    {
        byte[] bytes = ByteUtil.intToBytes(target);
        byte e = bytes[0];
        bytes[0] = 0;
        int i = ByteUtil.bytesToInt(bytes);
        return new BigInteger("2").pow(8 * (e - 3)).multiply(
            new BigInteger(i + ""));
    }


    // -------------------------------------------------------------------------
    /**
     * Returns the difficulty of the specified target.<br>
     * <br>
     * The difficulty is calculated by
     * {@code DIFFICULTY = MAXIMUM_TARGET / SPECIFIED_TARGET}<br>
     * <br>
     * This method uses exponent reduction to more accurately calculate the
     * difficulty of a target.
     * 
     * @param target
     *            Short-Format target
     * @return Difficult of the target
     */
    public static double getDifficulty(int target)
    {
        byte[] maxBytes = ByteUtil.intToBytes(MAXIMUM_TARGET);
        byte maxE = maxBytes[0];
        maxBytes[0] = 0;
        byte[] targetBytes = ByteUtil.intToBytes(target);
        byte tarE = targetBytes[0];
        targetBytes[0] = 0;
        double max = ByteUtil.bytesToInt(maxBytes);
        double tar = ByteUtil.bytesToInt(targetBytes);

        return max / tar
            * Math.pow(2, 8 * ((maxE - (byte) 3) - (tarE - (byte) 3)));
    }
}
