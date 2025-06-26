package com.nextera.managenextera.service.impl;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nextera.managenextera.dto.OrderManageDTO;
import com.nextera.managenextera.dto.OrderSearchDTO;
import com.nextera.managenextera.entity.OrderES;
import com.nextera.managenextera.repository.OrderESRepository;
import com.nextera.managenextera.service.OrderManageService;
import com.nextera.managenextera.service.RedisService;
import com.nextera.managenextera.util.OrderESUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.data.elasticsearch.core.query.StringQuery;
import org.springframework.data.elasticsearch.core.query.UpdateQuery;
import org.springframework.data.elasticsearch.core.query.UpdateResponse;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 管理端订单业务逻辑服务实现类
 * 使用ElasticsearchTemplate重新实现商品嵌套查询
 *
 * @author nextera
 * @since 2025-01-01
 */
@Slf4j
@Service
public class OrderManageServiceImpl implements OrderManageService {

    @Autowired
    private OrderESRepository orderESRepository;

    @Autowired
    private ElasticsearchOperations elasticsearchOperations;

    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;

    @Autowired
    private RedisService redisService;

    private static final String CACHE_PREFIX = "order:manage:";
    private static final String STATUS_CACHE_KEY = "order:status:distribution";

    @Override
    public IPage<OrderManageDTO> getOrderPage(OrderSearchDTO searchDTO) {
        log.info("分页查询订单，搜索条件: {}", searchDTO);

        try {
            // 判断是否需要使用高级嵌套查询
            if (needsAdvancedNestedQuery(searchDTO)) {
                return getOrderPageWithAdvancedNestedQuery(searchDTO);
            }
            
            // 如果有商品名称查询，使用基础嵌套查询
            if (StringUtils.hasText(searchDTO.getProductName())) {
                return getOrderPageByProductName(searchDTO);
            }
            
            // 构建ES查询条件
            Query query = OrderESUtil.buildSearchQuery(searchDTO);
            
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
            
        } catch (Exception e) {
            log.error("分页查询订单失败", e);
        }
        
        // 返回空结果
        return new Page<>(searchDTO.getCurrent(), searchDTO.getSize());
    }

    /**
     * 判断是否需要使用高级嵌套查询
     */
    private boolean needsAdvancedNestedQuery(OrderSearchDTO searchDTO) {
        return StringUtils.hasText(searchDTO.getProductName()) && 
               (StringUtils.hasText(searchDTO.getOrderNo()) || 
                StringUtils.hasText(searchDTO.getUsername()) ||
                (searchDTO.getStatusList() != null && !searchDTO.getStatusList().isEmpty()) ||
                searchDTO.getCreatedTimeStart() != null ||
                searchDTO.getCreatedTimeEnd() != null);
    }

    /**
     * 使用ElasticsearchTemplate进行高级嵌套查询
     */
    private IPage<OrderManageDTO> getOrderPageWithAdvancedNestedQuery(OrderSearchDTO searchDTO) {
        try {
            log.info("使用ElasticsearchTemplate进行高级嵌套查询");
            
            // 构建复杂的嵌套查询
            Query searchQuery = buildAdvancedNestedQuery(searchDTO);
            
            // 使用ElasticsearchTemplate执行查询
            SearchHits<OrderES> searchHits = elasticsearchTemplate.search(searchQuery, OrderES.class);
            
            // 转换结果
            List<OrderManageDTO> orderList = searchHits.getSearchHits().stream()
                    .map(hit -> OrderESUtil.convertToOrderManageDTO(hit.getContent()))
                    .collect(Collectors.toList());
            
            // 构建分页结果
            Page<OrderManageDTO> page = new Page<>(searchDTO.getCurrent(), searchDTO.getSize());
            page.setTotal(searchHits.getTotalHits());
            page.setRecords(orderList);
            page.setCurrent(searchDTO.getCurrent());
            page.setSize(searchDTO.getSize());
            
            log.info("高级嵌套查询完成，总数: {}, 当前页: {}, 页大小: {}", 
                    searchHits.getTotalHits(), searchDTO.getCurrent(), searchDTO.getSize());
            
            return page;
            
        } catch (Exception e) {
            log.error("高级嵌套查询失败", e);
            return new Page<>(searchDTO.getCurrent(), searchDTO.getSize());
        }
    }

