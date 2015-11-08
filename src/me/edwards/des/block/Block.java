package me.edwards.des.block;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

import me.edwards.des.util.ByteUtil;
import me.edwards.des.util.HashUtil;

/**
 * Data Structure containing information for a block
 * Created on: Nov 1, 2015 at 3:09:17 PM
 * @author Matthew Edwards
 */
public class Block
{
    /**
     * Maximum Target value for DES
     */
    public static final int MAXIMUM_TARGET = ByteUtil.bytesToInt(new byte[] {(byte) 0x1d, (byte) 0xF0, (byte) 0x00, (byte) 0x00});
    
    private final int VERSION = 1;
    private int version;
    private String prevBlockHash;
    private String merkleRootHash;
    private int time;
    private int target;
    private int nonce;
    private ArrayList<Ballot> ballots;
    
    private byte[] headerBytes;
    private String myHash;
    private boolean valid;
    
    /**
     * Creates new Block
     * @param prevBlockHash 256-bit hash of the previous block in the chain
     * @param target Short-Version target for hashes
     * @param ballots A list of votes to be included in this block
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
    }
    
    /**
     * Returns the hash for this block
     * @return
     */
    public String getHash()
    {
        return myHash;
    }
    
    /**
     * Generates a proof for this block and validates its information.
     */
    public void validate()
    {
        if (valid)
        {
            return;
        }
        this.time = (int) (System.currentTimeMillis() / 60000);
        this.merkleRootHash = HashUtil.generateLeadingZeros(getMerkleRoot(0, 0));
        genBytes();
        this.nonce = HashUtil.generateProof(headerBytes, target);
        this.myHash = HashUtil.generateLeadingZeros(HashUtil.generateBlockHash(headerBytes, nonce));
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
            return HashUtil.generateMerkleRoot(root1, root2 == null ? root1 : root2);
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
        bytes.put(new BigInteger(prevBlockHash.replaceFirst("0{0,31}", ""), 16).toByteArray());
        bytes.put(new BigInteger(merkleRootHash.replaceFirst("0{0,31}", ""), 16).toByteArray());
        bytes.putInt(time);
        bytes.putInt(target);
        bytes.putInt(ballots.size());
        headerBytes = bytes.array();
    }
    
    @Override
    public String toString()
    {
        String tar = new BigInteger(1, ByteUtil.intToBytes(target)).toString(16);
        while (tar.length() < 8)
        {
            tar = "0" + tar;
        }
        return "---- BLOCK V" + version + " @ "
                + DateFormat.getDateTimeInstance().format(new Date(((long) (time)) * 60000)) + " "
                + (valid && HashUtil.validateProof(headerBytes, nonce, target) ? "[VALID]" : "[INVALID]") + "\n"
                + "Hash:       " + myHash + "\n"
                + "PrevHash:   " + prevBlockHash + "\n"
                + "MerkleRoot: " + merkleRootHash + "\n"
                + "Target:     " + tar + "   Difficulty: " + getDifficulty(target) + "\n"
                + "Vote Size:  " + ballots.size() + "\n"
                + "Nonce:      " + nonce + "\n"
                + "-------------------------------------------";
    }
    
    /**
     * Returns the target as a BigInteger
     * @param target
     * @return
     */
    public static BigInteger getTarget(int target)
    {
        byte[] bytes = ByteUtil.intToBytes(target);
        byte e = bytes[0];
        bytes[0] = 0;
        int i = ByteUtil.bytesToInt(bytes);
        return new BigInteger("2").pow(8 * (e - 3)).multiply(new BigInteger(i + ""));
    }
    
    /**
     * Returns the difficulty of the specified target
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
        
        return max / tar * Math.pow(2, 8 * ((maxE - 3) - (tarE - 3)));
    }
}
