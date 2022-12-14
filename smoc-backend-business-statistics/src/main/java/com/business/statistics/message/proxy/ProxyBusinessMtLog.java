package com.business.statistics.message.proxy;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang3.StringUtils;


import com.base.common.constant.FixedConstant;
import com.base.common.constant.LogPathConstant;
import com.base.common.dao.LavenderDBSingleton;
import com.base.common.log.CategoryLog;
import com.base.common.manager.ResourceManager;
import com.base.common.util.DateUtil;
import com.base.common.util.TableNameGeneratorUtil;
import com.base.common.vo.BusinessRouteValue;
import com.business.statistics.util.FileFilterUtil;

/**
 * 代理业务层 mt下发记录  入库通道srcid临时表
 */
public class ProxyBusinessMtLog {

	private static final int BUSINESS_PROXY_MT_MESSAGE_LOG_LENGTH = ResourceManager.getInstance().getIntValue("business.proxy.mt.message.log.length");

	public static void main(String[] args) {
		CategoryLog.proxyLogger.info(Arrays.toString(args));
		try {
			long start = System.currentTimeMillis();
			String lineTime = null;
			int afterMinute = -1;
			if (!(args.length == 1 || args.length == 0)) {
				CategoryLog.proxyLogger.error("参数不符合规范");
				System.exit(0);
			}
			//通过传参 确定读取时间
			if (args.length >= 1) {
				afterMinute = Integer.valueOf(args[0]);
			}
			lineTime = DateUtil.getAfterMinuteDateTime(afterMinute, DateUtil.DATE_FORMAT_COMPACT_STANDARD_MINUTE);
			
			//读取的文件路径
			String filePath = new StringBuilder(LogPathConstant.LOG_BASE_PATH)
					.append(LogPathConstant.getProxyBusinessFilePathPart(FixedConstant.RouteLable.MT.name()))
					.toString();
			//读取的文件名
			String fileName = new StringBuilder(LogPathConstant.getFileNamePrefix(FixedConstant.RouteLable.MT.name()))
					.append(DateUtil.getAfterMinuteDateTime(afterMinute, DateUtil.DATE_FORMAT_COMPACT_HOUR)).toString();
			
			CategoryLog.proxyLogger.info("读取当前文件路径={},文件名={},生成时间={}",filePath,fileName,lineTime);
			
			ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(FixedConstant.CPU_NUMBER * 8,
					Integer.MAX_VALUE, 100000L, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
			threadPoolExecutor.prestartAllCoreThreads();
			
			Vector<Future<Integer>> calls = new Vector<Future<Integer>>();
			int fileNumber = 0;
			
			//得到yyyyMMddHH格式获取文件
			if (StringUtils.isNotEmpty(fileName) && StringUtils.isNotEmpty(filePath)) {
				File[] fileList = FileFilterUtil.findFile(filePath, fileName);
				fileNumber = fileList.length;
				for(File file : fileList) {
					//一个文件启动一个线程进行处理
					ProxyBusinessMtLog.Worker worker = new ProxyBusinessMtLog().new Worker(file, lineTime);
					Future<Integer> call = threadPoolExecutor.submit(worker);
					calls.add(call);
				}
			}
			
			int rowNumber = 0;
			//循环结束 所有子线程处理完毕
			for (Future<Integer> call : calls) {
				try {
					rowNumber += call.get();
				} catch (Exception e) {
					CategoryLog.proxyLogger.error(e.getMessage(),e);
				}
			}
			
			CategoryLog.proxyLogger.info("{},处理文件数量:{},处理数据行数:{},耗时:{}",lineTime,fileNumber,rowNumber,(System.currentTimeMillis() - start));
		} catch (Exception e) {
			CategoryLog.proxyLogger.error(e.getMessage(),e);
		}
		//保证所有数据处理完毕 关闭进程
		CategoryLog.proxyLogger.info("程序正常退出");
		System.exit(0);
	}

	class Worker implements Callable<Integer> {
		private File file;
		private String lineTime;

		public Worker(File file, String lineTime) {
			this.file = file;
			this.lineTime = lineTime;
		}

		@Override
		public Integer call() throws Exception {
			int rowNumber = 0;
			long start = System.currentTimeMillis();
			try {
				Map<String, List<BusinessRouteValue>> channelSRCIDMap = groupChannelSRCID(file, lineTime);
				String tableName = null;
				for (String channelSRCID : channelSRCIDMap.keySet()) {
					tableName = TableNameGeneratorUtil.generateRouteChannelSRCIDMessageMTInfoTableName(channelSRCID);
					CategoryLog.proxyLogger.info("表{} 保存数据{}条", tableName, channelSRCIDMap.get(channelSRCID).size());
					rowNumber += channelSRCIDMap.get(channelSRCID).size();
					//保存对应的channelsrcId的下发临时表中
					saveRouteChannelSRCIDMessageMTInfo(channelSRCIDMap.get(channelSRCID), tableName);
				}
				CategoryLog.proxyLogger.info("文件:{},时间段:{},处理数据{}条,耗时:{}",file.getName(),lineTime,rowNumber,(System.currentTimeMillis() - start));
			} catch (Exception e) {
				CategoryLog.proxyLogger.error(e.getMessage(), e);
			}

			CategoryLog.proxyLogger.info("线程退出循环");
			return rowNumber;

		}
	}
	
	/**
	 * 	读取文件中的业务日志 然后根据channelSRCID 进行分组处理
	 * @param file
	 * @param lineTime
	 * @return
	 * @throws Exception
	 */
	private Map<String,List<BusinessRouteValue>> groupChannelSRCID(File file,String lineTime) throws Exception {
		LineIterator lines = null;
		String[] array = null;
		boolean error = false;
		String error_line=null;
		String error_file=null;
		String line = null;
		Map<String,List<BusinessRouteValue>> channelChannelSRCIDMap = new HashMap<String,List<BusinessRouteValue>>();
		BusinessRouteValue businessRouteValue = null;
		try {
			lines = FileUtils.lineIterator(file, "utf-8");

			while (lines.hasNext()) {
				line = lines.next();
				array = line.split(FixedConstant.LOG_SEPARATOR);

				if (array.length < BUSINESS_PROXY_MT_MESSAGE_LOG_LENGTH) {
					error = true;
					error_line = line;
					error_file = file.getAbsolutePath();
					continue;
				}

				if (array[0].startsWith(lineTime)) {
					businessRouteValue = new BusinessRouteValue();
					businessRouteValue.setAccountID(array[1]);
					//businessRouteValue.setEnterpriseFlag(array[2]);
					//businessRouteValue.setProtocol(array[3]);
					//businessRouteValue.setAccountSubmitTime(array[4]);
					//businessRouteValue.setTableAuditTime(array[5]);
					//businessRouteValue.setTableSubmitTime(array[6]);
					//businessRouteValue.setQueueSubmitTime(array[7]);
					businessRouteValue.setChannelSubmitTime(array[8]);
					//businessRouteValue.setBusinessMessageID(array[9]);
					businessRouteValue.setPhoneNumber(array[10]);
					//businessRouteValue.setSegmentCarrier(array[11]);
					businessRouteValue.setBusinessCarrier(array[12]);
					//businessRouteValue.setAreaCode(array[13]);
					businessRouteValue.setAreaName(array[14]);
					//businessRouteValue.setCityName(array[15]);
					businessRouteValue.setChannelSubmitSRCID(array[16]);
					//businessRouteValue.setAccountExtendCode(array[17]);
					businessRouteValue.setAccountBusinessCode(array[18]);
					businessRouteValue.setAccountSubmitSRCID(array[19]);
					//businessRouteValue.setMessageFormat(array[20]);
					businessRouteValue.setMessageContent(array[21]);
					//businessRouteValue.setMessageSignature(array[22]);
					businessRouteValue.setInfoType(array[23]);
					//businessRouteValue.setIndustryTypes(array[24]);
					//businessRouteValue.setAccountPriority(array[25]);
					//businessRouteValue.setAccountMessageIDs(array[26]);
					businessRouteValue.setAccountTaskID(array[26]);
					businessRouteValue.setChannelID(array[27]);
					businessRouteValue.setChannelSRCID(array[28]);
					//businessRouteValue
					//		.setChannelTotal(StringUtils.isNotEmpty(array[29]) ? 0 : Integer.parseInt(array[43]));
					//businessRouteValue
					//		.setChannelIndex(StringUtils.isNotEmpty(array[30]) ? 0 : Integer.parseInt(array[43]));
					//businessRouteValue.setNextNodeCode(array[32]);
					//businessRouteValue.setNextNodeErrorCode(array[33]);
					//businessRouteValue.setChannelMessageID(array[34]);// 35-提交响应消息id
					//businessRouteValue.setChannelPrice(array[35]);
					//businessRouteValue.setPriceAreaCode(array[36]);// 37-价格区域编码
					//businessRouteValue.setFinanceAccountID(array[37]);// 38-计费账号
					//businessRouteValue.setMessageNumber(array[38]);
					//businessRouteValue.setMessagePrice(array[39]);// 40-账号计费单价
					//businessRouteValue.setMessageAmount(array[40]);
					businessRouteValue.setBusinessType(array[41]);
					businessRouteValue.setAccountTemplateID(array[42]);
					//businessRouteValue
					//		.setRepeatSendTimes(StringUtils.isNotEmpty(array[43]) ? 0 : Integer.parseInt(array[43]));
					businessRouteValue.setOptionParam(array[44]);
					//businessRouteValue.setConsumeType(array[45]);// 45-扣费类型
					
					List<BusinessRouteValue> businessRouteValues = channelChannelSRCIDMap.get(array[28]);
					if(businessRouteValues == null) {
						businessRouteValues = new ArrayList<BusinessRouteValue>();
						channelChannelSRCIDMap.put(array[28], businessRouteValues);
					}

					businessRouteValues.add(businessRouteValue);
				}
			}
		} catch (Exception e) {
			CategoryLog.proxyLogger.error(e.getMessage(),e);
			throw e;
		}
		if(error){
			CategoryLog.proxyLogger.warn("错误文件={},错误行数={}",error_file,error_line);
		}
		return channelChannelSRCIDMap;
	}
	
	private void saveRouteChannelSRCIDMessageMTInfo(List<BusinessRouteValue> businessRouteValues,String tableName) {
		Connection conn = null;
		PreparedStatement pstmt = null;

		StringBuffer sql = new StringBuffer();
		sql.append("INSERT INTO smoc_route.").append(tableName);
		sql.append(" (PHONE_NUMBER,MESSAGE_CONTENT,CHANNEL_ID,CHANNEL_MO_SRCID,ACCOUNT_ID,BUSINESS_CARRIER,CHANNEL_SUBMIT_TIME,AREA_NAME,");
		sql.append("ACCOUNT_BUSINESS_CODE,ACCOUNT_SUBMIT_SRCID,INFO_TYPE,ACCOUNT_TASK_ID,CHANNEL_SRCID,");
		sql.append("BUSINESS_TYPE,ACCOUNT_TEMPLATE_ID,OPTION_PARAM,CREATED_TIME) ");
		sql.append("values(");
		sql.append("?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,NOW())");
		
		CategoryLog.proxyLogger.debug("sql={}",sql.toString());
		// 在一个事务中更新数据
		int num = 0;
		try {
			conn = LavenderDBSingleton.getInstance().getConnection();
			conn.setAutoCommit(false);
			pstmt = conn.prepareStatement(sql.toString());

			for (BusinessRouteValue businessRouteValue : businessRouteValues) {
				num++;
				pstmt.setString(1, businessRouteValue.getPhoneNumber());
				pstmt.setString(2, businessRouteValue.getMessageContent());
				pstmt.setString(3, businessRouteValue.getChannelID());
				pstmt.setString(4, businessRouteValue.getChannelSubmitSRCID());
				pstmt.setString(5, businessRouteValue.getAccountID());
				pstmt.setString(6, businessRouteValue.getBusinessCarrier());
				pstmt.setString(7, businessRouteValue.getChannelSubmitTime());
				pstmt.setString(8, businessRouteValue.getAreaName());
				pstmt.setString(9, businessRouteValue.getAccountBusinessCode());
				
				pstmt.setString(10, businessRouteValue.getAccountSubmitSRCID());
				pstmt.setString(11, businessRouteValue.getInfoType());
				pstmt.setString(12, businessRouteValue.getAccountTaskID());
				pstmt.setString(13, businessRouteValue.getChannelSRCID());
				pstmt.setString(14, businessRouteValue.getBusinessType());
				pstmt.setString(15, businessRouteValue.getAccountTemplateID());
				pstmt.setString(16, businessRouteValue.getOptionParam());

				pstmt.addBatch();
				if(num % 10000 == 0) {
					pstmt.executeBatch();
				}
			}

			pstmt.executeBatch();
			conn.commit();
		} catch (Exception e) {
			CategoryLog.proxyLogger.error(e.getMessage(), e);
			try {
				conn.rollback();
			} catch (Exception e1) {
				CategoryLog.proxyLogger.error(e1.getMessage(), e1);
			}
		} finally {
			LavenderDBSingleton.getInstance().closeAll(null, pstmt, conn);
		}
	}
		
}
