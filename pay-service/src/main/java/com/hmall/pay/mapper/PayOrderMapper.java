package com.hmall.pay.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hmall.pay.domain.po.PayOrder;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * <p>
 * 支付订单 Mapper 接口
 * </p>
 *
 * @author 虎哥
 * @since 2023-05-16
 */
@Mapper
public interface PayOrderMapper extends BaseMapper<PayOrder> {

    @Select("select * from pay_order where biz_order_no = #{orderId}")
    PayOrder get(Long orderId);
}
