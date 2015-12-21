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
     * Called on application launch
     * @param args
     */
    public static void main(String[] args)
    {
        Node node = new Node();
        if (args.length == 1)
        {
            try
            {
                node.port = Integer.parseInt(args[0]);
            }
            catch (NumberFormatException e)
            {
                node.name = args[0];
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
        node.logger.info("DES Version " + Node.VERSION + " by Matthew Edwards");
        node.logger.info("(C) Copyright 2015 by Matthew Edwards");
        node.logger.info("------------------------------------------------");
        node.logger.info("DES Node is starting up @ " + SimpleDateFormat.getDateTimeInstance().format(new Date(System.currentTimeMillis())) + "...");
        
        node.logger.info("Loading Default Peer List...");
        node.peerList = new ArrayList<String>();
        try
        {
            BufferedReader reader = new BufferedReader(new InputStreamReader(Node.class.getClassLoader().getResourceAsStream("me/edwards/des/rec/peer_defaults.config")));
            while (reader.ready())
            {
                node.peerList.add(reader.readLine());
                node.logger.fine("> Added peer /" + node.peerList.get(node.peerList.size() - 1));
            }
            node.logger.info("Default Peer List loaded!");
        }
        catch (Exception e)
        {
            node.logger.log(Level.WARNING, "Could not load Default Peer List!", e);
        }
        
        node.logger.info("Loading BlockChain...");
        Block block = new Block("0", Block.MAXIMUM_TARGET, new ArrayList<Ballot>());
        block.genProof();
        node.blockChain = new BlockChain(block);
        
        node.start();
        
        Scanner in = new Scanner(System.in);
        while (node.running)
        {
            try
            {
                if (System.in.available() != 0 && in.hasNextLine())
                {
                    String[] input = in.nextLine().split(" ");
                    
                    node.logger.fine("Console input: " + Arrays.toString(input));
                    
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
                            node.logger.info("Sent PING to " + c);
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
                            node.logger.log(Level.INFO, "Connect Error", e);
                        }
                    }
                    else if (input[0].equalsIgnoreCase("getaddr"))
                    {
                        Connection c = node.getConnection(input[1]);
                        if (c != null)
                        {
                            c.send(new PacketGetAddr());
                            node.logger.info("Sent GETADDR to " + c);
                        }
                    }
                    else if (input[0].equalsIgnoreCase("send"))
                    {
                        if (input[1].equalsIgnoreCase("ballot"))
                        {
                            node.logger.info("Generating ballot...");
                            ArrayList<Vote> votes = new ArrayList<Vote>();
                            votes.add(new Vote(0, "John Doe"));
                            votes.add(new Vote(1, "Satoshi"));
                            Ballot b = new Ballot("FFFFFFFFFFFFFFFF", "<Signature>", votes);
                            node.logger.info("\n" + b.toString());
                            node.ballots.add(b);
                            node.logger.info("Sending ballot...");
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
                            node.logger.info("Generated Ballots!");
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
                        node.logger.info(node.ip + ":" + node.port + " [" + node.name + "]");
                    }
                    else if (input[0].equalsIgnoreCase("addr"))
                    {
                        for (Connection c : node.peers)
                        {
                            node.logger.info(c.toString());
                        }
                    }
                    else
                    {
                        node.logger.info("Invalid Command: " + Arrays.toString(input));
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
