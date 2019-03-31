package hk.siggi.bungeecord.bungeechat.util;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class Prowl {

	private Prowl() {
	}

	public static void sendNotification(String apiKey, String application, String event, String description) {
		if (apiKey == null || application == null || (event == null && description == null)) {
			throw new NullPointerException("apiKey cannot be null, application cannot be null, event and description cannot be both null");
		}
		StringBuilder postDataSB = new StringBuilder();
		postDataSB.append("apikey=").append(urlEncode(apiKey));
		postDataSB.append("&application=").append(urlEncode(application));
		if (event != null && !event.isEmpty()) {
			postDataSB.append("&event=").append(urlEncode(event));
		}
		if (description != null && !description.isEmpty()) {
			postDataSB.append("&description=").append(urlEncode(description));
		}
		final String postData = postDataSB.toString();
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				try {
					byte[] b = postData.getBytes();
					
					URL url = new URL("https://api.prowlapp.com/publicapi/add");
					HttpURLConnection urlc = (HttpURLConnection) url.openConnection();
					urlc.setDoOutput(true);
					urlc.setRequestMethod("POST");
					urlc.setRequestProperty("Content-Length", Integer.toString(b.length));
					urlc.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
					OutputStream out = urlc.getOutputStream();
					out.write(b);
					out.close();
					InputStream in = urlc.getInputStream();
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					b = new byte[4096];
					int c;
					while ((c = in.read(b, 0, b.length)) != -1) {
						baos.write(b, 0, c);
					}
					in.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
		new Thread(runnable, "Prowler-" + (prowlerID())).start();
	}

	private static int prowlerID = 0;

	private static synchronized int prowlerID() {
		return prowlerID++;
	}

	public static String urlEncode(String str) {
		try {
			return URLEncoder.encode(str, "UTF-8").replace("%20", "+");
		} catch (Exception e) {
			// This should never occur
			throw new RuntimeException(e);
		}
	}
}
