package com.sky.service.impl;

import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.service.WorkspaceService;
import com.sky.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
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
    @Autowired
    private WorkspaceService workspaceService;

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
                .dateList(StringUtils.join(dateList, ","))
                .orderCountList(StringUtils.join(orderCountList, ","))
                .validOrderCountList(StringUtils.join(validOrderCountList, ","))
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
     *
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


    /**
     * 导出运营数据报表
     *
     * @param response
     */
    @Override
    public void exportBusinesData(HttpServletResponse response) {
        //1.查询数据库，获取数据 -- 查询最近30天的运营数据
        LocalDate dateBegin = LocalDate.now().minusDays(30); //当前时间减去30天
        LocalDate dateEnd = LocalDate.now().minusDays(1);
        //获取30天内的概览数据
        BusinessDataVO businessDataVO = workspaceService.getBusinessData(LocalDateTime.of(dateBegin, LocalTime.MIN), LocalDateTime.of(dateEnd, LocalTime.MAX));

        //2.通过POI写入到Excel文件中
        //通过反射获取输入流
        InputStream in = this.getClass().getClassLoader().getResourceAsStream("template/运营数据报表模板.xlsx");
        try {
            //基于模板创建一个Excel文件
            XSSFWorkbook excel = new XSSFWorkbook(in);
            //获取表格文件sheet页
            XSSFSheet sheet = excel.getSheet("Sheet1");

            //填充数据-- 时间
            sheet.getRow(1).getCell(1).setCellValue("时间:" + dateBegin + "至" + dateEnd);

            //获得第4行
            XSSFRow row = sheet.getRow(3);
            row.getCell(2).setCellValue(businessDataVO.getTurnover());
            row.getCell(4).setCellValue(businessDataVO.getOrderCompletionRate());
            row.getCell(6).setCellValue(businessDataVO.getNewUsers());

            //获得第5行
            row = sheet.getRow(4);
            row.getCell(2).setCellValue(businessDataVO.getValidOrderCount());
            row.getCell(4).setCellValue(businessDataVO.getUnitPrice());

            //填充明细数据
            for (int i = 0; i < 30; i++) {
                //从开始时间一天一天加
                LocalDate date = dateBegin.plusDays(i);
                //查询每一天的营业数据
                BusinessDataVO businessData = workspaceService.getBusinessData(LocalDateTime.of(date, LocalTime.MIN), LocalDateTime.of(date, LocalTime.MAX));

                //获取某一行
                row = sheet.getRow(7 + i);
                row.getCell(1).setCellValue(date.toString());
                row.getCell(2).setCellValue(businessData.getTurnover());
                row.getCell(3).setCellValue(businessData.getValidOrderCount());
                row.getCell(4).setCellValue(businessData.getOrderCompletionRate());
                row.getCell(5).setCellValue(businessData.getUnitPrice());
                row.getCell(6).setCellValue(businessData.getNewUsers());

            }

            //3.通过输出流把Excel文件写到客户端浏览器
            ServletOutputStream sos = response.getOutputStream();
            excel.write(sos);

            //关闭资源
            sos.close();
            excel.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
