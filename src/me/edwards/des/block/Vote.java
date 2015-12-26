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

// -----------------------------------------------------------------------------
/**
 * Data structure maintaining a single vote for a single entity. This vote may
 * be for an office or a referendum. {@link Ballot Ballots} are made up of
 * multiple votes, making the Vote the most basic unit composing {@link Block
 * Blocks}.<br>
 * <br>
 * Created on: Nov 3, 2015 at 10:41:29 AM
 * 
 * @author Matthew Edwards
 */
public class Vote
{
    // ~ Static/Instance variables .............................................

    private int    id;
    private String vote;


    // ~ Constructors ..........................................................

    // -------------------------------------------------------------------------
    /**
     * Creates a new Vote using the specified ID and vote content.<br>
     * <br>
     * Ex: [ID:1,VOTE:Obama]
     * 
     * @param id
     *            ID of the office or referendum being voted on
     * @param vote
     *            Vote content (as a String)
     */
    public Vote(int id, String vote)
    {
        this.id = id;
        this.vote = vote;
    }


    // ~ Methods ...............................................................

    // -------------------------------------------------------------------------
    /**
     * Gets the vote ID of the office or referendum being voted on. Ex: 123
     * 
     * @return ID of the office or referendum being voted on
     */
    public int getID()
    {
        return id;
    }


    // -------------------------------------------------------------------------
    /**
     * Returns the vote content. Ex: Obama
     * 
     * @return Vote content (as a String)
     */
    public String getVote()
    {
        return vote;
    }


    // -------------------------------------------------------------------------
    /**
     * Returns this vote in binary format as a byte array.
     * 
     * @return Byte array representing this Vote
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
