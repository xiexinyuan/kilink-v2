package com.konka.iot.kilink.cloud.support.core.service;

import com.konka.iot.baseframe.common.exception.DataCheckException;
import com.konka.iot.baseframe.common.model.HttpResult;
import com.konka.iot.baseframe.common.utils.HttpUtil;
import com.konka.iot.baseframe.common.utils.JsonUtil;
import com.konka.iot.baseframe.common.utils.RedisUtil;
import com.konka.iot.kilink.cloud.support.api.model.device.DeviceModel;
import com.konka.iot.kilink.cloud.support.api.model.device.DeviceStatusModel;
import com.konka.iot.kilink.cloud.support.api.model.product.ProductDatapoint;
import com.konka.iot.kilink.cloud.support.config.KilinkConfig;
import com.konka.iot.kilink.cloud.support.enums.KilinkApiEnum;
import com.konka.iot.kilink.cloud.support.utils.KilinkApiUtil;
import org.codehaus.jackson.type.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author xiexinyuan
 * @version 1.0
 * @createTime 2019-10-08 16:32
 * @Description TODO
 */
@Service
public class KilinkServiceUtil {

    private static final Logger logger = LoggerFactory.getLogger(KilinkServiceUtil.class);

    @Autowired
    private HttpUtil httpUtil;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private KilinkConfig kilinkConfig;

    @Autowired
    private KilinkApiUtil kilinkApiUtil;

    /**
     * 添加设备
     * @param pId 产品ID
     * @param deviceModel 设备信息
     * @return
     * @throws Exception
     */
    public DeviceModel add(String pId, DeviceModel deviceModel) throws Exception {
        //调用云智易接口创建设备
        String url = kilinkConfig.getCloud_url() + KilinkApiEnum.ADD_DEVICE.getUrl().replace("{product_id}",pId);
        HttpResult result = httpUtil.doPost(url, JsonUtil.obj2String(deviceModel), kilinkApiUtil.createHeaderMap());
        if(result.getCode() == 200){
            logger.info("添加设备成功：{}", result.getBody());
            return JsonUtil.string2Obj(result.getBody(), DeviceModel.class);
        }else{
            errorMsg(result.getBody(), "添加设备失败");
        }
        return null;
    }

    /**
     * 批量添加设备
     * @param pId 产品ID
     * @param devices 设备信息列表
     * @throws Exception
     */
    public void batchDevice(String pId, List<DeviceModel> devices) throws Exception {
        //调用云智易接口创建设备
        String url = kilinkConfig.getCloud_url() + KilinkApiEnum.ADD_DEVICE_BATCH.getUrl().replace("{product_id}",pId);
        HttpResult result = httpUtil.doPost(url, JsonUtil.obj2String(devices), kilinkApiUtil.createHeaderMap());
        if(result.getCode() == 200){
            logger.info("批量添加设备成功：{}", result.getBody());
        }else{
            errorMsg(result.getBody(), "批量添加设备失败");
        }
    }

    /**
     * 获取产品数据端点列表
     * @param productId 产品id
     * @return 返回产品数据端点列表
     */
    public List<ProductDatapoint> getProductDatapoint(String productId) throws Exception {
        List<ProductDatapoint> productDatapoints = null;
        String datapoints = (String)redisUtil.get(kilinkConfig.getKilink_product_datapoint().concat(productId));
        if(datapoints != null){
            productDatapoints = JsonUtil.string2Obj(datapoints, new TypeReference<List<ProductDatapoint>>() {});
        }else{
            String url = kilinkConfig.getCloud_url() + KilinkApiEnum.PRODUCT_DATAPOINT_LIST.getUrl().replace("{product_id}", productId);
            HttpResult result = httpUtil.doGet(url, null, kilinkApiUtil.createHeaderMap());
            if(result.getCode() == 200){
                logger.info("获取产品数据端点成功：{}", result.getBody());
                productDatapoints = JsonUtil.string2Obj(result.getBody(), new TypeReference<List<ProductDatapoint>>() {});
                redisUtil.set(kilinkConfig.getKilink_product_datapoint().concat(productId), productDatapoints);
            }else{
                errorMsg(result.getBody(), "获取产品数据端点失败");
            }
        }
        return productDatapoints;
    }

