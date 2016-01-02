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

package me.edwards.des;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Scanner;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import me.edwards.des.block.Ballot;
import me.edwards.des.block.Block;
import me.edwards.des.block.BlockChain;
import me.edwards.des.block.BlockChainIO;
import me.edwards.des.block.Vote;
import me.edwards.des.demo.Counter;
import me.edwards.des.demo.Submitter;
import me.edwards.des.net.Connection;
import me.edwards.des.net.packet.PacketGetAddr;
import me.edwards.des.net.packet.PacketPing;
import me.edwards.des.util.ByteUtil;

// -----------------------------------------------------------------------------
/**
 * Handles the Launch of a Node from the command line. This class loads all
 * necessary logging configurations and initializes all loggers and working
 * directories for a Decentralized Election System Miner Node. After a Node is
 * successfully initialized, created, and launched, this class handles the
 * console input for the Node.<br>
 * <br>
 * Created on: Dec 21, 2015 at 10:11:56 AM
 * 
 * @see Launcher#main(String[])
 * @author Matthew Edwards
 */
public class Launcher
{
    // ~ Static/instance variables .............................................

    // -------------------------------------------------------------------------
    /**
     * The Global logger for DES (Used by Launcher and utilities)
     */
    public static final Logger GLOBAL = Logger.getLogger("DES");

    /**
     * Default directory for DES
     */
    public static String       DIR    = System.getProperty("user.home")
                                          + "/Desktop/DES/";


    // ~ Methods ...............................................................

