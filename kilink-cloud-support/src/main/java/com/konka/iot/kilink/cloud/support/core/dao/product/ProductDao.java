package com.konka.iot.kilink.cloud.support.core.dao.product;

import com.konka.iot.kilink.cloud.support.api.model.product.ProductMapping;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

/**
 * @author xiexinyuan
 * @version 1.0
 * @createTime 2019-09-20 11:05
 * @Description TODO
 */
public interface ProductDao {

    String getProductMapping(@Param("k_pid") String k_pid, @Param("t_pid") String t_pid) throws Exception;

    List<ProductMapping> getKilinkProductIdBatch(@Param("pids") Set<String> pids) throws Exception;
}
