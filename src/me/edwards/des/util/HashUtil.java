package me.edwards.des.util;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.logging.Level;
import java.util.logging.Logger;
import me.edwards.des.block.Block;

/**
 * Utility class for generating Hashes for the HashCash Proof of Work algorithm
 * Created on: Oct 16, 2015 at 5:30:31 PM
 * 
 * @author Matthew Edwards
 */
public class HashUtil
{
    private static final Logger logger = Logger.getLogger("DES.hashutil");
    private static int          hashes = 0;


    /**
     * Generates a hash string with the correct number of leading zeros (64
     * digits by default)
     * 
     * @param hash
     *            String to add leading zeros to
     * @return
     */
    public static String generateLeadingZeros(String hash)
    {
        return generateLeadingZeros(hash, 64);
    }


    /**
     * Generates a hash string with the correct number of leading zeros
     * 
     * @param hash
     *            String to add leading zeros to
     * @param digits
     *            Number of digits to match
     * @return
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


    /**
     * Generates a block hash from a given block
     * 
     * @param bytes
     * @param proof
     * @return
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


    /**
     * Generates a hash from given data
     * 
     * @param bytes
     * @return
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


    /**
     * Validates a generated proof of work
     * 
     * @param bytes
     *            The block in binary format
     * @param proof
     *            Proof integer value
     * @param target
     *            Number of valid zero bits
     * @return True if the proof is valid
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
            return validateDigest(
                new BigInteger(1, md.digest()),
                Block.getTarget(target));
        }
        catch (NoSuchAlgorithmException e)
        {
            logger.log(Level.SEVERE, "validateProof", e);
        }
        return false;
    }


    /**
     * Generates a proof of work
     * 
     * @param bytes
     *            The block in binary format
     * @param target
     *            Number of valid zeros
     * @return Proof integer value (nonce to generate target hash)
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
            while (!validateDigest(digest, tar))
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


    private static boolean validateDigest(BigInteger value, BigInteger target)
    {
        return value.compareTo(target) == -1;
    }


    /**
     * Generates a merkle root based on two other roots
     * 
     * @param root1
     * @param root2
     * @return
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


    /**
     * Returns the estimated time (in seconds) to generate a proof of work.
     * 
     * @param difficulty
     *            Difficulty of block
     * @return Time in seconds to generate a proof of work
     */
    public static int estimateTime(double difficulty)
    {
        if (hashes == 0)
        {
            try
            {
                long time = System.currentTimeMillis();
                MessageDigest md = MessageDigest.getInstance("SHA-256");
                while (System.currentTimeMillis() < time + 5000)
                {
                    md.reset();
                    md.update(ByteUtil.intToBytes(0));
                    byte[] digest = md.digest();
                    md.reset();
                    md.update(digest);
                    md.digest();
                    hashes++;
                }
                hashes /= 5;
            }
            catch (NoSuchAlgorithmException e)
            {
                logger.log(Level.SEVERE, "estimateTime", e);
            }
        }
        return (int)(difficulty * Math.pow(2, 32) / hashes);
    }

}
