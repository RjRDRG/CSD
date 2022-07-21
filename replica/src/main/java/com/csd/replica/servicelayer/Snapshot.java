package com.csd.replica.servicelayer;

import com.csd.replica.datalayer.BlockHeaderEntity;
import com.csd.replica.datalayer.ResourceEntity;

import java.io.Serializable;
import java.util.List;

public class Snapshot implements Serializable {

    private List<BlockHeaderEntity> blocks;
    private List<ResourceEntity> resources;

    public Snapshot(List<BlockHeaderEntity> blocks, List<ResourceEntity> resources) {
        this.blocks = blocks;
        this.resources = resources;
    }

    public Snapshot(List<ResourceEntity> resources) {
        this.resources = resources;
    }

    public List<ResourceEntity> getResources() {
        return resources;
    }

    public void setResources(List<ResourceEntity> resources) {
        this.resources = resources;
    }

    public List<BlockHeaderEntity> getBlocks() {
        return blocks;
    }

    public void setBlocks(List<BlockHeaderEntity> blocks) {
        this.blocks = blocks;
    }
}