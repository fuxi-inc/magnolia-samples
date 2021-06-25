package tech.fxn.magnolia.demo;

import com.github.javafaker.Faker;
import com.google.protobuf.ByteString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.fxn.magnolia.EntropyClient;
import tech.fxn.magnolia.v1.*;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.assertj.core.api.Assertions.*;

/**
 *
 */
public class IdentifierPlay {
    private static Logger logger = LoggerFactory.getLogger(IdentifierPlay.class);
    private EntropyClient client;
    private Faker faker = new Faker();
    private String identityIdentifierName;
    private List<String> dataIdentifierNames = new ArrayList<>();
    private KeyPair pair;
    private String[] movies = new String[]{"china", "us", "england", "india", "other"};
    private String[] movieTypes = new String[]{"comic", "documentary", "science fiction", "comedy"};

    public IdentifierPlay(EntropyClient client) {
        this.client = client;
        ;
    }


    public void show() throws UnsupportedEncodingException, NoSuchAlgorithmException, SignatureException, NoSuchProviderException, InvalidKeyException {

        // 查找可用的Namespace
        Namespace namespace = listAvailableNamespaces();
        // 激活某个Namespace，准备在此Namespace下注册和管理标识
        activateAndConfirmCurrentNamespaces(namespace);
        // 初始化身份标识名称
        identityIdentifierName = faker.internet().domainWord() + "." + namespace.getId();
        // 初始化数据标识名称
        for (int i = 0; i < 5; i++) {
            dataIdentifierNames.add(faker.internet().domainWord() + faker.name().suffix() + "." + namespace.getId());
        }
        // 注册身份标识
        IdentityIdentifier identityIdentifier = createIdentityIdentifier();
        // 查看注册成功的身份标识
        getIdentityIdentifier(identityIdentifier);
        // 创建数据标识
        crreateDataIdentifier();
        // 查询数据标识
        testGetDataIdentifier();
        // 根据标签查找数据标识
        findDataIdentifierByTag();
        // 根据Metadata查找数据标识
        findDataIdentifierByMetadata();
        // 删除数据标识
        deleteDataIdentifier();
        // 注销身份标识
        deleteIdentityIdentifier();
    }

    private Namespace listAvailableNamespaces() {
        NamespacesResponse resp = client.getStub().listNamespace(GeneralPaginationRequest.getDefaultInstance());
        assertThat(resp.getResult().getStatusCode()).isEqualTo(200);
        assertThat(resp.getDataCount() >= 1).isTrue();
        logger.info("current available namespace list:\n{}", resp.getDataList());
        return resp.getData(0);
    }

    private void activateAndConfirmCurrentNamespaces(Namespace namespace) {
        GeneralResponse generalResponse = client.getStub().activateNamespace(NamespaceRequest.newBuilder().setName(namespace.getId()).build());
        assertThat(generalResponse.getResult().getStatusCode()).isEqualTo(200);
        logger.info("activate the namespace {}", namespace.getId());
        NamespaceResponse resp = client.getStub().currentNamespace(GeneralRequest.getDefaultInstance());
        assertThat(resp.getResult().getStatusCode()).isEqualTo(200);
        logger.info("current namespace: {}", resp.getData().getId());
    }

    private IdentityIdentifier createIdentityIdentifier() throws UnsupportedEncodingException, NoSuchAlgorithmException, NoSuchProviderException, InvalidKeyException, SignatureException {
        // 建议使用Solid的WebID作为身份标识的ID
        String id = faker.internet().url();
        String email = faker.internet().emailAddress();

        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        pair = keyGen.generateKeyPair();
        PrivateKey priv = pair.getPrivate();
        PublicKey pub = pair.getPublic();

        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initSign(priv);
        signature.update((id + identityIdentifierName + email).getBytes(StandardCharsets.UTF_8));
        byte[] rawSignature = signature.sign();
        final String converted = new String(rawSignature, StandardCharsets.UTF_8);
        final byte[] sig = converted.getBytes(StandardCharsets.UTF_8);

        CreateIdentityIdentifierRequest request = CreateIdentityIdentifierRequest.newBuilder()
                .setId(id)
                .setName(identityIdentifierName)
                .setEmail(faker.internet().emailAddress())
                .setSignature(ByteString.copyFrom(sig))
                .setPublicKey(ByteString.copyFrom(pub.getEncoded()))
                .build();
        IdentityIdentifierResponse resp = client.getStub().createIdentityIdentifier(request);
        assertThat(resp.getResult().getStatusCode()).isEqualTo(200);
        logger.info("create identity identifier {} succesfully", identityIdentifierName);
        return resp.getData();
    }

