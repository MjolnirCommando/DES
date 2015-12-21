package me.edwards.des;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import me.edwards.des.block.Ballot;
import me.edwards.des.block.Block;
import me.edwards.des.block.BlockChain;
import me.edwards.des.net.Connection;
import me.edwards.des.net.packet.Packet;
import me.edwards.des.net.packet.PacketAddr;
import me.edwards.des.net.packet.PacketBallot;
import me.edwards.des.net.packet.PacketBlock;
import me.edwards.des.net.packet.PacketGetAddr;
import me.edwards.des.net.packet.PacketGetData;
import me.edwards.des.net.packet.PacketInv;
import me.edwards.des.net.packet.PacketNotFound;
import me.edwards.des.net.packet.PacketPing;
import me.edwards.des.net.packet.PacketPong;
import me.edwards.des.net.packet.PacketVerack;
import me.edwards.des.net.packet.PacketVersion;
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
     * Default Packet Buffer Size
     */
    public static final int BUFFER_SIZE = 4096;
    
    protected ArrayList<String> peerList;
    
    protected Logger logger;

    protected BlockChain blockChain;
    protected String blockGenHash;
    
    protected ArrayList<Connection> peers;
    protected ArrayList<Ballot> ballots;
    
    private ArrayList<String> dataRequests;
    
    protected InetAddress ip;
    
    private ServerSocket socket;
    
    protected int port;
    protected String name;
    protected boolean running;
    
    private Thread handshake;
    private Thread blockGen = null;
    
    /**
     * Starts the Node
     */
    public void start()
    {
        peers = new ArrayList<Connection>();
        ballots = new ArrayList<Ballot>();
        dataRequests = new ArrayList<String>();
        
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
                    //TODO
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
                    //TODO
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
                        if (dataRequests.contains(packet.getHash(i)))
                        {
                            continue;
                        }
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
                            logger.finer("New resource " + packet.getHash(i) + "(" + packet.getType(i) + ")! Requesting data...");
                            dataRequests.add(packet.getHash(i));
                            getData.addInv(packet.getType(i), packet.getHash(i));
                        }
                    }
                    else if (packet.getType(i) == PacketInv.VECTOR_BLOCK)
                    {
                        if (dataRequests.contains(packet.getHash(i)))
                        {
                            continue;
                        }
                        if (!blockChain.contains(packet.getHash(i)))
                        {
                            logger.finer("New resource " + packet.getHash(i) + "(" + packet.getType(i) + ")! Requesting data...");
                            dataRequests.add(packet.getHash(i));
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
                logger.finer("Received notice that resource " + packet.getHash() + "(" + packet.getType() + ") could not be found.");
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
                            if (ballots.get(v).getRoot().equalsIgnoreCase(packet.getHash(i)))
                            {
                                b = true;
                                logger.finer("Request for resource " + packet.getHash(i) + "(" + packet.getType(i) + ")! Sending data...");
                                connection.send(new PacketBallot(ballots.get(v)));
                                break;
                            }
                        }
                        if (!b)
                        {
                            logger.finer("Request for resource " + packet.getHash(i) + "(" + packet.getType(i) + ")! Could not be found! Sending reply...");
                            connection.send(new PacketNotFound(packet.getType(i), packet.getHash(i)));
                        }
                    }
                    else if (packet.getType(i) == PacketInv.VECTOR_BLOCK)
                    {
                        if (blockChain.contains(packet.getHash(i)))
                        {
                            logger.finer("Request for resource " + packet.getHash(i) + "(" + packet.getType(i) + ")! Sending data...");
                            connection.send(new PacketBlock(blockChain.get(packet.getHash(i))));
                        }
                        else
                        {
                            logger.finer("Request for resource " + packet.getHash(i) + "(" + packet.getType(i) + ")! Could not be found! Sending reply...");
                            connection.send(new PacketNotFound(packet.getType(i), packet.getHash(i)));
                        }
                    }
                }
                return;
            }
            case BALLOT:
            {
                PacketBallot packet = new PacketBallot(data);
                logger.info("Received ballot " + packet.getBallot().getRoot() + "!");
                final Ballot b = packet.getBallot();
                new Thread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        //TODO
                        if (!dataRequests.remove(b.getRoot()))
                        {
                            return;
                        }
                        for (int i = 0; ballots.size() > i; i++)
                        {
                            if (ballots.get(i).getRoot().equalsIgnoreCase(b.getRoot()))
                            {
                                return;
                            }
                        }
                        ballots.add(b);
                        PacketInv inv = new PacketInv();
                        inv.addInv(b);
                        sendToAll(inv);
                    }
                }, "Ballot Validation " + packet.getBallot().getRoot()).start();
                return;
            }
            case BLOCK:
            {
                PacketBlock packet = new PacketBlock(data);
                logger.info("Received block " + packet.getBlock().getHash() + "!");
                final Block b = packet.getBlock();
                new Thread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        //TODO
                        if (!dataRequests.remove(b.getHash()))
                        {
                            return;
                        }
                        if (blockChain.contains(b.getHash()))
                        {
                            return;
                        }
                        blockChain.queue(b);
                        PacketInv inv = new PacketInv();
                        inv.addInv(b);
                        sendToAll(inv);
                    }
                }, "Block Validation " + packet.getBlock().getHash()).start();
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
    
    /**
     * Attempts to generate a block from the known ballots
     */
    public void generateBlock()
    {
        if (blockGen != null)
        {
            return;
        }
        blockGen = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                logger.info("Generating Block...");
                ArrayList<Ballot> tempBallot = new ArrayList<Ballot>();
                int size = ballots.size();
                for (int i = 0; size > i; i++)
                {
                    tempBallot.add(ballots.get(0));
                    ballots.remove(0);
                }
                long time = System.currentTimeMillis();
                blockGenHash = blockChain.getTop().getHash();
                Block b = new Block(blockGenHash, Block.MAXIMUM_TARGET, tempBallot);
                b.genProof();
                logger.info("Generated Block in " + ((System.currentTimeMillis() - time) / 1000) + " seconds!\n" + b.toString());
                logger.info("Adding block to BlockChain...");
                blockChain.queue(b);
                PacketInv inv = new PacketInv();
                inv.addInv(b);
                logger.info("Notifying peers of block...");
                sendToAll(inv);
                blockGen = null;
            }
        }, "Block Generation");
        blockGen.start();
    }
    
    private static boolean validateVersion(Version v)
    {
        return VERSION.isEqualTo(v);
    }
    
}
