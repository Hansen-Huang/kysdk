package com.chuanshanjia.sdk.base;

import android.content.Context;

public class ResourceExchange {
	
	private static ResourceExchange self;

	private Context mContext;

	/*private static Class<?> CDrawable = null;

	private static Class<?> CLayout = null;

	private static Class<?> CId = null;

	private static Class<?> CAnim = null;

	private static Class<?> CStyle = null;

	private static Class<?> CString = null;

	private static Class<?> CArray = null;
	
	private static Class<?> CColor = null;
	
	private static Class<?> CRaw = null;
*/
	

	public static ResourceExchange getInstance(Context context){
		if(self == null){
			self = new ResourceExchange(context);
		}
		return self;
	}

	private ResourceExchange(Context context){
		this.mContext = context.getApplicationContext();
		/*try{
			CDrawable = Class.forName(this.mContext.getPackageName() );
			CLayout = Class.forName(this.mContext.getPackageName() );
			CId = Class.forName(this.mContext.getPackageName() );
			CAnim = Class.forName(this.mContext.getPackageName()  );
			CStyle = Class.forName(this.mContext.getPackageName());
			CString = Class.forName(this.mContext.getPackageName());
			CColor = Class.forName(this.mContext.getPackageName() );
			CRaw = Class.forName(this.mContext.getPackageName() );
			CArray = Class.forName(this.mContext.getPackageName());
		}catch(ClassNotFoundException e){
			Log.i(TAG,e.toString());
		}*/
	}

	public int getDrawableId(String resName){
		return ResourceUtil.getDrawableId(mContext, resName);
	}
	
	public int getLayoutId(String resName){
		return ResourceUtil.getLayoutId(mContext,resName);
	}
	
	public int getIdId(String resName){
		return ResourceUtil.getId(mContext,resName);
	}
	
	public int getAnimId(String resName){
		return ResourceUtil.getAnimId(mContext,resName);
	}
	
	public int getStyleId(String resName){
		return ResourceUtil.getStyleId(mContext,resName);
	}
	
	public int getStringId(String resName){
		return ResourceUtil.getStringId(mContext,resName);
	}
	
	public int getArrayId(String resName){
		return ResourceUtil.getArrayId(mContext,resName);
	}
	
	public int getColorId(String resName){
		return ResourceUtil.getColorId(mContext,resName);
	}
	
	public int getRaw(String resName) {
		return ResourceUtil.getRawId(mContext,resName);
	}
	
	/*private int getResId(Class<?> resClass,String resName){
		
		if(resClass == null){
			Log.i(TAG,"getRes(null," + resName + ")");
			throw new IllegalArgumentException("ResClass is not initialized. Please make sure you have added neccessary resources. Also make sure you have " + this.mContext.getPackageName() + ".R$* configured in obfuscation. field=" + resName);
		}
		
		//Log.i(TAG,"getRes(" + resName + ")" + resClass.getSimpleName());
		
		try {
			Field field = resClass.getField(resName);
			return field.getInt(resName);
		} catch (Exception e) {
			Log.i(TAG, "getRes(" + resClass.getName() + ", " + resName + ")");
			Log.i(TAG, "Error getting resource. Make sure you have copied all resources (res/) from SDK to your project. ");
			Log.i(TAG, e.getMessage());
		} 

		return -1;
	}*/
}
