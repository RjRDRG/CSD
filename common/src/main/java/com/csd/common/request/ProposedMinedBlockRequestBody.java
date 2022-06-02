package com.csd.common.request;

import com.csd.common.item.Block;

public class ProposedMinedBlockRequestBody implements IRequest {
    private Block block;

    public ProposedMinedBlockRequestBody(Block block) {
        this.block = block;
    }

    public ProposedMinedBlockRequestBody() {
    }

    public Block getBlock() {
        return block;
    }

    public void setBlock(Block block) {
        this.block = block;
    }

    @Override
    public Type type() {
        return Type.PROPOSE;
    }

    @Override
    public String toString() {
        return "ProposedMinedBlockRequestBody{" +
                "block=" + block +
                '}';
    }
}