    // -------------------------------------------------------------------------
    /**
     * This method is called on the application Launch.<br>
     * <br>
     * List of command line arguments:<br>
     * <table>
     * <thead>
     * <td><strong>Flag</strong></td>
     * <td><strong>Usage</strong></td>
     * <td><strong>Description</strong></td> </thead>
     * <tr>
     * <td>-count</td>
     * <td>-count (BlockChain File)</td>
     * <td>Tabulates the results of the specified BlockChain after loading it
     * from file. If no BlockChain is specified, the default BlockChain is used.
     * </td>
     * </tr>
     * <tr>
     * <td>-demo</td>
     * <td>-demo</td>
     * <td>Starts the Node in demonstration mode.</td>
     * </tr>
     * </tr>
     * <tr>
     * <td>-dir</td>
     * <td>-dir &lt;Directory&gt;</td>
     * <td>Sets the working directory of the Node.</td>
     * </tr>
     * <tr>
     * <td>-gen</td>
     * <td>-gen</td>
     * <td>Generates a Genesis Block and saves it to
     * "generated_blockchain.block" in the working directory.</td>
     * </tr>
     * <tr>
     * <td>-genids</td>
     * <td>-genids &lt;Number of IDs&gt; (Directory)</td>
     * <td>Generates key databases for demonstration purposes.</td>
     * </tr>
     * <tr>
     * <td>-name</td>
     * <td>-name &lt;Name&gt;</td>
     * <td>Sets the human-readable name of the Node.</td>
     * </tr>
     * <tr>
     * <td>-peer</td>
     * <td>-peer &lt;Peer&gt;</td>
     * <td>Adds an initial peer to the Node which will be contacted during the
     * Bootstrapping process.</td>
     * </tr>
     * <tr>
     * <td>-port</td>
     * <td>-port &lt;Port&gt;</td>
     * <td>Sets the port to be used by the Node.</td>
     * </tr>
     * <tr>
     * <td>-submit</td>
     * <td>-submit (Time in seconds)</td>
     * <td>Adds a Submitter to the Node for demonstration purposes. If a time is
     * specified, it will submit the available number of Ballots within that
     * timeframe.</td>
     * </tr>
     * </table>
     * 
     * @param args
     */
    public static void main(String[] args)
    {
        for (int i = 0; args.length > i; i++)
        {
            if (args[i].equalsIgnoreCase("-dir"))
            {
                DIR = args[++i];
            }
        }

        try
        {
            File f = new File(DIR + "logs/");
            if (!f.exists())
            {
                f.mkdirs();
            }
            LogManager.getLogManager().readConfiguration(
                Node.class.getClassLoader().getResourceAsStream(
                    "me/edwards/des/rec/log.config"));
            Handler fh = new FileHandler(DIR + "logs/des_%u.log");
            Logger.getLogger("DES").addHandler(fh);
        }
        catch (IOException e)
        {
            System.out.println("Could not initialize logger! Shutting down...");
            e.printStackTrace();
            System.exit(0);
        }

        final Node node = new Node();
        node.peerList = new ArrayList<String>();
        for (int i = 0; args.length > i; i++)
        {
            try
            {
                if (args[i].equalsIgnoreCase("-peer"))
                {
                    node.peerList.add(args[++i]);
                }
                else if (args[i].equalsIgnoreCase("-port"))
                {
                    node.port = Integer.parseInt(args[++i]);
                }
                else if (args[i].equalsIgnoreCase("-name"))
                {
                    node.name = args[++i];
                }
                else if (args[i].equalsIgnoreCase("-gen"))
                {
                    Block genesis =
                        new Block(
                            "0",
                            Block.MAXIMUM_TARGET,
                            new ArrayList<Ballot>());
                    genesis.genProof();
                    try
                    {
                        BlockChainIO.save(new BlockChain(genesis), DIR
                            + "generated_blockchain.block");
                    }
                    catch (IOException e)
                    {
                        GLOBAL.log(
                            Level.WARNING,
                            "Could not save BlockChain",
                            e);
                    }
                    System.exit(0);
                }
                else if (args[i].equalsIgnoreCase("-genids"))
                {
                    int ids = Integer.parseInt(args[++i]);
                    GLOBAL.info("Generating ID databases...");
                    Submitter.generateDatabase(
                        DIR
                            + (args.length == i + 1 ? "" : args[++i]
                                .replaceAll("\"", "")),
                        ids);
                    GLOBAL.info("ID databases generated!");
                }
                else if (args[i].equalsIgnoreCase("-count"))
                {
                    try
                    {
                        BlockChain bc =
                            BlockChainIO.load(DIR
                                + (args.length == i + 1
                                    ? "data.block"
                                    : args[++i]));
                        GLOBAL.info("Counting Ballots...");
                        ArrayList<Counter.Result> results = Counter.count(bc);
                        GLOBAL.info("Trimming results...");
                        results = Counter.trim(results);
                        GLOBAL.info("Formatting results...");
                        String r = Counter.toString(results);
                        GLOBAL.info("\n" + r);
                    }
                    catch (IOException e)
                    {
                        GLOBAL.log(Level.WARNING, "Counting IOException", e);
                    }
                    System.exit(0);
                }
                else if (args[i].equalsIgnoreCase("-demo"))
                {
                    node.demo = true;
                    try
                    {
                        GLOBAL.info("Loading ID databases...");
                        Submitter.loadDatabase(DIR);
                        GLOBAL.info("ID databases loaded!");
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
                else if (args[i].equalsIgnoreCase("-submit"))
                {
                    GLOBAL.info("Adding a Submitter to this Node...");
                    i++;
                    final int t =
                        args.length > i + 1
                            ? Integer.parseInt(args[i++])
                            : 40 * 60;
                    new Thread(new Runnable() {
                        @Override
                        public void run()
                        {
                            String[][] voteList =
                                { { "1.1", "1.2" }, { "2.1", "2.2" },
                                    { "3.1", "3.2" }, { "4.1", "4.2" } };
                            try
                            {
                                while (!node.running)
                                {
                                    Thread.sleep(1000);
                                }
                                Submitter.submit(
                                    DIR,
                                    node,
                                    voteList,
                                    t * 1000);
                            }
                            catch (Exception e)
                            {
                                GLOBAL.log(Level.WARNING, "Submit Exception", e);
                            }
                        }
                    },
                        "Submitter Wait").start();
                }
            }
            catch (Exception e)
            {
                System.err
                    .println("Malformed arguments! See documentation for proper usage.");
                System.exit(0);
            }
        }

        node.logger = Logger.getLogger("DES.node");

        GLOBAL.info("DES Version " + Node.VERSION + " by Matthew Edwards");
        GLOBAL.info("(C) Copyright 2015 by Matthew Edwards");
        GLOBAL.info("------------------------------------------------");
        if (node.demo)
        {
            GLOBAL.info("/// NOTICE! This Node is in Demonstration Mode \\\\\\");
        }
        GLOBAL.info("DES Node "
            + (node.name == null ? "" : "\"" + node.name + "\"")
            + " is starting up @ "
            + SimpleDateFormat.getDateTimeInstance().format(
                new Date(System.currentTimeMillis())) + "...");

        GLOBAL.info("Loading Default Peer List...");
        try
        {
            BufferedReader reader =
                new BufferedReader(new InputStreamReader(Node.class
                    .getClassLoader().getResourceAsStream(
                        "me/edwards/des/rec/peer_defaults.config")));
            while (reader.ready())
            {
                node.peerList.add(reader.readLine());
                GLOBAL.fine("> Added peer /"
                    + node.peerList.get(node.peerList.size() - 1));
            }
            GLOBAL.info("Default Peer List loaded!");
        }
        catch (Exception e)
        {
            GLOBAL.log(Level.WARNING, "Could not load Default Peer List!", e);
        }

        try
        {
            node.blockChain = BlockChainIO.load(DIR + "data.block");
        }
        catch (IOException e)
        {
            GLOBAL.log(
                Level.WARNING,
                "Could not load BlockChain! Shutting down...",
                e);
            System.exit(0);
        }

        node.start();

        Scanner in = new Scanner(System.in);
        while (node.running)
        {
            try
            {
                if (System.in.available() != 0 && in.hasNextLine())
                {
                    String[] input = in.nextLine().split(" ");

                    GLOBAL.fine("Console input: " + Arrays.toString(input));

                    if (input[0].equals(""))
                    {
                        continue;
                    }
                    else if (input[0].equalsIgnoreCase("stop"))
                    {
                        node.stop();
                        BlockChainIO.save(node.blockChain, DIR + "data.block");
                    }
                    else if (input[0].equalsIgnoreCase("ping"))
                    {
                        Connection c = node.getConnection(input[1]);
                        if (c != null)
                        {
                            c.send(new PacketPing(0L));
                            GLOBAL.info("Sent PING to " + c);
                        }
                    }
                    else if (input[0].equalsIgnoreCase("connect"))
                    {
                        try
                        {
                            if (input.length == 1)
                            {
                                node.connect(node.ip, node.port - 1);
                            }
                            else
                            {
                                node.connect(
                                    InetAddress.getByName(input[1]),
                                    Integer.parseInt(input[2]));
                            }
                        }
                        catch (Exception e)
                        {
                            GLOBAL.log(Level.WARNING, "Connect Error", e);
                        }
                    }
                    else if (input[0].equalsIgnoreCase("getaddr"))
                    {
                        Connection c = node.getConnection(input[1]);
                        if (c != null)
                        {
                            c.send(new PacketGetAddr());
                            GLOBAL.info("Sent GETADDR to " + c);
                        }
                    }
                    else if (input[0].equalsIgnoreCase("myaddr"))
                    {
                        GLOBAL.info(node.ip + ":" + node.port + " ["
                            + node.name + "]");
                    }
                    else if (input[0].equalsIgnoreCase("addr"))
                    {
                        for (Connection c : node.peers)
                        {
                            GLOBAL.info(c.toString());
                        }
                    }
                    else if (input[0].equalsIgnoreCase("testload"))
                    {
                        int blocks = input.length > 1 ? Integer.parseInt(input[1]) : 10;
                        int ballotNum = input.length > 2 ? Integer.parseInt(input[2]) : 10;
                        System.out
                            .print("This command will perform a load test on this machine."
                                + " The test may take several minutes and will be VERY processor intensive."
                                + " It is not recommended that a load test is performed with more than 8M Ballots for the current build."
                                + "\nYou have selected "
                                + blocks
                                + " Blocks with "
                                + ballotNum
                                + " Ballots each for a total of "
                                + (blocks * ballotNum)
                                + " Ballots, using " + (DIR
                                + (input.length == 4
                                ? input[3]
                                : "testload.block")) + " as the initial BlockChain file."
                                + "\nDo you wish to continue? (Y/N) ");
                        String s = in.next();
                        if (!s.equalsIgnoreCase("y"))
                        {
                            continue;
                        }
                        try
                        {
                            node.blockChain =
                                BlockChainIO.load(DIR
                                    + (input.length == 4
                                        ? input[3]
                                        : "testload.block"));
                        }
                        catch (Exception e)
                        {
                            GLOBAL.log(Level.WARNING, "Could not Load BlockChain", e);
                            continue;
                        }
                        Thread.sleep(3000);
                        long time = System.currentTimeMillis() - 100000;
                        for (int i = 0; blocks > i; i++)
                        {
                            ArrayList<Vote> votes = new ArrayList<Vote>();
                            votes.add(new Vote(0, "John Doe"));
                            votes.add(new Vote(1, "John Doe1"));
                            votes.add(new Vote(2, "John Doe2"));
                            votes.add(new Vote(3, "John Doe3"));
                            votes.add(new Vote(4, "John Doe4"));
                            votes.add(new Vote(5, "John Doe5"));
                            votes.add(new Vote(6, "John Doe6"));
                            votes.add(new Vote(7, "John Doe7"));
                            votes.add(new Vote(8, "John Doe8"));
                            votes.add(new Vote(9, "John Doe9"));
                            for (int j = 0; ballotNum > j; j++)
                            {
                                node.ballots
                                    .add(new Ballot(
                                        ByteUtil.bytesToHex(ByteUtil
                                            .intToBytes(i)),
                                        "0",
                                        votes));
                            }
                            if (System.currentTimeMillis() - time > 5000)
                            {
                                System.out.println((int) (i / (blocks / 100D))
                                    + "%");
                                time = System.currentTimeMillis();
                            }
                            node.generateBlock();
                            while (node.blockGenHash != null)
                            {
                                Thread.sleep(2);
                            }
                        }
                        try
                        {
                            System.out.println("Saving...");
                            Thread.sleep(1000);
                            while (node.blockGenHash != null)
                            {
                                Thread.sleep(2);
                            }
                            BlockChainIO.save(node.blockChain, DIR
                                + "testload.block");
                        }
                        catch (Exception e)
                        {
                            GLOBAL.log(Level.WARNING, "Could not Save BlockChain", e);
                            continue;
                        }
                        System.exit(0);
                    }
                    else
                    {
                        GLOBAL.log(
                            Level.WARNING,
                            "Invalid Command: " + Arrays.toString(input));
                    }
                }
                else
                {
                    try
                    {
                        Thread.sleep(100);
                    }
                    catch (InterruptedException e)
                    {
                        //
                    }
                }
            }
            catch (Exception e)
            {
                GLOBAL.log(Level.WARNING, "Console Exception", e);
                continue;
            }
        }
        in.close();
    }
}
