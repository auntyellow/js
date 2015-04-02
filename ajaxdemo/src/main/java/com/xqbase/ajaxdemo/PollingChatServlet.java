package com.xqbase.ajaxdemo;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Random;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/pollingchat/*")
public class PollingChatServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private LinkedHashMap<String, ByteArrayOutputStream> dataMap = new LinkedHashMap<>();

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		doGet(req, resp);
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
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
				baos = dataMap.get(session);
				if (baos != null && baos.size() > 0) {
					resp.getOutputStream().write(baos.toByteArray());
					baos.reset();
				}
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