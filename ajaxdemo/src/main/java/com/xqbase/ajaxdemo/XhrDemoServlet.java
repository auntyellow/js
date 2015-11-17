package com.xqbase.ajaxdemo;

import java.io.IOException;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.xqbase.util.Log;
import com.xqbase.util.Time;

@WebServlet("/xhrdemo/*")
public class XhrDemoServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		doGet(req, resp);
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		String origin = req.getHeader("Origin");
		if (origin != null) {
			Log.d("Origin: " + origin);
			resp.setHeader("Access-Control-Allow-Origin", origin);
		}
		resp.setContentType("text/plain");
		resp.getWriter().print(Time.toString(System.currentTimeMillis()));
	}
}