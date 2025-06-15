package com.nextera.article.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nextera.article.entity.UserArticle;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户文章关联记录Mapper接口
 *
 * @author nextera
 * @since 2025-06-16
 */
@Mapper
public interface UserArticleMapper extends BaseMapper<UserArticle> {
} 