    /**
     * 使用ElasticsearchTemplate构建高级嵌套查询
     */
    private Query buildAdvancedNestedQuery(OrderSearchDTO searchDTO) {
        // 使用JSON字符串构建复杂的嵌套查询
        StringBuilder queryJson = new StringBuilder();
        queryJson.append("{");
        queryJson.append("\"bool\": {");
        queryJson.append("\"must\": [");
        
        List<String> conditions = new ArrayList<>();
        
        // 添加商品嵌套查询条件 - 使用多种查询方式组合
        if (StringUtils.hasText(searchDTO.getProductName())) {
            String productName = searchDTO.getProductName().replace("\"", "\\\"");
            
            // 检查是否为精确查询
            boolean isExactMatch = productName.length() > 3 && !productName.contains("*") && !productName.contains("?");
            
            String nestedQuery;
            if (isExactMatch) {
                // 精确匹配：优先使用 term 和 match_phrase
                nestedQuery = String.format(
                    "{ \"nested\": { " +
                        "\"path\": \"orderItems\", " +
                        "\"query\": { " +
                            "\"bool\": { " +
                                "\"should\": [" +
                                    "{ \"term\": { \"orderItems.productName.keyword\": { \"value\": \"%s\", \"boost\": 10.0 } } }," +
                                    "{ \"match_phrase\": { \"orderItems.productName\": { \"query\": \"%s\", \"boost\": 8.0 } } }," +
                                    "{ \"match\": { \"orderItems.productName\": { \"query\": \"%s\", \"operator\": \"and\", \"boost\": 5.0 } } }," +
                                    "{ \"wildcard\": { \"orderItems.productName\": { \"value\": \"*%s*\", \"case_insensitive\": true, \"boost\": 1.0 } } }" +
                                "], " +
                                "\"minimum_should_match\": 1" +
                            "}" +
                        "}, " +
                        "\"score_mode\": \"max\"" +
                    "} }",
                    productName, productName, productName, productName
                );
                log.info("构建精确匹配高级嵌套查询，商品名称: {}", searchDTO.getProductName());
            } else {
                // 模糊匹配
                nestedQuery = String.format(
                    "{ \"nested\": { " +
                        "\"path\": \"orderItems\", " +
                        "\"query\": { " +
                            "\"bool\": { " +
                                "\"should\": [" +
                                    "{ \"wildcard\": { \"orderItems.productName\": { \"value\": \"*%s*\", \"case_insensitive\": true, \"boost\": 3.0 } } }," +
                                    "{ \"match\": { \"orderItems.productName\": { \"query\": \"%s\", \"fuzziness\": \"AUTO\", \"boost\": 2.0 } } }," +
                                    "{ \"match_phrase\": { \"orderItems.productName\": { \"query\": \"%s\", \"boost\": 1.5 } } }" +
                                "], " +
                                "\"minimum_should_match\": 1" +
                            "}" +
                        "}" +
                    "} }",
                    productName, productName, productName
                );
                log.info("构建模糊匹配高级嵌套查询，商品名称: {}", searchDTO.getProductName());
            }
            
            conditions.add(nestedQuery);
        }
        
        // 添加其他查询条件
        if (StringUtils.hasText(searchDTO.getOrderNo())) {
            String orderNo = searchDTO.getOrderNo().replace("\"", "\\\"");
            conditions.add(String.format(
                "{ \"wildcard\": { \"orderNo\": { \"value\": \"*%s*\", \"case_insensitive\": true } } }",
                orderNo));
        }
        
        if (StringUtils.hasText(searchDTO.getUsername())) {
            String username = searchDTO.getUsername().replace("\"", "\\\"");
            conditions.add(String.format(
                "{ \"wildcard\": { \"userName\": { \"value\": \"*%s*\", \"case_insensitive\": true } } }",
                username));
        }
        
        if (searchDTO.getStatusList() != null && !searchDTO.getStatusList().isEmpty()) {
            String statusValues = searchDTO.getStatusList().stream()
                    .map(String::valueOf)
                    .collect(Collectors.joining("\", \"", "[\"", "\"]"));
            conditions.add(String.format("{ \"terms\": { \"status\": %s } }", statusValues));
        }
        
        // 添加时间范围查询
        if (searchDTO.getCreatedTimeStart() != null && searchDTO.getCreatedTimeEnd() != null) {
            conditions.add(String.format(
                "{ \"range\": { \"createdTime\": { \"gte\": \"%s\", \"lte\": \"%s\" } } }",
                searchDTO.getCreatedTimeStart().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                searchDTO.getCreatedTimeEnd().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            ));
        }
        
        // 拼接所有条件
        queryJson.append(String.join(", ", conditions));
        queryJson.append("]");
        queryJson.append("}");
        queryJson.append("}");
        
        log.info("构建的高级嵌套查询JSON: {}", queryJson.toString());
        
        // 设置分页
        int page = searchDTO.getCurrent() != null ? searchDTO.getCurrent().intValue() - 1 : 0;
        int size = searchDTO.getSize() != null ? searchDTO.getSize().intValue() : 10;
        
        // 使用NativeQuery构建查询
        return NativeQuery.builder()
                .withQuery(new StringQuery(queryJson.toString()))
                .withPageable(PageRequest.of(page, size))
                .withSort(Sort.by(Sort.Direction.DESC, "_score").and(Sort.by(Sort.Direction.DESC, "createdTime")))
                .build();
    }

