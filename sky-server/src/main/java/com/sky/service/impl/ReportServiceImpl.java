package com.sky.service.impl;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.service.ReportService;
import com.sky.vo.TurnoverReportVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class ReportServiceImpl implements ReportService {

    @Autowired
    private OrderMapper orderMapper;

    /**
     * 统计指定区间内的营业额数据
     *
     * @param begin
     * @param end
     * @return
     */
    @Override
    public TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end) {
        //先拿到指定区间内的每一天的时间
        //使用集合来封装每一天
        List<LocalDate> dateList = new ArrayList<>();

        dateList.add(begin);
        while (!begin.equals(end)) {
            //当开始时间等于结束时间的时候就停止循环
            begin = begin.plusDays(1); //开始日期加1天
            dateList.add(begin); //把每一天添加进集合中
        }

        //创建集合存放每天的营业额
        List<Double> turnoverList = new ArrayList<>();
        //根据每一天的日期获取每天的营业额，营业额是指每天已完成订单的金额总数
        for (LocalDate date : dateList) {
            //因为数据库中的下单日期类型是：LocalDateTime，而前端传过来的是LocalDate，我们需要精确到秒，拿到最大最小时间
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);
            //创建Map集合来存放数据
            Map map = new HashMap();
            map.put("begin", beginTime); //开始时间
            map.put("end", endTime); //结束时间
            map.put("status", Orders.COMPLETED); //订单状态为 “已完成”
            //数据库查询语句：select sum(amount) from orders where order_time > ? and order_time < ?  and status = 5;
            Double turnover = orderMapper.sumByMap(map);

            //进行判断，如果当天营业额为0，则赋值0.0，不然会在集合中表示为null。 用三目运算符解决
            turnover = turnover == null ? 0.0 : turnover;
            //最后把查询到的营业额放入集合中
            turnoverList.add(turnover);
        }


        //构建并返回VO对象
        return TurnoverReportVO.builder()
                .dateList(StringUtils.join(dateList, ",")) //每一天
                .turnoverList(org.apache.commons.lang3.StringUtils.join(turnoverList)) //每一天营业额
                .build();
    }
}
