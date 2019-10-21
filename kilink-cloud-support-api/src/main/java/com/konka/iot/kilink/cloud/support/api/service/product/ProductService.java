package com.konka.iot.kilink.cloud.support.api.service.product;

import com.konka.iot.kilink.cloud.support.api.model.product.ProductMapping;

import java.util.List;
import java.util.Set;

/**
 * @author xiexinyuan
 * @version 1.0
 * @createTime 2019-09-20 10:55
 * @Description 产品管理
 */
public interface ProductService {

    /**
     * 当k_pid不为空 t_pid为空 -->返回t_pid
     * 当t_pid不为空 p_pid为空 -->返回p_pid
     * @param k_pid kilink产品ID
     * @param t_pid 第三方产品ID
     * @return kilink产品ID 或者 第三方产品ID
     * @throws Exception
     */
    String  getProductMapping(String k_pid, String t_pid) throws Exception;

    /**
     * 批量获取第三方产品对应的kilink产品
     * @param pids 第三方产品ID集合
     * @return 第三方产品ID-kilink产品ID
     * @throws Exception
     */
    List<ProductMapping> getKilinkProductIdBatch(Set<String> pids) throws Exception;
}
