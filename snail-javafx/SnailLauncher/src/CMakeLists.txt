# 设置JVM头文件
include_directories("../../builder/snail/java/include/" "../../builder/snail/java/include/win32/")
# 设置源码
FILE(GLOB H_FILES "./*.h")
FILE(GLOB RC_FILES "./*.rc")
FILE(GLOB CPP_FILES "./*.cpp")
# 设置生成可执行文件
add_executable(SnailLauncher ${H_FILES} ${RC_FILES} ${CPP_FILES})
set_target_properties(SnailLauncher PROPERTIES LINK_FLAGS "/SUBSYSTEM:WINDOWS")