package com.sky.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.context.BaseContext;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersPaymentDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.entity.*;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.OrderBusinessException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.*;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.utils.WeChatPayUtil;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;
    @Autowired
    private AddressBookMapper addressBookMapper;
    @Autowired
    private ShoppingCartMapper shoppingCartMapper;
    @Autowired
    private WeChatPayUtil weChatPayUtil;
    @Autowired
    private UserMapper userMapper;
    //设置全局变量，跳过微信支付的订单号
    //public String orderNumber;

    /**
     * 用户下订单
     *
     * @param ordersSubmitDTO
     * @return
     */
    @Transactional
    public OrderSubmitVO submitOrder(OrdersSubmitDTO ordersSubmitDTO) {
        //1.判断用户提交的数据（地址簿、购物车）是否为空，为空则抛出业务异常
        Long addressBookId = ordersSubmitDTO.getAddressBookId();
        AddressBook addressBook = addressBookMapper.getById(addressBookId);
        if (addressBook == null) {
            //为空则抛出业务异常
            throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        }

        //判断购物车数据是否为空
        Long userId = BaseContext.getCurrentId();
        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setUserId(userId);
        List<ShoppingCart> shoppingCartList = shoppingCartMapper.list(shoppingCart);
        if (shoppingCartList == null || shoppingCartList.size() == 0) {
            //抛出业务异常
            throw new ShoppingCartBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
        }
        //2.向订单表插入1条数据
        Orders orders = new Orders();
        BeanUtils.copyProperties(ordersSubmitDTO, orders);
        orders.setOrderTime(LocalDateTime.now());
        orders.setPayStatus(Orders.UN_PAID);
        orders.setStatus(Orders.PENDING_PAYMENT);
        orders.setNumber(String.valueOf(System.currentTimeMillis()));//设置订单号
        orders.setPhone(addressBook.getPhone());
        orders.setConsignee(addressBook.getConsignee());
        orders.setUserId(userId);
        orderMapper.insert(orders);
        //获取订单号
        //orderNumber = orders.getNumber();
        //3.向订单明细表插入n条数据
        //放入集合批量插入
        List<OrderDetail> orderDetailList = new ArrayList<>();
        for (ShoppingCart cart : shoppingCartList) {
            OrderDetail orderDetail = new OrderDetail();//订单明细
            BeanUtils.copyProperties(cart, orderDetail);
            orderDetail.setOrderId(orders.getId()); //设置当前订单明细关联的订单id
            orderDetailList.add(orderDetail);
        }
        orderDetailMapper.insertBatch(orderDetailList);
        //4.清空购物车
        shoppingCartMapper.deleteById(userId);
        //5.封装OrderSubmitVO对象,返回VO对象
        OrderSubmitVO orderSubmitVO = OrderSubmitVO.builder()
                .id(orders.getId())
                .orderAmount(orders.getAmount())
                .orderTime(orders.getOrderTime())
                .orderNumber(orders.getNumber())
                .build();
        return orderSubmitVO;
    }

    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */
    public void payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception {
   /*     // 当前登录用户id
        Long userId = BaseContext.getCurrentId();
        User user = userMapper.getById(userId);

       *//* //调用微信支付接口，生成预支付交易单
        JSONObject jsonObject = weChatPayUtil.pay(
                ordersPaymentDTO.getOrderNumber(), //商户订单号
                new BigDecimal(0.01), //支付金额，单位 元
                "苍穹外卖订单", //商品描述
                user.getOpenid() //微信用户的openid
        );

        if (jsonObject.getString("code") != null && jsonObject.getString("code").equals("ORDERPAID")) {
            throw new OrderBusinessException("该订单已支付");
        }*//*
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("code", "ORDERPAID");
        OrderPaymentVO vo = jsonObject.toJavaObject(OrderPaymentVO.class);
        vo.setPackageStr(jsonObject.getString("package"));

        //为替代微信支付成功后的数据库订单状态更新，多定义一个方法进行修改
        Integer OrderPaidStatus = Orders.PAID; //支付状态，已支付
        Integer OrderStatus = Orders.TO_BE_CONFIRMED;  //订单状态，待接单
        //发现没有将支付时间 check_out属性赋值，所以在这里更新
        LocalDateTime checkOutTime = LocalDateTime.now();
        //更新订单状态
        orderMapper.updateStatus(OrderStatus, OrderPaidStatus, checkoutTime, orderNumber);*/

        //查询订单状态，若已支付则抛出异常 改造代码
        Orders order = new Orders();
        order.setNumber(ordersPaymentDTO.getOrderNumber());
        List<Orders> orderList = orderMapper.list(order);
        if (orderList != null && orderList.size() == 1) {
            order = orderList.get(0);
            if (order.getPayStatus() == Orders.PAID) {
                throw new OrderBusinessException(MessageConstant.ORDER_ALREADY_PAID);
            }
        } else
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
    }

    /**
     * 支付成功，修改订单状态
     *
     * @param orderNumber
     */
    public void paySuccess(String orderNumber) {

/*        // 根据订单号查询订单
        Orders ordersDB = orderMapper.getByNumber(outTradeNo);

        // 根据订单id更新订单的状态、支付方式、支付状态、结账时间
        Orders orders = Orders.builder()
                .id(ordersDB.getId())
                .status(Orders.TO_BE_CONFIRMED)
                .payStatus(Orders.PAID)
                .checkoutTime(LocalDateTime.now())
                .build();

        orderMapper.update(orders);*/

        //改造代码
        Orders order = Orders.builder()
                .number(orderNumber)
                .build();
        //根据订单id更新订单的状态、支付方式、支付状态、结账时间
        List<Orders> ordersList = orderMapper.list(order);
        if (ordersList != null && ordersList.size() == 1) {
            order = ordersList.get(0);
            order.setCheckoutTime(LocalDateTime.now());
            order.setPayStatus(Orders.PAID);
            order.setStatus(Orders.TO_BE_CONFIRMED);
            orderMapper.update(order); //修改支付状态、时间、订单状态
        }

        // 支付成功后返回首页需要清空购物车
        shoppingCartMapper.delete(order.getUserId());

    }


    /**
     * 用户端历史订单查询
     *
     * @param pageNum
     * @param pageSize
     * @param status
     * @return
     */
    @Override
    public PageResult pageQuery4User(int pageNum, int pageSize, Integer status) {
        //大体思路：分页条件 -- 分页查询订单信息 -- 根据订单号查询菜品详细信息（订单详情） -- 封装入OrderVO -- 通过PageResult返回
        //构建分页条件
        PageHelper.startPage(pageNum, pageSize);
        OrdersPageQueryDTO ordersPageQueryDTO = new OrdersPageQueryDTO();
        ordersPageQueryDTO.setStatus(status);//设置订单状态
        ordersPageQueryDTO.setUserId(BaseContext.getCurrentId());//设置用户id
        //分页查询
        Page<Orders> page = orderMapper.pageQuery(ordersPageQueryDTO);

        List<OrderVO> list = new ArrayList<>();
        //查询订单明细，
        //判断查询结果是否为空
        if (page != null && page.getTotal() > 0) {
            //遍历分页查询结果，获取每一个订单
            for (Orders orders : page) {
                Long orderId = orders.getId();//获取订单id
                List<OrderDetail> orderDetailList = orderDetailMapper.getByOrderId(orderId);//获取订单明细
                //封装如OrderVO进行响应
                OrderVO orderVO = new OrderVO();
                BeanUtils.copyProperties(orders, orderVO);//拷贝信息
                orderVO.setOrderDetailList(orderDetailList);
                //添加集合
                list.add(orderVO);
            }
        }
        return new PageResult(page.getTotal(), list);
    }


    /**
     * 用户查询订单详情
     *
     * @param id
     * @return
     */
    @Override
    public OrderVO details(Long id) {
        //根据订单id查询订单
        Orders orders = orderMapper.getById(id);
        //根据查询到的订单获取地址id查询地址
        AddressBook addressBook = addressBookMapper.getById(orders.getAddressBookId());
        //设置地址
        orders.setAddress(addressBook.getProvinceName() + addressBook.getCityName() + addressBook.getDistrictName() + addressBook.getDetail());
        //根据查询到的订单的id，查询该订单对应的菜品/套餐明细
        List<OrderDetail> orderDetails = orderDetailMapper.getByOrderId(orders.getId());

        // 将该订单及其详情封装到OrderVO并返回
        OrderVO orderVO = new OrderVO();
        BeanUtils.copyProperties(orders, orderVO);
        orderVO.setOrderDetailList(orderDetails);

        return orderVO;
    }


    /**
     * 用户取消订单
     */
    @Override
    public void userCancelById(Long id) throws Exception {
        //业务规则：1.取消订单前，需要判断订单状态 -- 未支付、待接单（用户可以直接取消）
        //已接单、派送中（用户需要打电话跟商家沟通取消订单） 。 2. 取消订单后，商家需要给用户退款，并且把订单状态改为已取消

        //查询订单状态
        Orders ordersDB = orderMapper.getById(id);
        //判断订单是否存在
        if (ordersDB == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }

        //订单状态 1待付款 2待接单 3已接单 4派送中 5已完成 6已取消
        if (ordersDB.getStatus() > 2) {
            //只要订单不是 待付款和待接单就抛出异常,让用户自己去跟商家电话沟通协商
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }


        Orders orders = new Orders();
        orders.setId(ordersDB.getId());
        // 订单处于待接单状态下取消，需要进行退款
        if (ordersDB.getStatus().equals(Orders.TO_BE_CONFIRMED)) {
            //个人用户无法使用微信支付，所以直接改状态
            //调用微信支付退款接口
           /* weChatPayUtil.refund(
                    ordersDB.getNumber(), //商户订单号
                    ordersDB.getNumber(), //商户退款单号
                    new BigDecimal(0.01),//退款金额，单位 元
                    new BigDecimal(0.01));//原订单金额*/

            //支付状态修改为 退款
            orders.setPayStatus(Orders.REFUND);
        }

        // 更新订单状态、取消原因、取消时间
        orders.setStatus(Orders.CANCELLED);
        orders.setCancelReason("用户取消");
        orders.setCancelTime(LocalDateTime.now());
        orderMapper.update(orders);
    }


    /**
     * 用户再来一单
     *
     * @param id
     */
    @Override
    public void repetitionOrder(Long id) {
        // 查询当前用户id
        Long userId = BaseContext.getCurrentId();
        // 根据订单id查询当前订单详情
        List<OrderDetail> orderDetailList = orderDetailMapper.getByOrderId(id);
        //根据订单详情转换为购物车对象
        List<ShoppingCart> shoppingCartList = orderDetailList.stream()
                .map(orderDetail -> {
                    ShoppingCart shoppingCart = new ShoppingCart();
                    //拷贝属性 --第三个参数是忽略某个属性
                    BeanUtils.copyProperties(orderDetail, shoppingCart, "id");
                    shoppingCart.setUserId(userId);
                    shoppingCart.setCreateTime(LocalDateTime.now());
                    //因为是匿名内部类，所以需要把shoppingCart对象返回才能获取到
                    return shoppingCart;
                }).collect(Collectors.toList());

        //讲购物车对象批量添加进数据库
        shoppingCartMapper.insertBatch(shoppingCartList);
    }


    /**
     * 商家条件搜索订单
     *
     * @param ordersPageQueryDTO
     * @return
     */
    @Override
    public PageResult conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO) {
        //构建分页条件
        PageHelper.startPage(ordersPageQueryDTO.getPage(), ordersPageQueryDTO.getPageSize());
        Page<Orders> page = orderMapper.pageQuery(ordersPageQueryDTO);

        //部分订单状态，需要额外返回订单菜品信息， 将Orders转化为OrdersVO
        List<OrderVO> orderVOList = getOrderVOList(page);

        return new PageResult(page.getTotal(), orderVOList);
    }

    /**
     * 将Orders转化为OrdersVO
     *
     * @param page
     * @return
     */
    private List<OrderVO> getOrderVOList(Page<Orders> page) {
        //需要返回订单菜品信息，自定义OrderVO响应结果
        List<OrderVO> orderVOList = new ArrayList<>();
        //获取订单集合
        List<Orders> ordersList = page.getResult();
        if (!CollectionUtils.isEmpty(ordersList)) {
            for (Orders orders : ordersList) {
                //获取地址
                AddressBook addressBook = addressBookMapper.getById(orders.getAddressBookId());
                orders.setAddress(addressBook.getProvinceName() + addressBook.getCityName()
                        + addressBook.getDistrictName() + " " +"("+ addressBook.getDetail() + ")");
                //把共同字段拷贝到OrderVO
                OrderVO orderVO = new OrderVO();
                BeanUtils.copyProperties(orders, orderVO);
                String orderDishes = getOrderDishesStr(orders);
                //设置菜品详情
                orderVO.setOrderDishes(orderDishes);
                //放入集合中
                orderVOList.add(orderVO);
            }
        }
        return orderVOList;
    }

    /**
     * 根据订单id获取菜品信息字符串
     *
     * @param orders
     * @return
     */
    private String getOrderDishesStr(Orders orders) {
        // 查询订单菜品详情信息（订单中的菜品和数量）
        List<OrderDetail> orderDetailList = orderDetailMapper.getByOrderId(orders.getId());
        //将每一条订单菜品信息拼接为字符串（格式：宫保鸡丁*3；）
        List<String> orderDishList = orderDetailList.stream()
                .map(orderDetail -> {
                    String orderDish = orderDetail.getName() + "*" + orderDetail.getNumber() + ";";
                    return orderDish;
                }).collect(Collectors.toList());
        // 将该订单对应的所有菜品信息拼接在一起
        String orderDishes = String.join("", orderDishList);
        return orderDishes;
    }

    /**
     * 商家统计各个状态订单数量
     * @return
     */
    @Override
    public OrderStatisticsVO statistics() {
        //统计各个状态的订单， 根据状态，分别查询出待接单、待派送、派送中的订单数量
        //1.查询订单状态  2--待接单  3--待派送  4--派送中
        Integer toBeConfirmed = orderMapper.countStatus(Orders.TO_BE_CONFIRMED);
        Integer confirmed = orderMapper.countStatus(Orders.CONFIRMED);
        Integer deliveryInProgress = orderMapper.countStatus(Orders.DELIVERY_IN_PROGRESS);
        //2.给VO对应属性赋值
        OrderStatisticsVO orderStatisticsVO = new OrderStatisticsVO();
        orderStatisticsVO.setToBeConfirmed(toBeConfirmed);
        orderStatisticsVO.setConfirmed(confirmed);
        orderStatisticsVO.setDeliveryInProgress(deliveryInProgress);

        return orderStatisticsVO;

    }
}
