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
import java.util.ArrayList;
import me.edwards.des.Node;
import me.edwards.des.net.packet.PacketInv;
import me.edwards.des.util.ByteUtil;
import me.edwards.des.util.HashUtil;

// -----------------------------------------------------------------------------
/**
 * Data structure to store and maintain a single cast Ballot for a single entity
 * (user). Ballots are created in an Election Application when a user votes.<br>
 * <br>
 * The Ballot contains the {@link Vote Votes} chosen by the user in addition to
 * the user's unique UUID (created by the Election Authority). To prevent
 * changes from being made to the Ballot, its hash is signed using the ECDSA
 * private key generated by the Election Application. After the Ballot is
 * signed, the private key is destroyed and the public key is sent to the
 * Election Authority, marking the Ballot as cast. When the Node
 * {@link me.edwards.des.Node#parse(byte[], me.edwards.des.net.Connection)
 * receives} a Ballot, the signature is authenticated with the Ballot's hash to
 * ensure that the Ballot was not changed en-route to the Node.<br>
 * <br>
 * Created on: Nov 1, 2015 at 11:30:16 PM
 * 
 * @author Matthew Edwards
 */
public class Ballot
{
    // ~ Static/Instance variables .............................................

    // -------------------------------------------------------------------------
    /**
     * This class' Ballot version
     */
    private static final byte VERSION = 1;

    
    // -------------------------------------------------------------------------
    private byte              version;
    private String            id;
    private byte[]            votes;
    private String            signature;    // 32 bytes

    private String            signatureRoot;
    private String            root;
    private byte[]            bytes;


    // ~ Constructors ..........................................................

    // -------------------------------------------------------------------------
    /**
     * Creates new Ballot from a UUID, Signatures, and list of votes. This
     * constructor is mainly used for demonstration or testing purposes because
     * Election Applications create new Ballots. {@link Node Nodes} and
     * {@link Block Blocks} only create Ballots from
     * {@link Ballot#Ballot(byte[]) binary data}.
     * 
     * @param id
     *            UUID of Ballot signer (User who cast this Ballot)
     * @param signature
     *            Signature on this ballot generated using user's private ECDSA
     *            key
     * @param votes
     *            List of {@link Vote Votes} on this Ballot
     */
    public Ballot(String id, String signature, ArrayList<Vote> votes)
    {
        this.version = VERSION;
        this.id = HashUtil.generateLeadingZeros(id, 16);
        this.signature = HashUtil.generateLeadingZeros(signature);

        int size = 0;
        for (int i = 0; votes.size() > i; i++)
        {
            size += votes.get(i).getBytes().length;
        }
        ByteBuffer buffer = ByteBuffer.allocate(1 + 8 + size + 32);
        buffer.put(version);
        buffer.put(ByteUtil.hexToBytes(this.id));
        for (int i = 0; votes.size() > i; i++)
        {
            buffer.put(votes.get(i).getBytes());
        }
        this.signatureRoot = HashUtil.generateHash(buffer.array());
        buffer.put(ByteUtil.hexToBytes(this.signature));
        this.bytes = buffer.array();
        this.root = HashUtil.generateHash(this.bytes);
    }


    // -------------------------------------------------------------------------
    /**
     * Initializes Ballot from binary data as a byte array. This constructor is
     * used by {@link Node Nodes} and {@link Block Blocks} to load Ballots.
     * 
     * @param binary
     *            Byte array representing this Ballot
     */
    public Ballot(byte[] binary)
    {
        this.bytes = binary;
        ByteBuffer data = ByteBuffer.wrap(binary);
        this.version = data.get();
        byte[] idBytes = new byte[8];
        data.get(idBytes, 0, 8);
        this.id = ByteUtil.bytesToHex(idBytes);
        this.votes = new byte[data.capacity() - data.position() - 32];
        int rootLength = data.position() - 1;
        byte[] signatureBytes = new byte[32];
        data.get(signatureBytes, 0, signatureBytes.length);
        this.signature = new String(signatureBytes);
        root = HashUtil.generateHash(binary);
        data.position(0);
        byte[] signatureRootBytes = new byte[rootLength];
        data.get(signatureRootBytes, 0, rootLength);
        this.signatureRoot = HashUtil.generateHash(signatureRootBytes);
    }


    // ~ Methods ...............................................................

    // -------------------------------------------------------------------------
    /**
     * Returns this Ballot's (user) UUID
     * 
     * @return UUID as a 16-digit hexadecimal String
     */
    public String getID()
    {
        return id;
    }


    // -------------------------------------------------------------------------
    /**
     * Returns this Ballot's signature. Used for Ballot authentication by the
     * {@link Node Node}.
     * 
     * @return Signature as a 32-digit hexadecimal hash digest
     */
    public String getSignature()
    {
        return signature;
    }


    // -------------------------------------------------------------------------
    /**
     * Returns the signature root hash of this ballot. Used for Ballot
     * authentication by the {@link Node Node}.
     * 
     * @return Signature root as a 32-digit hexadecimal hash digest
     */
    public String getSignatureRoot()
    {
        return signatureRoot;
    }


    // -------------------------------------------------------------------------
    /**
     * Returns the root hash of this ballot. Used to identify this Ballot in
     * {@link PacketInv Inventory requests} and in the Node's memory.
     * 
     * @return Ballot root hash (32-digit hexadecimal)
     */
    public String getRoot()
    {
        return root;
    }


    // -------------------------------------------------------------------------
    /**
     * Returns the Ballot in binary format as a byte array.
     * 
     * @return Byte array representing this Ballot
     */
    public byte[] getBytes()
    {
        return bytes;
    }


    // -------------------------------------------------------------------------
    @Override
    public String toString()
    {
        StringBuffer voteDigest = new StringBuffer();
        ByteBuffer voteData = ByteBuffer.wrap(votes);
        int voteSize = 0;
        while (voteData.hasRemaining())
        {
            int id = voteData.getInt();
            byte[] strData = new byte[voteData.getInt()];
            voteData.get(strData);
            voteDigest.append("\n\t" + id + ":" + new String(strData));
            voteSize++;
        }
        return "--- Ballot --------------------------------" + "\nID:        "
            + id + "\nVersion:   " + version + "\nSignature: " + signature
            + "\nVotes (" + voteSize + "): " + voteDigest.toString()
            + "\n-------------------------------------------";
    }


    // -------------------------------------------------------------------------
    @Override
    public boolean equals(Object obj)
    {
        return obj instanceof Ballot
            && ((Ballot)obj).getRoot().equals(getRoot());
    }
}
