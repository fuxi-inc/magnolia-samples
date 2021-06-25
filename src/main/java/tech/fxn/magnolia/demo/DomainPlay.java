package tech.fxn.magnolia.demo;

import com.github.javafaker.Faker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xbill.DNS.TextParseException;
import tech.fxn.magnolia.EntropyClient;
import tech.fxn.magnolia.v1.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 *
 */
public class DomainPlay {
    private static Logger logger = LoggerFactory.getLogger(DomainPlay.class);
    EntropyClient client;

    private Faker faker = new Faker();
    private String domainName;

    public DomainPlay(EntropyClient client) {
        this.client = client;
    }


    public void show() throws TextParseException {
        // 查找可用的Namespace
        Namespace namespace = listAvailableNamespaces();
        // 激活某个Namespace，准备在此Namespace下注册和管理标识
        activateAndConfirmCurrentNamespaces(namespace);
        // 初始化域名
        domainName = faker.internet().domainWord() + "." + namespace.getId();
        // 注册域名
        createDomain();
        // 查看注册成功的域名
        getDomain();
        // 管理域名解析记录
        addDomainResolutionRecord();
        // 再次查询，确认解析记录都存在
        getDomain();
        // 删除域名
        deleteDomain();
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
        NamespaceResponse response = client.getStub().currentNamespace(GeneralRequest.getDefaultInstance());
        assertThat(response.getResult().getStatusCode()).isEqualTo(200);
        logger.info("current namespace: {}", response.getData().getId());
    }

    private void createDomain() {
        CreateDomainRequest request = CreateDomainRequest.newBuilder()
                .setName(domainName)
                .setRegistrant(faker.internet().domainName())
                .build();
        GeneralResponse response = client.getStub().createDomain(request);
        assertThat(response.getResult().getStatusCode()).isEqualTo(200);
        logger.info("create domain:{} successfully", domainName);
    }

    private Domain getDomain() {
        DomainRequest request = DomainRequest.newBuilder()
                .setName(domainName)
                .build();
        DomainResponse response = client.getStub().getDomain(request);
        assertThat(response.getResult().getStatusCode()).isEqualTo(200);
        logger.info("get domain:{} successfully", response.getData());
        return response.getData();
    }

    private void addDomainResolutionRecord() throws TextParseException {
        // 增加解析记录： A
        CreateDomainResolutionRecordRequest createDomainResolutionRecordRequest = CreateDomainResolutionRecordRequest.newBuilder()
                .setName("a." + domainName)
                .setDomain(domainName)
                .setType(DomainResolutionRecordType.A)
                .setRr(RRData.newBuilder().setValue("192.168.1.1").build())
                .build();
        DomainResolutionRecordResponse response = client.getStub().addDomainResolutionRecord(createDomainResolutionRecordRequest);
        assertThat(response.getResult().getStatusCode()).isEqualTo(200);
        logger.info("add domain a rr:{} successfully", response.getData());

        // 增加解析记录: CNAME`
        createDomainResolutionRecordRequest = CreateDomainResolutionRecordRequest.newBuilder()
                .setName("c." + domainName)
                .setDomain(domainName)
                .setType(DomainResolutionRecordType.CNAME)
                .setRr(RRData.newBuilder().setValue("www.baidu.com").build())
                .build();
        response = client.getStub().addDomainResolutionRecord(createDomainResolutionRecordRequest);
        assertThat(response.getResult().getStatusCode()).isEqualTo(200);
        logger.info("add domain cname rr:{} successfully", response.getData());

        // 增加解析记录: TXT
        createDomainResolutionRecordRequest = CreateDomainResolutionRecordRequest.newBuilder()
                .setName("txt." + domainName)
                .setDomain(domainName)
                .setType(DomainResolutionRecordType.TXT)
                .setRr(RRData.newBuilder().setValue("Nevermind").build())
                .build();
        response = client.getStub().addDomainResolutionRecord(createDomainResolutionRecordRequest);
        assertThat(response.getResult().getStatusCode()).isEqualTo(200);
        logger.info("add domain txt rr:{} successfully", response.getData());

        // 增加解析记录: AAAA
        createDomainResolutionRecordRequest = CreateDomainResolutionRecordRequest.newBuilder()
                .setName("aaaa." + domainName)
                .setDomain(domainName)
                .setType(DomainResolutionRecordType.AAAA)
                .setRr(RRData.newBuilder().setValue("FC00:0:130F:0:0:9C0:876A:130B").build())
                .build();
        response = client.getStub().addDomainResolutionRecord(createDomainResolutionRecordRequest);
        assertThat(response.getResult().getStatusCode()).isEqualTo(200);
        logger.info("add domain aaaa rr:{} successfully", response.getData());

        // 增加解析记录: MX
        createDomainResolutionRecordRequest = CreateDomainResolutionRecordRequest.newBuilder()
                .setName(domainName)
                .setDomain(domainName)
                .setType(DomainResolutionRecordType.MX)
                .setMx(MXData.newBuilder().setMx("ALT1.ASPMX.L.GOOGLE.COM.").setPreference(1).build())
                .build();
        response = client.getStub().addDomainResolutionRecord(createDomainResolutionRecordRequest);
        assertThat(response.getResult().getStatusCode()).isEqualTo(200);
        logger.info("add domain mx rr:{} successfully", response.getData());

        // 增加解析记录: NS
        createDomainResolutionRecordRequest = CreateDomainResolutionRecordRequest.newBuilder()
                .setName(domainName)
                .setDomain(domainName)
                .setType(DomainResolutionRecordType.NS)
                .setRr(RRData.newBuilder().setValue("ns.google.com").build())
                .build();
        response = client.getStub().addDomainResolutionRecord(createDomainResolutionRecordRequest);
        assertThat(response.getResult().getStatusCode()).isEqualTo(200);
        logger.info("add domain ns rr:{} successfully", response.getData());

        GeneralResponse generalResponse = client.getStub().deleteDomainResolutionRecord(DomainResolutionRecordRequest.newBuilder().setId(response.getData().getId()).build());
        assertThat(generalResponse.getResult().getStatusCode()).isEqualTo(200);
        logger.info("delete domain ns rr:{} successfully", response.getData());

    }

    private void deleteDomain() {
        DomainRequest request = DomainRequest.newBuilder()
                .setName(domainName)
                .build();
        GeneralResponse response = client.getStub().deleteDomain(request);
        assertThat(response.getResult().getStatusCode()).isEqualTo(200);
        logger.info("delete domain:{} successfully", domainName);
    }

}
