package com.lonelypluto.loadresourcedemo.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.util.Log;

import com.lonelypluto.loadresourcedemo.bean.LoadResourceBean;
import com.lonelypluto.loadresourcedemo.constants.SPConsts;

import java.io.File;
import java.lang.reflect.Method;
import dalvik.system.DexClassLoader;

/**
 * 资源加载工具类
 */
public class LoadResourceUtil {
    private static final String TAG = LoadResourceUtil.class.getSimpleName();

    private static final String RESOURCE_TYPE_ANIM = "anim";
    private static final String RESOURCE_TYPE_ANIMATOR = "animator";
    private static final String RESOURCE_TYPE_INTERPOLATOR = "interpolator";

    private static final String RESOURCE_TYPE_MIPMAP = "mipmap";
    private static final String RESOURCE_TYPE_DRAWABLE = "drawable";
    private static final String RESOURCE_TYPE_RAW = "raw";

    private static final String RESOURCE_TYPE_ATTR = "attr";
    private static final String RESOURCE_TYPE_STYLE = "style";
    private static final String RESOURCE_TYPE_STYLEABLE = "styleable";

    private static final String RESOURCE_TYPE_LAYOUT = "layout";
    private static final String RESOURCE_TYPE_XML = "xml";
    private static final String RESOURCE_TYPE_MENU = "menu";
    private static final String RESOURCE_TYPE_NAVIGATION = "navigation";

    private static final String RESOURCE_TYPE_STRING = "string";
    private static final String RESOURCE_TYPE_PLURALS = "plurals";
    private static final String RESOURCE_TYPE_ARRAY = "array";

    private static final String RESOURCE_TYPE_COLOR = "color";
    private static final String RESOURCE_TYPE_BOOL = "bool";
    private static final String RESOURCE_TYPE_DIMEN = "dimen";
    private static final String RESOURCE_TYPE_ID = "id";
    private static final String RESOURCE_TYPE_INTEGER = "integer";
    private static final String RESOURCE_TYPE_FRACTION = "fraction";

    private Context mContext;
    private String mDexDir;// 资源路径
    private static LoadResourceBean mResourceLoadBean;// 资源对象

    private static class LoadResourceUtilHolder {
        private static final LoadResourceUtil INSTANCE = new LoadResourceUtil();
    }

    public static final LoadResourceUtil getInstance() {
        return LoadResourceUtilHolder.INSTANCE;
    }

    /**
     * 初始化
     * @param resourcePath 本地资源路径
     */
    public void init(Context context, String resourcePath) {
        mContext = context.getApplicationContext();
        // /data/user/0/com.lonelypluto.loadresourcedemo/app_dex
        File dexDir = mContext.getDir("dex", Context.MODE_PRIVATE);
        Log.e(TAG, "init "+resourcePath + " | " + dexDir.getAbsolutePath());
        if (!dexDir.exists()) {
            dexDir.mkdir();
        }
        mDexDir = dexDir.getAbsolutePath();
        mResourceLoadBean = getResourceLoad(resourcePath);
    }

    /**
     * 加载未安装资源包
     * @param resourcePath 未安装资源包
     */
    public LoadResourceBean getResourceLoad(String resourcePath) {
        Log.e(TAG, "getLoadResource");
        LoadResourceBean resourceLoadBean = null;
        // 获取未安装APK的PackageInfo
        PackageInfo info = queryPackageInfo(resourcePath);
        if (info != null) {// 获取成功
            try {
                // 创建AssetManager实例
                AssetManager assetManager = AssetManager.class.newInstance();
                Class cls = AssetManager.class;
                Method method = cls.getMethod("addAssetPath", String.class);
                // 反射设置资源加载路径
                method.invoke(assetManager, resourcePath);
                // 构造出正确的Resource
                Resources resources = new Resources(assetManager, mContext.getResources().getDisplayMetrics(),
                        mContext.getResources().getConfiguration());
                resourceLoadBean = new LoadResourceBean();
                resourceLoadBean.setResources(resources);
                resourceLoadBean.setPackageName(info.packageName);
                // 设置正确的类加载器, 因为需要去加载R文件
                resourceLoadBean.setClassLoader(new DexClassLoader(resourcePath, mDexDir, null,
                        mContext.getClassLoader()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return resourceLoadBean;
    }

    /**
     * 重新加载未安装应用资源包
     * @param resourcePath 未安装资源包路径 /storage/emulated/0/testResource1.apk
     */
    public void setLoadResource(String resourcePath) {
        // 保存资源包路径 下次进入程序默认加载
        SharedPreferencesUtil.put(SPConsts.SP_RESOURCE_PATH, resourcePath);
        mResourceLoadBean = getResourceLoad(resourcePath);
    }

    /**
     * 获取未安装资源包信息
     * @param resourcePath 未安装资源包路径
     */
    private PackageInfo queryPackageInfo(String resourcePath) {
        return mContext.getPackageManager().getPackageArchiveInfo(resourcePath, PackageManager.GET_ACTIVITIES);
    }

    /**
     * 获取未安装资源Drawable
     * @param fieldName 资源名
     */
    public Drawable getDrawable(String fieldName) {
        Drawable drawable = null;
        int resourceID = getResourceID(mResourceLoadBean.getPackageName(), RESOURCE_TYPE_DRAWABLE, fieldName);
        if (mResourceLoadBean != null) {
            drawable = mResourceLoadBean.getResources().getDrawable(resourceID);
        }
        return drawable;
    }

    /**
     * 获取未安装资源String
     * @param fieldName 资源名
     */
    public String getString(String fieldName) {
        String string = null;
        int resourceID = getResourceID(mResourceLoadBean.getPackageName(), RESOURCE_TYPE_STRING, fieldName);
        if (mResourceLoadBean != null) {
            string = mResourceLoadBean.getResources().getString(resourceID);
        }
        return string;
    }

    /**
     * 获取未安装资源color
     * @param fieldName 资源名
     */
    public int getColor(String fieldName) {
        int color = 0;
        int resourceID = getResourceID(mResourceLoadBean.getPackageName(), RESOURCE_TYPE_COLOR, fieldName);
        if (mResourceLoadBean != null) {
            color = mResourceLoadBean.getResources().getColor(resourceID);
        }
        return color;
    }

    /**
     * 获取未安装资源的ID
     * @param packageName 包名
     * @param type        资源类型
     * @param fieldName   资源名
     */
    public int getResourceID(String packageName, String type, String fieldName) {
        int resID = 0;
        String rClassName = packageName + ".R$" + type;
        try {
            Class cls = mResourceLoadBean.getClassLoader().loadClass(rClassName);
            resID = (Integer) cls.getField(fieldName).get(null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resID;
    }
}