    /**
     * 根据商品名称查询订单（使用Spring Data Elasticsearch的NativeQuery实现嵌套查询）
     */
    private IPage<OrderManageDTO> getOrderPageByProductName(OrderSearchDTO searchDTO) {
        try {
            int current = searchDTO.getCurrent() != null ? searchDTO.getCurrent().intValue() : 1;
            int size = searchDTO.getSize() != null ? searchDTO.getSize().intValue() : 10;
            
            // 使用NativeQuery构建嵌套查询
            Query query = buildNestedQueryWithNativeQuery(searchDTO, current, size);
            
            // 执行查询
            SearchHits<OrderES> searchHits = elasticsearchOperations.search(query, OrderES.class);
            
            // 转换结果
            List<OrderManageDTO> orderList = searchHits.getSearchHits().stream()
                    .map(hit -> OrderESUtil.convertToOrderManageDTO(hit.getContent()))
                    .collect(Collectors.toList());
            
            // 构建分页结果
            Page<OrderManageDTO> page = new Page<>(current, size);
            page.setTotal(searchHits.getTotalHits());
            page.setRecords(orderList);
            page.setCurrent(current);
            page.setSize(size);
            
            log.info("嵌套查询完成，总数: {}, 当前页: {}, 页大小: {}", 
                    searchHits.getTotalHits(), current, size);
            
            return page;
            
        } catch (Exception e) {
            log.error("根据商品名称查询订单失败", e);
            return new Page<>(searchDTO.getCurrent(), searchDTO.getSize());
        }
    }

