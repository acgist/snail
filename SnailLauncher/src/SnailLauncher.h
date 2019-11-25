// SnailLauncher.h : SnailLauncher 应用程序的主头文件
//

#pragma once

// 主符号
#include "resource.h"

class SnailLauncher : public CWinApp {
public:
	SnailLauncher();

// 重写
public:
	virtual BOOL InitInstance();
	virtual int ExitInstance();

// 实现
public:
	DECLARE_MESSAGE_MAP();
};

extern SnailLauncher launcher;
