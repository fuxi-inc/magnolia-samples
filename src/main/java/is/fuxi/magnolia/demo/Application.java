package is.fuxi.magnolia.demo;


import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
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

    static EntropyClient client;

    public static void main(String[] args) {
        Config conf = ConfigFactory.load();
        apiKey = conf.getString("apiKey");
        apiSecret = conf.getString("apiSecret");
        apiServerAddress = conf.getString("apiServerAddress");

        client = new EntropyClient(apiServerAddress, apiKey, apiSecret);

        // 演示如何使用标识相关的API接口
        IdentifierPlay play = new IdentifierPlay(client);
        try {
            //play.show();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        DomainPlay domainPlay = new DomainPlay(client);
        try {
            domainPlay.show();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        try {
            client.close();
        } catch (Exception e) {
            logger.warn("close resource failed, caused by {}", e);
        }
    }
}
