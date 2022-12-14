/**
 * @desc
 * 
 */
package com.business.access.manager;

import com.base.common.constant.FixedConstant;
import com.base.common.manager.ResourceManager;
import com.base.common.vo.BusinessRouteValue;
import com.base.common.worker.SuperQueueWorker;
import com.business.access.worker.BusinessWorker;

public class BusinessWorkerManager extends SuperQueueWorker<BusinessRouteValue>{
	//启动线程数
	private static int businessWorkerThreadSize = ResourceManager.getInstance().getIntValue("business.worker.thread.size");
	
	private static BusinessWorkerManager manager = new BusinessWorkerManager();
	
	
	private BusinessWorkerManager(){
		//当未配置时按住cpu的核数
		if(businessWorkerThreadSize == 0){
			businessWorkerThreadSize = FixedConstant.CPU_NUMBER;
		}
		System.out.println("业务信息封装线程数:"+businessWorkerThreadSize);
		for(int i=0;i<businessWorkerThreadSize;i++){
			BusinessWorker businessWorker = new BusinessWorker(superQueue);
			businessWorker.setName(new StringBuilder("BusinessWorker-").append(i).toString());
			businessWorker.start();
		}
		
		this.start();
	}
	
	public static BusinessWorkerManager getInstance(){
		return manager;
	}
	
	/**
	 * 添加到共享队列中，由业务线程主动竞争获取
	 * @param businessRouteValue
	 */
	public void process(BusinessRouteValue businessRouteValue){
		add(businessRouteValue);
	}

	
	public void doRun() throws Exception {
		Thread.sleep(FixedConstant.COMMON_MONITOR_INTERVAL_TIME);
	}
	
	/**
	 * 获取业务缓存队列数量
	 * @return
	 */
	public int getSize(){
		return size();
	}
}


