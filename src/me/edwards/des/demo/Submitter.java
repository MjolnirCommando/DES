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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.interfaces.ECPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import me.edwards.des.Node;
import me.edwards.des.block.Ballot;
import me.edwards.des.block.Vote;
import me.edwards.des.net.packet.PacketBallot;
import me.edwards.des.util.ByteUtil;

// -----------------------------------------------------------------------------
/**
 * This class allows local Nodes to function without an Election Application.
 * This class is purely used for demonstration purposes.<br>
 * <br>
 * It handles submitting {@link Ballot Ballots} to a local {@link Node Node} for
 * demonstration purposes.<br>
 * <br>
 * Created on: Dec 30, 2015 at 2:01:45 PM
 * 
 * @author Matthew Edwards
 */
public class Submitter
{
    // ~ Static/Instance variables .............................................

    // -------------------------------------------------------------------------
    private static final HashMap<String, ECPublicKey> keys =
                                                               new HashMap<String, ECPublicKey>();


    // ~ Methods ...............................................................

    // -------------------------------------------------------------------------
    /**
     * Generates a database of key pairs for demonstration purposes. Two
     * databases are created. "private.data" is created for the private keys and
     * "public.data" is created for the public keys.
     * 
     * @param file
     *            Directory in which to create databases
     * @param users
     *            Number of users to generate data for
     * @throws IOException
     *             Thrown if an IOException occurs during file writing
     */
    public static void generateDatabase(String file, int users) throws IOException
    {
        ArrayList<String> uuids = new ArrayList<String>();
        Random rnd = new Random();
        File privateFile = new File(file + "private.data");
        privateFile.createNewFile();
        File publicFile = new File(file + "public.data");
        publicFile.createNewFile();
        
        BufferedOutputStream privateOut = new BufferedOutputStream(new FileOutputStream(privateFile));
        BufferedOutputStream publicOut = new BufferedOutputStream(new FileOutputStream(publicFile));
        for (int i = 0; users > i; i++)
        {
            try
            {
                String id = null;
                while (id == null || uuids.contains(id))
                {
                    byte[] idBytes = new byte[8];
                    rnd.nextBytes(idBytes);
                    id = ByteUtil.bytesToHex(idBytes);
                }
                uuids.add(id);
                
                KeyPairGenerator keyGen = KeyPairGenerator.getInstance("EC");
                SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
                keyGen.initialize(256, random);
    
                KeyPair pair = keyGen.generateKeyPair();

                byte[] privateBytes = new PKCS8EncodedKeySpec(pair.getPrivate().getEncoded()).getEncoded();
                privateOut.write(ByteUtil.hexToBytes(id));
                privateOut.write(ByteUtil.intToBytes(privateBytes.length));
                privateOut.write(privateBytes);
                
                byte[] publicBytes = new X509EncodedKeySpec(pair.getPublic().getEncoded()).getEncoded();
                publicOut.write(ByteUtil.hexToBytes(id));
                publicOut.write(ByteUtil.intToBytes(publicBytes.length));
                publicOut.write(publicBytes);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        privateOut.close();
        publicOut.close();
    }
    

    // -------------------------------------------------------------------------
    /**
     * Loads the "public.data" database located in the specified directory and
     * loads it for demonstration purposes. The keys stored in the database will
     * be used to authenticate {@link Ballot Ballots}.
     * 
     * @param file
     *            Directory in which to load the "public.data" database file
     * @throws IOException
     *             Thrown if an IOException occurs during file reading
     */
    public static void loadDatabase(String file) throws IOException
    {
        keys.clear();
        File publicFile = new File(file + "public.data");
        BufferedInputStream publicIn = new BufferedInputStream(new FileInputStream(publicFile));
        while (publicIn.available() > 0)
        {
            try
            {
                byte[] uuidBytes = new byte[8];
                publicIn.read(uuidBytes);
                String uuid = ByteUtil.bytesToHex(uuidBytes);
                
                byte[] lengthBytes = new byte[4];
                publicIn.read(lengthBytes);
                int length = ByteUtil.bytesToInt(lengthBytes);
                
                byte[] publicBytes = new byte[length];
                publicIn.read(publicBytes);
                
                KeyFactory keyFactory = KeyFactory.getInstance("EC");
                X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(publicBytes);
                keys.put(uuid, (ECPublicKey) keyFactory.generatePublic(publicKeySpec));
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        publicIn.close();
    }
    
    
    // -------------------------------------------------------------------------
    /**
     * Submits {@link Ballot Ballots} to a local {@link Node Node} for
     * demonstration purposes.
     * 
     * @param file
     *            Directory in which to load the "private.data" database file
     * @param node
     *            Local Node to submit Ballots to
     * @param voteList
     *            An array representing a list of Vote IDs, each containing list
     *            of possible Votes for that Vote ID
     * @param duration
     *            The total amount of time to be taken, in milliseconds
     * @throws IOException
     *             Thrown if an IOException occurs during file reading
     */
    public static void submit(
        final String file,
        final Node node,
        final String[][] voteList,
        final int duration) throws IOException
    {
        final HashMap<String, PrivateKey> privateKeys = new HashMap<String, PrivateKey>();
        
        File privateFile = new File(file + "private.data");
        BufferedInputStream privateIn = new BufferedInputStream(new FileInputStream(privateFile));
        byte[] userBytes = new byte[4];
        privateIn.read(userBytes);
        while (privateIn.available() > 0)
        {
            try
            {
                byte[] uuidBytes = new byte[8];
                privateIn.read(uuidBytes);
                String uuid = ByteUtil.bytesToHex(uuidBytes);
                
                byte[] lengthBytes = new byte[4];
                privateIn.read(lengthBytes);
                int length = ByteUtil.bytesToInt(lengthBytes);
                
                byte[] privateBytes = new byte[length];
                privateIn.read(privateBytes);
                
                KeyFactory keyFactory = KeyFactory.getInstance("EC");
                PKCS8EncodedKeySpec publicKeySpec = new PKCS8EncodedKeySpec(privateBytes);
                privateKeys.put(uuid, keyFactory.generatePrivate(publicKeySpec));
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        privateIn.close();
        
        new Thread(new Runnable() {
            @Override
            public void run()
            {
                Random rnd = new Random();
                double timePerBallot = (double) duration / privateKeys.size();
                int totalBallots = 0;
                long time = System.currentTimeMillis();
                for (String uuid : privateKeys.keySet())
                {
                    ArrayList<Vote> votes = new ArrayList<Vote>();
                    for (int i = 0; voteList.length > i; i++)
                    {
                        votes.add(new Vote(i, voteList[i][rnd
                            .nextInt(voteList[i].length)]));
                    }
                    Ballot ballot = new Ballot(uuid, "0", votes);
                    node.parse(new PacketBallot(ballot).getBinary(), null);
                    totalBallots++;
                    long projTime =
                        (long) (time + timePerBallot * totalBallots);
                    if (System.currentTimeMillis() < projTime)
                    {
                        try
                        {
                            Thread.sleep(projTime - System.currentTimeMillis());
                        }
                        catch (InterruptedException e)
                        {
                            //
                        }
                    }
                }
            }
        },
            "Submitter Thread").start();
    }
    

    // -------------------------------------------------------------------------
    /**
     * Returns the corresponding ECDSA Public Key for the specified UUID.
     * 
     * @param uuid
     *            UUID of the Ballot
     * @return If the UUID exists, the ECDSA Public Key is returned, otherwise
     *         null is returned.
     */
    public static ECPublicKey getKey(String uuid)
    {
        return keys.get(uuid);
    }
}
