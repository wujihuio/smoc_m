/**
 * @desc
 * 
 */
package com.business.proxy.server;

import com.base.common.constant.FixedConstant;
import com.base.common.log.PorxyBusinessLogManager;
import com.base.common.manager.ExtendParameterManager;
import com.base.common.manager.ResourceManager;
import com.business.proxy.manager.ReportPullWorkerManager;
import com.business.proxy.manager.ResponsePullWorkerManager;
import com.business.proxy.manager.SubmitJsonToBeanWorkerManager;
import com.business.proxy.manager.SubmitPullWorkerManager;
import com.business.proxy.manager.SubmitSaveWorkerManager;

public class ProxyServer {

	
	
	public static void main(String[] args) {
		try {
			ProxyServer proxyServer = new ProxyServer();
			proxyServer.startup();
			
			EchoServer echoServer = new EchoServer(proxyServer);
			echoServer.startup();
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("服务启动失败");
			System.exit(0);
		}
	}
	
	/**
	 * 启动服务
	 */
	private void startup(){

		System.out.println("服务正在启动");
		SubmitSaveWorkerManager.getInstance();
		SubmitJsonToBeanWorkerManager.getInstance();

		//初始化文件配置数据
		ResourceManager.getInstance();
		//初始化数据库配置数据
		ExtendParameterManager.getInstance();		
		//启动日志服务
		PorxyBusinessLogManager.getInstance();
		//启动提交信息拉取服务及通道下行表创建
		SubmitPullWorkerManager.getInstance();
		//启动响应信息拉取服务
		ResponsePullWorkerManager.getInstance();
		//启动状态报告拉取服务
		ReportPullWorkerManager.getInstance();
		System.out.println("服务已启动");

	}
	
	/**
	 * 停止服务
	 */
	public void stop(){
		try {
			SubmitPullWorkerManager.getInstance().exit();
			Thread.sleep(FixedConstant.COMMON_INTERVAL_TIME);
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("服务已停止");
		System.exit(0);
	}

}


