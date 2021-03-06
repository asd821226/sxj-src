package com.sxj.file.controller;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.SimpleTimeZone;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.eqt.utils.EqtLogger;
import com.eqt.utils.StringUtil;
import com.eqt.utils.cache.CacheFactory;
import com.eqt.utils.exception.WebException;
import com.eqt.utils.fileupload.UpLoadFactory;

@Controller
@RequestMapping("/upload")
public class FileController {

	@RequestMapping(value = "{group}/{st}/{f1}/{f2}/{id}")
	public void getImage(@PathVariable String group, @PathVariable String st,
			@PathVariable String f1, @PathVariable String f2,
			@PathVariable String id, HttpServletRequest request,
			HttpServletResponse response) throws WebException {
		try {
			StringBuffer modifyId = new StringBuffer();
			modifyId.append(group);
			modifyId.append("/");
			modifyId.append(st);
			modifyId.append("/");
			modifyId.append(f1);
			modifyId.append("/");
			modifyId.append(f2);
			modifyId.append("/");
			modifyId.append(id);
			modifyId.append("-LastModified");
			SimpleDateFormat dataformat = new SimpleDateFormat(
					"EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
			dataformat.setTimeZone(new SimpleTimeZone(0, "GMT"));
			Object lastModified = CacheFactory.buildCacheUtil().getObject(
					modifyId.toString());
			if (lastModified == null) {
				Date nowdate = new Date();
				lastModified = dataformat.format(nowdate);
				CacheFactory.buildCacheUtil().setObject(modifyId.toString(),
						lastModified.toString());
				response.addHeader("Last-Modified", lastModified.toString());
			} else {
				String ifmodify = request.getHeader("If-Modified-Since");
				response.addHeader("Last-Modified", lastModified.toString());
				if (StringUtil.isNotEmpty(ifmodify)) {
					Date ifmodifydate = dataformat.parse(ifmodify);
					Date lastmodifydate = dataformat.parse(lastModified
							.toString());
					if (ifmodifydate.getTime() == lastmodifydate.getTime()) {
						response.setStatus(304);
						return;
					}
				}
			}

			String url = request.getRequestURI();
			String type = url.substring(url.lastIndexOf(".") + 1, url.length());
			StringBuffer idbuff = new StringBuffer();
			idbuff.append(group);
			idbuff.append("/");
			idbuff.append(st);
			idbuff.append("/");
			idbuff.append(f1);
			idbuff.append("/");
			idbuff.append(f2);
			idbuff.append("/");
			idbuff.append(id);
			id = idbuff.append(".").append(type).toString();
			// id = id.replace("-", "/") + "." + type;
			byte[] image = null;
			if (StringUtil.isEmpty(id)) {
				response.setStatus(404);
				return;
			}
			// id = id.substring("/upload/".length(), id.length());
			int index = id.lastIndexOf(".");
			int index2 = id.indexOf(type);
			if (index2 == index + 1) {
				image = UpLoadFactory.buildImageUpLoad().downloadFile(id);
			} else {
				String size = id.substring(index2 + type.length(), index);
				id = id.substring(0, index2 + type.length());
				String[] sizes = size.split("[x]");
				int width = Integer.parseInt(sizes[0]);
				int height = Integer.parseInt(sizes[1]);
				if (width > 500 || height > 500) {
					width = 500;
					height = 500;
				}
				image = UpLoadFactory.buildImageUpLoad().getSmallImage(id,
						width, height);
			}
			if (image != null && image.length > 0) {
				ServletOutputStream output = response.getOutputStream();
				type = "image/" + type;
				response.setContentType(type);
				output.write(image);
				output.flush();
				output.close();
			}
		} catch (Exception e) {
			EqtLogger.error("获取图片错误", e, this.getClass());

		}

	}

	public static void main(String[] args) throws ParseException {
		SimpleDateFormat dataf = new SimpleDateFormat(
				"EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
		dataf.setTimeZone(new SimpleTimeZone(0, "GMT"));
		String ifmodify = "Mon, 29 Aug 2011 07:34:33 GMT";
		long time = System.currentTimeMillis();

		System.out.println("time=" + dataf.format(new Date()));

		Date date = dataf.parse(ifmodify);
		System.out.println("time2=" + date.getTime());
		ifmodify = ifmodify.substring(ifmodify.indexOf(",") + 1,
				ifmodify.length());
		System.out.println(ifmodify);

	}
}