    /**
     * 查询网关下的设备列表(分页)
     * @param gatewayId 网关设备ID
     * @param pageNo 当前页
     * @param pageSieze 页大小
     * @return
     * @throws Exception
     */
    public List<DeviceStatusModel> listGatewayDevice(String gatewayId, AtomicInteger pageNo, int pageSieze) throws Exception {
        String url = kilinkConfig.getCloud_url() + KilinkApiEnum.LIST_GATE_DEVICES.getUrl();

        Map<String, Object> params = new HashMap<>();
        List<String> filter = new ArrayList<>();
        filter.add("id");
        filter.add("name");
        filter.add("mac");
        filter.add("product_id");
        filter.add("is_active");
        filter.add("is_online");

        params.put("filter", filter);
        params.put("offset", pageNo.intValue() * pageSieze);
        params.put("limit", pageSieze);

        Map<String, Object> query = new HashMap<>();
        Map<String, Object> gateway_id = new HashMap<>();
        gateway_id.put("$eq", gatewayId);
        query.put("gateway_id", gateway_id);

        params.put("query", query);

        HttpResult result = httpUtil.doPost(url, JsonUtil.obj2String(params), kilinkApiUtil.createHeaderMap());
        if(result.getCode() == 200){
            logger.info("分页查询网关子设备成功：{}", result.getBody());
            String resultBody = result.getBody().replace("\'","");
            Map<String,Object> resultMap = JsonUtil.string2Obj(resultBody, new TypeReference<Map<String,Object>>(){});
            return JsonUtil.string2Obj(JsonUtil.obj2String(resultMap.get("list")), new TypeReference<List<DeviceStatusModel>>(){});
        }else{
            errorMsg(result.getBody(), "分页查询网关子设备失败");
        }

        return null;
    }

    /**
     * 通过二维码绑定设备
     * @param userId 用户ID
     * @param productId 产品ID
     * @param deviceId 设备ID
     * @return
     * @throws Exception
     */
    public boolean bindByQrcode(String userId, String productId, String deviceId, String accessToken) throws Exception{
        //不存在二维码绑定必要端点则加入端点
        String datapoints = (String)redisUtil.get(kilinkConfig.getKilink_product_datapoint().concat(productId));
        List<ProductDatapoint> productDatapoints = null;

        if(datapoints != null){
            productDatapoints = JsonUtil.string2Obj(datapoints, new TypeReference<List<ProductDatapoint>>() {});
        }else{
            productDatapoints = getProductDatapoint(productId);
            redisUtil.set(kilinkConfig.getKilink_product_datapoint().concat(productId), JsonUtil.obj2String(productDatapoints));
        }
        boolean exist = productDatapoints.stream().anyMatch(ProductDatapoint->ProductDatapoint.getName().equals("$1002"));
        if(!exist){
            addProductDatapoint(productId, "$1002", 1, 255);
            redisUtil.del(kilinkConfig.getKilink_product_datapoint().concat(productId));
        }

        //开启可通过二维码绑定接口
        Map<String, String> opration = new HashMap<>();
        opration.put("$1002", "true");
        setDeviceDatapoints(productId, deviceId, opration, false);

        //通过二维码绑定设备
        String deviceQrcode = getDeviceQrcode(productId, deviceId);
        String url = kilinkConfig.getCloud_url() + KilinkApiEnum.USER_BIND_DEVICE_QRCODE.getUrl().replace("{user_id}", userId);
        Map<String, Object> params = new HashMap<>(1);
        params.put("qrcode", deviceQrcode);
        HttpResult result = httpUtil.doPost(url, params, kilinkApiUtil.createHeaderMap(accessToken));
        if(result.getCode() == 200){
            logger.info("设备{}绑定至用户{}下成功：{}",deviceId, userId, result.getBody());
            return true;
        }else{
            errorMsg(result.getBody(), "设备绑定失败");
        }
        return false;
    }

    /**
     * 添加数据端点
     *
     * @param productId 产品id
     * @param name      端点名称
     * @param type      端点类型 1	布尔类型2	单字节(无符号)3	16位短整型（有符号）
     *                  4	32位整型（有符号）5	浮点6	字符串7	字节数组8	16位短整型（无符号）9	32位整型（无符号）
     * @param index     端点索引
     */
    public boolean addProductDatapoint(String productId, String name, Integer type, Integer index) throws Exception {
        String url = kilinkConfig.getCloud_url() + KilinkApiEnum.PRODUCT_ADD_DATAPOINT.getUrl().replace("{product_id}", productId);
        Map<String, Object> params = new HashMap<>(3);
        params.put("name", name);
        params.put("type", type);
        params.put("index", index);
        HttpResult result = httpUtil.doPost(url, params, kilinkApiUtil.createHeaderMap());
        if(result.getCode() == 200){
            logger.info("添加产品数据端点成功：{}", result.getBody());
            return true;
        }else{
            errorMsg(result.getBody(), "添加产品数据端点失败");
        }
        return false;
    }

