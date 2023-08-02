package com.psjava.ssyx.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.psjava.ssyx.common.constant.RedisConst;
import com.psjava.ssyx.common.exception.SsyxException;
import com.psjava.ssyx.common.result.ResultCodeEnum;
import com.psjava.ssyx.model.product.SkuAttrValue;
import com.psjava.ssyx.model.product.SkuImage;
import com.psjava.ssyx.model.product.SkuInfo;
import com.psjava.ssyx.model.product.SkuPoster;
import com.psjava.ssyx.mq.constant.MqConst;
import com.psjava.ssyx.mq.service.RabbitService;
import com.psjava.ssyx.product.mapper.SkuInfoMapper;
import com.psjava.ssyx.product.service.SkuAttrValueService;
import com.psjava.ssyx.product.service.SkuImageService;
import com.psjava.ssyx.product.service.SkuInfoService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.psjava.ssyx.product.service.SkuPosterService;
import com.psjava.ssyx.vo.product.SkuInfoQueryVo;
import com.psjava.ssyx.vo.product.SkuInfoVo;
import com.psjava.ssyx.vo.product.SkuStockLockVo;
import org.redisson.RedissonTopic;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * <p>
 * sku信息 服务实现类
 * </p>
 *
 * @author pshawn
 * @since 2023-06-28
 */
@Service
public class SkuInfoServiceImpl extends ServiceImpl<SkuInfoMapper, SkuInfo> implements SkuInfoService {
    @Autowired
    private SkuPosterService skuPosterService;

    @Autowired
    private SkuImageService skuImagesService;

    @Autowired
    private SkuAttrValueService skuAttrValueService;

    @Autowired
    private RabbitService rabbitService;

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public IPage<SkuInfo> selectPage(Page<SkuInfo> pageParam, SkuInfoQueryVo skuInfoQueryVo) {
        //获取条件值
        String keyword = skuInfoQueryVo.getKeyword();
        String skuType = skuInfoQueryVo.getSkuType();
        Long categoryId = skuInfoQueryVo.getCategoryId();
        //封装条件
        LambdaQueryWrapper<SkuInfo> wrapper = new LambdaQueryWrapper<>();
        if(!StringUtils.isEmpty(keyword)) {
            wrapper.like(SkuInfo::getSkuName, keyword);
        }
        if(!StringUtils.isEmpty(skuType)) {
            wrapper.eq(SkuInfo::getSkuType, skuType);
        }
        if(!StringUtils.isEmpty(categoryId)) {
            wrapper.eq(SkuInfo::getCategoryId, categoryId);
        }
        //调用方法查询
        return baseMapper.selectPage(pageParam, wrapper);
    }

    //添加商品
    @Transactional(rollbackFor = {Exception.class})
    @Override
    public void saveSkuInfo(SkuInfoVo skuInfoVo) {
        //保存sku信息
        SkuInfo skuInfo = new SkuInfo();
        BeanUtils.copyProperties(skuInfoVo, skuInfo);
        this.save(skuInfo);

        //保存sku海报
        List<SkuPoster> skuPosterList = skuInfoVo.getSkuPosterList();
        if(!CollectionUtils.isEmpty(skuPosterList)) {
            for(SkuPoster skuPoster : skuPosterList) {
                skuPoster.setSkuId(skuInfo.getId());
            }
            skuPosterService.saveBatch(skuPosterList);
        }

        //保存sku图片
        List<SkuImage> skuImagesList = skuInfoVo.getSkuImagesList();
        if(!CollectionUtils.isEmpty(skuImagesList)) {
            int sort = 1;
            for(SkuImage skuImages : skuImagesList) {
                skuImages.setSkuId(skuInfo.getId());
                skuImages.setSort(sort);
                sort++;
            }
            skuImagesService.saveBatch(skuImagesList);
        }

        //保存sku平台属性
        List<SkuAttrValue> skuAttrValueList = skuInfoVo.getSkuAttrValueList();
        if(!CollectionUtils.isEmpty(skuAttrValueList)) {
            int sort = 1;
            for(SkuAttrValue skuAttrValue : skuAttrValueList) {
                skuAttrValue.setSkuId(skuInfo.getId());
                skuAttrValue.setSort(sort);
                sort++;
            }
            skuAttrValueService.saveBatch(skuAttrValueList);
        }
    }

