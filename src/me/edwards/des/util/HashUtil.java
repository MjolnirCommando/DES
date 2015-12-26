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

package me.edwards.des.util;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.logging.Level;
import java.util.logging.Logger;
import me.edwards.des.block.Block;

// -----------------------------------------------------------------------------
/**
 * Utility class for generating and validating hashes. Also provides utility
 * methods for manipulating and formatting hashes. Handles generation of the
 * Proof of Work for {@link Block Blocks}.<br>
 * <br>
 * SHA-256 is the hashing method used by this class, and all hashes (except for
 * Merkle Root hashes) are squared, also known as SHA-256^2, or
 * SHA-256(SHA-256(DATA)). This is meant to provide added security against a
 * pre-image attack.<br>
 * <br>
 * Created on: Oct 16, 2015 at 5:30:31 PM
 * 
 * @author Matthew Edwards
 */
public class HashUtil
{
    // ~ Static/Instance variables .............................................

    private static final Logger logger  = Logger.getLogger("DES.hashutil");
    private static double       hashEst = -1;


    // ~ Methods ...............................................................

    // -------------------------------------------------------------------------
    /**
     * Formats a hash string with the correct number of leading zeros (64 digits
     * by default).
     * 
     * @param hash
     *            String to format
     * @return Hash String with 64 digits (including leading zeros)
     */
    public static String generateLeadingZeros(String hash)
    {
        return generateLeadingZeros(hash, 64);
    }


    // -------------------------------------------------------------------------
    /**
     * Formats a hash string with the correct number of leading zeros.
     * 
     * @param hash
     *            String to format
     * @param digits
     *            Number of digits to match (including leading zeros)
     * @return Hash String with the specified number of digits (including
     *         leading zeros)
     */
    public static String generateLeadingZeros(String hash, int digits)
    {
        StringBuffer output = new StringBuffer();
        if (hash != null)
        {
            output.append(hash);
        }
        while (output.length() < digits)
        {
            output.insert(0, "0");
        }
        return output.toString();
    }


    // -------------------------------------------------------------------------
    /**
     * Generates a {@link Block Block} hash from a byte array representing a
     * Block Header.
     * 
     * @param bytes
     *            Byte array representing a Block Header
     * @param proof
     *            Integer Proof of Work for the specified Block
     * @return Hexadecimal String representing the hash of the Block Header
     */
    public static String generateBlockHash(byte[] bytes, int proof)
    {
        try
        {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.reset();
            md.update(bytes);
            md.update(ByteUtil.intToBytes(proof));
            byte[] digest = md.digest();
            md.reset();
            md.update(digest);
            return ByteUtil.bytesToHex(md.digest());
        }
        catch (NoSuchAlgorithmException e)
        {
            logger.log(Level.SEVERE, "generateBlockHash", e);
        }
        return null;
    }


    // -------------------------------------------------------------------------
    /**
     * Generates a hash from given data as an array of bytes.
     * 
     * @param bytes
     *            Byte Array from which to generate hash
     * @return Hexadecimal String representing the hash of the data
     */
    public static String generateHash(byte[] bytes)
    {
        try
        {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.reset();
            md.update(bytes);
            byte[] digest = md.digest();
            md.reset();
            md.update(digest);
            return generateLeadingZeros(ByteUtil.bytesToHex(md.digest()));
        }
        catch (NoSuchAlgorithmException e)
        {
            logger.log(Level.SEVERE, "generateHash", e);
        }
        return null;
    }


    // -------------------------------------------------------------------------
    /**
     * Validates a generated Proof of Work for any given {@link Block Block}.
     * 
     * @param bytes
     *            The Block in binary format
     * @param proof
     *            Integer Proof of Work for the specified Block
     * @param target
     *            {@link Block#getTarget(int) Target} in shorthand form. The
     *            hash of the Block Header must be less than the target to be
     *            valid (increasing the difficulty to create a proof with the
     *            power and speed of the network).
     * @return True if the proof is valid, False otherwise
     */
    public static boolean validateProof(byte[] bytes, int proof, int target)
    {
        try
        {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.reset();
            md.update(bytes);
            md.update(ByteUtil.intToBytes(proof));
            byte[] digest = md.digest();
            md.reset();
            md.update(digest);
            return validateHash(
                new BigInteger(1, md.digest()),
                Block.getTarget(target));
        }
        catch (NoSuchAlgorithmException e)
        {
            logger.log(Level.SEVERE, "validateProof", e);
        }
        return false;
    }


