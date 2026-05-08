package com.gjh.shopdemo.template;

public abstract class CacheTemplate<T> {

    public final T getEntityById(Long id){
        T data = null;
        data = getDataFromCache(id);
        if(data == null){
            data = getDataFromDb(id);
            if(data != null){
                saveDataToCache(id, data);
            }
        }
        return data;
    }
    public abstract T getDataFromCache(Long id);
    public abstract T getDataFromDb(Long id);
    public abstract void saveDataToCache(Long id, T data);

    public final void removeEntityById(Long id){
        removeDataFromCache(id);
    }
    public abstract void removeDataFromCache(Long id);
}
