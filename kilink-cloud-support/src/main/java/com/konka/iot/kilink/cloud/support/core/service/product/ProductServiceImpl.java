package com.konka.iot.kilink.cloud.support.core.service.product;

import com.alibaba.dubbo.config.annotation.Service;
import com.konka.iot.baseframe.common.core.service.BaseService;
import com.konka.iot.baseframe.common.utils.JsonUtil;
import com.konka.iot.baseframe.common.utils.RedisUtil;
import com.konka.iot.kilink.cloud.support.api.model.product.ProductMapping;
import com.konka.iot.kilink.cloud.support.api.service.product.ProductService;
import com.konka.iot.kilink.cloud.support.config.Redis.TuyaRediskeyConfig;
import com.konka.iot.kilink.cloud.support.config.dataSource.DataSourceKey;
import com.konka.iot.kilink.cloud.support.config.dataSource.TargetDataSource;
import com.konka.iot.kilink.cloud.support.core.dao.product.ProductDao;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

/**
 * @author xiexinyuan
 * @version 1.0
 * @createTime 2019-09-20 11:03
 * @Description TODO
 */

@Service(version = "1.0", group = "test")
public class ProductServiceImpl extends BaseService implements ProductService {

    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private TuyaRediskeyConfig tuyaRediskeyConfig;
    @Autowired
    private ProductDao productDao;

    @Override
    @TargetDataSource(name = DataSourceKey.DB_KILINK)
    public String getProductMapping(String k_pid, String t_pid) throws Exception {
        return productDao.getProductMapping(k_pid, t_pid);
    }

    @Override
    @TargetDataSource(name = DataSourceKey.DB_KILINK)
    public List<ProductMapping> getKilinkProductIdBatch(Set<String> pids) throws Exception {

        String prefix = tuyaRediskeyConfig.getProduct_mapping_prefix();

        List<ProductMapping> productMappings = new ArrayList<>();
        List<String> keys = new ArrayList<>(pids.size());
        for (String pid : pids) {
            keys.add(prefix.concat(pid));
        }
        List<Object> list = redisUtil.get(keys);
        list.removeAll(Collections.singleton(null));

        if (list != null && !list.isEmpty()) {
            for (Object obj : list) {
                productMappings.add(JsonUtil.string2Obj(obj.toString(), ProductMapping.class));
            }
        } else {
            productMappings = productDao.getKilinkProductIdBatch(pids);
            for (ProductMapping productMapping : productMappings) {
                redisUtil.set(prefix.concat(productMapping.getTProductId()), JsonUtil.obj2String(productMapping));
            }
        }
        return productMappings;
    }
}
