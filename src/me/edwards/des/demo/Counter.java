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

package me.edwards.des.demo;

import java.util.ArrayList;
import me.edwards.des.block.Ballot;
import me.edwards.des.block.BlockChain;
import me.edwards.des.block.Vote;

// -----------------------------------------------------------------------------
/**
 * Handles tabulation of a BlockChain into result data. The
 * {@link Counter#count(BlockChain) count method} will walk the longest branch
 * in the specified {@linkplain BlockChain} and generate its results in a
 * compact, easy to read form.<br>
 * <br>
 * Created on: Dec 30, 2015 at 9:28:35 AM
 * 
 * @author Matthew Edwards
 */
public class Counter
{
    // ~ Methods ...............................................................

    // -------------------------------------------------------------------------
    /**
     * Tabulates the results from the longest branch in the specified
     * {@linkplain BlockChain}.
     * 
     * @param bc
     *            BlockChain from which to tabulate the results
     * @return The generated ArrayList of {@link Result Results}
     */
    public static ArrayList<Result> count(BlockChain bc)
    {
        ArrayList<Result> results = new ArrayList<Result>();

        BlockChain.Node n = bc.getNode(bc.getTop().getHash());
        while (n.getHeight() >= 0)
        {
            ArrayList<Ballot> ballots = n.getBlock().getBallots();
            for (int i = 0; ballots.size() > i; i++)
            {
                ArrayList<Vote> votes = ballots.get(i).getVotes();
                for (int j = 0; votes.size() > j; j++)
                {
                    boolean flag = true;
                    for (int k = 0; results.size() > k; k++)
                    {
                        if (results.get(k).id == votes.get(j).getID())
                        {
                            if (results.get(k).vote.equals(votes.get(j).getVote()))
                            {
                                results.get(k).count++;
                                flag = false;
                                break;
                            }
                        }
                    }
                    if (flag)
                    {
                        Result result = new Result();
                        result.id = votes.get(j).getID();
                        result.vote = votes.get(j).getVote();
                        result.count = 1;
                        results.add(result);
                    }
                }
            }
            if (n.getHeight() > 0)
            {
                n = n.getParent();
            }
            else
            {
                break;
            }
        }

        return results;
    }


    // -------------------------------------------------------------------------
    /**
     * Trims the specified list of results to include only the winners (or the
     * ties) of each respective election ID.
     * 
     * @param toTrim
     *            The results to trim
     * @return An ArrayList containing the trimmed results
     */
    public static ArrayList<Result> trim(ArrayList<Result> toTrim)
    {
        ArrayList<Integer> ids = new ArrayList<Integer>();
        ArrayList<Result> toTrimTemp = new ArrayList<Result>();
        for (int i = 0; toTrim.size() > i; i++)
        {
            if (!ids.contains(toTrim.get(i).id))
            {
                ids.add(toTrim.get(i).id);
            }
            toTrimTemp.add(toTrim.get(i));
        }
        ArrayList<Result> results = new ArrayList<Result>();
        for (int i = 0; ids.size() > i; i++)
        {
            ArrayList<Result> tempResults = new ArrayList<Result>();
            for (int j = 0; toTrimTemp.size() > j; j++)
            {
                if (toTrimTemp.get(j).id == ids.get(i))
                {
                    if (tempResults.size() == 0)
                    {
                        tempResults.add(toTrimTemp.get(j));
                    }
                    else
                    {
                        if (tempResults.get(0).count == toTrimTemp.get(j).count)
                        {
                            tempResults.add(toTrimTemp.get(j));
                        }
                        else if (tempResults.get(0).count < toTrimTemp.get(j).count)
                        {
                            tempResults.clear();
                            tempResults.add(toTrimTemp.get(j));
                        }
                    }
                    toTrimTemp.remove(j);
                }
            }
            for (int j = 0; tempResults.size() > j; j++)
            {
                results.add(tempResults.get(j));
            }
        }
        return results;
    }


    // -------------------------------------------------------------------------
    /**
     * Formats a list of results into a human-readable String.
     * 
     * @see Counter#trim(ArrayList)
     * @param results
     *            List of results to format
     * @return List of results formatted as a human-readable String
     */
    public static String toString(ArrayList<Result> results)
    {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; results.size() > i; i++)
        {
            boolean tie = false;
            for (int j = 0; results.size() > j; j++)
            {
                if (i != j && results.get(i).id == results.get(j).id)
                {
                    tie = true;
                    break;
                }
            }
            sb.append("------------------------------------------------\n");
            sb.append("ID:    " + results.get(i).id + "\n");
            sb.append("Vote:  " + results.get(i).vote
                + (tie ? " [TIE]" : " [WIN]") + "\n");
            sb.append("Votes: " + results.get(i).count + "\n");
        }
        sb.append("------------------------------------------------");
        return sb.toString();
    }


    // -------------------------------------------------------------------------
    /**
     * Data structure representing one section of result data: The election vote
     * ID (e.g. 0), the vote (e.g. Obama), and the number of votes cast for this
     * vote (e.g. 1000).<br>
     * <br>
     * Created on: Dec 30, 2015 at 9:29:53 AM
     * 
     * @author Matthew Edwards
     */
    public static class Result
    {
        private int    id;
        private String vote;
        private int    count;
    }
}
