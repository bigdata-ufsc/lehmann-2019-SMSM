package br.ufsc.lehmann.geocode.reverse;

import java.io.IOException;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.BoundRequestBuilder;
import org.asynchttpclient.DefaultAsyncHttpClient;
import org.asynchttpclient.Response;

import com.google.gson.Gson;

public class ReverseGeocoding {

	private static final Gson GSON = new Gson();

	public static void main(String[] args) {
		Place p = new ReverseGeocoding().fromLatLon(53.3724765, -6.49278733333334);
		System.out.println(p.getAddress().getRoad());
	}

	public Place fromLatLon(double lat, double lon) {
		DefaultAsyncHttpClient asyncHttpClient = new DefaultAsyncHttpClient();
		try {
			return fromLatLon(asyncHttpClient, lat, lon);
		} finally {
			asyncHttpClient.close();
		}
	}

	public Place fromLatLon(DefaultAsyncHttpClient asyncHttpClient, double lat, double lon) {
		Response r;
		try {
			BoundRequestBuilder get = asyncHttpClient.prepareGet(String.format(Locale.US,
					"http://nominatim.openstreetmap.org/reverse?format=json&lat=%.10f&lon=%.10f&zoom=18&addressdetails=1", lat, lon));
			get.addHeader("User-Agent", "node-nominatim");
			Future<Response> f = get.execute();
			r = f.get();
		} catch (InterruptedException | ExecutionException e) {
			return null;
		}
		String json = r.getResponseBody();
		if(json.startsWith("<html>")) {
			throw new IllegalArgumentException("Bandwidth limit exceeded");
		}
		return GSON.fromJson(json, Place.class);
	}
}
