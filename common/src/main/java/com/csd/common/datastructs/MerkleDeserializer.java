package com.csd.common.datastructs;

import java.nio.ByteBuffer;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

import static com.csd.common.datastructs.MerkleTree.LEAF_SIG_TYPE;

/**
 * The Deserialization code was separated from the MerkleTree class.
public final class MerkleDeserializer {

    private MerkleDeserializer() {}

    public static MerkleTree deserialize(byte[] serializedTree) {
        ByteBuffer buf = ByteBuffer.wrap(serializedTree);

        if (buf.getInt() != MerkleTree.MAGIC_HDR) {
            throw new IllegalArgumentException("serialized byte array does not start with appropriate Magic Header");
        }

        int totnodes = buf.getInt();

        List<byte[]> leafSigs = new ArrayList<>((totnodes / 2) + 1);

        // read root
        MerkleTree.Node root = new MerkleTree.Node();
        root.type = buf.get();
        if (root.type == LEAF_SIG_TYPE) {
            throw new IllegalStateException("First serialized node is a leaf");
        }
        readNextSignature(buf, root);

        Queue<MerkleTree.Node> q = new ArrayDeque<>((totnodes / 2) + 1);
        MerkleTree.Node curr = root;

        int height = 0;
        int expNumNodes = 2;
        int nodesThisLevel = 0;
        for (int i = 1; i < totnodes; i++) {
            MerkleTree.Node child = new MerkleTree.Node();
            child.type = buf.get();
            readNextSignature(buf, child);
            q.add(child);

            if (child.type == LEAF_SIG_TYPE) {
                leafSigs.add(child.sig);
            }

            // handles incomplete tree where a node has been "promoted"
            if (signaturesEqual(child.sig,curr.sig)) {
                curr.left = child;
                curr = q.remove();
                expNumNodes *= 2;
                nodesThisLevel = 0;
                height++;
                continue;
            }

            nodesThisLevel++;
            if (curr.left == null) {
                curr.left = child;
            } else {
                curr.right = child;
                curr = q.remove();

                if (nodesThisLevel >= expNumNodes) {
                    expNumNodes *= 2;
                    nodesThisLevel = 0;
                    height++;
                }
            }
        }

        return new MerkleTree(root, totnodes, height, leafSigs);
    }

    static boolean signaturesEqual(byte[] sig, byte[] sig2) {
        if (sig.length != sig2.length) {
            return false;
        }
        for (int i = 0; i < sig.length; i++) {
            if (sig[i] != sig2[i]) {
                return false;
            }
        }
        return true;
    }

    static void readNextSignature(ByteBuffer buf, MerkleTree.Node nd) {
        byte[] sigBytes = new byte[buf.getInt()];
        buf.get(sigBytes);
        nd.sig = sigBytes;
    }
}

 */
