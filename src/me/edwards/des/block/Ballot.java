package me.edwards.des.block;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.ArrayList;

import me.edwards.des.util.HashUtil;

/**
 * Data structure to store a maintain a single cast vote.
 * Created on: Nov 1, 2015 at 11:30:16 PM
 * @author Matthew Edwards
 */
public class Ballot
{
    private final int VERSION = 1;
    private int version;
    private String id; // 16 hex
    private ArrayList<Vote> votes;
    private String signature;
    
    private String signatureRoot;
    private String root;
    private byte[] bytes;
    
    /**
     * Creates new Ballot
     * @param id UUID of ballot caster
     * @param signature Signature on this ballot
     * @param votes List of votes
     */
    public Ballot(String id, String signature, ArrayList<Vote> votes)
    {
        this.version = VERSION;
        this.id = id;
        this.votes = votes;
        this.signature = signature;
        
        ByteBuffer buffer = ByteBuffer.allocate(16);
        buffer.putInt(version);
        buffer.put(new BigInteger(id.replaceFirst("0{0,15}", ""), 16).toByteArray());
        buffer.putInt(votes.size());
        for (int i = 0; votes.size() > i; i++)
        {
            buffer.put(votes.get(i).getBytes());
        }
        signatureRoot = HashUtil.generateHash(buffer.array());
        buffer.put(signature.getBytes());
        bytes = buffer.array();
        root = HashUtil.generateHash(bytes);
    }
    
    /**
     * Returns the Ballot ID
     * @return
     */
    public String getID()
    {
        return id;
    }
    
    /**
     * Returns the Ballot signature
     * @return
     */
    public String getSignature()
    {
        return signature;
    }
    
    /**
     * Returns the signature root hash of this ballot
     * @return
     */
    public String getSignatureRoot()
    {
        return signatureRoot;
    }
    
    /**
     * Returns the root hash of this ballot
     * @return
     */
    public String getRoot()
    {
        return root;
    }
    
    /**
     * Returns the byte data of this ballot
     * @return
     */
    public byte[] getBytes()
    {
        return bytes;
    }
    
    @Override
    public boolean equals(Object obj)
    {
        return obj instanceof Ballot && ((Ballot) obj).getRoot().equals(getRoot());
    }
}
