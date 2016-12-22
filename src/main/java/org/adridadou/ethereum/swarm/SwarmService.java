package org.adridadou.ethereum.swarm;


import org.adridadou.ethereum.values.smartcontract.SmartContractMetadata;
import org.adridadou.exception.EthereumApiException;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;
import org.apache.http.entity.ContentType;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;

/**
 * Created by davidroon on 21.12.16.
 * This code is released under Apache 2 license
 */
public class SwarmService {
    public final static String PUBLIC_HOST = "http://swarm-gateways.net";

    private final String host;

    public SwarmService(String host) {
        this.host = host;
    }

    public static SwarmService from(final String host) {
        return new SwarmService(host);
    }

    public SwarmHash publish(final String content) throws IOException {
        Response response = Request.Post(host + "/bzzr:/")
                .bodyString(content, ContentType.TEXT_PLAIN)
                .execute();

        return SwarmHash.of(response.returnContent().asString());
    }

    public String get(final SwarmHash hash) throws IOException {
        Response response = Request.Get(host + "/bzzr:/" + hash.toString())
                .execute();

        return response.returnContent().asString();
    }

    public SmartContractMetadata getMetadata(final SwarmHash hash) throws IOException {
        String metadata = get(hash);
        try {
            JSONParser parser = new JSONParser();
            JSONObject json = (JSONObject) parser.parse(metadata);

            return new SmartContractMetadata(((JSONObject) json.get("output")).get("abi").toString());
        } catch (ParseException e) {
            throw new EthereumApiException("error while parsing metadata", e);
        }
    }
}
