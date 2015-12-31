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
import me.edwards.des.net.packet.PacketPong;

// -----------------------------------------------------------------------------
/**
 * Data structure to handle the connection between {@link Node Nodes}. A
 * Connection tracks the ping and status of a connection between Nodes and
 * ensures that connection stay alive.<br>
 * <br>
 * Created on: Oct 17, 2015 at 10:21:02 AM
 * 
 * @author Matthew Edwards
 */
public class Connection
{
    // ~ Static/Instance variables .............................................

    // -------------------------------------------------------------------------
    /**
     * A connection was initiated by this node
     */
    public static int           CONNECTION_NODE_ONLY = 0;

    /**
     * A connection was initiated by a peer node
     */
    public static int           CONNECTION_PEER_ONLY = 1;

    /**
     * The connection is agreed upon by both nodes
     */
    public static int           CONNECTION_BOTH      = 2;

    private static final Logger logger               = Logger
                                                         .getLogger("DES.node");
    private static final int    CONNECT_TIMEOUT      = 3000;
    private static final int    PING_TIMEOUT         = 60000;

    
    // -------------------------------------------------------------------------
    private Node                node;
    private Socket              socket;
    private String              name;
    private InetAddress         address;
    private int                 port;

    private boolean             connected;
    private int                 connectionStatus;
    private long                ping;
    private long                pingValue;
    private boolean             pingSent;


    // ~ Constructors ..........................................................

