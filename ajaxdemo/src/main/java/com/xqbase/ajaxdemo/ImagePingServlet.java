package com.xqbase.ajaxdemo;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.xqbase.util.Log;
import com.xqbase.util.Time;

@WebServlet("/imageping/*")
public class ImagePingServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private static byte[] imgData;

	static {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		BufferedImage bi = new BufferedImage(1, 1, BufferedImage.TYPE_3BYTE_BGR);
		bi.setRGB(0, 0, 0);
		try {
			ImageIO.write(bi, "png", baos);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		imgData = baos.toByteArray();
	}

	@Override
	protected void doPost(HttpServletRequest req,
			HttpServletResponse resp) throws IOException {
		doGet(req, resp);
	}

	@Override
	protected void doGet(HttpServletRequest req,
			HttpServletResponse resp) throws IOException {
		Log.i("Ping from " + req.getRemoteAddr() +
				" at " + Time.toString(System.currentTimeMillis()));
		resp.setContentType("image/png");
		resp.getOutputStream().write(imgData);
	}
}