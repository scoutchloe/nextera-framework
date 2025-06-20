package com.nextera.managenextera.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nextera.managenextera.dto.ArticleCategoryDTO;
import com.nextera.managenextera.entity.articlemod.ArticleCategory;
import com.nextera.managenextera.mapper.ArticleCategoryMapper;
import com.nextera.managenextera.service.ArticleCategoryService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 文章分类服务实现类
 *
 * @author nextera
 * @since 2025-06-19
 */
@Service
public class ArticleCategoryServiceImpl extends ServiceImpl<ArticleCategoryMapper, ArticleCategory> 
        implements ArticleCategoryService {

    @Override
    public IPage<ArticleCategoryDTO> getCategoryPage(Page<ArticleCategory> page, String name, Integer status) {
        LambdaQueryWrapper<ArticleCategory> queryWrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(name)) {
            queryWrapper.like(ArticleCategory::getName, name);
        }
        if (status != null) {
            queryWrapper.eq(ArticleCategory::getStatus, status);
        }

        queryWrapper.orderByAsc(ArticleCategory::getSortOrder)
                   .orderByDesc(ArticleCategory::getCreateTime);

        IPage<ArticleCategory> categoryPage = this.page(page, queryWrapper);
        
        // 转换为DTO
        IPage<ArticleCategoryDTO> result = categoryPage.convert(this::convertToDTO);
        
        return result;
    }

    @Override
    public List<ArticleCategoryDTO> getCategoryTree() {
        List<ArticleCategory> allCategories = this.list(new LambdaQueryWrapper<ArticleCategory>()
                .orderByAsc(ArticleCategory::getSortOrder));
        
        List<ArticleCategoryDTO> categoryDTOs = allCategories.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        
        return buildTree(categoryDTOs);
    }

    @Override
    public ArticleCategoryDTO getCategoryById(Long id) {
        ArticleCategory category = this.getById(id);
        if (category == null) {
            return null;
        }
        return convertToDTO(category);
    }

    @Override
    @Transactional
    public boolean createCategory(ArticleCategoryDTO categoryDTO) {
        // 检查分类名称是否已存在
        if (existsByName(categoryDTO.getName(), categoryDTO.getParentId())) {
            throw new RuntimeException("分类名称已存在");
        }

        ArticleCategory category = convertToEntity(categoryDTO);
        category.setCreateTime(LocalDateTime.now());
        category.setUpdateTime(LocalDateTime.now());

        return this.save(category);
    }

    @Override
    @Transactional
    public boolean updateCategory(ArticleCategoryDTO categoryDTO) {
        ArticleCategory existingCategory = this.getById(categoryDTO.getId());
        if (existingCategory == null) {
            throw new RuntimeException("分类不存在");
        }

        // 检查分类名称是否已存在（排除自己）
        if (!existingCategory.getName().equals(categoryDTO.getName()) && 
            existsByName(categoryDTO.getName(), categoryDTO.getParentId())) {
            throw new RuntimeException("分类名称已存在");
        }

        ArticleCategory category = convertToEntity(categoryDTO);
        category.setUpdateTime(LocalDateTime.now());
        
        return this.updateById(category);
    }

    @Override
    @Transactional
    public boolean deleteCategory(Long id) {
        // 检查是否有子分类
        long childCount = this.count(new LambdaQueryWrapper<ArticleCategory>()
                .eq(ArticleCategory::getParentId, id));
        if (childCount > 0) {
            throw new RuntimeException("存在子分类，无法删除");
        }

        return this.removeById(id);
    }

    @Override
    @Transactional
    public boolean deleteCategories(List<Long> ids) {
        for (Long id : ids) {
            if (!deleteCategory(id)) {
                return false;
            }
        }
        return true;
    }

    @Override
    @Transactional
    public boolean updateCategoryStatus(Long id, Integer status) {
        ArticleCategory category = new ArticleCategory();
        category.setId(id);
        category.setStatus(status);
        category.setUpdateTime(LocalDateTime.now());
        return this.updateById(category);
    }

    @Override
    public List<ArticleCategoryDTO> getEnabledCategories() {
        List<ArticleCategory> categories = this.list(new LambdaQueryWrapper<ArticleCategory>()
                .eq(ArticleCategory::getStatus, 1)
                .orderByAsc(ArticleCategory::getSortOrder));
        
        return categories.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * 检查分类名称是否存在
     */
    private boolean existsByName(String name, Long parentId) {
        LambdaQueryWrapper<ArticleCategory> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ArticleCategory::getName, name);
        
        if (parentId != null) {
            queryWrapper.eq(ArticleCategory::getParentId, parentId);
        } else {
            queryWrapper.isNull(ArticleCategory::getParentId);
        }
        
        return this.count(queryWrapper) > 0;
    }

    /**
     * 构建树形结构
     */
    private List<ArticleCategoryDTO> buildTree(List<ArticleCategoryDTO> categories) {
        Map<Long, List<ArticleCategoryDTO>> parentMap = categories.stream()
                .filter(category -> category.getParentId() != null)
                .collect(Collectors.groupingBy(ArticleCategoryDTO::getParentId));

        List<ArticleCategoryDTO> rootCategories = categories.stream()
                .filter(category -> category.getParentId() == null)
                .collect(Collectors.toList());

        for (ArticleCategoryDTO rootCategory : rootCategories) {
            setChildren(rootCategory, parentMap);
        }

        return rootCategories;
    }

    /**
     * 递归设置子分类
     */
    private void setChildren(ArticleCategoryDTO parent, Map<Long, List<ArticleCategoryDTO>> parentMap) {
        List<ArticleCategoryDTO> children = parentMap.get(parent.getId());
        if (children != null) {
            parent.setChildren(children);
            for (ArticleCategoryDTO child : children) {
                setChildren(child, parentMap);
            }
        }
    }

    /**
     * 实体转DTO
     */
    private ArticleCategoryDTO convertToDTO(ArticleCategory category) {
        ArticleCategoryDTO dto = new ArticleCategoryDTO();
        BeanUtils.copyProperties(category, dto);
        
        // 设置父分类名称
        if (category.getParentId() != null) {
            ArticleCategory parentCategory = this.getById(category.getParentId());
            if (parentCategory != null) {
                dto.setParentName(parentCategory.getName());
            }
        }
        
        // 设置状态名称
        dto.setStatusName(getStatusName(category.getStatus()));
        
        return dto;
    }

    /**
     * DTO转实体
     */
    private ArticleCategory convertToEntity(ArticleCategoryDTO dto) {
        ArticleCategory category = new ArticleCategory();
        BeanUtils.copyProperties(dto, category);
        return category;
    }

    /**
     * 获取状态名称
     */
    private String getStatusName(Integer status) {
        if (status == null) {
            return "未知";
        }
        switch (status) {
            case 0:
                return "禁用";
            case 1:
                return "启用";
            default:
                return "未知";
        }
    }
}