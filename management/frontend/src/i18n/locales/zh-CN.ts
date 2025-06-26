import { OrderStatus } from "@/api/orderApi";
import { inputEmits } from "element-plus";
import { ca } from "element-plus/es/locales.mjs";

export default {
  // 通用
  common: {
    home: '首页',
    confirm: '确定',
    cancel: '取消',
    save: '保存',
    delete: '删除',
    edit: '编辑',
    add: '新增',
    search: '搜索',
    reset: '重置',
    submit: '提交',
    back: '返回',
    loading: '加载中...',
    noData: '暂无数据',
    operation: '操作',
    status: '状态',
    createTime: '创建时间',
    updateTime: '更新时间',
    action: '操作',
    yes: '是',
    no: '否',
    enable: '启用',
    disable: '禁用',
    normal: '正常',
    forbidden: '禁用',
    success: '成功',
    failed: '失败',
    tip: '提示',
    warning: '警告',
    error: '错误',
    info: '信息',
    unknown: '未知'
  },

  // 菜单
  menu: {
    dashboard: '仪表盘',
    system: '系统管理',
    admin: '管理员管理',
    role: '角色管理',
    permission: '权限管理',
    user: '用户管理',
    userList: '用户列表',
    article: '文章管理',
    articleList: '文章列表',
    articleCategory: '文章分类',
    articleTag: '文章标签',
    order: '订单管理',
    orderList: '订单列表',
    log: '系统日志',
    settings: '系统设置',
    profile: '个人中心',
    analysis: '用户分析'
  },

  // 登录
  login: {
    title: '用户登录',
    subtitle: '请输入您的账号信息',
    username: '用户名',
    password: '密码',
    rememberMe: '记住我',
    forgotPassword: '忘记密码？',
    loginBtn: '登录',
    loggingIn: '登录中...',
    loginSuccess: '登录成功',
    loginFailed: '登录失败',
    welcomeBack: '欢迎回来！正在跳转到主页...',
    contactAdmin: '请联系管理员重置密码',
    usernameRequired: '请输入用户名',
    passwordRequired: '请输入密码',
    usernameLength: '用户名长度在 2 到 20 个字符',
    passwordLength: '密码长度在 6 到 20 个字符'
  },

  // 头部操作
  header: {
    search: '搜索菜单...',
    fullscreen: '全屏',
    exitFullscreen: '退出全屏',
    toggleTheme: '切换主题',
    language: '语言',
    messageNotification: '消息通知',
    noMessages: '暂无消息',
    profile: '个人中心',
    settings: '系统设置',
    logout: '退出登录',
    logoutConfirm: '确定要退出登录吗？',
    logoutSuccess: '退出登录成功'
  },

  // 系统管理
  system: {
    notification: '系统通知',
    permissionReview: '您有新的权限配置需要审核',
    userFeedback: '用户反馈',
    feedbackSubmitted: '用户张三提交了新的反馈意见',
    // 管理员管理
    admin: {
      title: '管理员管理',
      subtitle: '管理系统管理员账户',
      systemAccountTitle: '系统账户管理',
      systemAccountSubtitle: '管理系统管理员账户，控制系统访问权限',
      username: '用户名',
      realName: '姓名',
      email: '邮箱',
      phone: '手机号',
      role: '角色',
      avatar: '头像',
      lastLoginTime: '最后登录',
      createAdmin: '新增管理员',
      editAdmin: '编辑管理员',
      deleteConfirm: '确定要删除管理员 "{name}" 吗？',
      deleteSuccess: '删除成功',
      deleteFailed: '删除失败',
      statusChangeSuccess: '状态更改成功',
      statusChangeFailed: '状态更改失败',
      enableSuccess: '启用成功',
      disableSuccess: '禁用成功',
      resetPassword: '重置密码',
      newPassword: '新密码',
      confirmPassword: '确认密码',
      passwordMismatch: '两次输入的密码不一致',
      uploadAvatar: '上传头像',
      downloadAvatar: '下载头像',
      deleteAvatar: '删除头像',
      download: '下载',
      avatarFormatError: '头像只能是 JPG/PNG 格式!',
      avatarSizeError: '头像图片大小不能超过 2MB!',
      // 搜索表单
      usernameLabel: '用户名',
      usernamePlaceholder: '请输入用户名',
      realNameLabel: '姓名',
      realNamePlaceholder: '请输入姓名',
      roleLabel: '角色',
      rolePlaceholder: '请选择角色',
      statusLabel: '状态',
      statusPlaceholder: '请选择状态',
      // 表格列头
      createTime: '创建时间',
      operation: '操作',
      // 操作按钮
      edit: '编辑',
      delete: '删除'
    },



    // 权限管理
    permission: {
      title: '权限管理',
      subtitle: '管理系统权限配置',
      permissionName: '权限名称',
      permissionCode: '权限编码',
      permissionType: '权限类型',
      parentPermission: '父级权限',
      menuPath: '菜单路径',
      icon: '图标',
      sortOrder: '排序'
    },

    // 用户管理
    user: {
      title: '用户管理',
      subtitle: '管理前台用户账户',
      userId: '用户ID',
      nickname: '昵称',
      email: '邮箱',
      phone: '手机号',
      registerTime: '注册时间',
      list: {
        title: '用户列表',
        subtitle: '管理系统用户信息',
        addUser: '新增用户',
        // 搜索表单
        usernameLabel: '用户名',
        usernamePlaceholder: '请输入用户名',
        nicknameLabel: '昵称',
        nicknamePlaceholder: '请输入昵称',
        emailLabel: '邮箱',
        emailPlaceholder: '请输入邮箱',
        statusLabel: '状态',
        statusPlaceholder: '请选择状态',
        // 表格列头
        avatar: '头像',
        username: '用户名',
        nickname: '昵称',
        email: '邮箱',
        phone: '手机号',
        gender: '性别',
        status: '状态',
        lastLoginTime: '最后登录',
        createTime: '注册时间',
        operation: '操作',
        // 性别
        male: '男',
        female: '女',
        unknown: '未知',
        // 状态
        normal: '正常',
        disabled: '禁用',
        // 操作按钮
        edit: '编辑',
        enable: '启用',
        disable: '禁用',
        resetPassword: '重置密码',
        delete: '删除'
      }
    },

    // 操作日志
    log: {
      title: '操作日志',
      subtitle: '查看系统操作记录，监控用户行为',
      operator: '操作人员',
      operationType: '操作类型',
      operationModule: '操作模块',
      operationTime: '操作时间',
      operationResult: '操作结果',
      operationDetail: '操作详情',
      ipAddress: 'IP地址',
      userAgent: '用户代理',
      exportLog: '导出日志',
      clearLog: '清空日志',
      clearConfirm: '确定要清空所有日志吗？此操作不可恢复！',
      // 搜索表单
      operatorLabel: '操作人员',
      operatorPlaceholder: '请输入操作人员',
      operationTypeLabel: '操作类型',
      operationTypePlaceholder: '请选择操作类型',
      operationModuleLabel: '操作模块',
      operationModulePlaceholder: '请选择操作模块',
      operationTimeLabel: '操作时间',
      dateRangeSeparator: '至',
      startDatePlaceholder: '开始日期',
      endDatePlaceholder: '结束日期',
      // 表格列头
      logId: '日志ID',
      username: '操作人员',
      operation: '操作类型',
      module: '操作模块',
      description: '操作描述',
      method: '请求方式',
      url: '请求URL',
      ip: 'IP地址',
      location: '操作地点',
      browser: '浏览器',
      status: '操作状态',
      costTime: '耗时(ms)',
      createTime: '操作时间',
      actions: '操作',
      // 状态
      success: '成功',
      failed: '失败',
      // 操作按钮
      detail: '详情',
      detailTitle: '操作日志详情',
      requestParams: '请求参数',
      responseResult: '响应结果',
      operations: {
        login: '登录',
        logout: '登出',
        create: '新增',
        update: '修改',
        delete: '删除',
        query: '查询'
      },
      modules: {
        user: '用户管理',
        role: '角色管理',
        permission: '权限管理',
        article: '文章管理',
        system: '系统设置'
      }
    },

    // 角色管理
    role: {
      title: '角色管理',
      subtitle: '管理系统角色信息，分配权限',
      addRole: '新增角色',
      editRole: '编辑角色',
      // 搜索表单
      roleNameLabel: '角色名称',
      roleNamePlaceholder: '请输入角色名称',
      statusLabel: '状态',
      statusPlaceholder: '请选择状态',
      // 表格列头
      roleId: '角色ID',
      roleName: '角色名称',
      roleCode: '角色编码',
      description: '描述',
      status: '状态',
      createTime: '创建时间',
      operations: '操作',
      // 权限统计
      permissionCount: '个权限',
      // 状态
      enabled: '启用',
      disabled: '禁用',
      // 操作按钮
      edit: '编辑',
      permission: '权限',
      delete: '删除',
      // 表单
      roleNameFormLabel: '角色名称',
      roleNameFormPlaceholder: '请输入角色名称',
      roleCodeFormLabel: '角色编码',
      roleCodeFormPlaceholder: '请输入角色编码',
      descriptionFormLabel: '描述',
      descriptionFormPlaceholder: '请输入角色描述',
      statusFormLabel: '状态',
      enableLabel: '启用',
      disableLabel: '禁用',
      // 权限配置
      permissionConfigTitle: '权限配置',
      selectedPermissions: '已选择',
      permissionActions: {
        expandAll: '展开全部',
        collapseAll: '收起全部',
        checkAll: '全选',
        uncheckAll: '取消全选'
      }
    }
  },

  // 仪表盘
  dashboard: {
    title: '仪表盘',
    welcome: '欢迎回来',
    admin: '管理员',
    totalUsers: '总用户数',
    totalArticles: '文章总数',
    todayVisits: '今日访问',
    onlineUsers: '在线用户',
    visitTrend: '访问趋势',
    userDistribution: '用户分布',
    quickActions: '快捷操作',
    recentActivity: '最近活动',
    timeRange: {
      '7d': '最近7天',
      '30d': '最近30天',
      '90d': '最近90天'
    },
    actions: {
      addAdmin: '新增管理员',
      publishArticle: '发布文章',
      roleManagement: '角色管理',
      systemLog: '系统日志'
    },
    activities: {
      login: '登录了系统',
      publishedArticle: '发布了新文章',
      modifiedPermission: '修改了用户权限',
      deletedArticle: '删除了一篇文章'
    },
    time: {
      minutesAgo: '分钟前',
      hoursAgo: '小时前'
    },

    orderSumData: {
      dataTitle: '订单统计数据',
      todayOrders: '今日订单数',
      monthOrders: '本月订单数',
      totalOrders: '总订单数'
    },

    hotProductsRange: {
      dataTitle: '热销商品排行',
      refresh: '刷新',
      
      noData: '暂无热销商品数据'
    }
  },

  // 文章管理
  article: {
    title: '文章管理',
    subtitle: '管理网站文章内容',
    list: {
      title: '文章列表',
      subtitle: '管理系统文章内容',
      publishArticle: '发布文章',
      // 搜索表单
      titleLabel: '文章标题',
      titlePlaceholder: '请输入文章标题',
      authorLabel: '作者',
      authorPlaceholder: '请输入作者名称',
      categoryLabel: '分类',
      categoryPlaceholder: '请选择分类',
      statusLabel: '状态',
      statusPlaceholder: '请选择状态',
      // 表格列头
      cover: '封面',
      articleTitle: '标题',
      category: '分类',
      author: '作者',
      viewCount: '浏览量',
      isTop: '置顶',
      isRecommend: '推荐',
      status: '状态',
      publishTime: '发布时间',
      createTime: '创建时间',
      actions: '操作',
      // 状态
      draft: '草稿',
      published: '已发布',
      unpublished: '已下架',
      noCover: '无封面',
      topTag: '置顶',
      recommendTag: '推荐',
      // 操作按钮
      preview: '预览',
      edit: '编辑',
      publish: '发布',
      unpublish: '下架',
      more: '更多',
      setTop: '设为置顶',
      cancelTop: '取消置顶',
      setRecommend: '设为推荐',
      cancelRecommend: '取消推荐',
      delete: '删除'
    },
    category: {
      title: '文章分类',
      subtitle: '管理文章分类信息',
      categoryName: '分类名称',
      categoryCode: '分类编码',
      description: '描述',
      articleCount: '文章数量',
      addCategory: '新增分类',
      editCategory: '编辑分类',
      // 搜索表单
      nameLabel: '分类名称',
      namePlaceholder: '请输入分类名称',
      statusLabel: '状态',
      statusPlaceholder: '请选择状态',
      // 表格列头
      parentName: '父分类',
      sortOrder: '排序',
      topLevel: '顶级分类',
      // 操作按钮
      edit: '编辑',
      delete: '删除',
      enable: '启用',
      disable: '禁用',
      deleteConfirm: '确定要删除分类 "{name}" 吗？',
      deleteTitle: '删除确认'
    },
    tag: {
      title: '文章标签',
      tagName: '标签名称',
      tagColor: '标签颜色',
      description: '标签描述',
      articleCount: '文章数量'
    }
  },

  order: {
    search: "订单搜索",
    orderNo: "订单号",
    inputOrderNo: "请输入订单号",
    username: "用户名",
    inputPutUsername: "请输入用户名",
    status: {
      title: '订单状态',
      waitPay: '待支付',
      paid: '已支付',
      waitDelivery: '待发货',
      delivered: '已发货',
      completed: '已完成',
      cancelled: '已取消',
    },
    refresh: '刷新',
    exportExcel: '导出Excel',
    table:{
      orderNo: '订单号',
      username: '用户名',
      orderPrice: '订单金额',
      orderStatus: '订单状态',
      payStatus: '支付状态',
      payment: '支付方式',
      createTime: '创建时间',
      updateTime: '更新时间',
      actions: '操作',
    }

  },

  // 错误页面
  error: {
    404: {
      title: '页面未找到',
      subtitle: '抱歉，您访问的页面不存在',
      backHome: '返回首页'
    },
    403: {
      title: '访问被拒绝',
      subtitle: '抱歉，您没有权限访问此页面',
      backHome: '返回首页'
    },
    500: {
      title: '服务器错误',
      subtitle: '抱歉，服务器出现了一些问题',
      backHome: '返回首页'
    }
  },

  // 表单验证
  validation: {
    required: '此字段为必填项',
    email: '请输入有效的邮箱地址',
    phone: '请输入有效的手机号码',
    minLength: '长度不能少于 {min} 个字符',
    maxLength: '长度不能超过 {max} 个字符',
    number: '请输入有效的数字',
    integer: '请输入有效的整数',
    positive: '请输入正数',
    url: '请输入有效的URL地址'
  },

  // 测试页面
  test: {
    title: 'i18n 国际化测试',
    subtitle: '测试中英文切换功能',
    currentLanguage: '当前语言',
    toggleLanguage: '切换语言',
    commonTexts: '通用文本',
    headerTexts: '头部文本',
    menuTexts: '菜单文本'
  },

  // 其他
  footer: {
    copyright: '© 2025 Nextera Management System. All rights reserved. Version 1.0.0'
  }
} 