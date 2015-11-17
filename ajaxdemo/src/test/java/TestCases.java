import java.io.IOException;
import java.util.HashMap;
import java.util.List;

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
import com.xqbase.util.Conf;
import com.xqbase.util.Time;
import com.xqbase.util.http.HttpPool;

public class TestCases {
	private static Tomcat tomcat;
	private static HttpPool pool;

	@BeforeClass
	public static void setUpBeforeClass() throws ServletException, LifecycleException {
		Connector connector = new Connector();
		connector.setPort(80);
		tomcat = new Tomcat();
		tomcat.setPort(80);
		tomcat.getService().addConnector(connector);
		tomcat.setConnector(connector);
		Context ctx = tomcat.addWebapp("", Conf.getAbsolutePath("../src/main/webapp"));
		WebResourceRoot resources = new StandardRoot(ctx);
		resources.addPreResources(new DirResourceSet(resources,
				"/WEB-INF/classes", Conf.getAbsolutePath("../target/classes"), "/"));
		ctx.setResources(resources);
		tomcat.start();
		pool = new HttpPool("http://localhost/", 15000);
	}

	@Test
	public void xhrDemoTest() {
		ByteArrayQueue body = new ByteArrayQueue();
		int status;
		try {
			status = pool.get("xhrdemo/", null, body, null);
		} catch (IOException e) {
			Assert.fail(e.getMessage());
			return;
		}
		Assert.assertEquals(200, status);
		Assert.assertTrue(body.toString().
				startsWith(Time.toDateString(System.currentTimeMillis()) + " "));
	}

	@Test
	public void jsonpDemoTest() {
		ByteArrayQueue body = new ByteArrayQueue();
		int status;
		try {
			status = pool.get("jsonpdemo/?callback=test", null, body, null);
		} catch (IOException e) {
			Assert.fail(e.getMessage());
			return;
		}
		Assert.assertEquals(200, status);
		Assert.assertTrue(body.toString().startsWith("test(\"" +
				Time.toDateString(System.currentTimeMillis()) + " "));
	}

	@Test
	public void imagePingTest() {
		ByteArrayQueue body = new ByteArrayQueue();
		HashMap<String, List<String>> headers = new HashMap<>();
		int status;
		try {
			status = pool.get("imageping/", null, body, headers);
		} catch (IOException e) {
			Assert.fail(e.getMessage());
			return;
		}
		Assert.assertEquals(200, status);
		Assert.assertEquals("image/png", headers.get("CONTENT-TYPE").get(0));
	}

	@Test
	public void cometChatTest() {
		// TODO
	}

	@AfterClass
	public static void tearDownAfterClass() throws LifecycleException {
		pool.close();
		tomcat.stop();
	}
}