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

// 单例 SnailLauncher 对象
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

bool startJVM() {
	// 环境变量
	SetEnvironmentVariable(_T("Path"), configEx(_T("java.path")));
	// JVM启动参数
	const int jvmOptionsCount = 7;
	JavaVMOption jvmOptions[jvmOptionsCount];
	jvmOptions[0].optionString = config(_T("model"));
	jvmOptions[1].optionString = config(_T("xms"));
	jvmOptions[2].optionString = config(_T("xmx"));
	jvmOptions[3].optionString = config(_T("file.encoding"));
	jvmOptions[4].optionString = config(_T("jar.path"));
	jvmOptions[5].optionString = config(_T("jvm.args.new.ratio"));
	jvmOptions[6].optionString = config(_T("jvm.args.survivor.ratio"));
	// 设置JVM启动参数
	JavaVMInitArgs jvmInitArgs;
	jvmInitArgs.version = JNI_VERSION_10;
	jvmInitArgs.options = jvmOptions;
	jvmInitArgs.nOptions = jvmOptionsCount;
	jvmInitArgs.ignoreUnrecognized = JNI_TRUE; // 忽略无法识别JVM错误
	// 加载JVM动态链接库
	TCHAR* jvmPath = configEx(_T("jvm.path"));
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
	if(jvmProc < 0 || jvm == NULL || env == NULL) {
		FreeLibrary(jvmDLL);
		::MessageBox(NULL, _T("JVM创建失败"), _T("启动失败"), MB_OK);
		return false;
	}
	// 启动类：注意分隔符
	const char startClass[] = "com/acgist/main/Application";
	// 加载启动类
	jclass mainClass = env -> FindClass(startClass);
	if(env -> ExceptionCheck() == JNI_TRUE || mainClass == NULL) {
		env -> ExceptionDescribe();
		env -> ExceptionClear();
		FreeLibrary(jvmDLL);
		::MessageBox(NULL, _T("启动类加载失败"), _T("启动失败"), MB_OK);
		return false;
	}
	// 启动方法：Main
	const char startMethod[] = "main";
	// 启动方法参数
	// const int argsCount = 2;
	// const char* args[argsCount] = {"a", "b"};
	// 加载启动方法
	jmethodID mainMethod = env -> GetStaticMethodID(mainClass, startMethod, "([Ljava/lang/String;)V");
	if(env -> ExceptionCheck() == JNI_TRUE || mainMethod == NULL) {
		env -> ExceptionDescribe();
		env -> ExceptionClear();
		FreeLibrary(jvmDLL);
		::MessageBox(NULL, _T("启动方法加载失败"), _T("启动失败"), MB_OK);
		return false;
	}
	// 启动JVM
	env -> CallStaticVoidMethod(mainClass, mainMethod, NULL);
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

// 扩展读取配置
TCHAR* configEx(LPCWSTR name) {
	char* value = config(name);
	int length = MultiByteToWideChar(CP_ACP, 0, value, -1, NULL, 0);
	TCHAR* buffer = new TCHAR[length * sizeof(TCHAR)];
	MultiByteToWideChar(CP_ACP, 0, value, -1, buffer, length);
	delete value;
	return buffer;
}
