package com.protocol.proxy.server.handler;

import com.alibaba.fastjson.JSONObject;
import com.protocol.proxy.manager.TemplateStatusManager;
import com.protocol.proxy.message.StatusMessage;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 模板审核回调通知接口
 */
public class StatusRequestHandler implements HttpHandler {
	private static final Logger logger = LoggerFactory.getLogger(StatusRequestHandler.class);
	private String channelID;

	public StatusRequestHandler(String channelID) {
		this.channelID=channelID;
	}

	@Override
	public void handle(HttpExchange exchange) throws IOException {

		String ip = exchange.getRemoteAddress().getAddress().getHostAddress();
		JSONObject resultject = new JSONObject();

		logger.info("{},{},ip={}", exchange.getRequestMethod(), exchange.getRequestURI(), ip);
		OutputStream responseBody = exchange.getResponseBody();
		Headers responseHeaders = exchange.getResponseHeaders();
		responseHeaders.set("Content-Type", "text/plain;charset=utf-8");
		exchange.sendResponseHeaders(200, 0);
		try {

			InputStream is = exchange.getRequestBody();
			List<String> lines = IOUtils.readLines(is,"UTF-8");
			StringBuffer sb = new StringBuffer();
			for (String line : lines) {
				sb.append(line);
			}
			String queryString = sb.toString();
			logger.info("{},{},ip={},body={}", exchange.getRequestMethod(), exchange.getRequestURI(),
					ip, queryString);

			// 请求数据转json对象
			JSONObject request = null;
			try {
				request = JSONObject.parseObject(queryString);
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}

			process(request);
			resultject.put("ResCode:", "1000");
			resultject.put("ResMsg", "成功");
			// 将响应结果map转成json数据返回
			responseBody.write(resultject.toString().getBytes());
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			resultject.put("ResCode:", "1000");
			resultject.put("ResMsg", "成功");
			responseBody.write(resultject.toString().getBytes());
		} finally {
			if (responseBody != null) {
				responseBody.close();
			}
		}
	}

	private void process(JSONObject jsonObject) {
		if (jsonObject != null) {
			String msgID = jsonObject.getString("MsgID");
			String checkState = jsonObject.getString("CheckState");
			String reason = jsonObject.getString("Reason");
			String date = jsonObject.getString("Date");
			StatusMessage message = new StatusMessage();

			message.setTemplateId(msgID);
			message.setCheckState(checkState);
			message.setReason(reason);
			message.setDate(date);
			message.setChannelID(channelID);
			TemplateStatusManager.getInstance().process(message);
		}
	}

}
