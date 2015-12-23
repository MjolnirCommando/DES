package me.edwards.des.block;

import java.nio.ByteBuffer;

/**
 * Data structure maintaining a single vote for a single entity. This vote may
 * be for an office or a referendum. Ballots are made up of multiple votes.
 * Created on: Nov 3, 2015 at 10:41:29 AM
 * 
 * @author Matthew Edwards
 */
public class Vote
{
    private int    id;
    private String vote;


    /**
     * Creates new Vote
     * 
     * @param id
     *            ID of the office or referendum being voted on
     * @param vote
     *            Vote (as a String)
     */
    public Vote(int id, String vote)
    {
        this.id = id;
        this.vote = vote;
    }


    /**
     * Returns the vote ID
     * 
     * @return
     */
    public int getID()
    {
        return id;
    }


    /**
     * Returns the vote
     * 
     * @return
     */
    public String getVote()
    {
        return vote;
    }


    /**
     * Returns this vote as bytes
     * 
     * @return
     */
    public byte[] getBytes()
    {
        ByteBuffer data = ByteBuffer.allocate(4 + 4 + vote.length());
        data.putInt(id);
        data.putInt(vote.length());
        data.put(vote.getBytes());
        return data.array();
    }
}
