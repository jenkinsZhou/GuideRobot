
cmake_minimum_required(VERSION 3.4.1)

#设置生成的so动态库最后输出的路径
set(CMAKE_LIBRARY_OUTPUT_DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR}/src/main/jni/${ANDROID_ABI})

find_library( # Sets the name of the path variable.
              log-lib

              # Specifies the name of the NDK library that
              # you want CMake to locate.
              log
              android
              )



add_library( # Sets the name of the library.
      # 设置so文件名称.
       serialPort

       # Sets the library as a shared library.
       SHARED
       # 设置这个so文件为共享.

       # Provides a relative path to your source file(s).
       # 设置这个so文件为共享.
       src/main/jni/serialPort.c
       )

target_link_libraries( # Specifies the target library.
            # 制定目标库.
            serialPort

            # Links the target library to the log library
            # included in the NDK.
            ${log-lib} )