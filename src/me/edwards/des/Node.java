package me.edwards.des;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
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
import me.edwards.des.block.Vote;
import me.edwards.des.net.Connection;
import me.edwards.des.net.packet.Packet;
import me.edwards.des.net.packet.PacketAddr;
import me.edwards.des.net.packet.PacketBallot;
import me.edwards.des.net.packet.PacketGetAddr;
import me.edwards.des.net.packet.PacketGetData;
import me.edwards.des.net.packet.PacketInv;
import me.edwards.des.net.packet.PacketNotFound;
import me.edwards.des.net.packet.PacketPing;
import me.edwards.des.net.packet.PacketPong;
import me.edwards.des.net.packet.PacketVerack;
import me.edwards.des.net.packet.PacketVersion;
import me.edwards.des.util.ByteUtil;
import me.edwards.des.util.HashUtil;
import me.edwards.des.util.Version;


/**
 * A Node meant to operate on the DES network.
 * Created on: Oct 16, 2015 at 9:35:54 PM
 * @author Matthew Edwards
 */
public class Node
{
    /**
     * Node Version
     */
    public static final Version VERSION = new Version("1.0.0 DES_ALPHA");
    
    /**
     * Default MJGL Protocol Buffer Size
     */
    public static final int BUFFER_SIZE = 4096;
    
    private ArrayList<String> peerList;
    
    private Logger logger;

    private ArrayList<Connection> peers;
    private ArrayList<Ballot> ballots;
    
    private InetAddress ip;
    private ServerSocket socket;
    private int port;
    private String name;
    private boolean running;
    private Thread handshake;
    
