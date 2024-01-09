package com.sky.controller.user;

import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersPaymentDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.entity.OrderDetail;
import com.sky.entity.Orders;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RequestMapping("/user/order")
@RestController("userOrderController")
@Api(tags = "用户端订单相关接口")
public class OrderController {

    @Autowired
    private OrderService orderService;

    /**
     * 用户下单
     *
     * @param ordersSubmitDTO
     * @return
     */
    @PostMapping("/submit")
    @ApiOperation("用户下单")
    public Result<OrderSubmitVO> submit(@RequestBody OrdersSubmitDTO ordersSubmitDTO) {
        log.info("用户下单:{}",ordersSubmitDTO);
        OrderSubmitVO orderSubmitVO = orderService.submitOrder(ordersSubmitDTO);
        return Result.success(orderSubmitVO);
    }

    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */
    @PutMapping("/payment")
    @ApiOperation("订单支付")
    public Result<OrderPaymentVO> payment(@RequestBody OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        log.info("订单支付：{}", ordersPaymentDTO);
        orderService.payment(ordersPaymentDTO);
        return Result.success();
    }

    /**
     * 跳过微信支付
     * @param orderNumber
     * @return
     */
    @ApiOperation("支付成功")
    @PutMapping("/paySuccess")
    public Result<String> paySuccess(@RequestBody String orderNumber){
        log.info("订单支付成功:{}",orderNumber);
        orderService.paySuccess(orderNumber);
        return Result.success();
    }


    /**
     * 历史订单查询
     * @param page
     * @param pageSize
     * @param status  订单状态 1待付款 2待接单 3已接单 4派送中 5已完成 6已取消
     * @return
     */
    @ApiOperation("历史订单查询")
    @GetMapping("/historyOrders")
    public Result<PageResult> page( int page, int pageSize, Integer status ){
        log.info("历史订单查询:{}",page,pageSize,status);
        PageResult pageResult =  orderService.pageQuery4User(page,pageSize,status);
        return Result.success(pageResult);
    }

    /**
     * 查询订单详情
     * @param id
     * @return
     */
    @ApiOperation("查询订单详情")
    @GetMapping("/orderDetail/{id}")
    public Result<OrderVO> details(@PathVariable("id") Long id){
        log.info("查询订单详情:{}",id);
        OrderVO orderVO = orderService.details(id);
        return Result.success(orderVO);
    }


    /**
     * 用户取消订单
     * @return
     */
    @ApiOperation("取消订单")
    @PutMapping("/cancel/{id}")
    public Result<String> cancelOrder(@PathVariable("id") Long id) throws Exception {
        log.info("用户取消订单:{}",id);
        orderService.userCancelById(id);
        return Result.success();
    }

    /**
     * 再来一单  -- 业务规则：再来一单就是将原订单中的商品重新加入到购物车中
     * @param id
     * @return
     */
    @ApiOperation("再来一单")
    @PostMapping("/repetition/{id}")
    public Result<String> repetition(@PathVariable("id") Long id){
        log.info("再来一单，订单id:{}",id);
        orderService.repetitionOrder(id);
        return Result.success();
    }

    /**
     * 客户催单
     * @param id
     * @return
     */
    @ApiOperation("客户催单")
    @GetMapping("/reminder/{id}")
    public Result reminder(@PathVariable("id") Long id){
        log.info("客户催单:{}",id);
        orderService.reminder(id);
        return Result.success();
    }

}