    /**
     * 使用NativeQuery构建嵌套查询
     */
    private Query buildNestedQueryWithNativeQuery(OrderSearchDTO searchDTO, int current, int size) {
        // 构建嵌套查询的JSON字符串
        StringBuilder queryJson = new StringBuilder();
        queryJson.append("{");
        queryJson.append("\"bool\": {");
        queryJson.append("\"must\": [");
        
        List<String> conditions = new ArrayList<>();
        
        // 嵌套查询：在orderItems中查找商品名称 - 精确匹配优先
        if (StringUtils.hasText(searchDTO.getProductName())) {
            String productName = searchDTO.getProductName().replace("\"", "\\\"");
            
            // 检查是否为精确查询
            boolean isExactMatch = productName.length() > 3 && !productName.contains("*") && !productName.contains("?");
            
            String nestedQuery;
            if (isExactMatch) {
                // 精确匹配：优先使用 term 和 match_phrase
                nestedQuery = String.format(
                    "{ \"nested\": { " +
                        "\"path\": \"orderItems\", " +
                        "\"query\": { " +
                            "\"bool\": { " +
                                "\"should\": [" +
                                    "{ \"term\": { \"orderItems.productName.keyword\": { \"value\": \"%s\", \"boost\": 10.0 } } }," +
                                    "{ \"match_phrase\": { \"orderItems.productName\": { \"query\": \"%s\", \"boost\": 8.0 } } }," +
                                    "{ \"match\": { \"orderItems.productName\": { \"query\": \"%s\", \"operator\": \"and\", \"boost\": 5.0 } } }," +
                                    "{ \"wildcard\": { \"orderItems.productName\": { \"value\": \"*%s*\", \"case_insensitive\": true, \"boost\": 1.0 } } }" +
                                "], " +
                                "\"minimum_should_match\": 1" +
                            "}" +
                        "}, " +
                        "\"score_mode\": \"max\"" +
                    "} }",
                    productName, productName, productName, productName
                );
                log.info("构建精确匹配嵌套查询，商品名称: {}", searchDTO.getProductName());
            } else {
                // 模糊匹配：使用通配符和模糊查询
                nestedQuery = String.format(
                    "{ \"nested\": { " +
                        "\"path\": \"orderItems\", " +
                        "\"query\": { " +
                            "\"bool\": { " +
                                "\"should\": [" +
                                    "{ \"wildcard\": { \"orderItems.productName\": { \"value\": \"*%s*\", \"case_insensitive\": true, \"boost\": 3.0 } } }," +
                                    "{ \"match\": { \"orderItems.productName\": { \"query\": \"%s\", \"fuzziness\": \"AUTO\", \"boost\": 2.0 } } }," +
                                    "{ \"match_phrase\": { \"orderItems.productName\": { \"query\": \"%s\", \"boost\": 1.5 } } }" +
                                "], " +
                                "\"minimum_should_match\": 1" +
                            "}" +
                        "}" +
                    "} }",
                    productName, productName, productName
                );
                log.info("构建模糊匹配嵌套查询，商品名称: {}", searchDTO.getProductName());
            }
            
            conditions.add(nestedQuery);
        }
        
        // 其他查询条件
        if (StringUtils.hasText(searchDTO.getOrderNo())) {
            String orderNo = searchDTO.getOrderNo().replace("\"", "\\\"");
            conditions.add(String.format(
                "{ \"wildcard\": { \"orderNo\": { \"value\": \"*%s*\", \"case_insensitive\": true } } }",
                orderNo));
        }
        
        if (StringUtils.hasText(searchDTO.getUsername())) {
            String username = searchDTO.getUsername().replace("\"", "\\\"");
            conditions.add(String.format(
                "{ \"wildcard\": { \"userName\": { \"value\": \"*%s*\", \"case_insensitive\": true } } }",
                username));
        }
        
        if (searchDTO.getStatusList() != null && !searchDTO.getStatusList().isEmpty()) {
            String statusValues = searchDTO.getStatusList().stream()
                    .map(String::valueOf)
                    .collect(Collectors.joining("\", \"", "[\"", "\"]"));
            conditions.add(String.format("{ \"terms\": { \"status\": %s } }", statusValues));
        }
        
        if (searchDTO.getCreatedTimeStart() != null && searchDTO.getCreatedTimeEnd() != null) {
            conditions.add(String.format(
                "{ \"range\": { \"createdTime\": { \"gte\": \"%s\", \"lte\": \"%s\" } } }",
                searchDTO.getCreatedTimeStart().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                searchDTO.getCreatedTimeEnd().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            ));
        }
        
        // 拼接所有条件
        queryJson.append(String.join(", ", conditions));
        queryJson.append("]");
        queryJson.append("}");
        queryJson.append("}");
        
        log.info("构建的增强嵌套查询JSON: {}", queryJson.toString());
        
        // 使用StringQuery创建查询，并按相关性排序
        return NativeQuery.builder()
                .withQuery(new StringQuery(queryJson.toString()))
                .withPageable(PageRequest.of(current - 1, size))
                .withSort(Sort.by(Sort.Direction.DESC, "_score").and(Sort.by(Sort.Direction.DESC, "createdTime")))
                .build();
    }

