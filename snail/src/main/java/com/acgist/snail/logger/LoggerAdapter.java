package com.acgist.snail.logger;

import java.io.IOException;
import java.io.OutputStream;

/**
 * 日志适配器
 * 
 * @author acgist
 */
public abstract class LoggerAdapter {

    /**
     * 是否可用
     */
    private volatile boolean available = true;
    /**
     * 常规日志输出流
     */
    protected OutputStream output;
    /**
     * 异常日志输出流
     */
    protected OutputStream errorOutput;

    protected LoggerAdapter() {
    }

    /**
     * @param output      常规日志输出流
     * @param errorOutput 异常日志输出流
     */
    protected LoggerAdapter(OutputStream output, OutputStream errorOutput) {
        this.output      = output;
        this.errorOutput = errorOutput;
    }
    
    /**
     * 输出日志
     * 
     * @param message 日志
     */
    public void output(String message) {
        if (this.available) {
            try {
                this.output.write(message.getBytes());
            } catch (IOException e) {
                LoggerFactory.error(e);
            }
        }
    }
    
    /**
     * 输出错误日志
     * 
     * @param message 日志
     */
    public void errorOutput(String message) {
        if (this.available) {
            try {
                this.errorOutput.write(message.getBytes());
            } catch (IOException e) {
                LoggerFactory.error(e);
            }
        }
    }
    
    /**
     * 释放资源
     */
    public void release() {
        this.available = false;
        // 是否需要关闭错误输出
        final boolean closeErrorOutput = this.output != this.errorOutput;
        if(this.output != null) {
            try {
                this.output.flush();
                this.output.close();
            } catch (IOException e) {
                LoggerFactory.error(e);
            }
        }
        if(closeErrorOutput && this.errorOutput != null) {
            try {
                this.errorOutput.flush();
                this.errorOutput.close();
            } catch (IOException e) {
                LoggerFactory.error(e);
            }
        }
    }
    
}
