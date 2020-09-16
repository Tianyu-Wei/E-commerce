package com.tyw.onlineshopping.list.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.tyw.onlineshopping.bean.SkuLsInfo;
import com.tyw.onlineshopping.bean.SkuLsParams;
import com.tyw.onlineshopping.bean.SkuLsResult;
import com.tyw.onlineshopping.service.ListService;
import com.tyw.onlineshopping.util.RedisUtil;
import io.searchbox.client.JestClient;
import io.searchbox.core.Index;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import io.searchbox.core.Update;
import io.searchbox.core.search.aggregation.TermsAggregation;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.TermsBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class ListServiceImpl implements ListService {

    @Autowired
    RedisUtil redisUtil;

    @Autowired
    JestClient jestClient;

    public void saveSkuLsInfo(SkuLsInfo skuLsInfo){

        Index.Builder indexBuilder = new Index.Builder(skuLsInfo);
        indexBuilder.index("gmall_sku_info").type("_doc").id(skuLsInfo.getId());
        Index index = indexBuilder.build();
        try {
            jestClient.execute(index);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public SkuLsResult getSkuLsInfoList(SkuLsParams skuLsParams) throws IOException {

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        BoolQueryBuilder boolQueryBuilder  = new BoolQueryBuilder();

        if (skuLsParams.getKeyword() != null) {
            boolQueryBuilder.must(new MatchQueryBuilder("skuName", skuLsParams.getKeyword()));

            searchSourceBuilder.highlight(new HighlightBuilder().field("skuName").preTags("<span style='color:red'>").postTags("</span>"));

        }

        if (skuLsParams.getCatalog3Id() != null) {
            boolQueryBuilder.filter(new TermQueryBuilder("catalog3Id", skuLsParams.getCatalog3Id()));
        }

        if (skuLsParams.getValueId() != null && skuLsParams.getValueId().length > 0) {
            String[] valuIds = skuLsParams.getValueId();

            for (String s : valuIds) {
                boolQueryBuilder.filter(new TermQueryBuilder("skuAttrValueList.valueId", s));
            }
        }

        searchSourceBuilder.query(boolQueryBuilder);

        searchSourceBuilder.from((skuLsParams.getPageNo() - 1)*skuLsParams.getPageSize());
        searchSourceBuilder.size(skuLsParams.getPageSize());

        TermsBuilder aggsBuilder = AggregationBuilders.terms("groupby_value_id").field("skuAttrValueList.valueId").size(1000);
        searchSourceBuilder.aggregation(aggsBuilder);
        searchSourceBuilder.sort("hotScore", SortOrder.DESC);

        Search.Builder searchBuilder = new Search.Builder(searchSourceBuilder.toString());
        Search search =  searchBuilder.addIndex("gmall_sku_info").addType("_doc").build();
        SearchResult searchResult = jestClient.execute(search);
        SkuLsResult skuLsResult = new SkuLsResult();
        List<SkuLsInfo> skuLsInfoList = new ArrayList<>();
        List<SearchResult.Hit<SkuLsInfo, Void>> hits = searchResult.getHits(SkuLsInfo.class);
        for (SearchResult.Hit<SkuLsInfo, Void> hit : hits) {
            SkuLsInfo skuLsInfo = hit.source;
            String skuNameHL = hit.highlight.get("skuName").get(0);
            skuLsInfo.setSkuName(skuNameHL);
            skuLsInfoList.add(skuLsInfo);
        }
        skuLsResult.setSkuLsInfoList(skuLsInfoList);
        Long total = searchResult.getTotal();
        skuLsResult.setTotal(total);
        Long totalPage = (total + skuLsParams.getPageSize() - 1) / skuLsParams.getPageSize();
        skuLsResult.setTotalPages(totalPage);

        List<String> attrValueIdList = new ArrayList<>();
        List<TermsAggregation.Entry> buckets = searchResult.getAggregations().getTermsAggregation("groupby_value_id").getBuckets();
        for (TermsAggregation.Entry bucket : buckets) {
            attrValueIdList.add(bucket.getKey());
        }
        skuLsResult.setAttrValueIdList(attrValueIdList);

        return skuLsResult;
    }

    @Override
    public void incrHotScore(String skuId) throws IOException {
        Jedis jedis = redisUtil.getJedis();
        int timesToEs=10;
        Double hotScore = jedis.zincrby("hotScore", 1, "skuId:" + skuId);
        if(hotScore%timesToEs==0){
            updateHotScoreEs(skuId,  Math.round(hotScore));
        }

    }


    private void updateHotScoreEs(String skuId,Long hotScore){
        String updateJson="{\n" +
                "   \"doc\":{\n" +
                "     \"hotScore\":"+hotScore+"\n" +
                "   }\n" +
                "}";

        Update update = new Update.Builder(updateJson).index("gmall_sku_info").type("_doc").id(skuId).build();
        try {
            jestClient.execute(update);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
