package com.reptile.util;

import java.util.HashMap;
import java.util.Map;

import javax.websocket.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.reptile.contorller.TalkframeController;

public class PushSocket {

	private static Logger logger = LoggerFactory.getLogger(PushSocket.class);

	/**
	 * 0001 失败 0000 成功
	 * 
	 * @param map
	 * @param UUID
	 * @param errorInfo
	 */
	public static Map<String, Object> push(Map<String, Object> map,
			String UUID, String errorInfo) {
		Map<String, Object> mapData = new HashMap<String, Object>();
		Session se = TalkframeController.getWsUserMap().get(UUID);
		String seq_id = TalkframeController.getWsInfoMap().get(UUID);
		logger.warn("----------------长连接推送开始------se：" + se + "-----seq_id:"
				+ seq_id);
		try {
			if (se != null && seq_id != null) {
				if (seq_id.equals("hello")) {
					se.getBasicRemote().sendText(
							"{\"resultCode\":\"" + errorInfo
									+ "\",\"seq_id\":\"" + seq_id + "\"}");
				} else {
					se.getBasicRemote().sendText(
							"{\"resultCode\":" + errorInfo + ",\"seq_id\":"
									+ seq_id + "}");
				}

			}


		} catch (Exception e) {
			logger.warn("----------------长连接推送失败------",e);
			map.put("errorCode", "0001");
			map.put("errorInfo", "网络异常");
		}
		return map;

	}

}
