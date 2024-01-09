package com.sky.controller.admin;

import com.sky.dto.OrdersCancelDTO;
import com.sky.dto.OrdersConfirmDTO;
import com.sky.dto.OrdersPageQueryDTO;

import com.sky.dto.OrdersRejectionDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RequestMapping("/admin/order")
@RestController("adminOrderController")
@Api(tags = "订单管理接口")
public class OrderController {

    @Autowired
    private OrderService orderService;

    /**
     * 订单搜索
     *
     * @param ordersPageQueryDTO
     * @return
     */
    @ApiOperation("订单搜索")
    @GetMapping("/conditionSearch")
    public Result<PageResult> conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO) {
        log.info("订单搜索：{}", ordersPageQueryDTO);
        PageResult pageResult = orderService.conditionSearch(ordersPageQueryDTO);
        return Result.success(pageResult);
    }

    /**
     * 各个订单状态数量统计
     *
     * @return
     */
    @ApiOperation("各个状态订单数量统计")
    @GetMapping("/statistics")
    public Result<OrderStatisticsVO> statistics() {
        log.info("各个状态订单数量统计:");
        OrderStatisticsVO orderStatisticsVO = orderService.statistics();
        return Result.success(orderStatisticsVO);
    }


    /**
     * 查询订单详情
     *
     * @param id
     * @return
     */
    @ApiOperation("查询订单详情")
    @GetMapping("/details/{id}")
    public Result<OrderVO> details(@PathVariable("id") Long id) {
        log.info("查询订单详情:{}", id);
        OrderVO orderVO = orderService.details(id);
        return Result.success(orderVO);
    }

    /**
     * 接单
     *
     * @param ordersConfirmDTO
     * @return
     */
    @ApiOperation("接单")
    @PutMapping("/confirm")
    public Result<String> confirm(@RequestBody OrdersConfirmDTO ordersConfirmDTO) {
        log.info("接单:{}", ordersConfirmDTO);
        //商家接单其实就是将订单的状态修改为“已接单”
        orderService.confirm(ordersConfirmDTO);
        return Result.success();
    }

    /**
     * 拒单
     *
     * @param ordersRejectionDTO
     * @return
     */
    @ApiOperation("拒单")
    @PutMapping("/rejection")
    public Result<String> rejection(@RequestBody OrdersRejectionDTO ordersRejectionDTO) {
        log.info("拒单:{}", ordersRejectionDTO);
        orderService.rejection(ordersRejectionDTO);
        return Result.success();
    }

    /**
     * 取消订单
     *
     * @param ordersCancelDTO
     * @return
     */
    @ApiOperation("取消订单")
    @PutMapping("/cancel")
    public Result<String> cancel(@RequestBody OrdersCancelDTO ordersCancelDTO) {
        log.info("取消订单：{}",ordersCancelDTO);
        orderService.adminCancel(ordersCancelDTO);
        return Result.success();
    }

    /**
     * 派送订单
     * @param id
     * @return
     */
    @ApiOperation("派送订单")
    @PutMapping("/delivery/{id}")
    public Result<String> delivery(@PathVariable("id") Long id){
        log.info("派送订单:{}",id);
        orderService.delivery(id);
        return Result.success();
    }

    /**
     * 完成订单
     *
     * @return
     */
    @PutMapping("/complete/{id}")
    @ApiOperation("完成订单")
    public Result complete(@PathVariable("id") Long id) {
        orderService.complete(id);
        return Result.success();
    }

}
