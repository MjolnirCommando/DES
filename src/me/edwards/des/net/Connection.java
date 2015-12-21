package me.edwards.des.net;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;

import me.edwards.des.Node;
import me.edwards.des.net.packet.Packet;
import me.edwards.des.net.packet.PacketPing;

/**
 * Data structure to handle elements of a connection between nodes
 * Created on: Oct 17, 2015 at 10:21:02 AM
 * @author Matthew Edwards
 */
public class Connection
{
    /**
     * A connection was initiated by this node
     */
    public static int CONNECTION_NODE_ONLY = 0;
    /**
     * A connection was initiated by a peer node
     */
    public static int CONNECTION_PEER_ONLY = 1;
    /**
     * The connection is agreed upon by both nodes
     */
    public static int CONNECTION_BOTH = 2;
    
    private static final Logger logger = Logger.getLogger("DES.node");
    private static final int CONNECT_TIMEOUT = 3000;
    private static final int PING_TIMEOUT = 60000;
    
    private Node node;
    private Socket socket;
    private String name;
    private InetAddress address;
    private int port;
    
    private boolean connected;
    private int connectionStatus;
    private long ping;
    private long pingValue;
    private boolean pingSent;
    
    /**
     * Creates new Connection
     * @param node Node which owns this connection
     * @param socket Socket which is used by this connection
     */
    public Connection(Node node, Socket socket)
    {
        this.node = node;
        this.socket = socket;
        this.address = socket.getInetAddress();
        this.port = socket.getPort();
        this.name = getHostName();
        
        this.connected = false;
        this.connectionStatus = CONNECTION_NODE_ONLY;
    }
    
    /**
     * Returns the socket used by this connection
     * @return
     */
    public Socket getSocket()
    {
        return socket;
    }
    
    /**
     * Returns this connection's common name (if any)
     * @return
     */
    public String getName()
    {
        return name;
    }
    
    /**
     * Sets this connection's name
     * @param name
     */
    public void setName(String name)
    {
        this.name = name;
    }
    
    /**
     * Returns this connection's host name
     * @return
     */
    public String getHostName()
    {
        return "/" + address.getHostAddress() + ":" + port;
    }
    
    /**
     * Returns the address of this connection
     * @return
     */
    public InetAddress getAddress()
    {
        return address;
    }
    
    /**
     * Returns the port of this connection
     * @return
     */
    public int getPort()
    {
        return port;
    }
    
    /**
     * Sets the port of this connection
     * @param port
     */
    public void setPort(int port)
    {
        this.port = port;
    }
    
    /**
     * Returns true if this connection is running
     * @return
     */
    public boolean isConnected()
    {
        return connected;
    }
    
    /**
     * Returns the connection status
     * @return
     */
    public int getConnectionStatus()
    {
        return connectionStatus;
    }
    
    /**
     * Sets the connection status
     * @param status
     */
    public void setConnectionStatus(int status)
    {
        this.connectionStatus = status;
    }
    
    @Override
    public String toString()
    {
        if (getHostName().equals(name))
        {
            return getHostName();
        }
        return getHostName() + " [" + name + "]";
    }
    
    /**
     * Sends a Packet through this connection
     * @param packet Packet to send
     */
    public void send(Packet packet)
    {
        try
        {
            socket.getOutputStream().write(packet.getBinary());
        }
        catch (IOException e)
        {
            if (packet instanceof PacketPing)
            {
                logger.log(Level.FINEST, "Could not send packet!", e);
            }
            else
            {
                logger.log(Level.WARNING, "Could not send packet!", e);
            }
        }
    }
    
    /**
     * Notifies the connection that the pong was received
     * @param pong
     */
    public void pong(long pong)
    {
        if (pingSent && pong - 1 == pingValue)
        {
            ping = System.currentTimeMillis();
            pingSent = false;
        }
    }
    
    /**
     * Connects the handled socket
     */
    public void connect()
    {
        connected = true;
        final long timeout = System.currentTimeMillis();
        final Connection c = this;
        ping = System.currentTimeMillis();
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                while (connected && node.isRunning())
                {
                    if (connectionStatus != CONNECTION_BOTH &&
                            System.currentTimeMillis() - timeout > CONNECT_TIMEOUT)
                    {
                        logger.warning("Request timeout in " + c);
                        disconnect();
                    }
                    if (connectionStatus == CONNECTION_BOTH &&
                            pingSent && System.currentTimeMillis() - ping > PING_TIMEOUT + CONNECT_TIMEOUT)
                    {
                        logger.warning("Ping timeout in " + c);
                        disconnect();
                    }
                    if (connectionStatus == CONNECTION_BOTH && System.currentTimeMillis() - ping > PING_TIMEOUT)
                    {
                        if (!pingSent)
                        {
                            pingValue = (long) (Long.MAX_VALUE * Math.random()) + 1;
                            pingSent = true;
                        }
                        send(new PacketPing(pingValue));
                    }
                    try
                    {
                        if (socket.getInputStream().available() != 0)
                        {
                            byte[] data = new byte[socket.getInputStream().available()];
                            socket.getInputStream().read(data);
                            ByteBuffer bytes = ByteBuffer.wrap(data);
                            int pointer = 0;
                            while (pointer < data.length - 1)
                            {
                                bytes.position(pointer);
                                bytes.get();
                                byte[] packet = new byte[bytes.getInt()];
                                bytes.position(pointer);
                                bytes.get(packet);
                                pointer += packet.length;
                                node.parse(packet, c);
                            }
                        }
                        else
                        {
                            Thread.sleep(10);
                        }
                    }
                    catch (Exception e)
                    {
                        logger.log(Level.WARNING, "Socket Receiving Error in " + c, e);
                    }
                }
            }
        }, getHostName()).start();
    }
    
    /**
     * Disconnects this connection from its handled socket and closes node communication
     */
    public void disconnect()
    {
        connected = false;
        node.removeConnection(this);
    }
}