    /**
     * Starts the Node
     */
    public void start()
    {
        peers = new ArrayList<Connection>();
        ballots = new ArrayList<Ballot>();
        
        try
        {
            socket = new ServerSocket(port);
            socket.setReceiveBufferSize(BUFFER_SIZE);
            ip = InetAddress.getLocalHost();
            port = socket.getLocalPort();
            if (name == null)
            {
                name = ip.getHostAddress() + ":" + port;
            }
            logger.info("Starting Node on /" + ip.getHostAddress() + ":" + port + " ...");
        }
        catch (Exception e)
        {
            logger.log(Level.SEVERE, "Could not bind socket!", e);
            return;
        }
        
        running = true;
        
        final Node n = this;
        handshake = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                while (running)
                {
                    try
                    {
                        Connection c = new Connection(n, socket.accept());
                        logger.info("Accepted connection from Node " + c.getHostName() + "!");
                        c.setConnectionStatus(Connection.CONNECTION_PEER_ONLY);
                        peers.add(c);
                        c.connect();
                    }
                    catch (IOException e)
                    {
                        if (running)
                        {
                            logger.log(Level.WARNING, "Could not accept Node to socket", e);
                        }
                    }
                    
                }
            }
        }, "Node Handshake");
        handshake.start();
        
        logger.info("Node started!");
        
        logger.info("Connecting to known peers...");
        
        for (String peer : peerList)
        {
            try
            {
                connect(InetAddress.getByName(peer.split(":")[0]), Integer.parseInt(peer.split(":")[1]));
            }
            catch (Exception e)
            {
                //
            }
        }
        
        logger.info("Connected to " + peers.size() + " known peers!");
    }
    
    /**
     * Stops the Node
     */
    public void stop()
    {
        if (running)
        {
            logger.info("Stopping Node...");
            running = false;
            
            if (socket != null)
            {
                try
                {
                    socket.close();
                }
                catch (IOException e)
                {
                    //
                }
                socket = null;
            }
            if (handshake != null)
            {
                handshake.interrupt();
                handshake = null;
            }
            logger.info("Node stopped!");
        }
    }
    
    /**
     * Returns true if this node is running
     * @return
     */
    public boolean isRunning()
    {
        return running;
    }
    
    /**
     * Parses a packet received by a connection
     * @param data Packet data received
     * @param connection Connection received by
     */
    public void parse(byte[] data, Connection connection)
    {
        logger.finest("Received 0x" + Packet.toHex(data[0]) + " packet from " + connection.getHostName());
        switch(Packet.lookup(data[0]))
        {
            case PING:
            {
                PacketPing packet = new PacketPing(data);
                connection.send(new PacketPong(packet.getPing() + 1));
                return;
            }
            case PONG:
            {
                PacketPong packet = new PacketPong(data);
                if (packet.getPing() - 1 == 0)
                {
                    logger.info("Received PONG from " + connection);
                }
                connection.pong(packet.getPing());
                return;
            }
            case VERSION:
            {
                PacketVersion packet = new PacketVersion(data);
                logger.finer("Received Version from " + connection.getHostName() + "! Validating...");
                if (connection.getConnectionStatus() == Connection.CONNECTION_NODE_ONLY
                        && validateVersion(packet.getVersion()))
                {
                    logger.finer("Version valid! Sending verack and completing handshake...");
                    connection.send(new PacketVerack());
                    connection.setConnectionStatus(Connection.CONNECTION_BOTH);
                    logger.finer("Requesting address cache information from " + connection);
                    connection.send(new PacketGetAddr());
                }
                else if (connection.getConnectionStatus() == Connection.CONNECTION_PEER_ONLY
                        && validateVersion(packet.getVersion()))
                {
                    logger.finer("Version valid! Sending version information...");
                    connection.setPort(packet.getPort());
                    connection.send(new PacketVersion(VERSION, port));
                }
                else
                {
                    logger.finer("Could not validate version! Aborting connection...");
                    removeConnection(connection);
                }
                return;
            }
            case VERACK:
            {
                if (connection.getConnectionStatus() != Connection.CONNECTION_BOTH)
                {
                    logger.finer("Received verack! Completing handshake...");
                    connection.setConnectionStatus(Connection.CONNECTION_BOTH);
                    logger.finer("Requesting address cache information from " + connection);
                    connection.send(new PacketGetAddr());
                }
                return;
            }
            case GETADDR:
            {
                logger.finer("Request for address cache information from " + connection);
                connection.send(new PacketAddr(this.getPeers()));
                return;
            }
            case ADDR:
            {
                PacketAddr packet = new PacketAddr(data);
                logger.info("Received address cache information from " + connection + ". Bootstrapping...");
                for (String s : packet.getPeers())
                {
                    try
                    {
                        logger.log(Level.FINE, "Connecting to " + s);
                        connect(InetAddress.getByName(s.substring(1, s.indexOf(':'))), Integer.parseInt(s.substring(s.indexOf(':') + 1)));
                    }
                    catch (Exception e)
                    {
                        logger.log(Level.FINE, "Could not connect!", e);
                    }
                }
                return;
            }
            case INV:
            {
                PacketInv packet = new PacketInv(data);
                PacketGetData getData = new PacketGetData();
                for (int i = 0; packet.getSize() > i; i++)
                {
                    if (packet.getType(i) == PacketInv.VECTOR_BALLOT)
                    {
                        boolean b = true;
                        for (int v = 0; ballots.size() > v; v++)
                        {
                            if (ballots.get(v).getRoot().equals(packet.getHash(i)))
                            {
                                b = false;
                                break;
                            }
                        }
                        if (b)
                        {
                            getData.addInv(packet.getType(i), packet.getHash(i));
                        }
                    }
                }
                if (getData.getSize() > 0)
                {
                    connection.send(getData);
                }
                return;
            }
            case NOTFOUND:
            {
                PacketNotFound packet = new PacketNotFound(data);
                logger.finer("Received that resource " + packet.getHash() + "(" + packet.getType() + ") could not be found.");
                return;
            }
            case GETDATA:
            {
                PacketGetData packet = new PacketGetData(data);
                for (int i = 0; packet.getSize() > i; i++)
                {
                    if (packet.getType(i) == PacketInv.VECTOR_BALLOT)
                    {
                        boolean b = false;
                        for (int v = 0; ballots.size() > v; v++)
                        {
                            if (ballots.get(v).getRoot().equals(packet.getHash(i)))
                            {
                                b = true;
                                connection.send(new PacketBallot(ballots.get(v)));
                                break;
                            }
                        }
                        if (!b)
                        {
                            connection.send(new PacketNotFound(packet.getType(i), packet.getHash(i)));
                        }
                    }
                }
                return;
            }
            case BALLOT:
            {
                PacketBallot packet = new PacketBallot(data);
                
                return;
            }
            default: logger.finest("Could not parse invalid packet.");
        }
    }
    
    /**
     * Connects this node to another
     * @param address
     * @param port
     * @return Returns connection if the connection was successful
     */
    public Connection connect(InetAddress address, int port)
    {
        if (ip.equals(address) && port == this.port
                || getConnection("/" + address.getHostAddress() + ":" + port) != null)
        {
            logger.finer("Duplicate connection /" + address.getHostAddress() + ":" + port + "!");
            return null;
        }
        
        try
        {
            Connection c = new Connection(this, new Socket(address, port));
            if (c.getSocket().isConnected())
            {
                logger.info("Connected to Node " + c.getHostName() + "! Sending version information...");
                c.setConnectionStatus(Connection.CONNECTION_NODE_ONLY);
                c.connect();
                c.send(new PacketVersion(VERSION, this.port));
                peers.add(c);
                return c;
            }
            else
            {
                logger.log(Level.FINE, "Could not connect to Node " + c.getHostName() + "!");
            }
        }
        catch (Exception e)
        {
            logger.log(Level.FINE, "Node connection error!", e);
        }
        return null;
    }
    
    /**
     * Removes a connection from the node
     * @param c Connection to remove
     */
    public void removeConnection(Connection c)
    {
        if (peers.contains(c))
        {
            if (c.isConnected())
            {
                c.disconnect();
            }
            peers.remove(c);
        }
    }
    
    /**
     * Returns the connection with the specified name
     * @param hostname
     * @return
     */
    public Connection getConnection(String hostname)
    {
        for (int i = 0; peers.size() > i; i++)
        {
            if (peers.get(i).getHostName().equals(hostname) || peers.get(i).getName().equals(hostname))
            {
                return peers.get(i);
            }
        }
        return null;
    }
    
    /**
     * Sends the specified packet to all peers
     * @param p
     */
    public void sendToAll(Packet p)
    {
        for (int i = 0; peers.size() > i; i++)
        {
            peers.get(i).send(p);
        }
    }
    
    /**
     * Returns a list of this node's peers
     * @return
     */
    public ArrayList<Connection> getPeers()
    {
        return peers;
    }
    
    private static boolean validateVersion(Version v)
    {
        return VERSION.isEqualTo(v);
    }
    
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
        node.logger.info("DES Version " + VERSION + " by Matthew Edwards");
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
                    
                    if (input.length == 0)
                    {
                        // ignore
                    }
                    else if (input[0].equalsIgnoreCase("test"))
                    {
                        //TODO
                        node.logger.info("Testing ballot...");
                        ArrayList<Vote> votes = new ArrayList<Vote>();
                        votes.add(new Vote(0, "John Doe"));
                        votes.add(new Vote(1, "Satoshi"));
                        Ballot b = new Ballot("FFFFFFFFFFFFFFFF", "<Signature>", votes);
                        node.logger.info("\n" + b.toString());
                        node.logger.info(b.getRoot());
                        PacketBallot p = new PacketBallot(b);
                        PacketBallot pR = new PacketBallot(p.getBinary());
                        Ballot bR = pR.getBallot();
                        node.logger.info("\n" + bR.toString());
                        node.logger.info(bR.getRoot());
                        node.logger.info("VALID?: " + b.getRoot().equals(bR.getRoot()));
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
                    else if (input[0].equalsIgnoreCase("gen"))
                    {
                        if (input[1].equalsIgnoreCase("ballot"))
                        {
                            node.logger.info("Generating ballot...");
                            ArrayList<Vote> votes = new ArrayList<Vote>();
                            votes.add(new Vote(0, "John Doe"));
                            votes.add(new Vote(1, "Satoshi"));
                            Ballot b = new Ballot("FFFFFFFFFFFFFFFF", "<Signature>", votes);
                            node.logger.info("\n" + b.toString());
                        }
                        else if (input[1].equalsIgnoreCase("block"))
                        {
                            node.logger.info("Generating block...");
                            ArrayList<Ballot> temp = new ArrayList<Ballot>();
                            for (int i = 0; node.ballots.size() > i; i++)
                            {
                                temp.add(node.ballots.get(i));
                            }
                            long time = System.currentTimeMillis();
                            Block b = new Block(input[2], ByteUtil.bytesToInt(ByteUtil.hexToBytes(HashUtil.generateLeadingZeros(input[3], 8))), temp);
                            b.validate();
                            node.logger.info("Generated Block in " + ((System.currentTimeMillis() - time) / 1000) + " seconds!\n" + b.toString());
                        }
                        else if (input[1].equalsIgnoreCase("myaddr"))
                        {
                            node.logger.info(node.ip + ":" + node.port + " [" + node.name + "]");
                        }
                        else if (input[1].equalsIgnoreCase("addr"))
                        {
                            for (Connection c : node.peers)
                            {
                                node.logger.info(c.toString());
                            }
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
