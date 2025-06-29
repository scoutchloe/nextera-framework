package com.nextera.managenextera.service;

import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.QueryBuilders;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nextera.managenextera.dto.OrderManageDTO;
import com.nextera.managenextera.dto.OrderSearchDTO;
import com.nextera.managenextera.entity.OrderES;
import com.nextera.managenextera.util.OrderESUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.client.elc.NativeQueryBuilder;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import static com.nextera.managenextera.util.LocalTimeUtil.localDateTimeToString;

/**
 * @author Scout
 * @date 2025-06-29 15:40
 * @since 1.0
 */
@Component
@Slf4j
public class OrderNativeQueryService {

    @Autowired
    private ElasticsearchOperations elasticsearchOperations;

    public IPage<OrderManageDTO> getOrderPage(OrderSearchDTO searchDTO) {
        log.info("分页查询订单Native，搜索条件: {}", searchDTO);

        Query query = buildNativeQuery(searchDTO);

        // 执行ES查询
        SearchHits<OrderES> searchHits = elasticsearchOperations.search(query, OrderES.class);

        // 构建分页结果
        Page<OrderManageDTO> page = new Page<>(searchDTO.getCurrent(), searchDTO.getSize());
        page.setTotal(searchHits.getTotalHits());

        // 转换数据
        List<OrderManageDTO> orderList = searchHits.getSearchHits().stream()
                .map(org.springframework.data.elasticsearch.core.SearchHit::getContent)
                .map(OrderESUtil::convertToOrderManageDTO)
                .collect(Collectors.toList());

        page.setRecords(orderList);
        page.setCurrent(searchDTO.getCurrent());
        page.setSize(searchDTO.getSize());

        return page;

    }

    private Query buildNativeQuery(OrderSearchDTO searchDTO) {
        // 订单号模糊查询
        BoolQuery.Builder b = new BoolQuery.Builder();
        if (StringUtils.hasText(searchDTO.getOrderNo())) {
            b.must(
             QueryBuilders.wildcard(m -> m.field("orderNo").wildcard("*" + searchDTO.getOrderNo() + "*"))
            );
        }

        // 用户名模糊查询
        if (StringUtils.hasText(searchDTO.getUsername())) {
            b.must(QueryBuilders.match(m-> m.field("userName").query(searchDTO.getUsername())));
        }

        // 订单状态查询（处理状态列表）
        if (searchDTO.getStatusList() != null && !searchDTO.getStatusList().isEmpty()) {
            List<co.elastic.clients.elasticsearch._types.query_dsl.Query> statusList = searchDTO.getStatusList().stream().map(status ->
                    QueryBuilders.term(t -> t.field("status").value(status))
            ).toList();
            BoolQuery.Builder bb = new BoolQuery.Builder();

            b.must(bb.should(statusList).build());
        }

        // 支付方式查询（处理支付方式列表）
        if (searchDTO.getPaymentMethodList() != null && !searchDTO.getPaymentMethodList().isEmpty()) {

            List<co.elastic.clients.elasticsearch._types.query_dsl.Query> paymentList = searchDTO.getPaymentMethodList().stream().map(status ->
                    QueryBuilders.term(t -> t.field("paymentMethod").value(status))
            ).toList();
            BoolQuery.Builder bb = new BoolQuery.Builder();
            b.must(bb.should(paymentList).build());
        }

        // 金额范围查询
        if (searchDTO.getMinAmount() != null && searchDTO.getMaxAmount() != null) {
            b.must(QueryBuilders.range(r-> r.number(t->t.field("totalAmount")
                            .gte(bigDecimalToDouble(searchDTO.getMinAmount()))
                            .lte(bigDecimalToDouble(searchDTO.getMaxAmount()))))
            );

        } else if (searchDTO.getMinAmount() != null) {
            b.must(QueryBuilders.range(r-> r.number(t->t.field("totalAmount")
                    .gte(bigDecimalToDouble(searchDTO.getMinAmount())))));

        } else if (searchDTO.getMaxAmount() != null) {
            b.must(QueryBuilders.range(r->r.number(t->t.field("totalAmount")
                    .lte(bigDecimalToDouble(searchDTO.getMaxAmount())))));
        }

        // 时间范围查询
        if (searchDTO.getCreatedTimeStart() != null && searchDTO.getCreatedTimeEnd() != null) {
            b.must(QueryBuilders.range(r -> r.date(t -> t.field("createdTime")
                    .gte(localDateTimeToString(searchDTO.getCreatedTimeStart()))
                    .lte(localDateTimeToString(searchDTO.getCreatedTimeEnd()))))
            );

        } else if (searchDTO.getCreatedTimeStart() != null) {
            b.must(QueryBuilders.range(r-> r.date(t -> t.field("createdTime")
                    .gte(localDateTimeToString(searchDTO.getCreatedTimeStart())))));
        } else if (searchDTO.getCreatedTimeEnd() != null) {
            b.must(QueryBuilders.range(r-> r.date(t -> t.field("createdTime")
                    .lte(localDateTimeToString(searchDTO.getCreatedTimeEnd())))));

        }

        if (searchDTO.getProductName() != null) {
            System.out.println("当前的产品名称为:" + searchDTO.getProductName());
            b.must(QueryBuilders.nested(t -> t.path("orderItems")
                    .query(s -> s.wildcard(w -> w.field("orderItems.productName")
                            .wildcard("*" + searchDTO.getProductName() + "*"))))
            );
        }

        co.elastic.clients.elasticsearch._types.query_dsl.Query query = b.build()._toQuery();

        System.out.println("当前的查询对象是：" + query.toString());

        return new NativeQueryBuilder().withQuery(query).withPageable(OrderESUtil.buildPageable(searchDTO)).build();
    }

    private Double bigDecimalToDouble(BigDecimal bigDecimal) {
        return bigDecimal.doubleValue();
    }
}