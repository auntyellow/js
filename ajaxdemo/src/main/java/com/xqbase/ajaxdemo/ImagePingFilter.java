package com.xqbase.ajaxdemo;

import java.io.IOException;
import java.util.Date;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;

@WebFilter("/images/*")
public class ImagePingFilter implements Filter {
	@Override
	public void init(FilterConfig conf) {/**/}

	@Override
	public void destroy() {/**/}

	@Override
	public void doFilter(ServletRequest req, ServletResponse resp,
			FilterChain chain) throws IOException, ServletException {
		System.out.println("Ping from " + req.getRemoteAddr() + ", " + new Date());
		chain.doFilter(req, resp);
	}
}