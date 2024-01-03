package com.sky.controller.admin;

import com.sky.dto.OrdersPageQueryDTO;

import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequestMapping("/admin/order")
@RestController("adminOrderController")
@Api(tags = "订单管理接口")
public class OrderController {

    @Autowired
    private OrderService orderService;

    /**
     * 订单搜索
     * @param ordersPageQueryDTO
     * @return
     */
    @ApiOperation("订单搜索")
    @GetMapping("/conditionSearch")
    public Result<PageResult> conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO){
        log.info("订单搜索：{}",ordersPageQueryDTO);
        PageResult pageResult =  orderService.conditionSearch(ordersPageQueryDTO);
        return Result.success(pageResult);
    }

    /**
     * 各个订单状态数量统计
     * @return
     */
    @ApiOperation("各个状态订单数量统计")
    @GetMapping("/statistics")
    public Result<OrderStatisticsVO> statistics(){
        log.info("各个状态订单数量统计:");
        OrderStatisticsVO orderStatisticsVO =  orderService.statistics();
        return Result.success(orderStatisticsVO);
    }
}