    // -------------------------------------------------------------------------
    /**
     * Generates the Proof of Work for a given {@link Block Block} using the
     * Block Header. This algorithm is based on methods used in Adam Back's <a
     * href="http://en.wikipedia.org/wiki/Hashcash>Hashcash</a>, an anti-spam
     * solution for email. The generation of the Proof of Work requires the most
     * time and resources during the mining process. It is based on the
     * difficulty of finding the pre-image of a hash. Theoretically, any hash
     * String of 64 characters can be created, but it is difficult to compute
     * the data that will result in the desired hash.<br>
     * <br>
     * For example, let's make a rule that a valid data hash will begin with
     * three zeros. Hashing "Hello" using SHA-256^2 would give a hash of
     * 70BC18BEF5AE66B72D1995F8DB90A583A60D77B4066E4653F1CEAD613025861C. In
     * order to find a pre-image that will result in a valid hash, a nonce
     * (single-use value) will be randomly generated and appended to the
     * original data. If the nonce 6591 is appended to our original data,
     * hashing "Hello6591" using SHA-256^2 yields a hash of
     * 0008A883DACB7094D6DA1A6CEFC6E7CBC13635D024AC15152C4EADBA7AF8D11C, which
     * is valid with the three zero rule.<br>
     * <br>
     * To successfully generate a Block, a challenge must be declared, called
     * the Target. The hash resulting from the Block Header and a Proof of Work
     * (nonce), called the solution, must be less than the Target. This usually
     * causes a number of zero bits at the beginning of each hash, which is why
     * the hash of every generated Block starts with a specific number of zeros.
     * As the target decreases, the difficulty to generate each proof, and
     * therefore the time it takes to generate a Block, increases. Because the
     * beginning nonce is initialized with a secure random long (64-bit integer)
     * on each Node, finding a valid solution is essentially a lottery among
     * each Node in the network.<br>
     * <br>
     * A Proof of Work is implemented in order to prevent manipulation of the
     * BlockChain. The network can produce a new valid Block every few minutes
     * as it may have hundreds or thousands of Miner Nodes all working to find a
     * valid proof for a Block. This means generating a new Block for the
     * BlockChain is a minor inconvenience to the network, as it may take a few
     * minutes to generate a single Block. However, it is almost infeasible for
     * any one Node to generate a valid Block on its own. A target allowing
     * Blocks to generate every five minutes on the network, on average, could
     * take a single Node several decades, on average. Therefore, to manipulate
     * the BlockChain using falsely generated Blocks or to change previously
     * added Blocks would require infeasible amounts of processing power, in
     * addition to 51% of Nodes on the network (See 51% Attack).<br>
     * <br>
     * The network can be configured to generate Blocks after a fixed time, on
     * average. The target adjustment after Block generation can keep the
     * configured time between Blocks fixed by increasing or decreasing the
     * target. If more Miner Nodes were added to the network, Block generation
     * would be faster, on average. The target would be adjusted to then
     * increase the difficulty of each subsequent Block bringing the average
     * Block generation time back to the configured time. (The fixed time is an
     * average as Proof of Work generation is a lottery, and a Node's ability to
     * mine a valid hash is largely based on luck. Half the time a Node can
     * generate a Block faster than the desired time.)
     * 
     * @param bytes
     *            The Block Header in binary format
     * @param target
     *            The hash must be less than the target to be valid (increasing
     *            the difficulty to create a proof with the power and speed of
     *            the network).
     * @return Integer Proof of Work for the specified Block (Nonce to generate
     *         Block's hash)
     */
    public static int generateProof(byte[] bytes, int target)
    {
        try
        {
            logger.fine("Generating Hash ...");
            SecureRandom rnd = SecureRandom.getInstance("SHA1PRNG");
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            BigInteger digest = Block.getTarget(Block.MAXIMUM_TARGET);
            int nonce = rnd.nextInt();
            long time = System.currentTimeMillis();
            BigInteger tar = Block.getTarget(target);
            while (!validateHash(digest, tar))
            {
                nonce++;
                md.reset();
                md.update(bytes);
                md.update(ByteUtil.intToBytes(nonce));
                byte[] digestBytes = md.digest();
                md.reset();
                md.update(digestBytes);
                digest = new BigInteger(1, md.digest());
            }
            logger.fine("Proof generated in "
                + ((System.currentTimeMillis() - time) / 1000) + " seconds.");
            logger.fine("Nonce: " + nonce);
            logger.fine(generateLeadingZeros(digest.toString(16)));
            return nonce;
        }
        catch (NoSuchAlgorithmException e)
        {
            logger.log(Level.SEVERE, "generateProof", e);
        }
        return 0;
    }