    public void getIdentityIdentifier(IdentityIdentifier identityIdentifier) {
        IdentityIdentifierRequest request = IdentityIdentifierRequest.newBuilder()
                .setName(identityIdentifierName)
                .build();
        IdentityIdentifierResponse response = client.getStub().getIdentityIdentifier(request);
        assertThat(response.getResult().getStatusCode()).isEqualTo(200);
        assertThat(response.getData().getName()).isEqualTo(identityIdentifier.getName());
    }


    private void deleteIdentityIdentifier() {
        IdentityIdentifierRequest request = IdentityIdentifierRequest.newBuilder()
                .setName(identityIdentifierName)
                .build();
        GeneralResponse resp = client.getStub().deleteIdentityIdentifier(request);
        assertThat(resp.getResult().getStatusCode()).isEqualTo(200);
        logger.info("delete identity identifier {} succesfully", identityIdentifierName);
    }

    public void crreateDataIdentifier() throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        String dataDigest = faker.crypto().sha256();
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initSign(pair.getPrivate());
        signature.update((dataDigest).getBytes(StandardCharsets.UTF_8));
        byte[] rawSignature = signature.sign();
        final String converted = new String(rawSignature, StandardCharsets.UTF_8);
        final byte[] sig = converted.getBytes(StandardCharsets.UTF_8);

        final Random random = new Random();
        for (String dataIdentifierName : dataIdentifierNames) {
            int idx = random.nextInt(movies.length - 1);
            int ict = random.nextInt(movieTypes.length - 1);
            CreateDataIdentifierRequest request = CreateDataIdentifierRequest.newBuilder()
                    .setName(dataIdentifierName)
                    .setDataAddress(faker.internet().url())
                    .setDataDigest(dataDigest)
                    .setDataSignature(ByteString.copyFrom(sig))
                    .setAuthAddress(faker.internet().url())
                    .setOwner(identityIdentifierName)
                    .addTags("movie")
                    .addTags(movies[idx])
                    .putMetadata("name", faker.lordOfTheRings().character())
                    .putMetadata("type", movieTypes[ict])
                    .build();
            DataIdentifierResponse response = client.getStub().createDataIdentifier(request);
            assertThat(response.getResult().getStatusCode()).isEqualTo(200);
            logger.info("create data identifier {} with tag:{} and metadata:{} succesfully", dataIdentifierName, movies[idx], movieTypes[ict]);
        }

    }

    private void testGetDataIdentifier() {
        for (String dataIdentifierName : dataIdentifierNames) {
            DataIdentifierRequest request = DataIdentifierRequest.newBuilder()
                    .setName(dataIdentifierName)
                    .build();
            DataIdentifierResponse response = client.getStub().getDataIdentifier(request);
            assertThat(response.getResult().getStatusCode()).isEqualTo(200);
            logger.info("get data identifier {} succesfully", response.getData());
        }
    }

    private void findDataIdentifierByTag() {
        String tag = movies[0];
        FindDataIdentifierByTagRequest request = FindDataIdentifierByTagRequest.newBuilder()
                .setTag(tag)
                .build();
        DataIdentifiersResponse response = client.getStub().findDataIdentifierByTag(request);
        assertThat(response.getResult().getStatusCode()).isEqualTo(200);
        assertThat(response.getDataCount() >= 1).isTrue();
        logger.info("find {} data identifier by tags: {}", response.getDataCount(), tag);
    }

    private void findDataIdentifierByMetadata() {
        final Random random = new Random();
        Criteria criteria = Criteria.newBuilder()
                .addCriterions(Criterion.newBuilder()
                        .setKey("type")
                        .setValue(movieTypes[random.nextInt(movieTypes.length - 1)])
                        .build())
                .addCriterions(Criterion.newBuilder()
                        .setKey("type")
                        .setValue(movieTypes[random.nextInt(movieTypes.length - 1)])
                        .build())
                .setLogicalType(LogicalOperator.OR)
                .build();

        FindDataIdentifierByMetadataRequest request = FindDataIdentifierByMetadataRequest.newBuilder()
                .setCriteria(criteria)
                .build();
        DataIdentifiersResponse response = client.getStub().findDataIdentifierByMetadata(request);
        assertThat(response.getResult().getStatusCode()).isEqualTo(200);
        logger.info("find {} data identifier by metadata: {}", response.getDataCount(), criteria);
    }

    private void deleteDataIdentifier() {
        for (String dataIdentifierName : dataIdentifierNames) {
            DataIdentifierRequest request = DataIdentifierRequest.newBuilder()
                    .setName(dataIdentifierName)
                    .build();
            GeneralResponse response = client.getStub().deleteDataIdentifier(request);
            assertThat(response.getResult().getStatusCode()).isEqualTo(200);
            logger.info("delete data identifier {} succesfully", dataIdentifierName);
        }

    }
}
