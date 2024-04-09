package com.gitlab.srcmc.mymodid.api.data.pack;

public interface IDataPackObject {
    void onLoad(DataPackManager dpm, String trainerId, String context);
}
