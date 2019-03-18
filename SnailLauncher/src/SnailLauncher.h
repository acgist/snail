
// SnailLauncher.h : SnailLauncher 应用程序的主头文件
//
#pragma once

#ifndef __AFXWIN_H__
	#error "在包含此文件之前包含“stdafx.h”以生成 PCH 文件"
#endif

// 主符号
#include "resource.h"

class SnailLauncher : public CWinApp
{
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
