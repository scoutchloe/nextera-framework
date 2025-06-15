package com.nextera.article.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nextera.article.entity.ArticleCategory;
import org.apache.ibatis.annotations.Mapper;

/**
 * 文章分类Mapper接口
 *
 * @author nextera
 * @since 2025-06-16
 */
@Mapper
public interface ArticleCategoryMapper extends BaseMapper<ArticleCategory> {
} 