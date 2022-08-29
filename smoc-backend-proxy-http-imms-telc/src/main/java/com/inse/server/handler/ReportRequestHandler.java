package com.inse.server.handler;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.UUID;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.base.common.vo.BusinessRouteValue;
import com.inse.worker.ReportWorker;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

/**
 * 状态报告回调通知接口
 */
public class ReportRequestHandler implements HttpHandler {
	private static final Logger logger = LoggerFactory.getLogger(ReportRequestHandler.class);
	ReportWorker reportWorker;
	

	public ReportRequestHandler(String channelID) {	
		reportWorker = new ReportWorker(channelID);
	}

	@Override
	public void handle(HttpExchange exchange) throws IOException {
		String msgid = UUID.randomUUID().toString().replaceAll("-", "");
		String ip = exchange.getRemoteAddress().getAddress().getHostAddress();
		JSONObject resultject = new JSONObject();
		logger.info("{},{},msgid={},ip={}", exchange.getRequestMethod(), exchange.getRequestURI(), msgid, ip);
		OutputStream responseBody = exchange.getResponseBody();
		Headers responseHeaders = exchange.getResponseHeaders();
		responseHeaders.set("Content-Type", "text/plain;charset=utf-8");
		exchange.sendResponseHeaders(200, 0);
		try {

			InputStream is = exchange.getRequestBody();
			List<String> lines = IOUtils.readLines(is);
			StringBuffer sb = new StringBuffer();
			for (String line : lines) {
				sb.append(line);
			}
			String queryString = sb.toString();
			logger.info("{},{},msgid={},ip={},body={}", exchange.getRequestMethod(), exchange.getRequestURI(), msgid,
					ip, queryString);

			// 请求数据转json对象
			JSONObject request = null;
			try {
				request = JSONObject.parseObject(queryString);
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
			if (request != null) {			
				process(request.getJSONArray("ReportList"));
			}
			
			resultject.put("ResCode", "1000");
			resultject.put("ResMsg", "成功");
			// 将响应结果map转成json数据返回
			responseBody.write(resultject.toString().getBytes());
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			resultject.put("ResCode", "1000");
			resultject.put("ResMsg", "成功");
			responseBody.write(resultject.toString().getBytes());
		} finally {
			if (responseBody != null) {
				responseBody.close();
			}
		}
	}

	private void process(JSONArray array) {
		if (array != null) {
			for (int i = 0; i < array.size(); i++) {
				JSONObject object = array.getJSONObject(i);
				
				String transId = object.getString("TransID");
				String phone = object.getString("Phone");
				String state = object.getString("State");
				
				BusinessRouteValue newBusinessRouteValue = new BusinessRouteValue();
				newBusinessRouteValue.setChannelMessageID(transId);
				newBusinessRouteValue.setPhoneNumber(phone);
				newBusinessRouteValue.setStatusCode(state);
				
				reportWorker.add(newBusinessRouteValue);
			}

		}

	}
}