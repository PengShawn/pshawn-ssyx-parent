package com.psjava.ssyx.order.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.psjava.ssyx.model.order.OrderItem;
import com.psjava.ssyx.order.mapper.OrderItemMapper;
import com.psjava.ssyx.order.service.OrderItemService;
import org.springframework.stereotype.Service;

@Service
public class OrderItemServiceImpl extends ServiceImpl<OrderItemMapper, OrderItem> implements OrderItemService {
}
