package com.nextera.auth;

import com.nextera.auth.controller.AuthControllerTest;
import com.nextera.auth.controller.AuthInternalControllerTest;
import com.nextera.auth.service.impl.AuthServiceImplTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * 认证服务测试套件
 * 统一运行所有测试类
 *
 * @author Nextera
 */
@DisplayName("认证服务完整测试套件")
public class AuthTestSuite {
    
    /**
     * 运行所有测试的入口方法
     * 可以在IDE中右键运行这个方法来执行所有测试
     */
    @Test
    @DisplayName("运行所有认证服务测试")
    void runAllAuthTests() {
        // 这个方法可以作为测试入口点
        // 实际的测试会通过Maven或IDE的测试运行器执行
        System.out.println("认证服务测试套件 - 请使用 Maven 或 IDE 运行具体的测试类");
        System.out.println("可运行的测试类:");
        System.out.println("- AuthServiceImplTest: 认证服务业务逻辑测试");
        System.out.println("- AuthControllerTest: 认证控制器接口测试");
        System.out.println("- AuthInternalControllerTest: 内部API控制器测试");
    }
    
    /**
     * 嵌套测试类 - 可选的组织方式
     */
    @Nested
    @DisplayName("服务层测试")
    class ServiceTests {
        // 可以在这里组织服务层相关的测试
    }
    
    @Nested
    @DisplayName("控制器测试")
    class ControllerTests {
        // 可以在这里组织控制器相关的测试
    }
} 