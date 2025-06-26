export default {
  // Common
  common: {
    home: 'Home',
    confirm: 'Confirm',
    cancel: 'Cancel',
    save: 'Save',
    delete: 'Delete',
    edit: 'Edit',
    add: 'Add',
    search: 'Search',
    reset: 'Reset',
    submit: 'Submit',
    back: 'Back',
    loading: 'Loading...',
    noData: 'No Data',
    operation: 'Operation',
    status: 'Status',
    createTime: 'Create Time',
    updateTime: 'Update Time',
    action: 'Action',
    yes: 'Yes',
    no: 'No',
    enable: 'Enable',
    disable: 'Disable',
    normal: 'Normal',
    forbidden: 'Disabled',
    success: 'Success',
    failed: 'Failed',
    tip: 'Tip',
    warning: 'Warning',
    error: 'Error',
    info: 'Info',
    unknown: 'Unknown'
  },

  // Menu
  menu: {
    dashboard: 'Dashboard',
    system: 'System Management',
    admin: 'Admin Management',
    role: 'Role Management',
    permission: 'Permission Management',
    user: 'User Management',
    userList: 'User List',
    article: 'Article Management',
    articleList: 'Article List',
    articleCategory: 'Article Category',
    articleTag: 'Article Tag',
    order: 'Order Management',
    orderList: 'Order List',
    log: 'System Log',
    settings: 'System Settings',
    profile: 'Profile',
    analysis: 'User Analysis'
  },

  // Login
  login: {
    title: 'User Login',
    subtitle: 'Please enter your account information',
    username: 'Username',
    password: 'Password',
    rememberMe: 'Remember Me',
    forgotPassword: 'Forgot Password?',
    loginBtn: 'Login',
    loggingIn: 'Logging in...',
    loginSuccess: 'Login Successful',
    loginFailed: 'Login Failed',
    welcomeBack: 'Welcome back! Redirecting to home page...',
    contactAdmin: 'Please contact the administrator to reset your password',
    usernameRequired: 'Please enter username',
    passwordRequired: 'Please enter password',
    usernameLength: 'Username length should be 2 to 20 characters',
    passwordLength: 'Password length should be 6 to 20 characters'
  },

  // Header
  header: {
    search: 'Search menu...',
    fullscreen: 'Fullscreen',
    exitFullscreen: 'Exit Fullscreen',
    toggleTheme: 'Toggle Theme',
    language: 'Language',
    messageNotification: 'Message Notification',
    noMessages: 'No Messages',
    profile: 'Profile',
    settings: 'Settings',
    logout: 'Logout',
    logoutConfirm: 'Are you sure you want to logout?',
    logoutSuccess: 'Logout successful'
  },

  // System Management
  system: {
    notification: 'System Notification',
    permissionReview: 'You have new permission configurations to review',
    userFeedback: 'User Feedback',
    feedbackSubmitted: 'User Zhang San submitted new feedback',
    // Admin Management
    admin: {
      title: 'Admin Management',
      subtitle: 'Manage system administrator accounts',
      systemAccountTitle: 'System Account Management',
      systemAccountSubtitle: 'Manage system administrator accounts and control system access permissions',
      username: 'Username',
      realName: 'Real Name',
      email: 'Email',
      phone: 'Phone',
      role: 'Role',
      avatar: 'Avatar',
      lastLoginTime: 'Last Login',
      createAdmin: 'Create Admin',
      editAdmin: 'Edit Admin',
      deleteConfirm: 'Are you sure you want to delete admin "{name}"?',
      deleteSuccess: 'Delete successful',
      deleteFailed: 'Delete failed',
      statusChangeSuccess: 'Status change successful',
      statusChangeFailed: 'Status change failed',
      enableSuccess: 'Enable successful',
      disableSuccess: 'Disable successful',
      resetPassword: 'Reset Password',
      newPassword: 'New Password',
      confirmPassword: 'Confirm Password',
      passwordMismatch: 'Passwords do not match',
      uploadAvatar: 'Upload Avatar',
      downloadAvatar: 'Download Avatar',
      deleteAvatar: 'Delete Avatar',
      download: 'Download',
      avatarFormatError: 'Avatar must be JPG/PNG format!',
      avatarSizeError: 'Avatar size cannot exceed 2MB!',
      // Search form
      usernameLabel: 'Username',
      usernamePlaceholder: 'Enter username',
      realNameLabel: 'Real Name',
      realNamePlaceholder: 'Enter real name',
      roleLabel: 'Role',
      rolePlaceholder: 'Select role',
      statusLabel: 'Status',
      statusPlaceholder: 'Select status',
      // Table headers
      createTime: 'Create Time',
      operation: 'Operations',
      // Action buttons
      edit: 'Edit',
      delete: 'Delete'
    },



    // Permission Management
    permission: {
      title: 'Permission Management',
      subtitle: 'Manage system permission configuration',
      permissionName: 'Permission Name',
      permissionCode: 'Permission Code',
      permissionType: 'Permission Type',
      parentPermission: 'Parent Permission',
      menuPath: 'Menu Path',
      icon: 'Icon',
      sortOrder: 'Sort Order'
    },

    // User Management
    user: {
      title: 'User Management',
      subtitle: 'Manage frontend user accounts',
      userId: 'User ID',
      nickname: 'Nickname',
      email: 'Email',
      phone: 'Phone',
      registerTime: 'Register Time',
      list: {
        title: 'User List',
        subtitle: 'Manage system user information',
        addUser: 'Add User',
        // Search form
        usernameLabel: 'Username',
        usernamePlaceholder: 'Enter username',
        nicknameLabel: 'Nickname',
        nicknamePlaceholder: 'Enter nickname',
        emailLabel: 'Email',
        emailPlaceholder: 'Enter email',
        statusLabel: 'Status',
        statusPlaceholder: 'Select status',
        // Table headers
        avatar: 'Avatar',
        username: 'Username',
        nickname: 'Nickname',
        email: 'Email',
        phone: 'Phone',
        gender: 'Gender',
        status: 'Status',
        lastLoginTime: 'Last Login',
        createTime: 'Register Time',
        operation: 'Operations',
        // Gender
        male: 'Male',
        female: 'Female',
        unknown: 'Unknown',
        // Status
        normal: 'Normal',
        disabled: 'Disabled',
        // Action buttons
        edit: 'Edit',
        enable: 'Enable',
        disable: 'Disable',
        resetPassword: 'Reset Password',
        delete: 'Delete'
      }
    },

    // Operation Log
    log: {
      title: 'Operation Log',
      subtitle: 'View system operation records and monitor user behavior',
      operator: 'Operator',
      operationType: 'Operation Type',
      operationModule: 'Operation Module',
      operationTime: 'Operation Time',
      operationResult: 'Operation Result',
      operationDetail: 'Operation Detail',
      ipAddress: 'IP Address',
      userAgent: 'User Agent',
      exportLog: 'Export Log',
      clearLog: 'Clear Log',
      clearConfirm: 'Are you sure you want to clear all logs? This operation cannot be undone!',
      // Search form
      operatorLabel: 'Operator',
      operatorPlaceholder: 'Enter operator',
      operationTypeLabel: 'Operation Type',
      operationTypePlaceholder: 'Select operation type',
      operationModuleLabel: 'Operation Module',
      operationModulePlaceholder: 'Select operation module',
      operationTimeLabel: 'Operation Time',
      dateRangeSeparator: 'to',
      startDatePlaceholder: 'Start date',
      endDatePlaceholder: 'End date',
      // Table headers
      logId: 'Log ID',
      username: 'Operator',
      operation: 'Operation Type',
      module: 'Operation Module',
      description: 'Operation Description',
      method: 'Request Method',
      url: 'Request URL',
      ip: 'IP Address',
      location: 'Location',
      browser: 'Browser',
      status: 'Operation Status',
      costTime: 'Cost Time(ms)',
      createTime: 'Operation Time',
      actions: 'Actions',
      // Status
      success: 'Success',
      failed: 'Failed',
      // Action buttons
      detail: 'Detail',
      detailTitle: 'Operation Log Detail',
      requestParams: 'Request Parameters',
      responseResult: 'Response Result',
      operations: {
        login: 'Login',
        logout: 'Logout',
        create: 'Create',
        update: 'Update',
        delete: 'Delete',
        query: 'Query'
      },
      modules: {
        user: 'User Management',
        role: 'Role Management',
        permission: 'Permission Management',
        article: 'Article Management',
        system: 'System Settings'
      }
    },

    // Role Management
    role: {
      title: 'Role Management',
      subtitle: 'Manage system role information and assign permissions',
      addRole: 'Add Role',
      editRole: 'Edit Role',
      // Search form
      roleNameLabel: 'Role Name',
      roleNamePlaceholder: 'Enter role name',
      statusLabel: 'Status',
      statusPlaceholder: 'Select status',
      // Table headers
      roleId: 'Role ID',
      roleName: 'Role Name',
      roleCode: 'Role Code',
      description: 'Description',
      status: 'Status',
      createTime: 'Create Time',
      operations: 'Operations',
      // Permission count
      permissionCount: 'permissions',
      // Status
      enabled: 'Enabled',
      disabled: 'Disabled',
      // Action buttons
      edit: 'Edit',
      permission: 'Permission',
      delete: 'Delete',
      // Form
      roleNameFormLabel: 'Role Name',
      roleNameFormPlaceholder: 'Enter role name',
      roleCodeFormLabel: 'Role Code',
      roleCodeFormPlaceholder: 'Enter role code',
      descriptionFormLabel: 'Description',
      descriptionFormPlaceholder: 'Enter role description',
      statusFormLabel: 'Status',
      enableLabel: 'Enable',
      disableLabel: 'Disable',
      // Permission configuration
      permissionConfigTitle: 'Permission Configuration',
      selectedPermissions: 'Selected',
      permissionActions: {
        expandAll: 'Expand All',
        collapseAll: 'Collapse All',
        checkAll: 'Check All',
        uncheckAll: 'Uncheck All'
      }
    }
  },

  // Dashboard
  dashboard: {
    title: 'Dashboard',
    welcome: 'Welcome back',
    admin: 'Administrator',
    totalUsers: 'Total Users',
    totalArticles: 'Total Articles',
    todayVisits: 'Today\'s Visits',
    onlineUsers: 'Online Users',
    visitTrend: 'Visit Trend',
    userDistribution: 'User Distribution',
    quickActions: 'Quick Actions',
    recentActivity: 'Recent Activity',
    timeRange: {
      '7d': 'Last 7 days',
      '30d': 'Last 30 days',
      '90d': 'Last 90 days'
    },
    actions: {
      addAdmin: 'Add Admin',
      publishArticle: 'Publish Article',
      roleManagement: 'Role Management',
      systemLog: 'System Log'
    },
    activities: {
      login: 'logged into the system',
      publishedArticle: 'published a new article',
      modifiedPermission: 'modified user permissions',
      deletedArticle: 'deleted an article'
    },
    time: {
      minutesAgo: 'minutes ago',
      hoursAgo: 'hours ago'
    },

     orderSumData: {
      dataTitle: 'Order Summary',
      todayOrders: 'Today\'s Orders',
      monthOrders: 'month Orders',
      totalOrders: 'Total Orders'
    },

    hotProductsRange: {
      dataTitle: 'Hot Products',
      refresh: 'Refresh',
      
      noData: 'No hot products data available'
    }
  },

  // Article Management
  article: {
    title: 'Article Management',
    subtitle: 'Manage website article content',
    list: {
      title: 'Article List',
      subtitle: 'Manage system article content',
      publishArticle: 'Publish Article',
      // Search form
      titleLabel: 'Article Title',
      titlePlaceholder: 'Enter article title',
      authorLabel: 'Author',
      authorPlaceholder: 'Enter author name',
      categoryLabel: 'Category',
      categoryPlaceholder: 'Select category',
      statusLabel: 'Status',
      statusPlaceholder: 'Select status',
      // Table headers
      cover: 'Cover',
      articleTitle: 'Title',
      category: 'Category',
      author: 'Author',
      viewCount: 'Views',
      isTop: 'Top',
      isRecommend: 'Recommend',
      status: 'Status',
      publishTime: 'Publish Time',
      createTime: 'Create Time',
      actions: 'Operation',
      // Status
      draft: 'Draft',
      published: 'Published',
      unpublished: 'Unpublished',
      noCover: 'No Cover',
      topTag: 'Top',
      recommendTag: 'Recommend',
      // Action buttons
      preview: 'Preview',
      edit: 'Edit',
      publish: 'Publish',
      unpublish: 'Unpublish',
      more: 'More',
      setTop: 'Set Top',
      cancelTop: 'Cancel Top',
      setRecommend: 'Set Recommend',
      cancelRecommend: 'Cancel Recommend',
      delete: 'Delete'
    },
    category: {
      title: 'Article Category',
      subtitle: 'Manage article category information',
      categoryName: 'Category Name',
      categoryCode: 'Category Code',
      description: 'Description',
      articleCount: 'Article Count',
      addCategory: 'Add Category',
      editCategory: 'Edit Category',
      // Search form
      nameLabel: 'Category Name',
      namePlaceholder: 'Enter category name',
      statusLabel: 'Status',
      statusPlaceholder: 'Select status',
      // Table headers
      parentName: 'Parent Category',
      sortOrder: 'Sort Order',
      topLevel: 'Top Level',
      // Action buttons
      edit: 'Edit',
      delete: 'Delete',
      enable: 'Enable',
      disable: 'Disable',
      deleteConfirm: 'Are you sure you want to delete category "{name}"?',
      deleteTitle: 'Delete Confirmation'
    },
    tag: {
      title: 'Article Tag',
      tagName: 'Tag Name',
      tagColor: 'Tag Color',
      description: 'Description',
      articleCount: 'Article Count'
    }
  },

   order: {
    search: "Order search",
    orderNo: "OrderNo",
    inputOrderNo: "Please enter order no",
    username: "Username",
    inputUsername: "Please enter username",
    status: {
      title: 'OrderStatus',
      waitPay: 'waiting for payment',
      paid: 'paid',
      waitDelivery: 'waiting for delivery',
      delivered: 'delivered',
      completed: 'completed',
      cancelled: 'cancelled',
    },
    refresh: 'Refresh',
    exportExcel: 'Export Excel',
    table:{
      orderNo: 'OrderNo',
      username: 'Username',
      orderPrice: 'Order Price',
      orderStatus: 'Order Status',
      payStatus: 'Payment Status',
      payment: 'Payment Method',
      createTime: 'Create Time',
      updateTime: 'Update Time',
      actions: 'Actions',
    }
    
  },

  // Error Pages
  error: {
    404: {
      title: 'Page Not Found',
      subtitle: 'Sorry, the page you are looking for does not exist',
      backHome: 'Back to Home'
    },
    403: {
      title: 'Access Denied',
      subtitle: 'Sorry, you do not have permission to access this page',
      backHome: 'Back to Home'
    },
    500: {
      title: 'Server Error',
      subtitle: 'Sorry, there was a server error',
      backHome: 'Back to Home'
    }
  },

  // Form Validation
  validation: {
    required: 'This field is required',
    email: 'Please enter a valid email address',
    phone: 'Please enter a valid phone number',
    minLength: 'Length cannot be less than {min} characters',
    maxLength: 'Length cannot be more than {max} characters',
    number: 'Please enter a valid number',
    integer: 'Please enter a valid integer',
    positive: 'Please enter a positive number',
    url: 'Please enter a valid URL'
  },

  // Test Page
  test: {
    title: 'i18n Internationalization Test',
    subtitle: 'Test Chinese/English switching functionality',
    currentLanguage: 'Current Language',
    toggleLanguage: 'Toggle Language',
    commonTexts: 'Common Texts',
    headerTexts: 'Header Texts',
    menuTexts: 'Menu Texts'
  },

  // Other
  footer: {
    copyright: '© 2025 Nextera Management System. All rights reserved. Version 1.0.0'
  }
} 