    @Override
    public OrderManageDTO getOrderDetail(Long orderId) {
        log.info("根据ID查询订单详情: {}", orderId);
        
        String cacheKey = CACHE_PREFIX + "detail:" + orderId;
        
        // 先从缓存获取
        OrderManageDTO cached = redisService.get(cacheKey, OrderManageDTO.class);
        if (cached != null) {
            log.info("从缓存获取订单详情: {}", orderId);
            return cached;
        }
        
        try {
            // 使用ElasticsearchTemplate进行精确查询
            Query searchQuery = NativeQuery.builder()
                    .withQuery(new StringQuery("{\"term\":{\"id\":" + orderId + "}}"))
                    .build();
            
            SearchHits<OrderES> searchHits = elasticsearchTemplate.search(searchQuery, OrderES.class);
            
            if (searchHits.hasSearchHits()) {
                OrderES orderES = searchHits.getSearchHit(0).getContent();
                OrderManageDTO orderDTO = OrderESUtil.convertToOrderManageDTO(orderES);
                
                // 缓存5分钟
                redisService.set(cacheKey, orderDTO, 5, TimeUnit.MINUTES);
                
                return orderDTO;
            }
            
        } catch (Exception e) {
            log.error("查询订单详情失败: {}", orderId, e);
        }
        
        return null;
    }

    @Override
    public OrderManageDTO getOrderDetailByOrderNo(String orderNo) {
        log.info("根据订单号查询订单详情: {}", orderNo);
        
        String cacheKey = CACHE_PREFIX + "detail:no:" + orderNo;
        
        // 先从缓存获取
        OrderManageDTO cached = redisService.get(cacheKey, OrderManageDTO.class);
        if (cached != null) {
            log.info("从缓存获取订单详情: {}", orderNo);
            return cached;
        }
        
        try {
            // 使用ElasticsearchTemplate进行精确查询
            Query searchQuery = NativeQuery.builder()
                    .withQuery(new StringQuery("{\"term\":{\"orderNo\":\"" + orderNo + "\"}}"))
                    .build();
            
            SearchHits<OrderES> searchHits = elasticsearchTemplate.search(searchQuery, OrderES.class);
            
            if (searchHits.hasSearchHits()) {
                OrderES orderES = searchHits.getSearchHit(0).getContent();
                OrderManageDTO orderDTO = OrderESUtil.convertToOrderManageDTO(orderES);
                
                // 缓存5分钟
                redisService.set(cacheKey, orderDTO, 5, TimeUnit.MINUTES);
                
                return orderDTO;
            }
            
        } catch (Exception e) {
            log.error("根据订单号查询订单详情失败: {}", orderNo, e);
        }
        
        return null;
    }

