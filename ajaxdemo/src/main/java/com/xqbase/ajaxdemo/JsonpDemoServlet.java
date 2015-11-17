package com.xqbase.ajaxdemo;

import java.io.IOException;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.xqbase.util.Time;

@WebServlet("/jsonpdemo/*")
public class JsonpDemoServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
	protected void doPost(HttpServletRequest req,
			HttpServletResponse resp) throws IOException {
		doGet(req, resp);
	}

	@Override
	protected void doGet(HttpServletRequest req,
			HttpServletResponse resp) throws IOException {
		resp.setContentType("text/javascript");
		resp.getWriter().print(req.getParameter("callback") + "(\"" +
				Time.toString(System.currentTimeMillis()) + "\")");
	}
}