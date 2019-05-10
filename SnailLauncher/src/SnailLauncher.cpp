// Snail启动器：SnailLauncher

#include "stdafx.h"
#include "afxwin.h"
#include "SnailLauncher.h"
#include "jni.h"

#ifdef _DEBUG
#define new DEBUG_NEW
#endif

// 加载JVM启动JAVA程序
bool startJVM();
typedef jint(JNICALL* JNICREATEPROC) (JavaVM**, void**, void*);

// SnailLauncher

BEGIN_MESSAGE_MAP(SnailLauncher, CWinApp)
END_MESSAGE_MAP()

// SnailLauncher 构造

SnailLauncher::SnailLauncher()
{
	// 应用程序 ID：CompanyName.ProductName.SubProduct.VersionInformation
	SetAppID(_T("acgist.Snail.SnailLauncher.1.0.1"));
}

// 唯一的一个 SnailLauncher 对象

SnailLauncher launcher;

// SnailLauncher 初始化

BOOL SnailLauncher::InitInstance()
{
	CWinApp::InitInstance();

	EnableTaskbarInteraction(FALSE);

	startJVM();

	return TRUE;
}

int SnailLauncher::ExitInstance()
{
	return CWinApp::ExitInstance();
}

// 启动JVM
bool startJVM(){
	// JVM动态库
	TCHAR* jvmPath = _T("./java/bin/server/jvm.dll");

	//JVM启动参数
	const int jvmOptionCount = 5;
	JavaVMOption jvmOptions[jvmOptionCount];
	jvmOptions[0].optionString = "-server";
	jvmOptions[1].optionString = "-Xmx128M";
	jvmOptions[2].optionString = "-Xmx128m";
	jvmOptions[3].optionString = "-Dfile.encoding=UTF-8";
	jvmOptions[4].optionString = "-Djava.class.path=./snail-1.0.1.jar";

	JavaVMInitArgs jvmInitArgs;
	jvmInitArgs.version = JNI_VERSION_10;
	jvmInitArgs.options = jvmOptions;
	jvmInitArgs.nOptions = jvmOptionCount;

	// 忽略无法识别jvm的情况
	jvmInitArgs.ignoreUnrecognized = JNI_TRUE;

	// 设置启动类，注意分隔符：（/），不能设置（.）
	const char startClass[] = "com/acgist/main/Application";

	// 启动方法，设置main函数
	const char startMethod[] = "main";

	// 传入参数
	// int nParamCount = 2;
	// const char* params[nParamCount] = {"a","b"};

	// 加载JVM动态链接库
	HINSTANCE jvmDLL = LoadLibrary(jvmPath);
	if(jvmDLL == NULL){
		::MessageBox(NULL, _T("加载JVM动态链接库失败"), _T("启动失败"), MB_OK);
		return false;
	}

	// 初始化JVM
	JNICREATEPROC jvmProcAddress = (JNICREATEPROC) GetProcAddress(jvmDLL, "JNI_CreateJavaVM");
	if(jvmDLL == NULL){
		FreeLibrary(jvmDLL);
		::MessageBox(NULL, _T("初始化JVM失败"), _T("启动失败"), MB_OK);
		return false;
	}

	// 创建JVM
	JNIEnv* env;
	JavaVM* jvm;
	jint jvmProc = (jvmProcAddress) (&jvm, (void**) &env, &jvmInitArgs);
	if(jvmProc < 0 || jvm == NULL ||env == NULL){
		FreeLibrary(jvmDLL);
		::MessageBox(NULL, _T("创建JVM失败"), _T("启动失败"), MB_OK);
		return false;
	}

	// 加载启动类
	jclass mainClass = env -> FindClass(startClass);
	if(env -> ExceptionCheck() == JNI_TRUE || mainClass == NULL){
		env -> ExceptionDescribe();
		env -> ExceptionClear();
		FreeLibrary(jvmDLL);
		::MessageBox(NULL, _T("加载Java Class失败"), _T("启动失败"), MB_OK);
		return false;
	}

	// 加载启动方法
	jmethodID methedID = env -> GetStaticMethodID(mainClass, startMethod, "([Ljava/lang/String;)V");
	if(env -> ExceptionCheck() == JNI_TRUE || methedID == NULL){
		env -> ExceptionDescribe();
		env -> ExceptionClear();
		FreeLibrary(jvmDLL);
		::MessageBox(NULL, _T("启动Java Main方法失败"), _T("启动失败"), MB_OK);
		return false;
	}

	env -> CallStaticVoidMethod(mainClass, methedID, NULL);

	// 释放JVM
	jvm -> DestroyJavaVM();

	return true;
}