    // -------------------------------------------------------------------------
    /**
     * Validates a hash against the specified target.
     * 
     * @param value
     *            The hash which must be validated
     * @param target
     *            The hash must be less than the target to be valid (increasing
     *            the difficulty to create a proof with the power and speed of
     *            the network).
     * @return True if the hash is valid (less than the target), False otherwise
     */
    private static boolean validateHash(BigInteger value, BigInteger target)
    {
        return value.compareTo(target) == -1;
    }


    // -------------------------------------------------------------------------
    /**
     * Generates a Merkle Root hash based on two other Merkle Roots. A Merkle
     * tree, also known as a Hash tree is a tree in which every non-leaf node is
     * labeled with the hash of the labels of its children nodes, or values of
     * leaf nodes. Merkle Roots allow efficient and secure verification of the
     * contents of large data structures. A change in one of the leaf nodes
     * would result in a completely different Merkle Root, as all changes would
     * be propagated in the tree.
     * 
     * @param root1
     *            First Merkle Root
     * @param root2
     *            Second Merkle Root
     * @return Merkle Root of root1 and root2
     */
    public static String generateMerkleRoot(String root1, String root2)
    {
        try
        {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.reset();
            md.update(root1.getBytes());
            md.update(root2.getBytes());
            return generateLeadingZeros(ByteUtil.bytesToHex(md.digest()));
        }
        catch (NoSuchAlgorithmException e)
        {
            logger.log(Level.SEVERE, "generateMerkleRoot", e);
        }
        return null;
    }


    // -------------------------------------------------------------------------
    /**
     * Returns the estimated time (in seconds) to generate a proof of work. This
     * method runs a simulation the first time it is called to accurately
     * predict the time to generate a hash given a difficulty.
     * 
     * @param difficulty
     *            Difficulty of block
     * @return Time in seconds to generate a proof of work
     */
    public static int estimateTime(double difficulty)
    {
        if (hashEst == -1)
        {
            try
            {
                int hashes = 0;
                long time = System.currentTimeMillis();
                MessageDigest md = MessageDigest.getInstance("SHA-256");
                while (System.currentTimeMillis() < time + 125)
                {
                    md.reset();
                    md.update(ByteUtil.intToBytes(0));
                    byte[] digest = md.digest();
                    md.reset();
                    md.update(digest);
                    md.digest();
                    hashes++;
                }
                hashes *= 8;
                byte[] bytes =
                    Block.getTarget(Block.MAXIMUM_TARGET).toByteArray();
                int zeros = (32 - bytes.length) * 8;
                for (int i = 0; bytes.length > i; i++)
                {
                    if (bytes[i] < (byte)0)
                    {
                        break;
                    }
                    else if (bytes[i] > (byte)63)
                    {
                        zeros += 1;
                        break;
                    }
                    else if (bytes[i] > (byte)31)
                    {
                        zeros += 2;
                        break;
                    }
                    else if (bytes[i] > (byte)15)
                    {
                        zeros += 3;
                        break;
                    }
                    else if (bytes[i] > (byte)7)
                    {
                        zeros += 4;
                        break;
                    }
                    else if (bytes[i] > (byte)3)
                    {
                        zeros += 5;
                        break;
                    }
                    else if (bytes[i] > (byte)1)
                    {
                        zeros += 6;
                        break;
                    }
                    else if (bytes[i] > (byte)0)
                    {
                        zeros += 7;
                        break;
                    }
                    else
                    {
                        zeros += 8;
                    }
                }
                hashEst = Math.pow(2, zeros) / hashes;
            }
            catch (NoSuchAlgorithmException e)
            {
                logger.log(Level.SEVERE, "estimateTime", e);
            }
        }
        return (int)(difficulty * hashEst);
    }

}
