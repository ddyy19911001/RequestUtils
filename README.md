# RequestUtils

Step 1. Add the JitPack repository to your build file

Add it in your root build.gradle at the end of repositories:

	allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
Step 2. Add the dependency

	dependencies {
	        implementation 'com.github.ddyy19911001:RequestUtils:1.3'
	}
Step 3. fast use it
       
        MyHttpUtils httpUtils=new MyHttpUtils(getApplication());
        httpUtils.sendMsgGet("url", new JsonObject(), new Callback() {
            @Override
            public void onSuccess(HttpInfo info) throws IOException {
                
            }

            @Override
            public void onFailure(HttpInfo info) throws IOException {

            }
        });

（转载）原文链接：https://www.jianshu.com/p/be3c4197c6a9

