// stdafx.h：标准系统包含文件的包含文件或是经常使用但不常更改的特定于项目的包含文件
//

#pragma once

#ifndef VC_EXTRALEAN
#define VC_EXTRALEAN // 从 Windows 头中排除极少使用的资料
#endif

#include "targetver.h"

#define _ATL_CSTRING_EXPLICIT_CONSTRUCTORS // 某些 CString 构造函数将是显式的

#define _AFX_ALL_WARNINGS // 关闭 MFC 对某些常见但经常可放心忽略的警告消息的隐藏

#include <afxwin.h> // MFC 核心组件和标准组件
#include <afxext.h> // MFC 扩展

#ifndef _AFX_NO_OLE_SUPPORT
#include <afxdtctl.h> // MFC 对 Internet Explorer 4 公共控件的支持
#endif
#ifndef _AFX_NO_AFXCMN_SUPPORT
#include <afxcmn.h> // MFC 对 Windows 公共控件的支持
#endif

#include <afxcontrolbars.h> // 功能区和控件条的 MFC 支持