    //获取商品
    @Override
    public SkuInfoVo getSkuInfoVo(Long skuId) {
        SkuInfoVo skuInfoVo = new SkuInfoVo();

        SkuInfo skuInfo = baseMapper.selectById(skuId);
        List<SkuImage> skuImageList = skuImagesService.findBySkuId(skuId);
        List<SkuPoster> skuPosterList = skuPosterService.findBySkuId(skuId);
        List<SkuAttrValue> skuAttrValueList = skuAttrValueService.findBySkuId(skuId);

        BeanUtils.copyProperties(skuInfo, skuInfoVo);
        skuInfoVo.setSkuImagesList(skuImageList);
        skuInfoVo.setSkuPosterList(skuPosterList);
        skuInfoVo.setSkuAttrValueList(skuAttrValueList);
        return skuInfoVo;
    }

    //修改商品
    @Transactional(rollbackFor = {Exception.class})
    @Override
    public void updateSkuInfo(SkuInfoVo skuInfoVo) {
        Long id = skuInfoVo.getId();
        SkuInfo skuInfo = new SkuInfo();
        BeanUtils.copyProperties(skuInfoVo, skuInfo);
        //更新sku信息
        this.updateById(skuInfo);

        //删除sku海报
        skuPosterService.remove(new LambdaQueryWrapper<SkuPoster>().eq(SkuPoster::getSkuId, id));
        //保存sku海报
        List<SkuPoster> skuPosterList = skuInfoVo.getSkuPosterList();
        if(!CollectionUtils.isEmpty(skuPosterList)) {
            for(SkuPoster skuPoster : skuPosterList) {
                skuPoster.setSkuId(id);
            }
            skuPosterService.saveBatch(skuPosterList);
        }

        //删除sku图片
        skuImagesService.remove(new LambdaQueryWrapper<SkuImage>().eq(SkuImage::getSkuId, id));
        //保存sku图片
        List<SkuImage> skuImagesList = skuInfoVo.getSkuImagesList();
        if(!CollectionUtils.isEmpty(skuImagesList)) {
            int sort = 1;
            for(SkuImage skuImages : skuImagesList) {
                skuImages.setSkuId(id);
                skuImages.setSort(sort);
                sort++;
            }
            skuImagesService.saveBatch(skuImagesList);
        }

        //删除sku平台属性
        skuAttrValueService.remove(new LambdaQueryWrapper<SkuAttrValue>().eq(SkuAttrValue::getSkuId, id));
        //保存sku平台属性
        List<SkuAttrValue> skuAttrValueList = skuInfoVo.getSkuAttrValueList();
        if(!CollectionUtils.isEmpty(skuAttrValueList)) {
            int sort = 1;
            for(SkuAttrValue skuAttrValue : skuAttrValueList) {
                skuAttrValue.setSkuId(id);
                skuAttrValue.setSort(sort);
                sort++;
            }
            skuAttrValueService.saveBatch(skuAttrValueList);
        }
    }

    //商品审核
    @Transactional(rollbackFor = {Exception.class})
    @Override
    public void check(Long skuId, Integer status) {
        // 更改发布状态
        SkuInfo skuInfoUp = baseMapper.selectById(skuId);
        skuInfoUp.setCheckStatus(status);
        baseMapper.updateById(skuInfoUp);
    }

    //商品上架
    @Transactional(rollbackFor = {Exception.class})
    @Override
    public void publish(Long skuId, Integer status) {
        SkuInfo skuInfoUp = baseMapper.selectById(skuId);
        // 更改发布状态
        if(status == 1) {
            skuInfoUp.setPublishStatus(1);
            baseMapper.updateById(skuInfoUp);
            //商品上架：发送mq消息同步es
            rabbitService.sendMessage(MqConst.EXCHANGE_GOODS_DIRECT, MqConst.ROUTING_GOODS_UPPER, skuId);
        } else {
            skuInfoUp.setPublishStatus(0);
            baseMapper.updateById(skuInfoUp);
            //商品下架：发送mq消息同步es
            rabbitService.sendMessage(MqConst.EXCHANGE_GOODS_DIRECT, MqConst.ROUTING_GOODS_LOWER, skuId);
        }
    }

