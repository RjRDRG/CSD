package com.csd.common.item;

import com.csd.common.traits.Signature;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static com.csd.common.util.Serialization.dataToJson;

public class Transaction implements Serializable {

    public static class Value implements Serializable {
        private int id;
        private byte[] owner;
        private double value;

        public Value(int id, byte[] owner, double value) {
            this.id = id;
            this.owner = owner;
            this.value = value;
        }

        public Value() {
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public byte[] getOwner() {
            return owner;
        }

        public void setOwner(byte[] owner) {
            this.owner = owner;
        }

        public double getValue() {
            return value;
        }

        public void setValue(double value) {
            this.value = value;
        }
    }

    private Long block;
    private Long txid;
    private List<Value> inputs;
    private List<Value> outputs;
    private OffsetDateTime timestamp;
    private byte[] requestSignature;
    private byte[] serializedRequest;

    public Transaction(Long block, Long txid, List<Value> inputs, List<Value> outputs, OffsetDateTime timestamp, byte[] requestSignature, byte[] serializedRequest) {
        this.block = block;
        this.txid = txid;
        this.inputs = inputs;
        this.outputs = outputs;
        this.timestamp = timestamp;
        this.requestSignature = requestSignature;
        this.serializedRequest = serializedRequest;
    }

    public Transaction(List<Value> inputs, List<Value> outputs, OffsetDateTime timestamp, byte[] requestSignature, byte[] serializedRequest) {
        this.inputs = inputs;
        this.outputs = outputs;
        this.timestamp = timestamp;
        this.requestSignature = requestSignature;
        this.serializedRequest = serializedRequest;
    }

    public Transaction() {
    }

    public Long getBlock() {
        return block;
    }

    public void setBlock(Long block) {
        this.block = block;
    }

    public Long getTxid() {
        return txid;
    }

    public void setTxid(Long txid) {
        this.txid = txid;
    }

    public List<Value> getInputs() {
        return inputs;
    }

    public void setInputs(List<Value> inputs) {
        this.inputs = inputs;
    }

    public List<Value> getOutputs() {
        return outputs;
    }

    public void setOutputs(List<Value> outputs) {
        this.outputs = outputs;
    }

    public OffsetDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(OffsetDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public byte[] getRequestSignature() {
        return requestSignature;
    }

    public void setRequestSignature(byte[] requestSignature) {
        this.requestSignature = requestSignature;
    }

    public byte[] getSerializedRequest() {
        return serializedRequest;
    }

    public void setSerializedRequest(byte[] serializedRequest) {
        this.serializedRequest = serializedRequest;
    }

    @Override
    public String toString() {
        return "\nTransaction " + dataToJson(this);
    }
}