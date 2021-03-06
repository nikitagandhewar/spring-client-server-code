package com.scriptuit.ConfigServer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController

public class Json_Reader {

	ExecutorService executorService = Executors.newFixedThreadPool(20);

	@RequestMapping(value = "/auto-refresh", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
	public void readData(@RequestBody List<String> clientList)
			throws FileNotFoundException, IOException, ParseException {

		JSONParser parser = new JSONParser();

		JSONObject jsonObject = (JSONObject) parser.parse(new FileReader("/opt/Client/ClientProperties.json"));

		Set<String> set = jsonObject.keySet();

		List<String> list = null;

		if (null == clientList || clientList.isEmpty()) {
			list = new ArrayList<>(set);
		} else {
			list = clientList;
		}

		for (int i = 0; i < list.size(); i++) {

			HashMap<String, String> obj = (HashMap<String, String>) jsonObject.get(list.get(i));
			String host = obj.get("host");
			String port = obj.get("port");
			String username = obj.get("username");
			String password = obj.get("password");

			Runnable worker = new MyRunnable(host, port, username, password);
			executorService.execute(worker);

		}

	}

	public static class MyRunnable implements Runnable {
		String host;
		String port;
		String username;
		String password;

		MyRunnable(String host, String port, String username, String password) {
			this.host = host;
			this.port = port;
			this.username = username;
			this.password = password;
		}

		@Override
		public void run() {

			System.out.println("Client is refreshing using this thread : " + Thread.currentThread().getName());

			String restUrl = "http://" + host + ":" + port + "/actuator/refresh";

			try {
				HttpPost post = new HttpPost(restUrl);
				post.setHeader("Content-Type", "application/json");

				if (username != null && password != null) {

					String authString = username + ":" + password;

					byte[] authEncBytes = Base64.encodeBase64(authString.getBytes());
					String authStringEnc = new String(authEncBytes);

					post.setHeader("Authorization", "Basic " + authStringEnc);
				}

				HttpClient client1 = HttpClientBuilder.create().build();
				HttpResponse response = client1.execute(post);

				int responseCode = response.getStatusLine().getStatusCode();
				System.out.println("Response Code : " + responseCode);
			} catch (Exception e) {
				System.out.println("Exception occurred at host : " + host + " and port : " + port);
			}
		}
	}

}