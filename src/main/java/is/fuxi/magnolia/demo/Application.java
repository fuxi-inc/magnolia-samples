package is.fuxi.magnolia.demo;


import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import is.fuxi.magnolia.TrustedLedgerClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import is.fuxi.magnolia.EntropyClient;

/**
 *
 */
public class Application {
    static Logger logger = LoggerFactory.getLogger(Application.class);
    static String apiServerAddress;
    static String apiKey;
    static String apiSecret;

    static EntropyClient entropyClient;
    static TrustedLedgerClient ledgerClient;

    public static void main(String[] args) {
        Config conf = ConfigFactory.load();
        apiKey = conf.getString("apiKey");
        apiSecret = conf.getString("apiSecret");
        apiServerAddress = conf.getString("apiServerAddress");

        entropyClient = new EntropyClient(apiServerAddress, apiKey, apiSecret);
        ledgerClient = new TrustedLedgerClient(apiServerAddress, apiKey, apiSecret);

        // 演示如何使用标识相关的API接口
        IdentifierPlay play = new IdentifierPlay(entropyClient);
        try {
            //play.show();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        DomainPlay domainPlay = new DomainPlay(entropyClient, ledgerClient);
        try {
            domainPlay.show();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        try {
            entropyClient.close();
            ledgerClient.close();
        } catch (Exception e) {
            logger.warn("close resource failed, caused by {}", e);
        }
    }
}
