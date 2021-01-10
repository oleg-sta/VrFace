# VrFace

## What is it?

Android library for Augmented reality effects on face
Demonstration video:

<a href="http://www.youtube.com/watch?feature=player_embedded&v=tnyJwTl7KT4
" target="_blank"><img src="http://img.youtube.com/vi/tnyJwTl7KT4/0.jpg" 
alt="IMAGE ALT TEXT HERE" width="240" height="180" border="10" /></a>

## How to use it?

Download model file from http://dlib.net/files/shape_predictor_68_face_landmarks.dat.bz2.
Unzip it, like this `bzip2 -d shape_predictor_68_face_landmarks.dat.bz2`.
As a result you should have file `shape_predictor_68_face_landmarks.dat`,
rename it to `sp68.dat`, and put under the directory `app/src/main/assets/`.

Add maven repository:

```
maven {
            url "https://dl.bintray.com/olegst/maven"
        }
```

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

Example of application:
* https://github.com/oleg-sta/FaceMaskExample
* https://github.com/oleg-sta/Masks
* https://github.com/oleg-sta/MakeUp


## How it works?
...