package me.edwards.des.block;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import me.edwards.des.util.ByteUtil;
import me.edwards.des.util.HashUtil;

/**
 * Data structure to store a maintain a single cast vote. Created on: Nov 1,
 * 2015 at 11:30:16 PM
 * 
 * @author Matthew Edwards
 */
public class Ballot
{
    private final int       VERSION = 1;
    private int             version;
    private String          id;
    private ArrayList<Vote> votes;
    private String          signature;

    private String          signatureRoot;
    private String          root;
    private byte[]          bytes;


    /**
     * Creates new Ballot
     * 
     * @param id
     *            UUID of ballot caster
     * @param signature
     *            Signature on this ballot
     * @param votes
     *            List of votes
     */
    public Ballot(String id, String signature, ArrayList<Vote> votes)
    {
        this.version = VERSION;
        this.id = HashUtil.generateLeadingZeros(id, 16);
        this.votes = votes;
        this.signature = signature;

        int size = 0;
        for (int i = 0; votes.size() > i; i++)
        {
            size += votes.get(i).getBytes().length;
        }
        ByteBuffer buffer =
            ByteBuffer.allocate(4 + 8 + 4 + size + 4 + signature.length());
        buffer.putInt(version);
        buffer.put(ByteUtil.hexToBytes(this.id));
        buffer.putInt(votes.size());
        for (int i = 0; votes.size() > i; i++)
        {
            buffer.put(votes.get(i).getBytes());
        }
        this.signatureRoot = HashUtil.generateHash(buffer.array());
        buffer.putInt(signature.length());
        buffer.put(signature.getBytes());
        this.bytes = buffer.array();
        this.root = HashUtil.generateHash(this.bytes);
    }


    /**
     * Creates new Ballot from binary data
     * 
     * @param binary
     *            Binary data
     */
    public Ballot(byte[] binary)
    {
        this.bytes = binary;
        ByteBuffer data = ByteBuffer.wrap(binary);
        this.version = data.getInt();
        byte[] idBytes = new byte[8];
        data.get(idBytes, 0, 8);
        this.id = ByteUtil.bytesToHex(idBytes);
        int size = data.getInt();
        this.votes = new ArrayList<Vote>();
        for (int i = 0; size > i; i++)
        {
            int voteId = data.getInt();
            byte[] voteStrBytes = new byte[data.getInt()];
            data.get(voteStrBytes, 0, voteStrBytes.length);
            String voteStr = new String(voteStrBytes);
            votes.add(new Vote(voteId, voteStr));
        }
        int rootLength = data.position() - 1;
        byte[] signatureBytes = new byte[data.getInt()];
        data.get(signatureBytes, 0, signatureBytes.length);
        this.signature = new String(signatureBytes);
        root = HashUtil.generateHash(binary);
        data.position(0);
        byte[] signatureRootBytes = new byte[rootLength];
        data.get(signatureRootBytes, 0, rootLength);
        this.signatureRoot = HashUtil.generateHash(signatureRootBytes);
    }


    /**
     * Returns the Ballot ID
     * 
     * @return
     */
    public String getID()
    {
        return id;
    }


    /**
     * Returns the Ballot signature
     * 
     * @return
     */
    public String getSignature()
    {
        return signature;
    }


    /**
     * Returns the signature root hash of this ballot
     * 
     * @return
     */
    public String getSignatureRoot()
    {
        return signatureRoot;
    }


    /**
     * Returns the root hash of this ballot
     * 
     * @return
     */
    public String getRoot()
    {
        return root;
    }


    /**
     * Returns the list of votes on this ballot
     * 
     * @return
     */
    public ArrayList<Vote> getVotes()
    {
        return votes;
    }


    /**
     * Returns the byte data of this ballot
     * 
     * @return
     */
    public byte[] getBytes()
    {
        return bytes;
    }


    @Override
    public String toString()
    {
        StringBuffer voteDigest = new StringBuffer();
        for (int i = 0; votes.size() > i; i++)
        {
            voteDigest.append("\n\t" + votes.get(i).getID() + ":"
                + votes.get(i).getVote());
        }
        return "--- Ballot --------------------------------" + "\nID:        "
            + id + "\nVersion:   " + version + "\nSignature: " + signature
            + "\nVotes (" + votes.size() + "): " + voteDigest.toString()
            + "\n-------------------------------------------";
    }


    @Override
    public boolean equals(Object obj)
    {
        return obj instanceof Ballot
            && ((Ballot)obj).getRoot().equals(getRoot());
    }
}
