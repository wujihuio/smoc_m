/**
 * @desc
 * 
 */
package com.base.common.worker;

import java.util.concurrent.BlockingQueue;

import com.base.common.cache.CacheBaseService;
import com.base.common.constant.FixedConstant;
import com.base.common.vo.BusinessRouteValue;

public class MessageSubmitFailWorker extends SuperCacheWorker{
	
	private BlockingQueue<BusinessRouteValue> businessRouteValueQueue; 
	
	public MessageSubmitFailWorker(
			BlockingQueue<BusinessRouteValue> businessRouteValueQueue) {
		super();
		this.businessRouteValueQueue = businessRouteValueQueue;
	}

	@Override
	public void doRun() throws Exception {
		BusinessRouteValue businessRouteValue = businessRouteValueQueue.take();
		String accountMessageIDs =businessRouteValue.getAccountMessageIDs();
		int messageIndex = 0;
		for(String accountMessageID : accountMessageIDs.split(FixedConstant.SPLICER)){
			messageIndex++;
			businessRouteValue.setAccountMessageIDs(accountMessageID);
			businessRouteValue.setMessageIndex(messageIndex);
			CacheBaseService.saveBusinessReportToMiddlewareCache(businessRouteValue);
			logger.debug(
					new StringBuilder().append("模拟回执").append("{}accountID={}").append("{}phoneNumber={}")
							.append("{}businessMessageID={}").append("{}statusCode={}").append("{}statusCodeSource={}")
							.toString(),
					FixedConstant.SPLICER, businessRouteValue.getAccountID(), 
					FixedConstant.SPLICER, businessRouteValue.getPhoneNumber(),
					FixedConstant.SPLICER,businessRouteValue.getBusinessMessageID(),
					FixedConstant.SPLICER, businessRouteValue.getStatusCode(),
					FixedConstant.SPLICER, businessRouteValue.getStatusCodeSource());
		}
		businessRouteValue = null;
	}

}


