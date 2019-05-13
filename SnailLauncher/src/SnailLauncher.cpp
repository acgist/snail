// Snail启动器：SnailLauncher
//

#include "jni.h"
#include "stdafx.h"
#include "afxwin.h"
#include "SnailLauncher.h"

// 启动JVM
bool startJVM();
// 读取配置
char* config(LPCWSTR name);
TCHAR* configEx(LPCWSTR name);

typedef jint(JNICALL* JNICREATEPROC) (JavaVM**, void**, void*);

// SnailLauncher
BEGIN_MESSAGE_MAP(SnailLauncher, CWinApp)
END_MESSAGE_MAP()

// SnailLauncher 构造
SnailLauncher::SnailLauncher() {
	// 应用程序 ID：CompanyName.ProductName.SubProduct.VersionInformation
	SetAppID(_T("acgist.Snail.SnailLauncher.1.0.0.0"));
}

// 唯一的一个 SnailLauncher 对象
SnailLauncher launcher;

// SnailLauncher 初始化
BOOL SnailLauncher::InitInstance() {
	CWinApp::InitInstance();
	EnableTaskbarInteraction(FALSE);
	startJVM();
	return TRUE;
}

int SnailLauncher::ExitInstance() {
	return CWinApp::ExitInstance();
}

// 启动JVM
bool startJVM() {
	// 修改环境变量，设置java.lib.path无效
	SetEnvironmentVariable(_T("Path"), configEx(_T("java.path")));
	// JVM动态库
	TCHAR* jvmPath = configEx(_T("jvm.file.path"));
	// JVM启动参数
	const int jvmOptionCount = 5;
	JavaVMOption jvmOptions[jvmOptionCount];
	jvmOptions[0].optionString = config(_T("model"));
	jvmOptions[1].optionString = config(_T("xms"));
	jvmOptions[2].optionString = config(_T("xmx"));
	jvmOptions[3].optionString = config(_T("file.encoding"));
	jvmOptions[4].optionString = config(_T("jar.file.path"));
	// jvmOptions[5].optionString = config(_T("java.lib.path")); // 无效直接设置环境变量
	// 设置JVM启动参数
	JavaVMInitArgs jvmInitArgs;
	jvmInitArgs.version = JNI_VERSION_10;
	jvmInitArgs.options = jvmOptions;
	jvmInitArgs.nOptions = jvmOptionCount;
	// 忽略无法识别JVM的情况
	jvmInitArgs.ignoreUnrecognized = JNI_TRUE;
	// 设置启动类，注意分隔符（/），不能设置（.）
	const char startClass[] = "com/acgist/main/Application";
	// 启动方法，设置main函数
	const char startMethod[] = "main";
	// 启动参数
	// int paramCount = 2;
	// const char* params[paramCount] = {"a", "b"};
	// 加载JVM动态链接库
	HINSTANCE jvmDLL = LoadLibrary(jvmPath);
	if(jvmDLL == NULL) {
		::MessageBox(NULL, _T("JVM动态链接库加载失败"), _T("启动失败"), MB_OK);
		return false;
	}
	// 初始化JVM
	JNICREATEPROC jvmProcAddress = (JNICREATEPROC) GetProcAddress(jvmDLL, "JNI_CreateJavaVM");
	if(jvmDLL == NULL) {
		FreeLibrary(jvmDLL);
		::MessageBox(NULL, _T("JVM初始化失败"), _T("启动失败"), MB_OK);
		return false;
	}
	// 创建JVM
	JNIEnv* env;
	JavaVM* jvm;
	jint jvmProc = (jvmProcAddress) (&jvm, (void**) &env, &jvmInitArgs);
	if(jvmProc < 0 || jvm == NULL ||env == NULL) {
		FreeLibrary(jvmDLL);
		::MessageBox(NULL, _T("JVM创建失败"), _T("启动失败"), MB_OK);
		return false;
	}
	// 加载启动类
	jclass mainClass = env -> FindClass(startClass);
	if(env -> ExceptionCheck() == JNI_TRUE || mainClass == NULL) {
		env -> ExceptionDescribe();
		env -> ExceptionClear();
		FreeLibrary(jvmDLL);
		::MessageBox(NULL, _T("JavaMainClass加载失败"), _T("启动失败"), MB_OK);
		return false;
	}
	// 加载启动方法
	jmethodID methedID = env -> GetStaticMethodID(mainClass, startMethod, "([Ljava/lang/String;)V");
	if(env -> ExceptionCheck() == JNI_TRUE || methedID == NULL) {
		env -> ExceptionDescribe();
		env -> ExceptionClear();
		FreeLibrary(jvmDLL);
		::MessageBox(NULL, _T("JavaMainMethod加载失败"), _T("启动失败"), MB_OK);
		return false;
	}
	// 启动JVM
	env -> CallStaticVoidMethod(mainClass, methedID, NULL);
	// 释放JVM
	jvm -> DestroyJavaVM();
	return true;
}

// 读取配置
char* config(LPCWSTR name) {
	// 配置文件、临时变量
	CString configPath = _T("./snail.ini"), value;
	// 读取配置，config=节，name=键，value=值
	GetPrivateProfileString(_T("config"), name, NULL, value.GetBuffer(128), 128, configPath);
	int length = WideCharToMultiByte(CP_ACP, 0, value, -1, NULL, 0, NULL, NULL);
	char* buffer = new char[sizeof(char) * length];
	WideCharToMultiByte(CP_ACP, 0, value, -1, buffer, length, NULL, NULL);
	return buffer;
}

TCHAR* configEx(LPCWSTR name) {
	char* value = config(name);
	int length = MultiByteToWideChar(CP_ACP, 0, value, -1, NULL, 0);
	TCHAR* buffer = new TCHAR[length * sizeof(TCHAR)];
	MultiByteToWideChar(CP_ACP, 0, value, -1, buffer, length);
	delete value;
	return buffer;
}

// TCHAR -> char
/*
	int length = WideCharToMultiByte(CP_ACP, 0, value, -1, NULL, 0, NULL, NULL);
	char* buffer = new char[sizeof(char) * length];
	WideCharToMultiByte(CP_ACP, 0, value, -1, buffer, length, NULL, NULL);
*/

// char -> TCHAR
/*
	int length = MultiByteToWideChar(CP_ACP, 0, value, -1, NULL, 0);
	TCHAR* buffer = new TCHAR[length * sizeof(TCHAR)];
	MultiByteToWideChar(CP_ACP, 0, value, -1, buffer, length);
*/