    /**
     * 用于通过操作虚拟设备来回写操作设备
     * @param productId  产品id
     * @param deviceId   设备id
     * @param opration   设备的操作指令 key端点名，value是值
     * @param write_back 是否回写到客户端
     */
    public boolean setDeviceDatapoints(String productId, String deviceId, Map<String,String> opration, boolean write_back) throws Exception {
        List<Map<String, String>> dataPointList = createDataPointList(productId, opration);
        if (dataPointList == null || dataPointList.isEmpty()) {
            return false;
        }
        String url = kilinkConfig.getCloud_url() + KilinkApiEnum.SET_DEVICE_DATAPOINT.getUrl().replace("{device_id}", deviceId);
        Map<String, Object> params = new HashMap<>();
        params.put("command", dataPointList);
        /**
         * 非必须 是否回写至设备端, 默认为false
         */
        params.put("write_back", write_back ? "true" : "false");

        /**
         * 必须 数据端点的来源类型
         * -1	未知
         * 1	设备上报
         * 2	动态计算
         * 3	应用设置
         */
        params.put("source", "1");
        HttpResult result = httpUtil.doPost(url, params, kilinkApiUtil.createHeaderMap());
        if(result.getCode() == 200){
            logger.info("设置产品数据端点成功：{}", result.getBody());
            return true;
        }else{
            errorMsg(result.getBody(), "设置产品数据端点失败");
        }
        return false;
    }


    /**
     * 通过 产品id和操作命令封装成 kilink的command
     * @param productId 产品id
     * @param opration  设备的操作指令 key端点名，value是值
     */
    private List<Map<String, String>> createDataPointList(String productId, Map<String, String> opration) throws Exception {
        if (opration == null) {
            return null;
        }
        List<Map<String, String>> operationList = new ArrayList<>();
        List<ProductDatapoint> productDatapoints = getProductDatapoint(productId);
        oprationForeach: for (Map.Entry<String, String> entry : opration.entrySet()) {
            String dataPointName = entry.getKey();
            Map<String, String> commandMap = new HashMap<>();
            for (ProductDatapoint productDatapoint : productDatapoints){
                if(productDatapoint.getName().equals(dataPointName)){
                    if(productDatapoint.getIndex() == null || "".equals(productDatapoint.getIndex())){
                        continue oprationForeach;
                    }else {
                        commandMap.put("index", productDatapoint.getIndex().toString());
                    }
                    break;
                }
            }
            commandMap.put("value", entry.getValue());
            operationList.add(commandMap);
        }
        return operationList;
    }


    /**
     * 生成设备二维码
     *
     * @param productId 产品id
     * @param deviceId  设备 id
     * @return 二维码字符串
     */
    private String getDeviceQrcode(String productId, String deviceId) throws Exception {
        String url = kilinkConfig.getCloud_url() + KilinkApiEnum.DEVICE_QRCODE.getUrl().replace("{product_id}", productId).replace("{device_id}", deviceId);
        Map<String, Object> params = new HashMap<>(1);
        Map<String, Object> format = new HashMap<>(1);
        //编码格式
        format.put("encode", "source");
        params.put("format", format);
        HttpResult result = httpUtil.doPost(url, params, kilinkApiUtil.createHeaderMap());
        if(result.getCode() == 200){
            logger.info("生成设备二维码成功：{}", result.getBody());
            Map<String, String> resultMap = JsonUtil.string2Obj(result.getBody(), new TypeReference<Map<String, String>>() {});
            return resultMap.get("qrcode");
        }else{
            errorMsg(result.getBody(), "生成设备二维码失败");
        }
        return null;
    }

    private void errorMsg(String resultBody, String message) throws DataCheckException{
        resultBody = resultBody.replace("\'","");
        Map<String,Object> resultMap = JsonUtil.string2Obj(resultBody, new TypeReference<Map<String,Object>>(){});
        Map<String, Object> errorMap = (Map)resultMap.get("error");
        logger.error(message + "：{}", errorMap);
        int code = Integer.parseInt(errorMap.get("code") + "");
        throw new DataCheckException(code, message + ": "+ errorMap.get("msg"));
    }
}
