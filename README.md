# VrFace

## What is it?

Android library for Augmented reality effects on face
Demonstration video:

<a href="http://www.youtube.com/watch?feature=player_embedded&v=tnyJwTl7KT4
" target="_blank"><img src="http://img.youtube.com/vi/tnyJwTl7KT4/0.jpg" 
alt="IMAGE ALT TEXT HERE" width="240" height="180" border="10" /></a>

## How to use it?
You need to download dlib [model](http://dlib.net/files/). Put it in assets as sp68.dat file. The file is heavy ~70 Mbytes.

Add dependency:

```
implementation 'com.stoleg:vrface:1.1.3'
```

Add view to layout:

```
    <com.stoleg.vrface.camera.FastCameraView
        android:id="@+id/fd_fase_surface_view"
        android:layout_width="match_parent"
        android:layout_height="match_parent" />

    <android.opengl.GLSurfaceView
        android:id="@+id/fd_glsurface"
        android:layout_width="match_parent"
        android:layout_height="match_parent" />
```    

Load library, configure camera and GlSurfaceView:

```
    cameraView = findViewById(R.id.fd_fase_surface_vie
    if (!Static.libsLoaded) {
        compModel = new CompModel();
        compModel.context = getApplicationContext();
    
    gLSurfaceView = findViewById(R.id.fd_glsurface);
    gLSurfaceView.setEGLContextClientVersion(2);
    gLSurfaceView.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
    gLSurfaceView.getHolder().setFormat(PixelFormat.TRANSPARENT);
    final MaskRenderer meRender = new MaskRenderer(this, compModel, new ShaderEffectMask(this), this);
    gLSurfaceView.setRenderer(meRender);
    gLSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
    meRender.frameCamera = cameraView.frameCamera;
    getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
```

example of application: 



## How it works?
...