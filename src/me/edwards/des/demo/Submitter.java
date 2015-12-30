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

import me.edwards.des.Node;

// -----------------------------------------------------------------------------
/**
 * Handles submitting {@link Ballot Ballots} to a local {@link Node Node} for
 * demonstration purposes.<br>
 * <br>
 * Created on: Dec 30, 2015 at 2:01:45 PM
 * 
 * @author Matthew Edwards
 */
public class Submitter
{
    // ~ Methods ...............................................................

    // -------------------------------------------------------------------------
    /**
     * Submits {@link Ballot Ballots} to a local {@link Node Node} for
     * demonstration purposes.
     * 
     * @param node
     *            Local Node to submit Ballots to
     * @param votes
     *            An array representing a list of Vote IDs, each containing list
     *            of possible Votes for that Vote ID
     * @param ballots
     *            The number of Ballots to submit in total
     * @param time
     *            The total amount of time to be taken
     */
    public static void
        submit(Node node, String[][] votes, int ballots, int time)
    {
        //
    }
}
