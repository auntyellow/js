package com.xqbase.ajaxdemo;

import java.io.IOException;
import java.util.Date;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
			System.out.println("Origin: " + origin);
			resp.setHeader("Access-Control-Allow-Origin", origin);
		}
		resp.setContentType("text/plain");
		resp.getWriter().print(new Date());
	}
}