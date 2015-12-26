package me.edwards.des.block;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import me.edwards.des.util.ByteUtil;
import me.edwards.des.util.HashUtil;

/**
 * Data Structure containing information for a block Created on: Nov 1, 2015 at
 * 3:09:17 PM
 * 
 * @author Matthew Edwards
 */
public class Block
{
    /**
     * Maximum Target value for DES
     */
    public static final int   MAXIMUM_TARGET = ByteUtil.bytesToInt(new byte[] {
        (byte)0x1f, (byte)0xff, (byte)0xff, (byte)0xff }); // ByteUtil.bytesToInt(new
// byte[] {(byte) 0x1d, (byte) 0xF0, (byte) 0x00, (byte) 0x00});

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


    /**
     * Creates new Block
     * 
     * @param prevBlockHash
     *            256-bit hash of the previous block in the chain
     * @param target
     *            Short-Version target for hashes
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


    /**
     * Creates new Block from binary data
     * 
     * @param binary
     *            Binary data
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
        this.merkleRootHash = HashUtil.generateLeadingZeros(getMerkleRoot(0, 0));
        genBytes();
        this.myHash =
            HashUtil.generateLeadingZeros(HashUtil.generateBlockHash(
                headerBytes,
                nonce));
        validate();
    }


    /**
     * Returns the hash for this block
     * 
     * @return
     */
    public String getHash()
    {
        return myHash;
    }


    /**
     * Returns the hash for this block's parent
     * 
     * @return
     */
    public String getPrevHash()
    {
        return prevBlockHash;
    }


    /**
     * Generates a proof for this block and validates its information.
     */
    public void genProof()
    {
        if (valid)
        {
            return;
        }
        this.time = (int)(System.currentTimeMillis() / 60000);
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


    /**
     * Returns the byte data of this Block
     * 
     * @return
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


    /**
     * Returns true if this Block is valid
     * 
     * @return
     */
    public boolean validate()
    {
        valid = HashUtil.validateProof(headerBytes, nonce, target);
        return valid;
    }


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
                new Date(((long)(time)) * 60000)) + " "
            + (validate() ? "[VALID]" : "[INVALID]") + "\nHash:       "
            + myHash + "\nPrevHash:   " + prevBlockHash + "\nMerkleRoot: "
            + merkleRootHash + "\nTarget:     " + tar + "   Difficulty: "
            + getDifficulty(target) + "\nBallots:    " + ballots.size()
            + "\nNonce:      " + nonce
            + "\n-------------------------------------------";
    }


    /**
     * Returns the target as a BigInteger
     * 
     * @param target
     * @return
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


    /**
     * Returns the difficulty of the specified target
     * 
     * @param target
     * @return
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

        return max / tar * Math.pow(2, 8 * ((maxE - (byte) 3) - (tarE - (byte) 3)));
    }
}
