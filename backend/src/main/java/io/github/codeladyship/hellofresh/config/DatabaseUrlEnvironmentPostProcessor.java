package io.github.codeladyship.hellofresh.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Neon (and Render) hand out connection strings like
 * {@code postgresql://user:password@host/dbname?sslmode=require}, which JDBC cannot use directly.
 * If {@code DATABASE_URL} is set and {@code JDBC_DATABASE_URL} is not, this converts it into
 * {@code JDBC_DATABASE_URL}, {@code JDBC_DATABASE_USERNAME} and {@code JDBC_DATABASE_PASSWORD}.
 */
public class DatabaseUrlEnvironmentPostProcessor implements EnvironmentPostProcessor {

    static final String SOURCE_NAME = "databaseUrlConversion";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        String databaseUrl = environment.getProperty("DATABASE_URL");
        if (!StringUtils.hasText(databaseUrl) || StringUtils.hasText(environment.getProperty("JDBC_DATABASE_URL"))) {
            return;
        }
        environment.getPropertySources().addFirst(new MapPropertySource(SOURCE_NAME, convert(databaseUrl)));
    }

    static Map<String, Object> convert(String databaseUrl) {
        URI uri = URI.create(databaseUrl.trim());
        String scheme = uri.getScheme();
        if (!"postgres".equals(scheme) && !"postgresql".equals(scheme)) {
            throw new IllegalArgumentException("DATABASE_URL must start with postgres:// or postgresql://");
        }
        StringBuilder jdbc = new StringBuilder("jdbc:postgresql://").append(uri.getHost());
        if (uri.getPort() > 0) {
            jdbc.append(':').append(uri.getPort());
        }
        jdbc.append(uri.getRawPath());
        String query = uri.getRawQuery();
        if (query == null || !query.contains("sslmode=")) {
            query = (query == null || query.isEmpty() ? "" : query + "&") + "sslmode=require";
        }
        jdbc.append('?').append(query);

        Map<String, Object> props = new HashMap<>();
        props.put("JDBC_DATABASE_URL", jdbc.toString());
        String userInfo = uri.getRawUserInfo();
        if (userInfo != null) {
            int colon = userInfo.indexOf(':');
            String user = colon >= 0 ? userInfo.substring(0, colon) : userInfo;
            props.put("JDBC_DATABASE_USERNAME", URLDecoder.decode(user, StandardCharsets.UTF_8));
            if (colon >= 0) {
                props.put("JDBC_DATABASE_PASSWORD",
                        URLDecoder.decode(userInfo.substring(colon + 1), StandardCharsets.UTF_8));
            }
        }
        return props;
    }
}
