/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 3.0.1
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package com.petuum.petuum_ps;

public class MapInt32HostInfo {
  private long swigCPtr;
  protected boolean swigCMemOwn;

  protected MapInt32HostInfo(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  protected static long getCPtr(MapInt32HostInfo obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected void finalize() {
    delete();
  }

  public synchronized void delete() {
    if (swigCPtr != 0) {
      if (swigCMemOwn) {
        swigCMemOwn = false;
        petuumJNI.delete_MapInt32HostInfo(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

  public MapInt32HostInfo() {
    this(petuumJNI.new_MapInt32HostInfo__SWIG_0(), true);
  }

  public MapInt32HostInfo(MapInt32HostInfo arg0) {
    this(petuumJNI.new_MapInt32HostInfo__SWIG_1(MapInt32HostInfo.getCPtr(arg0), arg0), true);
  }

  public long size() {
    return petuumJNI.MapInt32HostInfo_size(swigCPtr, this);
  }

  public boolean empty() {
    return petuumJNI.MapInt32HostInfo_empty(swigCPtr, this);
  }

  public void clear() {
    petuumJNI.MapInt32HostInfo_clear(swigCPtr, this);
  }

  public HostInfo get(int key) {
    return new HostInfo(petuumJNI.MapInt32HostInfo_get(swigCPtr, this, key), false);
  }

  public void set(int key, HostInfo x) {
    petuumJNI.MapInt32HostInfo_set(swigCPtr, this, key, HostInfo.getCPtr(x), x);
  }

  public void del(int key) {
    petuumJNI.MapInt32HostInfo_del(swigCPtr, this, key);
  }

  public boolean has_key(int key) {
    return petuumJNI.MapInt32HostInfo_has_key(swigCPtr, this, key);
  }

}
