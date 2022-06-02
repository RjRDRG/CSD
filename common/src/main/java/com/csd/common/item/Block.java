package com.csd.common.item;

import org.rebaze.integrity.tree.Tree;

import java.time.OffsetDateTime;
import java.util.Arrays;

public class Block {

    public static class Header {
        private OffsetDateTime timestamp;
        byte[] previousBlockHash;
        byte[] merkleRootHash;
        int difficultyTarget;
        byte[] nonce;

        public Header(OffsetDateTime timestamp, byte[] previousBlockHash, byte[] merkleRootHash, int difficultyTarget, byte[] nonce) {
            this.timestamp = timestamp;
            this.previousBlockHash = previousBlockHash;
            this.merkleRootHash = merkleRootHash;
            this.difficultyTarget = difficultyTarget;
            this.nonce = nonce;
        }

        public Header() {
        }

        public OffsetDateTime getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(OffsetDateTime timestamp) {
            this.timestamp = timestamp;
        }

        public byte[] getPreviousBlockHash() {
            return previousBlockHash;
        }

        public void setPreviousBlockHash(byte[] previousBlockHash) {
            this.previousBlockHash = previousBlockHash;
        }

        public byte[] getMerkleRootHash() {
            return merkleRootHash;
        }

        public void setMerkleRootHash(byte[] merkleRootHash) {
            this.merkleRootHash = merkleRootHash;
        }

        public int getDifficultyTarget() {
            return difficultyTarget;
        }

        public void setDifficultyTarget(int difficultyTarget) {
            this.difficultyTarget = difficultyTarget;
        }

        public byte[] getNonce() {
            return nonce;
        }

        public void setNonce(byte[] nonce) {
            this.nonce = nonce;
        }

        @Override
        public String toString() {
            return "Header{" +
                    "timestamp=" + timestamp +
                    ", previousBlockHash=" + Arrays.toString(previousBlockHash) +
                    ", merkleRootHash=" + Arrays.toString(merkleRootHash) +
                    ", difficultyTarget=" + difficultyTarget +
                    ", nonce=" + Arrays.toString(nonce) +
                    '}';
        }
    }

    public Header header;
    public Tree transactions;

    public Block(Header header, Tree transactions) {
        this.header = header;
        this.transactions = transactions;
    }

    public Block() {
    }

    public Header getHeader() {
        return header;
    }

    public void setHeader(Header header) {
        this.header = header;
    }

    public Tree getTransactions() {
        return transactions;
    }

    public void setTransactions(Tree transactions) {
        this.transactions = transactions;
    }

    @Override
    public String toString() {
        return "Block{" +
                "header=" + header +
                ", transactions=" + transactions +
                '}';
    }
}
