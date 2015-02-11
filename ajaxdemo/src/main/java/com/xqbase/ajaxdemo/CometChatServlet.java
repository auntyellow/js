package com.xqbase.ajaxdemo;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.servlet.AsyncContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.xqbase.util.Runnables;

@WebServlet(urlPatterns="/cometchat/*", asyncSupported=true)
public class CometChatServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private LinkedHashMap<String, ByteArrayOutputStream> dataMap = new LinkedHashMap<>();
	private LinkedHashMap<AsyncContext, String> asyncMap = new LinkedHashMap<>();
	private ScheduledExecutorService timer;

	@Override
	public void init() throws ServletException {
		timer = Executors.newSingleThreadScheduledExecutor();
		timer.scheduleWithFixedDelay(() -> {
			synchronized (CometChatServlet.this) {
				Iterator<Map.Entry<AsyncContext, String>> i = asyncMap.entrySet().iterator();
				while (i.hasNext()) {
					Map.Entry<AsyncContext, String> entry = i.next();
					final AsyncContext async = entry.getKey();
					ByteArrayOutputStream baos = dataMap.get(entry.getValue());
					if (baos == null) {
						i.remove();
						async.complete();
						continue;
					}
					if (baos.size() == 0) {
						continue;
					}
					i.remove();
					try {
						async.getResponse().getOutputStream().write(baos.toByteArray());
					} catch (IOException e) {
						// Ignored
					}
					baos.reset();
					async.complete();
				}
			}
		}, 0, 10, TimeUnit.MILLISECONDS);
	}

	@Override
	public void destroy() {
		Runnables.shutdown(timer);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		doGet(req, resp);
	}

	@Override
	protected void doGet(HttpServletRequest req, final HttpServletResponse resp) throws IOException {
		String cmd = req.getParameter("cmd");
		if (cmd == null) {
			return;
		}
		resp.setContentType("application/octet-stream");
		switch (cmd) {
		case "open":
			Random random = new Random();
			String session = ("" + (1000000000 + random.nextInt(1000000000))).substring(1);
			session += ("" + (1000000000 + random.nextInt(1000000000))).substring(1);
			synchronized (this) {
				dataMap.put(session, new ByteArrayOutputStream());
			}
			resp.getWriter().print(session);
			break;
		case "send":
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			InputStream in = req.getInputStream();
			byte[] buffer = new byte[2048];
			int bytesRead;
			while ((bytesRead = in.read(buffer)) > 0) {
				baos.write(buffer, 0, bytesRead);
			}
			byte[] b = baos.toByteArray();
			synchronized (this) {
				for (ByteArrayOutputStream baos_ : dataMap.values()) {
					baos_.write(b);
				}
			}
			break;
		case "recv":
			session = req.getParameter("session");
			if (session == null) {
				return;
			}
			synchronized (this) {
				AsyncContext async = req.startAsync();
				async.setTimeout(0);
				asyncMap.put(async, session);
			}
			break;
		case "close":
			session = req.getParameter("session");
			if (session == null) {
				return;
			}
			synchronized (this) {
				dataMap.remove(session);
			}
		}
	}
}