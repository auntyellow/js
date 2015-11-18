import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import javax.servlet.ServletException;

import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.WebResourceRoot;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.webresources.DirResourceSet;
import org.apache.catalina.webresources.StandardRoot;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.xqbase.util.ByteArrayQueue;
import com.xqbase.util.Bytes;
import com.xqbase.util.Conf;
import com.xqbase.util.Log;
import com.xqbase.util.Time;
import com.xqbase.util.http.HttpPool;

public class TestCases {
	private static final int HTTP_PORT = 10080;

	private static Tomcat tomcat;
	private static HttpPool pool;

	@BeforeClass
	public static void setUpBeforeClass() throws ServletException, LifecycleException {
		Connector connector = new Connector();
		connector.setPort(HTTP_PORT);
		tomcat = new Tomcat();
		tomcat.setPort(HTTP_PORT);
		tomcat.getService().addConnector(connector);
		tomcat.setConnector(connector);
		Context ctx = tomcat.addWebapp("", Conf.getAbsolutePath("../src/main/webapp"));
		WebResourceRoot resources = new StandardRoot(ctx);
		resources.addPreResources(new DirResourceSet(resources,
				"/WEB-INF/classes", Conf.getAbsolutePath("../target/classes"), "/"));
		ctx.setResources(resources);
		tomcat.start();
		pool = new HttpPool("http://localhost:" + HTTP_PORT + "/", 15000);
	}

	@Test
	public void xhrDemoTest() {
		try {
			ByteArrayQueue body = new ByteArrayQueue();
			int status = pool.get("xhrdemo/", null, body, null);
			Assert.assertEquals(200, status);
			Assert.assertTrue(body.toString().
					startsWith(Time.toDateString(System.currentTimeMillis()) + " "));
		} catch (IOException e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test
	public void jsonpDemoTest() {
		try {
			ByteArrayQueue body = new ByteArrayQueue();
			int status = pool.get("jsonpdemo/?callback=test", null, body, null);
			Assert.assertEquals(200, status);
			Assert.assertTrue(body.toString().startsWith("test(\"" +
					Time.toDateString(System.currentTimeMillis()) + " "));
		} catch (IOException e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test
	public void imagePingTest() {
		try {
			ByteArrayQueue body = new ByteArrayQueue();
			HashMap<String, List<String>> headers = new HashMap<>();
			int status = pool.get("imageping/", null, body, headers);
			Assert.assertEquals(200, status);
			Assert.assertEquals("image/png", headers.get("CONTENT-TYPE").get(0));
		} catch (IOException e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test
	public void cometChatTest() {
		try {
			// Connect
			ByteArrayQueue sessionData = new ByteArrayQueue();
			pool.get("cometchat/?cmd=open", null, sessionData, null);
			String session = sessionData.toString();

			// Recv
			FutureTask<String> task = new FutureTask<>(() -> {
				try {
					ByteArrayQueue recvData = new ByteArrayQueue();
					pool.get("cometchat/?cmd=recv&session=" + session, null, recvData, null);
					return recvData.toString();
				} catch (IOException e) {
					Log.i(e.getMessage());
					return null;
				}
			});
			new Thread(task).start();

			// Send
			String send = Bytes.toHexLower(Bytes.random(16));
			ByteArrayQueue sendData = new ByteArrayQueue();
			sendData.add(send.getBytes());
			pool.post("cometchat/?cmd=send&session=" + session, sendData, null, null, null);
			String recv = task.get();
			Assert.assertEquals(send, recv);

			// Disconnect
			pool.get("cometchat/?cmd=close&session=" + session, null, null, null);
		} catch (IOException | InterruptedException | ExecutionException e) {
			Assert.fail(e.getMessage());
		}
	}

	@AfterClass
	public static void tearDownAfterClass() throws LifecycleException {
		pool.close();
		tomcat.stop();
	}
}