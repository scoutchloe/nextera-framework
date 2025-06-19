package com.nextera.managenextera.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nextera.managenextera.entity.articlemod.ArticleCategory;
import org.apache.ibatis.annotations.Mapper;

@Mapper
@DS("article")
public interface ArticleCategoryMapper extends BaseMapper<ArticleCategory> {
} 