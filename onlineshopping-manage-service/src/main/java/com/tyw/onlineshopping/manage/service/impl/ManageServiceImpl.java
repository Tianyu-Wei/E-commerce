package com.tyw.onlineshopping.manage.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.tyw.onlineshopping.bean.*;
import com.tyw.onlineshopping.manage.mapper.*;
import com.tyw.onlineshopping.service.ManageService;
import com.tyw.onlineshopping.util.RedisUtil;
import org.apache.commons.lang3.StringUtils;
import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import redis.clients.jedis.Jedis;

import tk.mybatis.mapper.entity.Example;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class ManageServiceImpl implements ManageService {

    @Autowired
    RedisUtil redisUtil;

    @Autowired
    SkuAttrValueMapper skuAttrValueMapper;

    @Autowired
    SkuImageMapper skuImageMapper;

    @Autowired
    SkuInfoMapper skuInfoMapper;

    @Autowired
    SkuSaleAttrValueMapper skuSaleAttrValueMapper;

    @Autowired
    SpuInfoMapper spuInfoMapper;

    @Autowired
    SpuImageMapper spuImageMapper;

    @Autowired
    SpuSaleAttrMapper spuSaleAttrMapper;

    @Autowired
    SpuSaleAttrValueMapper spuSaleAttrValueMapper;

    @Autowired
    BaseSaleAttrMapper baseSaleAttrMapper;

    @Autowired
    BaseAttrValueMapper baseAttrValueMapper;

    @Autowired
    BaseAttrInfoMapper baseAttrInfoMapper;

    @Autowired
    BaseCatalog1Mapper baseCatalog1Mapper;

    @Autowired
    BaseCatalog2Mapper baseCatalog2Mapper;

    @Autowired
    BaseCatalog3Mapper baseCatalog3Mapper;

    public static final String SKUKEY_PREFIX="sku:";
    public static final String SKUKEY_INFO_SUFFIX=":info";
    public static final String SKUKEY_LOCK_SUFFIX=":lock";

    @Override
    public List<BaseCatalog1> getCatalog1() {
        return baseCatalog1Mapper.selectAll();
    }

    @Override
    public List<BaseCatalog2> getCatalog2(String basecatalogId) {
        BaseCatalog2 baseCatalog2 = new BaseCatalog2();
        baseCatalog2.setCatalog1Id(basecatalogId);
        List<BaseCatalog2> baseCatalog2List = baseCatalog2Mapper.select(baseCatalog2);
        return baseCatalog2List;
    }

    @Override
    public List<BaseCatalog3> getCatalog3(String basecatalogId) {
        BaseCatalog3 baseCatalog3 = new BaseCatalog3();
        baseCatalog3.setCatalog2Id(basecatalogId);
        List<BaseCatalog3> baseCatalog3List = baseCatalog3Mapper.select(baseCatalog3);
        return baseCatalog3List;
    }

    @Override
    public List<BaseAttrInfo> getAttrList(String catalogId) {
//        Example example = new Example(BaseAttrInfo.class);
//        example.createCriteria().andEqualTo("catalog3Id", catalogId);
//        List<BaseAttrInfo> baseAttrInfoList = baseAttrInfoMapper.selectByExample(example);
//        //Check attribute value from platform
//        for (BaseAttrInfo baseAttrInfo : baseAttrInfoList) {
//            BaseAttrValue baseAttrValue = new BaseAttrValue();
//            baseAttrValue.setAttrId(baseAttrInfo.getId());
//            List<BaseAttrValue> baseAttrValueList = baseAttrValueMapper.select(baseAttrValue);
//            baseAttrInfo.setAttrValueList(baseAttrValueList);
//        }

        List<BaseAttrInfo> baseAttrInfoList = baseAttrInfoMapper.getBaseAttrInfoListByCatalog3Id(catalogId);
        return baseAttrInfoList;
    }

    @Override
    @Transactional
    public void saveAttrInfo(BaseAttrInfo baseAttrInfo) {
        if (baseAttrInfo.getId() != null && baseAttrInfo.getId().length() > 0) {
            baseAttrInfoMapper.updateByPrimaryKeySelective(baseAttrInfo);
        } else {
            baseAttrInfo.setId(null);
            baseAttrInfoMapper.insertSelective(baseAttrInfo);
        }

        Example example = new Example(BaseAttrValue.class);
        example.createCriteria().andEqualTo("attrId", baseAttrInfo.getId());

        //Delete attrId when it comes to save new attr or trying to update it based on whether attrId exists or not.
        baseAttrValueMapper.deleteByExample(example);

        List<BaseAttrValue> baseAttrValueList = baseAttrInfo.getAttrValueList();
        for (BaseAttrValue baseAttrValue : baseAttrValueList) {
            String id = baseAttrInfo.getId();
            baseAttrValue.setAttrId(id);
            baseAttrValueMapper.insertSelective(baseAttrValue);
        }
    }

    @Override
    public BaseAttrInfo getBaseAttrInfo(String attrId) {
        BaseAttrInfo baseAttrInfo = baseAttrInfoMapper.selectByPrimaryKey(attrId);
        BaseAttrValue baseAttrValueQuery = new BaseAttrValue();
        baseAttrValueQuery.setAttrId(attrId);
        List<BaseAttrValue> baseAttrValueList = baseAttrValueMapper.select(baseAttrValueQuery);

        baseAttrInfo.setAttrValueList(baseAttrValueList);

        return baseAttrInfo;
    }

    @Override
    public List<BaseSaleAttr> getBaseSaleAttrList() {
        return baseSaleAttrMapper.selectAll();
    }

    @Override
    public void saveSpuInfo(SpuInfo spuInfo) {

        //save spu basic information
        spuInfoMapper.insertSelective(spuInfo);

        //save information of image
        List<SpuImage> spuImageList = spuInfo.getSpuImageList();
        for (SpuImage spuImage : spuImageList) {
            spuImage.setSpuId(spuInfo.getId());
            spuImageMapper.insertSelective(spuImage);
        }

        //selling attributes
        List<SpuSaleAttr> spuSaleAttrList = spuInfo.getSpuSaleAttrList();
        for (SpuSaleAttr spuSaleAttr : spuSaleAttrList) {
            spuSaleAttr.setSpuId(spuInfo.getId());
            spuSaleAttrMapper.insertSelective(spuSaleAttr);

            //values of selling attributes
            List<SpuSaleAttrValue> spuSaleAttrValueList = spuSaleAttr.getSpuSaleAttrValueList();
            for (SpuSaleAttrValue spuSaleAttrValue : spuSaleAttrValueList) {
                spuSaleAttrValue.setSpuId(spuInfo.getId());
                spuSaleAttrValueMapper.insertSelective(spuSaleAttrValue);
            }
        }
    }

    @Override
    public List<SpuInfo> getSpuList(String catalog3Id) {
        SpuInfo spuInfo = new SpuInfo();
        spuInfo.setCatalog3Id(catalog3Id);
        return spuInfoMapper.select(spuInfo);
    }

    @Override
    public List<SpuImage> getSpuImageList(String spuId) {
        SpuImage spuImage = new SpuImage();
        spuImage.setSpuId(spuId);
        return spuImageMapper.select(spuImage);
    }

    @Override
    public List<SpuSaleAttr> getSpuSaleAttrList(String spuId) {
        return spuSaleAttrMapper.getSpuSaleAttrListBySpuId(spuId);
    }

    @Override
    @Transactional
    public void saveSkuInfo(SkuInfo skuInfo) {
        //save basic information
        if (skuInfo.getId() == null || skuInfo.getId().length() == 0) {
            skuInfoMapper.insertSelective(skuInfo);
        } else {
            skuInfoMapper.updateByPrimaryKeySelective(skuInfo);
        }

        //platform attributes;
        SkuAttrValue skuAttrValue = new SkuAttrValue();
        skuAttrValue.setSkuId(skuInfo.getId());
        skuAttrValueMapper.delete(skuAttrValue);

        List<SkuAttrValue> skuAttrValues = skuInfo.getSkuAttrValueList();
        for (SkuAttrValue attrValue : skuAttrValues) {
            attrValue.setSkuId(skuInfo.getId());
            skuAttrValueMapper.insertSelective(attrValue);
        }

        //Selling attributes;
        SkuSaleAttrValue skuSaleAttrValue = new SkuSaleAttrValue();
        skuSaleAttrValue.setSkuId(skuInfo.getId());
        skuSaleAttrValueMapper.delete(skuSaleAttrValue);
        List<SkuSaleAttrValue> skuSaleAttrValueList = skuInfo.getSkuSaleAttrValueList();
        for (SkuSaleAttrValue saleAttrValue : skuSaleAttrValueList) {
            saleAttrValue.setSkuId(skuInfo.getId());
            skuSaleAttrValueMapper.insertSelective(saleAttrValue);
        }

        //Image

        SkuImage skuImage = new SkuImage();
        skuImage.setId(skuInfo.getId());
        skuImageMapper.delete(skuImage);

        for (SkuImage image : skuInfo.getSkuImageList()) {
            image.setSkuId(skuInfo.getId());
            skuImageMapper.insertSelective(image);
        }
    }

    public SkuInfo getSkuInfoDB(String skuId) {
        System.out.println(Thread.currentThread() + "Reading from database!");
//        try{
//            Thread.sleep(3000);
//        }catch (InterruptedException e){
//            e.printStackTrace();
//        }

        SkuInfo skuInfo = skuInfoMapper.selectByPrimaryKey(skuId);
        if (skuInfo == null) {
            return null;
        }
        SkuImage skuImage = new SkuImage();
        skuImage.setSkuId(skuId);
        List<SkuImage> skuImageList = skuImageMapper.select(skuImage);
        skuInfo.setSkuImageList(skuImageList);

        SkuSaleAttrValue skuSaleAttrValue = new SkuSaleAttrValue();
        skuSaleAttrValue.setSkuId(skuId);
        List<SkuSaleAttrValue> skuSaleAttrValueList = skuSaleAttrValueMapper.select(skuSaleAttrValue);
        skuInfo.setSkuSaleAttrValueList(skuSaleAttrValueList);

        SkuAttrValue skuAttrValue = new SkuAttrValue();
        skuAttrValue.setSkuId(skuId);
        List<SkuAttrValue> skuAttrValueList = skuAttrValueMapper.select(skuAttrValue);
        skuInfo.setSkuAttrValueList(skuAttrValueList);

        return skuInfo;
    }

    public SkuInfo getSkuInfo_redis(String skuId) {

        SkuInfo skuInfoResult = null;
        //1 check redis if not exists check database
        Jedis jedis = redisUtil.getJedis();
        int SKU_EXPIRE_SEC=100;
        //The structure of redis type key value
        String skuKey=SKUKEY_PREFIX + skuId + SKUKEY_INFO_SUFFIX;
        String skuInfoJson = jedis.get(skuKey);
        if (skuInfoJson!=null){
            if (!"EMPTY".equals(skuInfoJson)) {
                System.out.println(Thread.currentThread()+"Hit cache");
                skuInfoResult = JSON.parseObject(skuInfoJson, SkuInfo.class);
            }
        }else {
            System.out.println(Thread.currentThread()+"Not hit cache");
            String lockKey = SKUKEY_PREFIX + skuId +SKUKEY_LOCK_SUFFIX;

            String token = UUID.randomUUID().toString();
            String locked = jedis.set(lockKey, token, "NX", "EX", 100);
            if ("OK".equals(locked)){
                skuInfoResult = getSkuInfoDB(skuId);
                System.out.println(Thread.currentThread()+"Cache Writing!");
                String skuInfoJsonResult = null;
                if (skuInfoResult != null) {
                    skuInfoJsonResult = JSON.toJSONString(skuInfoResult);
                }else {
                    skuInfoJsonResult = "EMPTY";
                }

                jedis.setex(skuKey, SKU_EXPIRE_SEC, skuInfoJsonResult);
                System.out.println(Thread.currentThread() + "Releasing lock!" + lockKey);
                if (jedis.exists(lockKey) && token.equals(jedis.get(lockKey))) {
                    jedis.del(lockKey);
                }
            }else {
                System.out.println(Thread.currentThread() + "Not getting lock! Start looping over");
                try {
                    Thread.sleep(1000);
                }catch (InterruptedException e){
                    e.printStackTrace();
                }
            }
        }

        jedis.close();
        return skuInfoResult;
    }

    @Override
    public SkuInfo getSkuInfo(String skuId) throws InterruptedException {
        SkuInfo skuInfoResult = null;
        //1 check redis if not exists check database
        Jedis jedis = redisUtil.getJedis();
        int SKU_EXPIRE_SEC = 100;
        //The structure of redis type key value
        String skuKey = SKUKEY_PREFIX + skuId + SKUKEY_INFO_SUFFIX;
        String skuInfoJson = jedis.get(skuKey);
        if (skuInfoJson != null) {
            if (!"EMPTY".equals(skuInfoJson)) {
                System.out.println(Thread.currentThread() + "Hit cache");
                skuInfoResult = JSON.parseObject(skuInfoJson, SkuInfo.class);
            }
        } else {
            Config config = new Config();
            config.useSingleServer().setAddress("redis://manage.gmall.com:6379");

            RedissonClient redissonClient = Redisson.create(config);
            String lockKey=SKUKEY_PREFIX+skuId+SKUKEY_LOCK_SUFFIX;
            RLock rLock = redissonClient.getLock(lockKey);
//            rLock.lock(10, TimeUnit.SECONDS);
            boolean locked=false ;
            try {
                locked = rLock.tryLock(10, 5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }


            if (locked) {
                String skuInfoJsonResult = jedis.get(skuKey);
                if (skuInfoJsonResult != null) {
                    if (!"EMPTY".equals(skuInfoJsonResult)) {
                        System.out.println(Thread.currentThread() + "Hit cache");
                        skuInfoResult = JSON.parseObject(skuInfoJsonResult, SkuInfo.class);
                    }
                } else {

                    skuInfoResult = getSkuInfoDB(skuId);
                    System.out.println(Thread.currentThread() + "Cache Writing!");
                    
                    if (skuInfoResult != null) {
                        skuInfoJsonResult = JSON.toJSONString(skuInfoResult);
                    } else {
                        skuInfoJsonResult = "EMPTY";
                    }

                    jedis.setex(skuKey, SKU_EXPIRE_SEC, skuInfoJsonResult);

                }
                rLock.unlock();
            }
        }
        return skuInfoResult;

    }

    @Override
    public List<SpuSaleAttr> getSpuSaleAttrListCheckSku(String skuId, String spuId) {
       List<SpuSaleAttr> spuSaleAttrList =  spuSaleAttrMapper.getSpuSaleAttrListBySpuIdCheckSku(skuId, spuId);
       return spuSaleAttrList;
    }

    @Override
    public Map getSkuValueIdsMap(String spuId) {
        List<Map> mapList = skuSaleAttrValueMapper.getSaleAttrValuesBySpu(spuId);
        Map skuValueIdsMap = new HashMap();

        for (Map map : mapList) {
            String skuid = (Long) map.get("sku_id") + "";
            String valueIds = (String) map.get("value_ids");
            skuValueIdsMap.put(valueIds, skuid);
        }

        return skuValueIdsMap;
    }

    @Override
    public List<BaseAttrInfo> getAttrList(List attrValueIdList) {
        String valueIds = StringUtils.join(attrValueIdList.toArray(), ",");

        return baseAttrInfoMapper.getBaseAttrInfoListByValueIds(valueIds);

    }
}
