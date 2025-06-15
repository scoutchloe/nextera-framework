package com.nextera.article.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nextera.article.entity.Article;
import org.apache.ibatis.annotations.Mapper;

/**
 * 文章Mapper接口
 *
 * @author nextera
 * @since 2025-06-16
 */
@Mapper
public interface ArticleMapper extends BaseMapper<Article> {
} 