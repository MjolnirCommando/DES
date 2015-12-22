package me.edwards.des;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.interfaces.ECPublicKey;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import me.edwards.des.block.Ballot;
import me.edwards.des.block.Block;
import me.edwards.des.block.BlockChain;
import me.edwards.des.block.BlockChainIO;
import me.edwards.des.block.Vote;
import me.edwards.des.net.Connection;
import me.edwards.des.net.packet.PacketGetAddr;
import me.edwards.des.net.packet.PacketInv;
import me.edwards.des.net.packet.PacketPing;
import me.edwards.des.util.ByteUtil;
import me.edwards.des.util.HashUtil;

/**
 * Handles launch of a single Node and its console input
 * Created on: Dec 21, 2015 at 10:11:56 AM
 * @author Matthew Edwards
 */
public class Launcher
{
    /**
     * The Global logger for DES (Used by Launcher and utilities)
     */
    public static final Logger GLOBAL = Logger.getLogger("DES");
    
    /**
     * Default directory for DES
     */
    public static String DIR = System.getProperty("user.home") + "/Desktop/DES/";
    
    /**
     * Called on application launch
     * @param args
     */
    public static void main(String[] args)
    {
        Node node = new Node();
        node.peerList = new ArrayList<String>();
        for (int i = 0; args.length > i; i++)
        {
            if (args[i].equalsIgnoreCase("-dir"))
            {
                DIR = args[++i];
            }
            else if (args[i].equalsIgnoreCase("-peer"))
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
                Block genesis = new Block("0", Block.MAXIMUM_TARGET, new ArrayList<Ballot>());
                genesis.genProof();
                try
                {
                    BlockChainIO.save(new BlockChain(genesis), DIR + "generated_blockchain.block");
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
                System.exit(0);
            }
        }
        
        try
        {
            LogManager.getLogManager().readConfiguration(Node.class.getClassLoader().getResourceAsStream("me/edwards/des/rec/log.config"));
        }
        catch (IOException e)
        {
            System.out.println("Could not initialize logger! Shutting down...");
            System.exit(0);
        }
        
        node.logger = Logger.getLogger("DES.node");
        
        GLOBAL.info("DES Version " + Node.VERSION + " by Matthew Edwards");
        GLOBAL.info("(C) Copyright 2015 by Matthew Edwards");
        GLOBAL.info("------------------------------------------------");
        GLOBAL.info("DES Node " + (node.name == null ? "" : "\"" + node.name + "\"") + " is starting up @ " + SimpleDateFormat.getDateTimeInstance().format(new Date(System.currentTimeMillis())) + "...");
        
        GLOBAL.info("Loading Default Peer List...");
        try
        {
            BufferedReader reader = new BufferedReader(new InputStreamReader(Node.class.getClassLoader().getResourceAsStream("me/edwards/des/rec/peer_defaults.config")));
            while (reader.ready())
            {
                node.peerList.add(reader.readLine());
                GLOBAL.fine("> Added peer /" + node.peerList.get(node.peerList.size() - 1));
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
            GLOBAL.log(Level.WARNING, "Could not load BlockChain! Shutting down...", e);
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
                                node.connect(InetAddress.getByName(input[1]), Integer.parseInt(input[2]));
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
                    else if (input[0].equalsIgnoreCase("send"))
                    {
                        if (input[1].equalsIgnoreCase("ballot"))
                        {
                            GLOBAL.info("Generating ballot...");
                            ArrayList<Vote> votes = new ArrayList<Vote>();
                            votes.add(new Vote(0, "John Doe"));
                            votes.add(new Vote(1, "Satoshi"));
                            Ballot b = new Ballot("FFFFFFFFFFFFFFFF", "<Signature>", votes);
                            GLOBAL.info("\n" + b.toString());
                            node.ballots.add(b);
                            GLOBAL.info("Sending ballot...");
                            PacketInv inv = new PacketInv();
                            inv.addInv(b);
                            node.sendToAll(inv);
                        }
                    }
                    else if (input[0].equalsIgnoreCase("gen"))
                    {
                        if (input[1].equalsIgnoreCase("block"))
                        {
                            node.generateBlock();
                        }
                        else if (input[1].equalsIgnoreCase("ballot"))
                        {
                            int ballotNum = Integer.parseInt(input[2]);
                            ArrayList<Vote> votes = new ArrayList<Vote>();
                            votes.add(new Vote(0, "John Doe"));
                            votes.add(new Vote(1, "Satoshi"));
                            for (int i = 0; ballotNum > i; i++)
                            {
                                Ballot b = new Ballot(ByteUtil.bytesToHex(ByteUtil.intToBytes(i)), "<Signature>", votes);
                                node.ballots.add(b);
                            }
                            GLOBAL.info("Generated Ballots!");
                        }
                        else if (input[1].equalsIgnoreCase("key"))
                        {
                            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("EC");
                            SecureRandom random = SecureRandom.getInstance("SHA1PRNG");

                            keyGen.initialize(256, random);

                            KeyPair pair = keyGen.generateKeyPair();
                            PrivateKey priv = pair.getPrivate();
                            ECPublicKey pub = (ECPublicKey) pair.getPublic();
                            
                            Signature dsa = Signature.getInstance("SHA1withECDSA");
                            dsa.initSign(priv);
                            String root = HashUtil.generateHash("THIS IS THE ROOT".getBytes());
                            dsa.update(root.getBytes());
                            byte[] realSig = dsa.sign();
                            dsa.initVerify(pub);
                            dsa.update(root.getBytes());
                            System.out.println("Root:      " + root);
                            System.out.println("Signature: " + HashUtil.generateLeadingZeros(ByteUtil.bytesToHex(realSig)));
                            System.out.println("Pub Key X: " + HashUtil.generateLeadingZeros(pub.getW().getAffineX().toString(16)));
                            System.out.println("Pub Key Y: " + HashUtil.generateLeadingZeros(pub.getW().getAffineY().toString(16)));
                            System.out.println("Verified:  " + dsa.verify(realSig));
                        }
                    }
                    else if (input[0].equalsIgnoreCase("myaddr"))
                    {
                        GLOBAL.info(node.ip + ":" + node.port + " [" + node.name + "]");
                    }
                    else if (input[0].equalsIgnoreCase("addr"))
                    {
                        for (Connection c : node.peers)
                        {
                            GLOBAL.info(c.toString());
                        }
                    }
                    else if (input[0].equalsIgnoreCase("testappend"))
                    {
                        Block a = new Block("0", Block.MAXIMUM_TARGET, new ArrayList<Ballot>());
                        a.genProof();
                        Block b = new Block(a.getHash(), Block.MAXIMUM_TARGET, new ArrayList<Ballot>());
                        b.genProof();
                        Block c = new Block(b.getHash(), Block.MAXIMUM_TARGET, new ArrayList<Ballot>());
                        c.genProof();
                        Block d = new Block(c.getHash(), Block.MAXIMUM_TARGET, new ArrayList<Ballot>());
                        d.genProof();
                        Block e = new Block(d.getHash(), Block.MAXIMUM_TARGET, new ArrayList<Ballot>());
                        e.genProof();

                        Block f = new Block(b.getHash(), Block.MAXIMUM_TARGET, new ArrayList<Ballot>());
                        f.genProof();
                        Block g = new Block(f.getHash(), Block.MAXIMUM_TARGET, new ArrayList<Ballot>());
                        g.genProof();

                        Block h = new Block(a.getHash(), Block.MAXIMUM_TARGET, new ArrayList<Ballot>());
                        h.genProof();
                        
                        Block i = new Block("2", Block.MAXIMUM_TARGET, new ArrayList<Ballot>());
                        i.genProof();

                        StringBuffer sb = new StringBuffer();
                        
                        BlockChain bc = new BlockChain(a);
                        sb.append(bc + "\n");
                        bc.append(i);
                        sb.append(bc + "\n");
                        bc.append(d);
                        sb.append(bc + "\n");
                        bc.append(h);
                        sb.append(bc + "\n");
                        bc.append(g);
                        sb.append(bc + "\n");
                        bc.append(c);
                        sb.append(bc + "\n");
                        bc.append(e);
                        sb.append(bc + "\n");
                        bc.append(f);
                        sb.append(bc + "\n");
                        bc.append(b);
                        sb.append(bc + "\n");
                        
                        System.out.println(sb.toString()
                                .replaceAll(a.getHash(), "A")
                                .replaceAll(b.getHash(), "B")
                                .replaceAll(c.getHash(), "C")
                                .replaceAll(d.getHash(), "D")
                                .replaceAll(e.getHash(), "E")
                                .replaceAll(f.getHash(), "F")
                                .replaceAll(g.getHash(), "G")
                                .replaceAll(h.getHash(), "H")
                                .replaceAll(i.getHash(), "I"));
                    }
                    else if (input[0].equalsIgnoreCase("testload"))
                    {
                        try
                        {
                            node.blockChain = BlockChainIO.load(DIR + "testload.block");
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                        }
                        long time = System.currentTimeMillis() - 100000;
                        for (int i = 0; 10000 > i; i++)
                        {
                            int ballotNum = 1800;
                            ArrayList<Vote> votes = new ArrayList<Vote>();
                            votes.add(new Vote(0, "John Doe"));
                            votes.add(new Vote(1, "Satoshi"));
                            votes.add(new Vote(2, "John Doe2"));
                            votes.add(new Vote(3, "Satoshi2"));
                            votes.add(new Vote(4, "John Doe3"));
                            votes.add(new Vote(5, "Satoshi3"));
                            votes.add(new Vote(6, "John Doe4"));
                            votes.add(new Vote(7, "Satoshi4"));
                            votes.add(new Vote(8, "John Doe5"));
                            votes.add(new Vote(9, "Satoshi5"));
                            for (int j = 0; ballotNum > j; j++)
                            {
                                node.ballots.add(new Ballot(ByteUtil.bytesToHex(ByteUtil.intToBytes(i)), "<Signature>", votes));
                            }
                            if (System.currentTimeMillis() - time > 5000)
                            {
                                System.out.println((i / 18) + "%");
                                time = System.currentTimeMillis();
                            }
                            while (node.blockGenHash != null)
                            {
                                try
                                {
                                    Thread.sleep(2);
                                }
                                catch (InterruptedException e)
                                {
                                    e.printStackTrace();
                                }
                            }
                            node.generateBlock();
                        }
                        try
                        {
                            System.out.println("Saving...");
                            Thread.sleep(1000);
                            BlockChainIO.save(node.blockChain, DIR + "testload.block");
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                        }
                        System.exit(0);
                    }
                    else
                    {
                        GLOBAL.log(Level.WARNING,"Invalid Command: " + Arrays.toString(input));
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
                e.printStackTrace();
            }
        }
        in.close();
    }
}
