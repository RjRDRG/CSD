package com.csd.replica;

import com.csd.replica.consensuslayer.IConsensusLayer;
import com.csd.replica.consensuslayer.bftsmart.BftSmartReplica;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.security.Security;

@SpringBootApplication
public class ReplicaApplication implements CommandLineRunner {

	static {
		Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
	}

	private final IConsensusLayer consensusLayer;

	public ReplicaApplication(BftSmartReplica replica) {
		this.consensusLayer = replica;
	}

	public static void main(String[] args) {
		SpringApplication.run(ReplicaApplication.class, args);
	}

	@Override
	public void run(String... args) {
		consensusLayer.start(args);
	}
}
