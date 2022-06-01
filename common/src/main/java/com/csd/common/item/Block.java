package com.csd.common.item;

import java.time.OffsetDateTime;

public class Block {

    public static class Header {
        private OffsetDateTime timestamp;
        byte[] previousBlockHash;
        byte[] merkleRootHash;
        int difficultyTarget;
        byte[] nonce;
    }

    public Header header;
    public MerkleTree transactions;

}