    // -------------------------------------------------------------------------
    /**
     * Creates new Connection using the local {@link Node Node} and the socket
     * connecting to the remote Node.
     * 
     * @param node
     *            Local Node which owns this connection
     * @param socket
     *            Socket to remote Node which is used by this connection
     */
    public Connection(Node node, Socket socket)
    {
        this.node = node;
        this.socket = socket;
        this.address = socket.getInetAddress();
        this.port = socket.getPort();
        this.name = getHostName();

        try
        {
            socket.setReceiveBufferSize(Node.BUFFER_SIZE);
            socket.setSendBufferSize(Node.BUFFER_SIZE);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        this.connected = false;
        this.connectionStatus = CONNECTION_NODE_ONLY;
    }


    // -------------------------------------------------------------------------
    /**
     * Returns the socket used by this connection.
     * 
     * @return Socket connecting to remote {@link Node Node}
     */
    public Socket getSocket()
    {
        return socket;
    }


    // -------------------------------------------------------------------------
    /**
     * Returns this connection's common name (if any).
     * 
     * @return This connection's human-readable common name
     */
    public String getName()
    {
        return name;
    }


    // -------------------------------------------------------------------------
    /**
     * Sets this connection's common name.
     * 
     * @param name
     *            Human-readable common name of this connection
     */
    public void setName(String name)
    {
        this.name = name;
    }


    // -------------------------------------------------------------------------
    /**
     * Returns this connection's host name in the format "/ADDRESS:PORT".
     * 
     * @return The connection host name in the format "/ADDRESS:PORT"
     */
    public String getHostName()
    {
        return "/" + address.getHostAddress() + ":" + port;
    }


    // -------------------------------------------------------------------------
    /**
     * Returns the destination address of this connection.
     * 
     * @return Address of remote Node as InetAddress
     */
    public InetAddress getAddress()
    {
        return address;
    }


    // -------------------------------------------------------------------------
    /**
     * Returns the destination port of this connection.
     * 
     * @return Port of remote Node
     */
    public int getPort()
    {
        return port;
    }


    // -------------------------------------------------------------------------
    /**
     * Sets the port of this connection. Port is initially set to connecting
     * port. Should only be used to reset the port to the remote Node's server
     * port by {@linkplain Node#parse(byte[], Connection)}.
     * 
     * @param port
     *            Port of remote Node
     */
    public void setPort(int port)
    {
        this.port = port;
    }


    // -------------------------------------------------------------------------
    /**
     * Returns true if the connection is currently connected.
     * 
     * @return True if connected, False otherwise
     */
    public boolean isConnected()
    {
        return connected;
    }


    // -------------------------------------------------------------------------
    /**
     * Returns the current connection status.<br>
     * <br>
     * <table>
     * <tr>
     * <td>{@link Connection#CONNECTION_NODE_ONLY CONNECTION_NODE_ONLY}</td>
     * <td>A connection was initiated by this node</td>
     * </tr>
     * <tr>
     * <td>{@link Connection#CONNECTION_PEER_ONLY CONNECTION_PEER_ONLY}</td>
     * <td>A connection was initiated by a peer node</td>
     * </tr>
     * <tr>
     * <td>{@link Connection#CONNECTION_BOTH CONNECTION_BOTH}</td>
     * <td>The connection is agreed upon by both nodes</td>
     * </tr>
     * </table>
     * 
     * @return Current connection status
     */
    public int getConnectionStatus()
    {
        return connectionStatus;
    }


    // -------------------------------------------------------------------------
    /**
     * Sets the current connection status.<br>
     * <br>
     * <table>
     * <tr>
     * <td>{@link Connection#CONNECTION_NODE_ONLY CONNECTION_NODE_ONLY}</td>
     * <td>A connection was initiated by this node</td>
     * </tr>
     * <tr>
     * <td>{@link Connection#CONNECTION_PEER_ONLY CONNECTION_PEER_ONLY}</td>
     * <td>A connection was initiated by a peer node</td>
     * </tr>
     * <tr>
     * <td>{@link Connection#CONNECTION_BOTH CONNECTION_BOTH}</td>
     * <td>The connection is agreed upon by both nodes</td>
     * </tr>
     * </table>
     * 
     * @param status
     *            New connection status
     */
    public void setConnectionStatus(int status)
    {
        this.connectionStatus = status;
    }


    // -------------------------------------------------------------------------
    @Override
    public String toString()
    {
        if (getHostName().equals(name))
        {
            return getHostName();
        }
        return getHostName() + " [" + name + "]";
    }


    // -------------------------------------------------------------------------
    /**
     * Sends a {@link Packet Packet} through this connection to the remote
     * {@link Node Node}.
     * 
     * @param packet
     *            Packet to send
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


    // -------------------------------------------------------------------------
    /**
     * Notifies the connection that the {@link PacketPong Pong} was received.
     * This method is used by {@linkplain Node#parse(byte[], Connection)}.
     * 
     * @param pong
     *            Payload of the Pong received (Must be one more than the sent
     *            Ping value to be valid)
     */
    public void pong(long pong)
    {
        if (pingSent && pong - 1 == pingValue)
        {
            ping = System.currentTimeMillis();
            pingSent = false;
        }
    }


    // -------------------------------------------------------------------------
    /**
     * Connects the local Node to the remote Node through this Connection.
     */
    public void connect()
    {
        connected = true;
        final long timeout = System.currentTimeMillis();
        final Connection c = this;
        ping = System.currentTimeMillis();
        new Thread(new Runnable() {
            @Override
            public void run()
            {
                while (connected && node.isRunning())
                {
                    if (connectionStatus != CONNECTION_BOTH
                        && System.currentTimeMillis() - timeout > CONNECT_TIMEOUT)
                    {
                        logger.warning("Request timeout in " + c);
                        disconnect();
                    }
                    if (connectionStatus == CONNECTION_BOTH
                        && pingSent
                        && System.currentTimeMillis() - ping > PING_TIMEOUT
                            + CONNECT_TIMEOUT)
                    {
                        logger.warning("Ping timeout in " + c);
                        disconnect();
                    }
                    if (connectionStatus == CONNECTION_BOTH
                        && System.currentTimeMillis() - ping > PING_TIMEOUT)
                    {
                        if (!pingSent)
                        {
                            pingValue =
                                (long) (Long.MAX_VALUE * Math.random()) + 1;
                            pingSent = true;
                        }
                        send(new PacketPing(pingValue));
                    }
                    try
                    {
                        if (socket.getInputStream().available() != 0)
                        {
                            byte[] data =
                                new byte[socket.getInputStream().available()];
                            socket.getInputStream().read(data);
                            ByteBuffer bytes = ByteBuffer.wrap(data);
                            int pointer = 0;
                            while (pointer < data.length - 1)
                            {
                                bytes.position(pointer);
                                bytes.get();
                                int packetSize = bytes.getInt();
                                if (packetSize > data.length - pointer)
                                {
                                    logger.log(Level.SEVERE, "Packet size overload! Expected " + packetSize + " but received " + (data.length - pointer));
                                }
                                byte[] packet = new byte[packetSize];
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
                        logger.log(Level.WARNING, "Socket Receiving Error in "
                            + c, e);
                    }
                }
            }
        },
            getHostName()).start();
    }


    // -------------------------------------------------------------------------
    /**
     * Disconnects the local and remote Nodes, closes the connection socket and
     * closes node communication.
     */
    public void disconnect()
    {
        connected = false;
        node.removeConnection(this);
    }
}
