# pms
### 性能监控系统(p)erformance (m)onitor (s)ystem

### 项目目标：
- 分析梳理系统调用层次关系，通过parentid表示，具体顺序通过orderno表示
- 监控系统方法执行性能，包括方法执行总时间，以及排除调用系统其它方法的真实耗时
- 记录项目调用时传参，方便后期跟踪排查问题
- 方便汇总功能调用频次及其相应的性能

### 技术栈：
- java 8
- spring boot
- maven
- hibernate
- kafka
- mysql等

### 待办事项：
- 引入前端查看功能
- 记录真实类和参数
- 改造为spring boot starter