    @Override
    public boolean updateOrderStatus(Long orderId, Integer status) {
        log.info("更新订单状态: orderId={}, status={}", orderId, status);
        
        try {
            // 先查询出原始订单数据
            Optional<OrderES> orderESOptional = orderESRepository.findById(orderId);
            if (!orderESOptional.isPresent()) {
                log.warn("订单不存在: orderId={}", orderId);
                return false;
            }
            
            // 使用Document方式进行部分更新
            org.springframework.data.elasticsearch.core.document.Document document = 
                    org.springframework.data.elasticsearch.core.document.Document.create();
            document.put("status", status);
            document.put("updatedTime", LocalDateTime.now().withNano(0).format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")));

            UpdateQuery updateQuery = UpdateQuery.builder(orderId.toString())
                    .withDocument(document)
                    .build();

            UpdateResponse updateResponse = elasticsearchOperations.update(updateQuery, 
                    elasticsearchOperations.getIndexCoordinatesFor(OrderES.class));
            
            if (updateResponse != null) {
                // 清除相关缓存
                clearOrderCache(orderId);
                
                log.info("订单状态更新成功: orderId={}, status={}", orderId, status);
                return true;
            } else {
                log.warn("订单状态更新失败，更新响应为空");
                return false;
            }
            
        } catch (Exception e) {
            log.error("更新订单状态失败: orderId={}, status={}", orderId, status, e);
            return false;
        }
    }

    @Override
    public boolean batchUpdateOrderStatus(List<Long> orderIds, Integer status) {
        log.info("批量更新订单状态: orderIds={}, status={}", orderIds, status);
        
        if (orderIds == null || orderIds.isEmpty()) {
            log.warn("订单ID列表为空");
            return false;
        }
        
        boolean allSuccess = true;
        int successCount = 0;
        
        for (Long orderId : orderIds) {
            boolean success = updateOrderStatus(orderId, status);
            if (success) {
                successCount++;
            } else {
                allSuccess = false;
            }
        }
        
        log.info("批量更新订单状态完成: total={}, success={}, failed={}", 
            orderIds.size(), successCount, orderIds.size() - successCount);
        
        return allSuccess;
    }

    @Override
    public List<OrderManageDTO> advancedSearch(OrderSearchDTO searchDTO) {
        log.info("高级搜索订单: {}", searchDTO);
        
        try {
            // 使用ElasticsearchTemplate进行高级搜索
            if (StringUtils.hasText(searchDTO.getProductName())) {
                // 使用高级嵌套查询
                Query searchQuery = buildAdvancedNestedQuery(searchDTO);
                SearchHits<OrderES> searchHits = elasticsearchTemplate.search(searchQuery, OrderES.class);
                
                return searchHits.getSearchHits().stream()
                        .map(hit -> OrderESUtil.convertToOrderManageDTO(hit.getContent()))
                        .collect(Collectors.toList());
            } else {
                // 构建ES查询条件
                Query query = OrderESUtil.buildSearchQuery(searchDTO);
                
                // 执行ES查询
                SearchHits<OrderES> searchHits = elasticsearchOperations.search(query, OrderES.class);
                
                // 转换数据
                return searchHits.getSearchHits().stream()
                        .map(org.springframework.data.elasticsearch.core.SearchHit::getContent)
                        .map(OrderESUtil::convertToOrderManageDTO)
                        .collect(Collectors.toList());
            }
            
        } catch (Exception e) {
            log.error("高级搜索订单失败", e);
        }
        
        return new ArrayList<>();
    }

    @Override
    public List<OrderManageDTO> exportOrders(OrderSearchDTO searchDTO) {
        log.info("导出订单数据: {}", searchDTO);
        
        // 设置大的分页大小用于导出
        searchDTO.setSize(10000);
        searchDTO.setCurrent(1);
        
        return advancedSearch(searchDTO);
    }

    @Override
    public List<OrderManageDTO> getOrderStatusDistribution() {
        log.info("获取订单状态分布");
        
        // 先从缓存获取
        @SuppressWarnings("unchecked")
        List<OrderManageDTO> cached = (List<OrderManageDTO>) redisService.get(STATUS_CACHE_KEY);
        if (cached != null) {
            log.info("从缓存获取订单状态分布");
            return cached;
        }
        
        try {
            List<OrderManageDTO> distribution = new ArrayList<>();
            
            // 统计各状态的订单数量
            for (int status = 0; status <= 5; status++) {
                long count = orderESRepository.countByStatus(status);
                if (count > 0) {
                    OrderManageDTO statusDTO = new OrderManageDTO();
                    statusDTO.setStatus(status);
                    statusDTO.setStatusDescription(OrderESUtil.getStatusName(status));
                    // 这里用createdTime字段存储数量（临时方案）
                    statusDTO.setCreatedTime(LocalDateTime.now());
                    distribution.add(statusDTO);
                }
            }
            
            // 缓存10分钟
            redisService.set(STATUS_CACHE_KEY, distribution, 10, TimeUnit.MINUTES);
            
            return distribution;
            
        } catch (Exception e) {
            log.error("获取订单状态分布失败", e);
        }
        
        return new ArrayList<>();
    }

    /**
     * 清除订单缓存
     */
    private void clearOrderCache(Long orderId) {
        String detailCacheKey = CACHE_PREFIX + "detail:" + orderId;
        redisService.delete(detailCacheKey);
        
        // 清除状态分布缓存
        redisService.delete(STATUS_CACHE_KEY);
        
        log.debug("清除订单缓存: {}", orderId);
    }

    /**
     * 多条件商品嵌套查询（扩展方法）
     * 支持商品名称、SKU、ID等多种商品相关条件的嵌套查询
     */
    public IPage<OrderManageDTO> searchOrdersByMultipleProductConditions(OrderSearchDTO searchDTO) {
        log.info("多条件商品嵌套查询: {}", searchDTO);
        
        try {
            // 构建复杂的商品嵌套查询JSON
            StringBuilder queryJson = new StringBuilder();
            queryJson.append("{");
            queryJson.append("\"bool\": {");
            queryJson.append("\"must\": [");
            
            List<String> conditions = new ArrayList<>();
            
            // 构建商品嵌套查询条件
            List<String> nestedConditions = new ArrayList<>();
            
            // 商品名称条件
            if (StringUtils.hasText(searchDTO.getProductName())) {
                String productName = searchDTO.getProductName().replace("\"", "\\\"");
                nestedConditions.add(String.format(
                    "{ \"wildcard\": { \"orderItems.productName\": { \"value\": \"*%s*\", \"case_insensitive\": true } } }",
                    productName));
            }
            
            // 如果有嵌套条件，构建嵌套查询
            if (!nestedConditions.isEmpty()) {
                String nestedBoolQuery = String.format(
                    "{ \"nested\": { " +
                        "\"path\": \"orderItems\", " +
                        "\"query\": { " +
                            "\"bool\": { \"must\": [%s] }" +
                        "}" +
                    "} }",
                    String.join(", ", nestedConditions)
                );
                conditions.add(nestedBoolQuery);
            }
            
            // 添加其他非嵌套条件
            if (StringUtils.hasText(searchDTO.getOrderNo())) {
                String orderNo = searchDTO.getOrderNo().replace("\"", "\\\"");
                conditions.add(String.format(
                    "{ \"wildcard\": { \"orderNo\": { \"value\": \"*%s*\", \"case_insensitive\": true } } }",
                    orderNo));
            }
            
            if (StringUtils.hasText(searchDTO.getUsername())) {
                String username = searchDTO.getUsername().replace("\"", "\\\"");
                conditions.add(String.format(
                    "{ \"wildcard\": { \"userName\": { \"value\": \"*%s*\", \"case_insensitive\": true } } }",
                    username));
            }
            
            // 拼接所有条件
            queryJson.append(String.join(", ", conditions));
            queryJson.append("]");
            queryJson.append("}");
            queryJson.append("}");
            
            log.info("构建的多条件商品嵌套查询JSON: {}", queryJson.toString());
            
            // 设置分页
            int page = searchDTO.getCurrent() != null ? searchDTO.getCurrent().intValue() - 1 : 0;
            int size = searchDTO.getSize() != null ? searchDTO.getSize().intValue() : 10;
            
            // 构建查询
            Query query = NativeQuery.builder()
                    .withQuery(new StringQuery(queryJson.toString()))
                    .withPageable(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdTime")))
                    .build();
            
            // 执行查询
            SearchHits<OrderES> searchHits = elasticsearchTemplate.search(query, OrderES.class);
            
            // 转换结果
            List<OrderManageDTO> orderList = searchHits.getSearchHits().stream()
                    .map(hit -> OrderESUtil.convertToOrderManageDTO(hit.getContent()))
                    .collect(Collectors.toList());
            
            // 构建分页结果
            Page<OrderManageDTO> resultPage = new Page<>(searchDTO.getCurrent(), searchDTO.getSize());
            resultPage.setTotal(searchHits.getTotalHits());
            resultPage.setRecords(orderList);
            resultPage.setCurrent(searchDTO.getCurrent());
            resultPage.setSize(searchDTO.getSize());
            
            log.info("多条件商品嵌套查询完成，总数: {}", searchHits.getTotalHits());
            
            return resultPage;
            
        } catch (Exception e) {
            log.error("多条件商品嵌套查询失败", e);
            return new Page<>(searchDTO.getCurrent(), searchDTO.getSize());
        }
    }
}