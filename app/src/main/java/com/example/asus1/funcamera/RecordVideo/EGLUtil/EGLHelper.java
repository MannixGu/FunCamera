package com.example.asus1.funcamera.RecordVideo.EGLUtil;

import android.content.Context;
import android.opengl.EGL14;
import android.opengl.EGLConfig;
import android.opengl.EGLContext;
import android.opengl.EGLDisplay;
import android.opengl.EGLSurface;
import android.opengl.GLES20;
import android.util.Log;
import android.view.Surface;

import com.example.asus1.funcamera.RecordVideo.Views.Photo;
import com.example.asus1.funcamera.Utils.ShaderUtil;

import javax.microedition.khronos.egl.EGL;


public class EGLHelper {

    private EGLDisplay mDisplay; //对实际显示设备的抽象
    private EGLContext mShare_Context;
    private EGLContext mEGLContext = EGL14.EGL_NO_CONTEXT;

    private EGLSurface mSurface;
    private Surface mlinkSurface;
    private Photo mPhoto;
    private static final int EGL_RECORDABLE_ANDROID = 0x3142;
    private EGLConfig mConfig;
    private int mWidth;
    private int mHeight;

    private static final String TAG = "EGLHelper";

    public EGLHelper(EGLContext context, Surface surface,int textId){

        mShare_Context = context;
        mlinkSurface = surface;
        //先创建环境
        init();
        mPhoto = new Photo();
        mlinkSurface = null;

    }

    private void init(){
        if ( (mDisplay = EGL14.
                eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY)) == EGL14.EGL_NO_DISPLAY) {
            throw new RuntimeException("unable to get EGL14 display");
        }

        //配置版本号
        int[] version = new int[2];
        if (!EGL14.eglInitialize(mDisplay, version,0, version,1)) {
            throw new RuntimeException("unable to initialize EGL14");
        }

        //配置选项
        int[] configAttribs = {
                EGL14.EGL_BUFFER_SIZE,32,
                EGL14.EGL_ALPHA_SIZE,8,
                EGL14.EGL_BLUE_SIZE,8,
                EGL14.EGL_GREEN_SIZE,8,
                EGL14.EGL_RED_SIZE,8,
                EGL14.EGL_RENDERABLE_TYPE,EGL14.EGL_OPENGL_ES2_BIT,
                EGL14.EGL_SURFACE_TYPE,EGL14.EGL_WINDOW_BIT,
                EGL14.EGL_NONE
        };

        EGLConfig[] config = new EGLConfig[1];
        int[] numComfig = new int[1];
        EGL14.eglChooseConfig(mDisplay,configAttribs,0,config,0,
                config.length,numComfig,0);
        mConfig = config[0];

        //创建OpenGL上下文
        int[] attrib_list = {
                EGL14.EGL_CONTEXT_CLIENT_VERSION, 2,
                EGL14.EGL_NONE
        };

        mShare_Context = mShare_Context!=null?mShare_Context:EGL14.EGL_NO_CONTEXT;
        if(mEGLContext == EGL14.EGL_NO_CONTEXT){
            mEGLContext = EGL14.eglCreateContext(mDisplay,
                    config[0],mShare_Context,
                    attrib_list, 0);
            ShaderUtil.checkGLError("eglCreateContext");
        }

        final int[] values = new int[1];
        EGL14.eglQueryContext(mDisplay, mEGLContext, EGL14.EGL_CONTEXT_CLIENT_VERSION, values, 0);

        mSurface =EGL14.EGL_NO_SURFACE;

        int[] surfaceAttribs = {
                EGL14.EGL_NONE
        };
        mSurface = EGL14.eglCreateWindowSurface(mDisplay,mConfig,
                mlinkSurface,surfaceAttribs,0);
       mWidth = querySurface(mSurface, EGL14.EGL_WIDTH);
       mHeight = querySurface(mSurface,EGL14.EGL_HEIGHT);
        Log.d(TAG, "init: "+mWidth+"---"+mHeight);
       EGL14.eglMakeCurrent(mDisplay,mSurface,mSurface,mEGLContext);

    }

    public int querySurface(final EGLSurface eglSurface, final int what) {
        final int[] value = new int[1];
        EGL14.eglQuerySurface(mDisplay, eglSurface, what, value, 0);
        return value[0];
    }


    public void SwapSurface(EGLSurface surface){

        if (!EGL14.eglSwapBuffers(mDisplay, surface)) {
            final int err = EGL14.eglGetError();
             Log.w(TAG, "swap:err=" + err);
           
        }
    }

    public void makeCurrent(){

        EGL14.eglMakeCurrent(mDisplay,mSurface,mSurface,mEGLContext);
    }

    public void render(int textid,float[] stMatrix){
        GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glViewport(0,0,mWidth,mHeight);
        mPhoto.draw(textid,stMatrix);
        SwapSurface(mSurface);
    }

}
