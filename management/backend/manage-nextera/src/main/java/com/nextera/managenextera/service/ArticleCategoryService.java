package com.nextera.managenextera.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.nextera.managenextera.dto.ArticleCategoryDTO;
import com.nextera.managenextera.entity.articlemod.ArticleCategory;

import java.util.List;

/**
 * 文章分类服务接口
 *
 * @author nextera
 * @since 2025-06-19
 */
public interface ArticleCategoryService extends IService<ArticleCategory> {

    /**
     * 分页查询分类列表
     *
     * @param page 分页参数
     * @param name 分类名称（可选）
     * @param status 状态（可选）
     * @return 分页结果
     */
    IPage<ArticleCategoryDTO> getCategoryPage(Page<ArticleCategory> page, String name, Integer status);

    /**
     * 获取分类树形结构
     *
     * @return 分类树
     */
    List<ArticleCategoryDTO> getCategoryTree();

    /**
     * 根据ID获取分类详情
     *
     * @param id 分类ID
     * @return 分类DTO
     */
    ArticleCategoryDTO getCategoryById(Long id);

    /**
     * 创建分类
     *
     * @param categoryDTO 分类DTO
     * @return 是否成功
     */
    boolean createCategory(ArticleCategoryDTO categoryDTO);

    /**
     * 更新分类
     *
     * @param categoryDTO 分类DTO
     * @return 是否成功
     */
    boolean updateCategory(ArticleCategoryDTO categoryDTO);

    /**
     * 删除分类
     *
     * @param id 分类ID
     * @return 是否成功
     */
    boolean deleteCategory(Long id);

    /**
     * 批量删除分类
     *
     * @param ids 分类ID列表
     * @return 是否成功
     */
    boolean deleteCategories(List<Long> ids);

    /**
     * 更新分类状态
     *
     * @param id 分类ID
     * @param status 状态
     * @return 是否成功
     */
    boolean updateCategoryStatus(Long id, Integer status);

    /**
     * 获取所有启用的分类（用于下拉选择）
     *
     * @return 分类列表
     */
    List<ArticleCategoryDTO> getEnabledCategories();
} 