package com.sky.service.impl;

import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.vo.OrderReportVO;
import com.sky.vo.SalesTop10ReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
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
import java.util.stream.Collectors;

@Service
@Slf4j
public class ReportServiceImpl implements ReportService {

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private UserMapper userMapper;

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

    /**
     * 统计指定区间内的用户数据
     *
     * @param begin
     * @param end
     * @return
     */
    @Override
    public UserReportVO getUserStatistics(LocalDate begin, LocalDate end) {
        //先拿到指定区间内的每一天的时间
        //使用集合来封装每一天
        List<LocalDate> dateList = new ArrayList<>();

        dateList.add(begin);
        while (!begin.equals(end)) {
            //当开始时间等于结束时间的时候就停止循环
            begin = begin.plusDays(1); //开始日期加1天
            dateList.add(begin); //把每一天添加进集合中
        }

        //存放统计每天的新增用户数量  select count(id) from user where create_time < ? and create_time > ?
        List<Integer> newUserList = new ArrayList<>();
        //存放统计指定区间的总用户数量  select count(id) from user where create_time < ?
        List<Integer> totalUserList = new ArrayList<>();

        for (LocalDate date : dateList) {
            //获取每一天的最大最小时间
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);

            Map map = new HashMap();
            //先统计总用户，利用动态SQL，一条查询语句可以复用，
            map.put("end", endTime);
            Integer totalUser = userMapper.countByMap(map);
            totalUser = totalUser == null ? 0 : totalUser; //非空判断，如果为空则吧null变为0，用于前端展示数据
            //再统计新增用户
            map.put("begin", beginTime);
            Integer newUser = userMapper.countByMap(map);
            newUser = newUser == null ? 0 : newUser; //非空判断，如果为空则吧null变为0，用于前端展示数据
            //放入集合中
            totalUserList.add(totalUser);
            newUserList.add(newUser);

        }
        //创建UserReportVO并返回
        return UserReportVO.builder()
                .dateList(StringUtils.join(dateList, ","))
                .newUserList(StringUtils.join(newUserList, ","))
                .totalUserList(StringUtils.join(totalUserList, ","))
                .build();
    }


    /**
     * 统计指定区间内订单数据
     *
     * @param begin
     * @param end
     * @return
     */
    @Override
    public OrderReportVO getOrdersStatistics(LocalDate begin, LocalDate end) {
        //先拿到指定区间内的每一天的时间
        //使用集合来封装每一天
        List<LocalDate> dateList = new ArrayList<>();

        dateList.add(begin);
        while (!begin.equals(end)) {
            //当开始时间等于结束时间的时候就停止循环
            begin = begin.plusDays(1); //开始日期加1天
            dateList.add(begin); //把每一天添加进集合中
        }

        //创建集合封装数据
        List<Integer> orderCountList = new ArrayList<>();
        List<Integer> validOrderCountList = new ArrayList<>();

        //遍历dateList集合，查询每天的订单总数和有效订单数
        for (LocalDate date : dateList) {
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);
            //查询每天的订单总数 select count(id) from orders where order_time > ? and order_time < ?
            Integer orderCount = getOrderCount(beginTime, endTime, null);
            //查询每天的有效订单数select count(id) from orders where order_time > ? and order_time < ? and status =5
            Integer validOrderCount = getOrderCount(beginTime, endTime, Orders.COMPLETED);

            orderCountList.add(orderCount);
            validOrderCountList.add(validOrderCount);
        }

        //统计订单总数 --使用stream流， reduce可以合并
        Integer totalOrderCount = orderCountList.stream().reduce(Integer::sum).get();

        //统计有效订单总数
        Integer totalValidOrderCount = validOrderCountList.stream().reduce(Integer::sum).get();

        Double orderCompletionRate = 0.0;
        if (totalOrderCount != 0) {
            //计算订单完成率
            orderCompletionRate = totalValidOrderCount.doubleValue() / totalOrderCount;
        }

        return OrderReportVO.builder()
                .dateList(StringUtils.join(dateList,","))
                .orderCountList(StringUtils.join(orderCountList,","))
                .validOrderCountList(StringUtils.join(validOrderCountList,","))
                .totalOrderCount(totalOrderCount)
                .validOrderCount(totalValidOrderCount)
                .orderCompletionRate(orderCompletionRate)
                .build();
    }


    /**
     * 根据条件统计订单数据
     *
     * @param begin
     * @param end
     * @param status
     * @return
     */
    private Integer getOrderCount(LocalDateTime begin, LocalDateTime end, Integer status) {
        Map map = new HashMap();
        map.put("begin", begin);
        map.put("end", end);
        map.put("status", status);

        return orderMapper.countByMap(map);
    }

    /**
     * 统计指定区间内销量Top10
     * @param begin
     * @param end
     * @return
     */
    @Override
    public SalesTop10ReportVO getSalesTop10(LocalDate begin, LocalDate end) {
        //获取时间区间
        LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);

        //获取top10
        List<GoodsSalesDTO> salesTop10 = orderMapper.getSalesTop10(beginTime, endTime);
        //stream流转换数据类型
        List<String> names = salesTop10.stream().map(GoodsSalesDTO::getName).collect(Collectors.toList());
        String nameList = StringUtils.join(names, ",");

        List<Integer> numbers = salesTop10.stream().map(GoodsSalesDTO::getNumber).collect(Collectors.toList());
        String numberList = StringUtils.join(numbers, ",");

        return SalesTop10ReportVO
                .builder()
                .nameList(nameList)
                .numberList(numberList)
                .build();
    }

}
