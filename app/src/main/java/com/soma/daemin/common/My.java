package com.soma.daemin.common;
/**
 * Created by hernia on 2015-06-27.
 */
public enum My {
    INFO();
    My(){
    }

    public int loginType; //0:google 1:facebook 2:email 3: anonymous
    public String appVer;
    public String backKeyName;
}