    //新人专享
    @Override
    public void isNewPerson(Long skuId, Integer status) {
        SkuInfo skuInfoUp = baseMapper.selectById(skuId);
        skuInfoUp.setIsNewPerson(status);
        baseMapper.updateById(skuInfoUp);
    }

    //批量获取sku信息
    @Override
    public List<SkuInfo> findSkuInfoList(List<Long> skuIdList) {
        return baseMapper.selectBatchIds(skuIdList);
    }

    //根据关键字获取sku列表
    @Override
    public List<SkuInfo> findSkuInfoByKeyword(String keyword) {
        return baseMapper.selectList(new LambdaQueryWrapper<SkuInfo>().like(SkuInfo::getSkuName, keyword));
    }

    //获取新人专享商品
    @Override
    public List<SkuInfo> findNewPersonSkuInfoList() {
        //条件1：is_new_person=1
        //条件2：publish_status=1
        //条件3：显示其中三个
        //获取第一页数据，每页显示三条记录
        Page<SkuInfo> pageParam = new Page<>(1,3);
        //封装条件
        LambdaQueryWrapper<SkuInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SkuInfo::getIsNewPerson,1);
        wrapper.eq(SkuInfo::getPublishStatus,1);
        wrapper.orderByDesc(SkuInfo::getStock);//库存排序
        //调用方法查询
        IPage<SkuInfo> skuInfoPage = baseMapper.selectPage(pageParam, wrapper);
        return skuInfoPage.getRecords();
    }

    @Transactional(rollbackFor = {Exception.class})
    @Override
    public Boolean checkAndLock(List<SkuStockLockVo> skuStockLockVoList,
                                String orderToken) {
        if (CollectionUtils.isEmpty(skuStockLockVoList)){
            throw new SsyxException(ResultCodeEnum.DATA_ERROR);
        }

        // 遍历所有商品，验库存并锁库存，要具备原子性
        skuStockLockVoList.forEach(this::checkLock);

        // 只要有一个商品锁定失败，所有锁定成功的商品要解锁库存
        if (skuStockLockVoList.stream().anyMatch(skuStockLockVo -> !skuStockLockVo.getIsLock())) {
            // 获取所有锁定成功的商品，遍历解锁库存
            skuStockLockVoList.stream().filter(SkuStockLockVo::getIsLock).forEach(skuStockLockVo -> {
                baseMapper.unlockStock(skuStockLockVo.getSkuId(), skuStockLockVo.getSkuNum());
            });
            // 响应锁定状态
            return false;
        }

        // 如果所有商品都锁定成功的情况下，需要缓存锁定信息到redis。以方便将来解锁库存 或者 减库存
        // 以orderToken作为key，以lockVos锁定信息作为value
        redisTemplate.opsForValue().set(RedisConst.SROCK_INFO + orderToken, skuStockLockVoList);

        // 锁定库存成功之后，定时解锁库存。
        //this.rabbitTemplate.convertAndSend("ORDER_EXCHANGE", "stock.ttl", orderToken);
        return true;
    }

    //遍历list得到每个商品，验证库存并锁定库存，具备原子性
    private void checkLock(SkuStockLockVo skuStockLockVo){
        //公平锁，就是保证客户端获取锁的顺序，跟他们请求获取锁的顺序，是一样的。
        // 公平锁需要排队
        // ，谁先申请获取这把锁，
        // 谁就可以先获取到这把锁，是按照请求的先后顺序来的。
        RLock rLock = this.redissonClient
                .getFairLock(RedisConst.SKUKEY_PREFIX + skuStockLockVo.getSkuId());
        rLock.lock();

        try {
            // 验库存：查询，返回的是满足要求的库存列表
            SkuInfo skuInfo = baseMapper.checkStock(skuStockLockVo.getSkuId(), skuStockLockVo.getSkuNum());
            // 如果没有一个仓库满足要求，这里就验库存失败
            if (null == skuInfo) {
                skuStockLockVo.setIsLock(false);
                return;
            }

            // 锁库存：更新
            Integer row = baseMapper.lockStock(skuStockLockVo.getSkuId(), skuStockLockVo.getSkuNum());
            if (row == 1) {
                skuStockLockVo.setIsLock(true);
            }
        } finally {
            rLock.unlock();
        }
    }
}
