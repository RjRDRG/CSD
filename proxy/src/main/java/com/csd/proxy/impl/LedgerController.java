package com.csd.proxy.impl;

import com.csd.common.cryptography.config.ISuiteConfiguration;
import com.csd.common.cryptography.config.IniSpecification;
import com.csd.common.cryptography.config.StoredSecrets;
import com.csd.common.cryptography.config.SuiteConfiguration;
import com.csd.common.cryptography.key.KeyStoresInfo;
import com.csd.common.cryptography.suites.digest.FlexibleDigestSuite;
import com.csd.common.cryptography.suites.digest.IDigestSuite;
import com.csd.common.cryptography.suites.digest.SignatureSuite;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Collectors;

import static org.springframework.util.SerializationUtils.*;

@RestController
class LedgerController {

    public static final String CONFIG_PATH = "security.conf";

    //private final LedgerProxy ledgerProxy;
    //private final TransactionRepository ledger;
    //private final TestimonyRepository testimonies;

    private final IDigestSuite clientIdDigestSuite;
    private final SignatureSuite clientSignatureSuite;

    LedgerController(LedgerProxy ledgerProxy, TransactionRepository ledger, TestimonyRepository testimonies) throws Exception {
        this.ledgerProxy = ledgerProxy;
        this.testimonies = testimonies;
        this.ledger = ledger;
        ISuiteConfiguration suiteConfiguration =
                new SuiteConfiguration(
                        new IniSpecification("client_id_digest_suite", CONFIG_PATH),
                        new StoredSecrets(new KeyStoresInfo("stores",CONFIG_PATH))
                );
        this.clientIdDigestSuite = new FlexibleDigestSuite(suiteConfiguration, SignatureSuite.Mode.Verify);
        this.clientSignatureSuite = new SignatureSuite(new IniSpecification("client_signature_suite", CONFIG_PATH));
    }

    @PostMapping("/obtain")
    public Transaction obtainValueTokens(@RequestBody OrderedRequest<ObtainRequestBody> request) {

        boolean valid;
        try {
            valid = request.verifyClientId(clientIdDigestSuite) && request.verifySignature(clientSignatureSuite);
        } catch (Exception e) {
            throw new ForbiddenException(e.getMessage());
        }

        if(!valid) throw new ForbiddenException("Invalid Signature");

        ReplicatedRequest replicatedRequest = new ReplicatedRequest(
                LedgerOperation.OBTAIN,
                serialize(request),
                getLastTransactionId()
        );

        ReplicaReply replicaReply;
        try{
            replicaReply = ledgerProxy.invokeOrdered(replicatedRequest);
            if(!replicaReply.getMissingEntries().isEmpty()) {
                ledger.saveAll(replicaReply.getMissingEntries().stream().map(TransactionEntity::new).collect(Collectors.toList()));
            }
        } catch (Exception e) {
            throw new ServerErrorException(e.getMessage());
        }

        Result<Transaction> result = replicaReply.extractReply();
        throwPossibleException(result);

        return result.value();
    }

    @PostMapping("/transfer")
    public Transaction transferValueTokens(@RequestBody OrderedRequest<TransferRequestBody> request) {

        boolean valid;
        try {
            valid = request.verifyClientId(clientIdDigestSuite) && request.verifySignature(clientSignatureSuite);
        } catch (Exception e) {
            throw new ForbiddenException(e.getMessage());
        }

        if(!valid) throw new ForbiddenException("Invalid Signature");

        if(request.getRequestBody().extractData().getAmount()<0) throw new BadRequestException("Amount must be positive");

        ReplicatedRequest replicatedRequest = new ReplicatedRequest(
                LedgerOperation.TRANSFER,
                serialize(request),
                getLastTransactionId()
        );

        ReplicaReply replicaReply;
        try{
            replicaReply = ledgerProxy.invokeOrdered(replicatedRequest);
            if(!replicaReply.getMissingEntries().isEmpty()) {
                ledger.saveAll(replicaReply.getMissingEntries().stream().map(TransactionEntity::new).collect(Collectors.toList()));
            }
        } catch (Exception e) {
            throw new ServerErrorException(e.getMessage());
        }

        Result<Transaction> result = replicaReply.extractReply();
        throwPossibleException(result);

        return result.value();
    }

    @GetMapping("/balance/{clientId}")
    public Double consultBalance(@PathVariable String clientId) {

        ReplicatedRequest replicatedRequest = new ReplicatedRequest(
                LedgerOperation.BALANCE,
                serialize(clientId),
                getLastTransactionId()
        );

        ReplicaReply replicaReply;
        try{
            replicaReply = ledgerProxy.invokeUnordered(replicatedRequest);
            if(!replicaReply.getMissingEntries().isEmpty()) {
                ledger.saveAll(replicaReply.getMissingEntries().stream().map(TransactionEntity::new).collect(Collectors.toList()));
            }
        } catch (Exception e) {
            throw new ServerErrorException(e.getMessage());
        }

        Result<Double> result = replicaReply.extractReply();
        System.out.println(result);
        throwPossibleException(result);

        return result.value();
    }

    @GetMapping("/transactions")
    public Transaction[] allTransactions() {

        ReplicatedRequest replicatedRequest = new ReplicatedRequest(
                LedgerOperation.ALL_TRANSACTIONS,
                getLastTransactionId()
        );

        ReplicaReply replicaReply;
        try{
            replicaReply = ledgerProxy.invokeUnordered(replicatedRequest);
            if(!replicaReply.getMissingEntries().isEmpty()) {
                ledger.saveAll(replicaReply.getMissingEntries().stream().map(TransactionEntity::new).collect(Collectors.toList()));
            }
        } catch (Exception e) {
            throw new ServerErrorException(e.getMessage());
        }

        Result<Transaction[]> result = replicaReply.extractReply();
        throwPossibleException(result);

        return result.value();
    }

    @GetMapping("/transactions/{clientId}")
    public Transaction[] clientTransactions(@PathVariable String clientId) {

        ReplicatedRequest replicatedRequest = new ReplicatedRequest(
                LedgerOperation.CLIENT_TRANSACTIONS,
                serialize(clientId),
                getLastTransactionId()
        );

        ReplicaReply replicaReply;
        try{
            replicaReply = ledgerProxy.invokeUnordered(replicatedRequest);
            if(!replicaReply.getMissingEntries().isEmpty()) {
                ledger.saveAll(replicaReply.getMissingEntries().stream().map(TransactionEntity::new).collect(Collectors.toList()));
            }
        } catch (Exception e) {
            throw new ServerErrorException(e.getMessage());
        }

        Result<Transaction[]> result = replicaReply.extractReply();
        throwPossibleException(result);

        return result.value();
    }

    @GetMapping("/testimonies/{requestId}")
    public Testimony[] consultTestimonies(@PathVariable long requestId) {
        try {
            Testimony[] t = testimonies.findByRequestId(requestId).stream().map(TestimonyEntity::toItem).toArray(Testimony[]::new);
            if (t.length == 0)
                throw new NotFoundException("Transaction Not Found");
            else
                return t;
        } catch (Exception e) {
            throw new ServerErrorException(e.getMessage());
        }
    }

    private long getLastTransactionId() {
        long lastId = 0L;
        List<TransactionEntity> last = ledger.findTopByOrderByIdDesc();
        if(!last.isEmpty()) lastId = last.get(0).getId();
        return lastId;
    }
}
